package com.performiq.reviewcycle.service;

import com.performiq.audit.service.AuditService;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
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
public class ReviewCycleService {

    private final ReviewCycleRepository reviewCycleRepository;
    private final GoalRepository goalRepository;
    private final AuditService auditService;

    @Transactional
    public ReviewCycle createReviewCycle(String businessId, String name, String description,
                                         LocalDate startDate, LocalDate endDate, Employee actor) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be earlier than start date");
        }
        if (businessId != null && reviewCycleRepository.existsByBusinessId(businessId)) {
            throw new BadRequestException("ReviewCycle with business ID '" + businessId + "' already exists");
        }

        ReviewCycle cycle = ReviewCycle.builder()
                .businessId(businessId != null && !businessId.isBlank() ? businessId : "CYCLE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .name(name)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .status(ReviewCycleStatus.DRAFT)
                .build();

        ReviewCycle saved = reviewCycleRepository.save(cycle);
        auditService.recordAudit(actor, "CREATE_REVIEW_CYCLE", "ReviewCycle",
                saved.getBusinessId(), null, saved.getStatus().name(), "Review cycle created");

        return saved;
    }

    @Transactional
    public ReviewCycle transitionStatus(UUID cycleId, ReviewCycleStatus targetStatus, Employee actor, String reason, boolean force) {
        ReviewCycle cycle = reviewCycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCycle not found with id: " + cycleId));

        ReviewCycleStatus currentStatus = cycle.getStatus();
        if (currentStatus == targetStatus) {
            return cycle;
        }

        validateCycleTransition(currentStatus, targetStatus, cycle);

        // Perform side-effects on transition
        if (currentStatus == ReviewCycleStatus.DRAFT && targetStatus == ReviewCycleStatus.ACTIVE) {
            onCycleActivated(cycle);
        } else if (currentStatus == ReviewCycleStatus.ACTIVE && targetStatus == ReviewCycleStatus.UNDER_REVIEW) {
            log.info("Review cycle {} moved to UNDER_REVIEW. Review window opened.", cycle.getBusinessId());
        } else if (currentStatus == ReviewCycleStatus.UNDER_REVIEW && targetStatus == ReviewCycleStatus.CLOSED) {
            log.info("Review cycle {} CLOSED. Performance ratings locked.", cycle.getBusinessId());
        }

        cycle.setStatus(targetStatus);
        ReviewCycle saved = reviewCycleRepository.save(cycle);

        auditService.recordAudit(actor, "TRANSITION_CYCLE_STATUS", "ReviewCycle",
                saved.getBusinessId(), currentStatus.name(), targetStatus.name(), reason);

        return saved;
    }

    private void validateCycleTransition(ReviewCycleStatus current, ReviewCycleStatus target, ReviewCycle cycle) {
        if (current == ReviewCycleStatus.CLOSED) {
            throw new BadRequestException("Cannot transition from a CLOSED review cycle");
        }

        boolean valid = switch (current) {
            case DRAFT -> target == ReviewCycleStatus.ACTIVE;
            case ACTIVE -> target == ReviewCycleStatus.UNDER_REVIEW;
            case UNDER_REVIEW -> target == ReviewCycleStatus.CLOSED;
            default -> false;
        };

        if (!valid) {
            throw new BadRequestException(String.format(
                    "Invalid state transition for ReviewCycle %s: cannot transition from %s to %s",
                    cycle.getBusinessId(), current, target));
        }
    }

    private void onCycleActivated(ReviewCycle cycle) {
        log.info("Cycle {} activated. Activating DRAFT goals into ACTIVE state.", cycle.getBusinessId());
        List<Goal> goals = goalRepository.findAll().stream()
                .filter(g -> g.getReviewCycle().getId().equals(cycle.getId()))
                .filter(g -> g.getStatus() == GoalStatus.DRAFT)
                .toList();

        for (Goal g : goals) {
            g.setStatus(GoalStatus.ACTIVE);
            goalRepository.save(g);
        }
    }
}
