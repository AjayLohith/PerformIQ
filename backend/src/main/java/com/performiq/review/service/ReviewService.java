package com.performiq.review.service;

import com.performiq.audit.service.AuditService;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.feedback.service.FeedbackService;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.review.entity.Review;
import com.performiq.review.entity.ReviewGoal;
import com.performiq.review.entity.ReviewStatus;
import com.performiq.review.repository.ReviewGoalRepository;
import com.performiq.review.repository.ReviewRepository;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewGoalRepository reviewGoalRepository;
    private final GoalRepository goalRepository;
    private final FeedbackService feedbackService;
    private final AuditService auditService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewGoalScoreDto {
        private UUID goalId;
        private BigDecimal score;
        private String comments;
    }

    @Transactional
    public Review initializeReview(Employee employee, Employee reviewer, ReviewCycle cycle, String businessId) {
        if (cycle.getStatus() == ReviewCycleStatus.CLOSED) {
            throw new BadRequestException("Cannot create a review in a CLOSED cycle");
        }

        if (reviewRepository.findByEmployeeIdAndReviewCycleId(employee.getId(), cycle.getId()).isPresent()) {
            throw new BadRequestException("Review already exists for employee in this review cycle");
        }

        Review review = Review.builder()
                .businessId(businessId != null ? businessId : "REV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .employee(employee)
                .reviewer(reviewer)
                .reviewCycle(cycle)
                .status(ReviewStatus.PENDING)
                .build();

        Review saved = reviewRepository.save(review);

        // Pre-populate review goals from active cycle goals
        List<Goal> goals = goalRepository.findByEmployeeIdAndReviewCycleId(employee.getId(), cycle.getId());
        for (Goal g : goals) {
            ReviewGoal rg = ReviewGoal.builder()
                    .review(saved)
                    .goal(g)
                    .build();
            reviewGoalRepository.save(rg);
        }

        return saved;
    }

    @Transactional
    public Review scoreReview(UUID reviewId, List<ReviewGoalScoreDto> goalScores,
                              String summary, String recommendation, Employee reviewer) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (review.getStatus() == ReviewStatus.COMPLETED) {
            throw new BadRequestException("Cannot edit an already COMPLETED review");
        }

        // Score each goal
        BigDecimal totalGoalScore = BigDecimal.ZERO;
        int scoredGoalCount = 0;

        if (goalScores != null) {
            for (ReviewGoalScoreDto gsd : goalScores) {
                if (gsd.getScore() != null) {
                    if (gsd.getScore().compareTo(BigDecimal.ONE) < 0 || gsd.getScore().compareTo(new BigDecimal("5.00")) > 0) {
                        throw new BadRequestException("Goal score must be between 1.00 and 5.00");
                    }
                    ReviewGoal rg = reviewGoalRepository.findByReviewIdAndGoalId(review.getId(), gsd.getGoalId())
                            .orElseThrow(() -> new ResourceNotFoundException("ReviewGoal not found for goal: " + gsd.getGoalId()));

                    rg.setGoalScore(gsd.getScore());
                    rg.setComments(gsd.getComments());
                    reviewGoalRepository.save(rg);

                    totalGoalScore = totalGoalScore.add(gsd.getScore());
                    scoredGoalCount++;
                }
            }
        }

        // Calculate average goal score (scale 1 to 5)
        BigDecimal avgGoalScore = scoredGoalCount > 0
                ? totalGoalScore.divide(BigDecimal.valueOf(scoredGoalCount), 2, RoundingMode.HALF_UP)
                : new BigDecimal("3.00");

        // Calculate feedback score from 360 feedback engine
        var feedbackSummary = feedbackService.calculateFeedbackSummary(review.getEmployee().getId(), review.getReviewCycle().getId());
        BigDecimal feedbackScore = feedbackSummary.getAverageOverallRating();
        if (feedbackScore.compareTo(BigDecimal.ZERO) == 0) {
            feedbackScore = avgGoalScore; // Fallback if no feedback
        }

        // Composite Overall Score: 60% Goals + 40% 360 Feedback
        BigDecimal compositeScore = avgGoalScore.multiply(new BigDecimal("0.60"))
                .add(feedbackScore.multiply(new BigDecimal("0.40")))
                .setScale(2, RoundingMode.HALF_UP);

        review.setOverallScore(compositeScore);
        review.setSummary(summary);
        review.setRecommendation(recommendation);
        review.setStatus(ReviewStatus.COMPLETED);
        review.setCompletedAt(Instant.now());

        Review saved = reviewRepository.save(review);

        auditService.recordAudit(reviewer, "COMPLETE_REVIEW", "Review",
                saved.getBusinessId(), ReviewStatus.PENDING.name(), ReviewStatus.COMPLETED.name(),
                "Overall score: " + compositeScore);

        return saved;
    }
}
