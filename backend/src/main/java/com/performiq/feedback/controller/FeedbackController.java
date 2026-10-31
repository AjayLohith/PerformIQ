package com.performiq.feedback.controller;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.competency.entity.Competency;
import com.performiq.competency.repository.CompetencyRepository;
import com.performiq.employee.entity.Employee;
import com.performiq.feedback.dto.RequestFeedbackDto;
import com.performiq.feedback.dto.SubmitFeedbackDto;
import com.performiq.feedback.entity.Feedback;
import com.performiq.feedback.repository.FeedbackRepository;
import com.performiq.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
@Tag(name = "360 Feedback", description = "Multi-directional 360 feedback requests, rubric submissions, and competency score aggregation")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;
    private final CompetencyRepository competencyRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/employee/{employeeId}/cycle/{cycleId}")
    @Operation(summary = "List feedbacks for an employee", description = "Returns all 360 feedback requests and submissions for an employee in a cycle")
    public ResponseEntity<List<Feedback>> getFeedbacksForEmployee(
            @PathVariable UUID employeeId,
            @PathVariable UUID cycleId
    ) {
        return ResponseEntity.ok(feedbackRepository.findByEmployeeIdAndReviewCycleId(employeeId, cycleId));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get feedback summary", description = "Computes multi-dimensional feedback summary: average overall, breakdown by feedback type, and competency scores")
    public ResponseEntity<FeedbackService.FeedbackSummaryDto> getFeedbackSummary(
            @RequestParam UUID employeeId,
            @RequestParam UUID reviewCycleId
    ) {
        return ResponseEntity.ok(feedbackService.calculateFeedbackSummary(employeeId, reviewCycleId));
    }

    @GetMapping("/competencies")
    @Operation(summary = "List competencies", description = "Returns the standard organizational competencies for rubric evaluations")
    public ResponseEntity<List<Competency>> listCompetencies() {
        return ResponseEntity.ok(competencyRepository.findAll());
    }

    @PostMapping("/request")
    @Operation(summary = "Request 360 feedback", description = "Creates a feedback request validating relationship rules (SELF vs PEER/MANAGER/UPWARD)")
    public ResponseEntity<Feedback> requestFeedback(@Valid @RequestBody RequestFeedbackDto request,
                                                    Authentication authentication) {
        Employee actor = getActor(authentication);
        Feedback fb = feedbackService.requestFeedback(
                request.getEmployeeId(), request.getReviewerId(), request.getReviewCycleId(),
                request.getFeedbackType(), actor);
        return ResponseEntity.ok(fb);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit feedback", description = "Submits feedback with overall rating (1-5), comments, and per-competency ratings")
    public ResponseEntity<Feedback> submitFeedback(@PathVariable UUID id,
                                                   @Valid @RequestBody SubmitFeedbackDto request,
                                                   Authentication authentication) {
        Employee actor = getActor(authentication);
        Feedback fb = feedbackService.submitFeedback(
                id, request.getOverallRating(), request.getComments(), request.getCompetencyRatings(), actor);
        return ResponseEntity.ok(fb);
    }

    private Employee getActor(Authentication authentication) {
        if (authentication == null) return null;
        return appUserRepository.findByUsername(authentication.getName())
                .map(AppUser::getEmployee)
                .orElse(null);
    }
}
