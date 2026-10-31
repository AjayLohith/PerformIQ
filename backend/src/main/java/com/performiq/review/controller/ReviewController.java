package com.performiq.review.controller;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.review.dto.ReviewDetailsDto;
import com.performiq.review.dto.ScoreReviewDto;
import com.performiq.review.entity.Review;
import com.performiq.review.repository.ReviewGoalRepository;
import com.performiq.review.repository.ReviewRepository;
import com.performiq.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Performance reviews, formal evaluations, goal scoring, and composite rating calculation")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final ReviewGoalRepository reviewGoalRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/cycle/{cycleId}")
    @Operation(summary = "List reviews in a cycle", description = "Returns paginated list of employee reviews in the specified review cycle")
    public ResponseEntity<Page<Review>> listReviewsInCycle(
            @PathVariable UUID cycleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(reviewRepository.findByReviewCycleId(cycleId, pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review details", description = "Returns review details including goal scores and recommendation")
    public ResponseEntity<ReviewDetailsDto> getReviewById(@PathVariable UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        var reviewGoals = reviewGoalRepository.findByReviewId(id);

        return ResponseEntity.ok(ReviewDetailsDto.builder()
                .review(review)
                .reviewGoals(reviewGoals)
                .build());
    }

    @GetMapping("/employee/{employeeId}/cycle/{cycleId}")
    @Operation(summary = "Get review for employee in cycle", description = "Returns review for a specific employee in a cycle, or 404 if not found")
    public ResponseEntity<ReviewDetailsDto> getReviewForEmployeeInCycle(
            @PathVariable UUID employeeId,
            @PathVariable UUID cycleId
    ) {
        Review review = reviewRepository.findByEmployeeIdAndReviewCycleId(employeeId, cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for employee in this cycle"));

        var reviewGoals = reviewGoalRepository.findByReviewId(review.getId());

        return ResponseEntity.ok(ReviewDetailsDto.builder()
                .review(review)
                .reviewGoals(reviewGoals)
                .build());
    }

    @PostMapping("/{id}/score")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Score a review (Manager/Admin)", description = "Scores review goals, computes composite overall rating (60% Goals + 40% Feedback), and seals review")
    public ResponseEntity<Review> scoreReview(
            @PathVariable UUID id,
            @Valid @RequestBody ScoreReviewDto request,
            Authentication authentication
    ) {
        Employee actor = getActor(authentication);
        Review scored = reviewService.scoreReview(
                id, request.getGoalScores(), request.getSummary(), request.getRecommendation(), actor);
        return ResponseEntity.ok(scored);
    }

    private Employee getActor(Authentication authentication) {
        if (authentication == null) return null;
        return appUserRepository.findByUsername(authentication.getName())
                .map(AppUser::getEmployee)
                .orElse(null);
    }
}
