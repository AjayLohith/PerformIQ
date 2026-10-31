package com.performiq.development.service;

import com.performiq.audit.service.AuditService;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.development.entity.ApprovalAction;
import com.performiq.development.entity.ApprovalHistory;
import com.performiq.development.entity.DevelopmentPlan;
import com.performiq.development.entity.DevelopmentPlanStatus;
import com.performiq.development.repository.ApprovalHistoryRepository;
import com.performiq.development.repository.DevelopmentPlanRepository;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.review.entity.Review;
import com.performiq.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevelopmentPlanService {

    private final DevelopmentPlanRepository developmentPlanRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final ReviewRepository reviewRepository;
    private final AuditService auditService;

    @Transactional
    public DevelopmentPlan createPlan(UUID employeeId, UUID reviewId, String title, String description,
                                      String objectives, String skillsToImprove, String actionItems,
                                      LocalDate targetDate, Employee actor) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Review review = null;
        if (reviewId != null) {
            review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        }

        DevelopmentPlan plan = DevelopmentPlan.builder()
                .businessId("DEV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .employee(employee)
                .review(review)
                .title(title)
                .description(description)
                .objectives(objectives)
                .skillsToImprove(skillsToImprove)
                .actionItems(actionItems)
                .targetDate(targetDate)
                .status(DevelopmentPlanStatus.DRAFT)
                .build();

        DevelopmentPlan saved = developmentPlanRepository.save(plan);
        auditService.recordAudit(actor, "CREATE_DEV_PLAN", "DevelopmentPlan",
                saved.getBusinessId(), null, saved.getStatus().name(), "Plan created");

        return saved;
    }

    @Transactional
    public DevelopmentPlan submitPlan(UUID planId, Employee employee, String comments) {
        DevelopmentPlan plan = developmentPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("DevelopmentPlan not found with id: " + planId));

        if (plan.getStatus() != DevelopmentPlanStatus.DRAFT && plan.getStatus() != DevelopmentPlanStatus.REJECTED) {
            throw new BadRequestException("Can only submit plans in DRAFT or REJECTED status. Current status: " + plan.getStatus());
        }

        DevelopmentPlanStatus oldStatus = plan.getStatus();
        plan.setStatus(DevelopmentPlanStatus.SUBMITTED);
        DevelopmentPlan saved = developmentPlanRepository.save(plan);

        // Record approval history
        ApprovalHistory ah = ApprovalHistory.builder()
                .developmentPlan(saved)
                .approver(employee)
                .action(ApprovalAction.SUBMITTED)
                .comments(comments != null ? comments : "Submitted for manager review")
                .build();
        approvalHistoryRepository.save(ah);

        auditService.recordAudit(employee, "SUBMIT_DEV_PLAN", "DevelopmentPlan",
                saved.getBusinessId(), oldStatus.name(), DevelopmentPlanStatus.SUBMITTED.name(), comments);

        return saved;
    }

    @Transactional
    public DevelopmentPlan approvePlan(UUID planId, Employee approver, String comments) {
        DevelopmentPlan plan = developmentPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("DevelopmentPlan not found with id: " + planId));

        if (plan.getStatus() != DevelopmentPlanStatus.SUBMITTED) {
            throw new BadRequestException("Can only approve plans that are in SUBMITTED status. Current status: " + plan.getStatus());
        }

        plan.setStatus(DevelopmentPlanStatus.APPROVED);
        DevelopmentPlan saved = developmentPlanRepository.save(plan);

        ApprovalHistory ah = ApprovalHistory.builder()
                .developmentPlan(saved)
                .approver(approver)
                .action(ApprovalAction.APPROVED)
                .comments(comments != null ? comments : "Plan approved")
                .build();
        approvalHistoryRepository.save(ah);

        auditService.recordAudit(approver, "APPROVE_DEV_PLAN", "DevelopmentPlan",
                saved.getBusinessId(), DevelopmentPlanStatus.SUBMITTED.name(), DevelopmentPlanStatus.APPROVED.name(), comments);

        return saved;
    }

    @Transactional
    public DevelopmentPlan rejectPlan(UUID planId, Employee approver, String comments) {
        DevelopmentPlan plan = developmentPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("DevelopmentPlan not found with id: " + planId));

        if (plan.getStatus() != DevelopmentPlanStatus.SUBMITTED) {
            throw new BadRequestException("Can only reject plans that are in SUBMITTED status. Current status: " + plan.getStatus());
        }
        if (comments == null || comments.isBlank()) {
            throw new BadRequestException("A rejection reason/comment is mandatory when rejecting a development plan");
        }

        plan.setStatus(DevelopmentPlanStatus.REJECTED);
        DevelopmentPlan saved = developmentPlanRepository.save(plan);

        ApprovalHistory ah = ApprovalHistory.builder()
                .developmentPlan(saved)
                .approver(approver)
                .action(ApprovalAction.REJECTED)
                .comments(comments)
                .build();
        approvalHistoryRepository.save(ah);

        auditService.recordAudit(approver, "REJECT_DEV_PLAN", "DevelopmentPlan",
                saved.getBusinessId(), DevelopmentPlanStatus.SUBMITTED.name(), DevelopmentPlanStatus.REJECTED.name(), comments);

        return saved;
    }

    @Transactional
    public DevelopmentPlan startPlan(UUID planId, Employee actor) {
        DevelopmentPlan plan = developmentPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("DevelopmentPlan not found with id: " + planId));

        if (plan.getStatus() != DevelopmentPlanStatus.APPROVED) {
            throw new BadRequestException("Can only start execution of an APPROVED plan. Current status: " + plan.getStatus());
        }

        plan.setStatus(DevelopmentPlanStatus.IN_PROGRESS);
        DevelopmentPlan saved = developmentPlanRepository.save(plan);

        auditService.recordAudit(actor, "START_DEV_PLAN", "DevelopmentPlan",
                saved.getBusinessId(), DevelopmentPlanStatus.APPROVED.name(), DevelopmentPlanStatus.IN_PROGRESS.name(), "Execution started");

        return saved;
    }

    @Transactional
    public DevelopmentPlan completePlan(UUID planId, Employee actor, String notes) {
        DevelopmentPlan plan = developmentPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("DevelopmentPlan not found with id: " + planId));

        if (plan.getStatus() != DevelopmentPlanStatus.IN_PROGRESS) {
            throw new BadRequestException("Can only complete an IN_PROGRESS plan. Current status: " + plan.getStatus());
        }

        plan.setStatus(DevelopmentPlanStatus.COMPLETED);
        DevelopmentPlan saved = developmentPlanRepository.save(plan);

        auditService.recordAudit(actor, "COMPLETE_DEV_PLAN", "DevelopmentPlan",
                saved.getBusinessId(), DevelopmentPlanStatus.IN_PROGRESS.name(), DevelopmentPlanStatus.COMPLETED.name(), notes);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<ApprovalHistory> getApprovalHistory(UUID planId) {
        return approvalHistoryRepository.findByDevelopmentPlanIdOrderByActionDateDesc(planId);
    }
}
