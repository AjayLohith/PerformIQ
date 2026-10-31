package com.performiq.feedback.service;

import com.performiq.audit.service.AuditService;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.competency.entity.Competency;
import com.performiq.competency.repository.CompetencyRepository;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.feedback.entity.Feedback;
import com.performiq.feedback.entity.FeedbackCompetency;
import com.performiq.feedback.entity.FeedbackStatus;
import com.performiq.feedback.entity.FeedbackType;
import com.performiq.feedback.repository.FeedbackCompetencyRepository;
import com.performiq.feedback.repository.FeedbackRepository;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackCompetencyRepository feedbackCompetencyRepository;
    private final CompetencyRepository competencyRepository;
    private final EmployeeRepository employeeRepository;
    private final ReviewCycleRepository reviewCycleRepository;
    private final AuditService auditService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompetencyRatingDto {
        private UUID competencyId;
        private Integer rating;
        private String comments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeedbackSummaryDto {
        private UUID employeeId;
        private UUID reviewCycleId;
        private int totalFeedbacks;
        private BigDecimal averageOverallRating;
        private Map<FeedbackType, BigDecimal> ratingByFeedbackType;
        private Map<String, BigDecimal> ratingByCompetency;
    }

    @Transactional
    public Feedback requestFeedback(UUID employeeId, UUID reviewerId, UUID reviewCycleId,
                                    FeedbackType feedbackType, Employee actor) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found with id: " + reviewerId));
        ReviewCycle cycle = reviewCycleRepository.findById(reviewCycleId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCycle not found with id: " + reviewCycleId));

        if (cycle.getStatus() == ReviewCycleStatus.CLOSED) {
            throw new BadRequestException("Cannot request feedback in a CLOSED review cycle");
        }

        // Validate relationship
        if (feedbackType == FeedbackType.SELF && !employeeId.equals(reviewerId)) {
            throw new BadRequestException("Self-evaluation must have reviewer equal to employee");
        }
        if (feedbackType != FeedbackType.SELF && employeeId.equals(reviewerId)) {
            throw new BadRequestException(feedbackType + " feedback cannot have reviewer equal to employee");
        }

        Feedback feedback = Feedback.builder()
                .businessId("FB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .employee(employee)
                .reviewer(reviewer)
                .reviewCycle(cycle)
                .feedbackType(feedbackType)
                .status(FeedbackStatus.DRAFT)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        auditService.recordAudit(actor, "REQUEST_FEEDBACK", "Feedback",
                saved.getBusinessId(), null, feedbackType.name(), "Feedback requested");

        return saved;
    }

    @Transactional
    public Feedback submitFeedback(UUID feedbackId, Integer overallRating, String comments,
                                   List<CompetencyRatingDto> competencyRatings, Employee actor) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackId));

        if (feedback.getReviewCycle().getStatus() == ReviewCycleStatus.CLOSED) {
            throw new BadRequestException("Cannot submit feedback in a CLOSED review cycle");
        }
        if (feedback.getStatus() == FeedbackStatus.SUBMITTED) {
            throw new BadRequestException("Feedback has already been submitted");
        }
        if (overallRating == null || overallRating < 1 || overallRating > 5) {
            throw new BadRequestException("Overall rating must be between 1 and 5 (inclusive)");
        }

        feedback.setOverallRating(overallRating);
        feedback.setComments(comments);
        feedback.setStatus(FeedbackStatus.SUBMITTED);
        feedback.setSubmittedAt(Instant.now());

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Process competencies
        if (competencyRatings != null && !competencyRatings.isEmpty()) {
            for (CompetencyRatingDto cr : competencyRatings) {
                if (cr.getRating() == null || cr.getRating() < 1 || cr.getRating() > 5) {
                    throw new BadRequestException("Competency rating must be between 1 and 5 (inclusive)");
                }
                Competency competency = competencyRepository.findById(cr.getCompetencyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Competency not found with id: " + cr.getCompetencyId()));

                FeedbackCompetency fc = feedbackCompetencyRepository.findByFeedbackIdAndCompetencyId(savedFeedback.getId(), competency.getId())
                        .orElseGet(() -> FeedbackCompetency.builder()
                                .feedback(savedFeedback)
                                .competency(competency)
                                .build());

                fc.setRating(cr.getRating());
                fc.setComments(cr.getComments());
                feedbackCompetencyRepository.save(fc);
            }
        }

        auditService.recordAudit(actor, "SUBMIT_FEEDBACK", "Feedback",
                savedFeedback.getBusinessId(), FeedbackStatus.DRAFT.name(), FeedbackStatus.SUBMITTED.name(),
                "Overall rating: " + overallRating);

        return savedFeedback;
    }

    @Transactional(readOnly = true)
    public FeedbackSummaryDto calculateFeedbackSummary(UUID employeeId, UUID reviewCycleId) {
        List<Feedback> feedbacks = feedbackRepository.findByEmployeeIdAndReviewCycleId(employeeId, reviewCycleId).stream()
                .filter(f -> f.getStatus() == FeedbackStatus.SUBMITTED)
                .toList();

        if (feedbacks.isEmpty()) {
            return FeedbackSummaryDto.builder()
                    .employeeId(employeeId)
                    .reviewCycleId(reviewCycleId)
                    .totalFeedbacks(0)
                    .averageOverallRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .ratingByFeedbackType(Collections.emptyMap())
                    .ratingByCompetency(Collections.emptyMap())
                    .build();
        }

        // Calculate average overall rating
        double sumOverall = 0;
        int countOverall = 0;
        Map<FeedbackType, List<Integer>> typeRatings = new HashMap<>();

        for (Feedback fb : feedbacks) {
            if (fb.getOverallRating() != null) {
                sumOverall += fb.getOverallRating();
                countOverall++;
                typeRatings.computeIfAbsent(fb.getFeedbackType(), k -> new ArrayList<>()).add(fb.getOverallRating());
            }
        }

        BigDecimal avgOverall = countOverall > 0
                ? BigDecimal.valueOf(sumOverall / countOverall).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        Map<FeedbackType, BigDecimal> avgByType = new HashMap<>();
        for (var entry : typeRatings.entrySet()) {
            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            avgByType.put(entry.getKey(), BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        }

        // Aggregate competency ratings
        Map<String, List<Integer>> competencyScores = new HashMap<>();
        for (Feedback fb : feedbacks) {
            List<FeedbackCompetency> compList = feedbackCompetencyRepository.findByFeedbackId(fb.getId());
            for (FeedbackCompetency fc : compList) {
                if (fc.getRating() != null) {
                    competencyScores.computeIfAbsent(fc.getCompetency().getName(), k -> new ArrayList<>()).add(fc.getRating());
                }
            }
        }

        Map<String, BigDecimal> avgByComp = new HashMap<>();
        for (var entry : competencyScores.entrySet()) {
            double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            avgByComp.put(entry.getKey(), BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP));
        }

        return FeedbackSummaryDto.builder()
                .employeeId(employeeId)
                .reviewCycleId(reviewCycleId)
                .totalFeedbacks(feedbacks.size())
                .averageOverallRating(avgOverall)
                .ratingByFeedbackType(avgByType)
                .ratingByCompetency(avgByComp)
                .build();
    }
}
