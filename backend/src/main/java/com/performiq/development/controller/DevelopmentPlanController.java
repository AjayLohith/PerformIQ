package com.performiq.development.controller;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.development.dto.ActionCommentDto;
import com.performiq.development.dto.CreateDevPlanDto;
import com.performiq.development.dto.DevelopmentPlanDetailsDto;
import com.performiq.development.entity.DevelopmentPlan;
import com.performiq.development.repository.DevelopmentPlanRepository;
import com.performiq.development.service.DevelopmentPlanService;
import com.performiq.employee.entity.Employee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/development-plans")
@RequiredArgsConstructor
@Tag(name = "Development Plans", description = "Growth and development plans, coaching objectives, and approval workflows")
public class DevelopmentPlanController {

    private final DevelopmentPlanService developmentPlanService;
    private final DevelopmentPlanRepository developmentPlanRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get development plans for employee", description = "Returns all development plans associated with the specified employee")
    public ResponseEntity<List<DevelopmentPlan>> getPlansForEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(developmentPlanRepository.findByEmployeeId(employeeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get development plan details", description = "Returns detailed plan information along with the complete approval history trail")
    public ResponseEntity<DevelopmentPlanDetailsDto> getPlanDetails(@PathVariable UUID id) {
        DevelopmentPlan plan = developmentPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Development plan not found with id: " + id));

        var history = developmentPlanService.getApprovalHistory(id);

        return ResponseEntity.ok(DevelopmentPlanDetailsDto.builder()
                .plan(plan)
                .approvalHistory(history)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create development plan", description = "Creates a development plan in DRAFT status")
    public ResponseEntity<DevelopmentPlan> createPlan(@Valid @RequestBody CreateDevPlanDto request,
                                                      Authentication authentication) {
        Employee actor = getActor(authentication);
        DevelopmentPlan plan = developmentPlanService.createPlan(
                request.getEmployeeId(), request.getReviewId(), request.getTitle(),
                request.getDescription(), request.getObjectives(), request.getSkillsToImprove(),
                request.getActionItems(), request.getTargetDate(), actor);
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit development plan", description = "Transitions plan to SUBMITTED status for manager approval")
    public ResponseEntity<DevelopmentPlan> submitPlan(@PathVariable UUID id,
                                                      @RequestBody(required = false) ActionCommentDto request,
                                                      Authentication authentication) {
        Employee actor = getActor(authentication);
        String comments = request != null ? request.getComments() : "Submitted for approval";
        return ResponseEntity.ok(developmentPlanService.submitPlan(id, actor, comments));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Approve development plan (Manager/Admin)", description = "Approves a submitted development plan (SUBMITTED -> APPROVED)")
    public ResponseEntity<DevelopmentPlan> approvePlan(@PathVariable UUID id,
                                                       @RequestBody(required = false) ActionCommentDto request,
                                                       Authentication authentication) {
        Employee actor = getActor(authentication);
        String comments = request != null ? request.getComments() : "Approved";
        return ResponseEntity.ok(developmentPlanService.approvePlan(id, actor, comments));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Reject development plan (Manager/Admin)", description = "Rejects a submitted development plan with mandatory feedback comments (SUBMITTED -> REJECTED)")
    public ResponseEntity<DevelopmentPlan> rejectPlan(@PathVariable UUID id,
                                                       @RequestBody ActionCommentDto request,
                                                       Authentication authentication) {
        Employee actor = getActor(authentication);
        String comments = request != null ? request.getComments() : null;
        return ResponseEntity.ok(developmentPlanService.rejectPlan(id, actor, comments));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start development plan execution", description = "Transitions an approved plan into IN_PROGRESS execution")
    public ResponseEntity<DevelopmentPlan> startPlan(@PathVariable UUID id,
                                                     Authentication authentication) {
        Employee actor = getActor(authentication);
        return ResponseEntity.ok(developmentPlanService.startPlan(id, actor));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete development plan", description = "Marks an in-progress development plan as COMPLETED")
    public ResponseEntity<DevelopmentPlan> completePlan(@PathVariable UUID id,
                                                        @RequestBody(required = false) ActionCommentDto request,
                                                        Authentication authentication) {
        Employee actor = getActor(authentication);
        String notes = request != null ? request.getComments() : "Plan completed";
        return ResponseEntity.ok(developmentPlanService.completePlan(id, actor, notes));
    }

    private Employee getActor(Authentication authentication) {
        if (authentication == null) return null;
        return appUserRepository.findByUsername(authentication.getName())
                .map(AppUser::getEmployee)
                .orElse(null);
    }
}
