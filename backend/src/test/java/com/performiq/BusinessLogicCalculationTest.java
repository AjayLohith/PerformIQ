package com.performiq;

import com.performiq.common.exception.BadRequestException;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalHistoryRepository;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.keyresult.entity.MetricType;
import com.performiq.keyresult.service.KeyResultService;
import com.performiq.goal.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessLogicCalculationTest {

    private KeyResultService keyResultService;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalHistoryRepository goalHistoryRepository;

    private GoalService goalService;

    @BeforeEach
    void setUp() {
        keyResultService = new KeyResultService(null, null, goalRepository, null);
        goalService = new GoalService(goalRepository, goalHistoryRepository, null, null, keyResultService, null);
    }

    @Test
    @DisplayName("Test INCREASE metric progress formula and clamping")
    void testIncreaseMetricProgress() {
        // Start: 10, Target: 50, Current: 30 -> (30-10)/(50-10) * 100 = 50.00%
        BigDecimal progress50 = keyResultService.computeProgress(
                MetricType.INCREASE,
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                new BigDecimal("30.00")
        );
        assertThat(progress50).isEqualByComparingTo("50.00");

        // Over-achieved: Start: 10, Target: 50, Current: 70 -> Clamped to 100.00%
        BigDecimal progressOver = keyResultService.computeProgress(
                MetricType.INCREASE,
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                new BigDecimal("70.00")
        );
        assertThat(progressOver).isEqualByComparingTo("100.00");

        // Regressed: Start: 10, Target: 50, Current: 5 -> Clamped to 0.00%
        BigDecimal progressUnder = keyResultService.computeProgress(
                MetricType.INCREASE,
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                new BigDecimal("5.00")
        );
        assertThat(progressUnder).isEqualByComparingTo("0.00");

        // Edge case: Start == Target
        BigDecimal progressEqual = keyResultService.computeProgress(
                MetricType.INCREASE,
                new BigDecimal("50.00"),
                new BigDecimal("50.00"),
                new BigDecimal("50.00")
        );
        assertThat(progressEqual).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Test DECREASE metric progress formula and clamping (e.g. latency or defect count)")
    void testDecreaseMetricProgress() {
        // Start: 200ms, Target: 100ms, Current: 150ms -> (200-150)/(200-100) * 100 = 50.00%
        BigDecimal progress50 = keyResultService.computeProgress(
                MetricType.DECREASE,
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("150.00")
        );
        assertThat(progress50).isEqualByComparingTo("50.00");

        // Over-achieved: Start: 200, Target: 100, Current: 50 -> Clamped to 100.00%
        BigDecimal progressOver = keyResultService.computeProgress(
                MetricType.DECREASE,
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00")
        );
        assertThat(progressOver).isEqualByComparingTo("100.00");

        // Regressed: Start: 200, Target: 100, Current: 300 -> Clamped to 0.00%
        BigDecimal progressUnder = keyResultService.computeProgress(
                MetricType.DECREASE,
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("300.00")
        );
        assertThat(progressUnder).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Test Goal weight validation: <= 100% passes, > 100% throws BadRequestException")
    void testGoalWeightValidation() {
        UUID empId = UUID.randomUUID();
        UUID cycleId = UUID.randomUUID();

        Goal g1 = Goal.builder().weight(new BigDecimal("40.00")).status(GoalStatus.ACTIVE).build();
        Goal g2 = Goal.builder().weight(new BigDecimal("35.00")).status(GoalStatus.ACTIVE).build();

        when(goalRepository.findByEmployeeIdAndReviewCycleId(empId, cycleId)).thenReturn(List.of(g1, g2));

        // Total existing = 75.00%. Adding 25.00% makes exactly 100.00% -> should succeed
        goalService.validateGoalWeights(empId, cycleId, new BigDecimal("25.00"), null);

        // Adding 26.00% makes 101.00% -> should throw BadRequestException
        assertThatThrownBy(() -> goalService.validateGoalWeights(empId, cycleId, new BigDecimal("26.00"), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot exceed 100.00%");
    }
}
