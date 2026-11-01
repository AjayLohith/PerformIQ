package com.performiq.goal.service;

import com.performiq.audit.service.AuditService;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalHistory;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalHistoryRepository;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.keyresult.service.KeyResultService;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalHistoryRepository goalHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final ReviewCycleRepository reviewCycleRepository;
    private final KeyResultService keyResultService;
    private final AuditService auditService;

    public void validateGoalWeights(UUID employeeId, UUID reviewCycleId, BigDecimal goalWeight, UUID excludeGoalId) {
        if (goalWeight == null || goalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Goal weight must be strictly positive");
        }

        List<Goal> existingGoals = goalRepository.findByEmployeeIdAndReviewCycleId(employeeId, reviewCycleId);
        BigDecimal currentTotal = existingGoals.stream()
                .filter(g -> excludeGoalId == null || !g.getId().equals(excludeGoalId))
                .filter(g -> g.getStatus() != GoalStatus.CANCELLED)
                .map(Goal::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedTotal = currentTotal.add(goalWeight);
        if (projectedTotal.compareTo(new BigDecimal("100.00")) > 0) {
            throw new BadRequestException(String.format(
                    "Total goal weight for employee in this review cycle cannot exceed 100.00%%. Current: %.2f%%, Attempted: +%.2f%%, Result: %.2f%%",
                    currentTotal, goalWeight, projectedTotal));
        }
    }

    @Transactional
    public Goal createGoal(UUID employeeId, UUID reviewCycleId, String businessId, String title,
                           String description, String category, BigDecimal weight, Employee actor) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        ReviewCycle cycle = reviewCycleRepository.findById(reviewCycleId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewCycle not found with id: " + reviewCycleId));

        if (cycle.getStatus() == ReviewCycleStatus.CLOSED) {
            throw new BadRequestException("Cannot create goals in a CLOSED review cycle");
        }

        BigDecimal validatedWeight = weight != null ? weight : new BigDecimal("25.00");
        validateGoalWeights(employeeId, reviewCycleId, validatedWeight, null);

        String finalBusinessId = (businessId != null && !businessId.isBlank())
                ? businessId
                : "GOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Goal goal = Goal.builder()
                .businessId(finalBusinessId)
                .employee(emp)
                .reviewCycle(cycle)
                .title(title)
                .description(description)
                .category(category != null ? category : "INDIVIDUAL")
                .weight(validatedWeight)
                .status(GoalStatus.DRAFT)
                .progress(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .build();

        Goal saved = goalRepository.save(goal);

        // Record history
        recordGoalHistory(saved, "STATUS", null, GoalStatus.DRAFT.name(), actor);
        auditService.recordAudit(actor, "CREATE_GOAL", "Goal", saved.getBusinessId(), null, saved.getTitle(), "Goal created");

        return saved;
    }

    @Transactional
    public Goal updateGoalStatus(UUID goalId, GoalStatus targetStatus, Employee actor, String reason) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        GoalStatus currentStatus = goal.getStatus();
        if (currentStatus == targetStatus) {
            return goal;
        }

        // Validate lifecycle transition
        validateStatusTransition(currentStatus, targetStatus);

        goal.setStatus(targetStatus);
        Goal saved = goalRepository.save(goal);

        recordGoalHistory(saved, "STATUS", currentStatus.name(), targetStatus.name(), actor);
        auditService.recordAudit(actor, "TRANSITION_GOAL_STATUS", "Goal", saved.getBusinessId(),
                currentStatus.name(), targetStatus.name(), reason);

        return saved;
    }

    @Transactional
    public Goal updateDirectProgress(UUID goalId, BigDecimal newProgress, Employee actor, String notes) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        if (goal.getStatus() == GoalStatus.CANCELLED || goal.getStatus() == GoalStatus.COMPLETED) {
            throw new BadRequestException("Cannot update progress on " + goal.getStatus() + " goal");
        }

        BigDecimal clampedProgress = newProgress.max(BigDecimal.ZERO).min(new BigDecimal("100.00")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal oldProgress = goal.getProgress();

        goal.setProgress(clampedProgress);
        if (clampedProgress.compareTo(new BigDecimal("100.00")) == 0 && goal.getStatus() == GoalStatus.ACTIVE) {
            goal.setStatus(GoalStatus.COMPLETED);
            recordGoalHistory(goal, "STATUS", GoalStatus.ACTIVE.name(), GoalStatus.COMPLETED.name(), actor);
        }

        Goal saved = goalRepository.save(goal);
        recordGoalHistory(saved, "PROGRESS", oldProgress.toString(), clampedProgress.toString(), actor);
        auditService.recordAudit(actor, "UPDATE_GOAL_PROGRESS", "Goal", saved.getBusinessId(),
                oldProgress.toString(), clampedProgress.toString(), notes);

        return saved;
    }

    private void validateStatusTransition(GoalStatus current, GoalStatus target) {
        if (current == GoalStatus.CANCELLED) {
            throw new BadRequestException("Cannot transition a CANCELLED goal");
        }
        if (current == GoalStatus.COMPLETED && target != GoalStatus.ACTIVE && target != GoalStatus.CANCELLED) {
            throw new BadRequestException("Cannot transition a COMPLETED goal to " + target);
        }
        if (current == GoalStatus.DRAFT && target == GoalStatus.COMPLETED) {
            throw new BadRequestException("Cannot directly complete a DRAFT goal without activating");
        }
    }

    private void recordGoalHistory(Goal goal, String fieldChanged, String oldValue, String newValue, Employee actor) {
        GoalHistory gh = GoalHistory.builder()
                .goal(goal)
                .fieldChanged(fieldChanged)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(actor)
                .build();
        goalHistoryRepository.save(gh);
    }
}
