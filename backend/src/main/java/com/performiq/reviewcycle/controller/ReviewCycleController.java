package com.performiq.reviewcycle.controller;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.reviewcycle.dto.CreateReviewCycleDto;
import com.performiq.reviewcycle.dto.TransitionCycleDto;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
import com.performiq.reviewcycle.service.ReviewCycleService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/review-cycles")
@RequiredArgsConstructor
@Tag(name = "Review Cycles", description = "Performance review cycle configuration, scheduling, and state machine transitions")
public class ReviewCycleController {

    private final ReviewCycleService reviewCycleService;
    private final ReviewCycleRepository reviewCycleRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping
    @Operation(summary = "List review cycles", description = "Returns a paginated list of all review cycles")
    public ResponseEntity<Page<ReviewCycle>> listReviewCycles(
            @RequestParam(required = false) ReviewCycleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ReviewCycle> result = status != null
                ? reviewCycleRepository.findByStatus(status, pageRequest)
                : reviewCycleRepository.findAll(pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active review cycle", description = "Returns the currently active review cycle, or 404 if none is active")
    public ResponseEntity<ReviewCycle> getActiveCycle() {
        return reviewCycleRepository.findFirstByStatus(ReviewCycleStatus.ACTIVE)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("No review cycle is currently ACTIVE"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review cycle by ID", description = "Returns review cycle details by UUID")
    public ResponseEntity<ReviewCycle> getCycleById(@PathVariable UUID id) {
        ReviewCycle cycle = reviewCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review cycle not found with id: " + id));
        return ResponseEntity.ok(cycle);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create review cycle (Admin)", description = "Creates a new review cycle in DRAFT status")
    public ResponseEntity<ReviewCycle> createCycle(@Valid @RequestBody CreateReviewCycleDto request,
                                                  Authentication authentication) {
        Employee actor = getActor(authentication);
        ReviewCycle created = reviewCycleService.createReviewCycle(
                request.getBusinessId(), request.getName(), request.getDescription(),
                request.getStartDate(), request.getEndDate(), actor);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/transition")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Transition cycle state (Admin)", description = "Transitions review cycle to next phase (DRAFT -> ACTIVE -> UNDER_REVIEW -> CLOSED)")
    public ResponseEntity<ReviewCycle> transitionCycle(@PathVariable UUID id,
                                                       @Valid @RequestBody TransitionCycleDto request,
                                                       Authentication authentication) {
        Employee actor = getActor(authentication);
        ReviewCycle transitioned = reviewCycleService.transitionStatus(
                id, request.getTargetStatus(), actor, request.getReason(), Boolean.TRUE.equals(request.getForce()));
        return ResponseEntity.ok(transitioned);
    }

    private Employee getActor(Authentication authentication) {
        if (authentication == null) return null;
        return appUserRepository.findByUsername(authentication.getName())
                .map(AppUser::getEmployee)
                .orElse(null);
    }
}
