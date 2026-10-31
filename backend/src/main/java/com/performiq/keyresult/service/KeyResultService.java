package com.performiq.keyresult.service;

import com.performiq.audit.service.AuditService;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.keyresult.entity.KeyResult;
import com.performiq.keyresult.entity.KeyResultHistory;
import com.performiq.keyresult.entity.MetricType;
import com.performiq.keyresult.repository.KeyResultHistoryRepository;
import com.performiq.keyresult.repository.KeyResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyResultService {

    private final KeyResultRepository keyResultRepository;
    private final KeyResultHistoryRepository keyResultHistoryRepository;
    private final GoalRepository goalRepository;
    private final AuditService auditService;

    public BigDecimal computeProgress(MetricType metricType, BigDecimal startValue, BigDecimal targetValue, BigDecimal currentValue) {
        if (metricType == null || startValue == null || targetValue == null || currentValue == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal progress;
        if (metricType == MetricType.INCREASE) {
            BigDecimal delta = targetValue.subtract(startValue);
            if (delta.compareTo(BigDecimal.ZERO) == 0) {
                progress = currentValue.compareTo(targetValue) >= 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
            } else {
                progress = currentValue.subtract(startValue)
                        .multiply(new BigDecimal("100"))
                        .divide(delta, 4, RoundingMode.HALF_UP);
            }
        } else { // DECREASE
            BigDecimal delta = startValue.subtract(targetValue);
            if (delta.compareTo(BigDecimal.ZERO) == 0) {
                progress = currentValue.compareTo(targetValue) <= 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
            } else {
                progress = startValue.subtract(currentValue)
                        .multiply(new BigDecimal("100"))
                        .divide(delta, 4, RoundingMode.HALF_UP);
            }
        }

        // Clamp between 0.00 and 100.00
        if (progress.compareTo(BigDecimal.ZERO) < 0) {
            progress = BigDecimal.ZERO;
        } else if (progress.compareTo(new BigDecimal("100.00")) > 0) {
            progress = new BigDecimal("100.00");
        }

        return progress.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public KeyResult createKeyResult(UUID goalId, String businessId, String title, MetricType metricType,
                                     BigDecimal startValue, BigDecimal targetValue, BigDecimal currentValue,
                                     BigDecimal weight, String unit, Employee actor) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        BigDecimal initialCurrent = currentValue != null ? currentValue : startValue;
        BigDecimal initialProgress = computeProgress(metricType, startValue, targetValue, initialCurrent);

        KeyResult kr = KeyResult.builder()
                .businessId(businessId != null ? businessId : "KR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .goal(goal)
                .title(title)
                .metricType(metricType)
                .startValue(startValue)
                .targetValue(targetValue)
                .currentValue(initialCurrent)
                .weight(weight != null ? weight : BigDecimal.ONE)
                .unit(unit != null ? unit : "%")
                .progress(initialProgress)
                .build();

        KeyResult saved = keyResultRepository.save(kr);
        recalculateParentGoalProgress(goal);

        auditService.recordAudit(actor, "CREATE_KEY_RESULT", "KeyResult",
                saved.getBusinessId(), null, saved.getTitle(), "KeyResult created");

        return saved;
    }

    @Transactional
    public KeyResult updateCurrentValue(UUID keyResultId, BigDecimal newValue, Employee actor, String note) {
        KeyResult kr = keyResultRepository.findById(keyResultId)
                .orElseThrow(() -> new ResourceNotFoundException("KeyResult not found with id: " + keyResultId));

        BigDecimal oldValue = kr.getCurrentValue();
        if (newValue == null) {
            throw new BadRequestException("New value cannot be null");
        }

        BigDecimal calculatedProgress = computeProgress(kr.getMetricType(), kr.getStartValue(), kr.getTargetValue(), newValue);

        kr.setCurrentValue(newValue);
        kr.setProgress(calculatedProgress);
        KeyResult savedKr = keyResultRepository.save(kr);

        // Record history
        KeyResultHistory history = KeyResultHistory.builder()
                .keyResult(savedKr)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(actor)
                .build();
        keyResultHistoryRepository.save(history);

        // Recalculate parent goal progress
        recalculateParentGoalProgress(kr.getGoal());

        // Audit
        auditService.recordAudit(actor, "UPDATE_KEY_RESULT_VALUE", "KeyResult",
                kr.getBusinessId(), oldValue.toString(), newValue.toString(), note);

        return savedKr;
    }

    @Transactional
    public void recalculateParentGoalProgress(Goal goal) {
        if (goal == null) return;
        List<KeyResult> keyResults = keyResultRepository.findByGoalId(goal.getId());
        if (keyResults.isEmpty()) {
            return;
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        for (KeyResult kr : keyResults) {
            BigDecimal weight = kr.getWeight() != null ? kr.getWeight() : BigDecimal.ONE;
            BigDecimal progress = kr.getProgress() != null ? kr.getProgress() : BigDecimal.ZERO;

            weightedSum = weightedSum.add(progress.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        BigDecimal finalGoalProgress;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            finalGoalProgress = weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP);
        } else {
            finalGoalProgress = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // Clamp
        if (finalGoalProgress.compareTo(BigDecimal.ZERO) < 0) {
            finalGoalProgress = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (finalGoalProgress.compareTo(new BigDecimal("100.00")) > 0) {
            finalGoalProgress = new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP);
        }

        goal.setProgress(finalGoalProgress);
        goalRepository.save(goal);
    }
}
