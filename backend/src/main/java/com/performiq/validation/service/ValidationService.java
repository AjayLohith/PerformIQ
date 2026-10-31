package com.performiq.validation.service;

import com.performiq.checkin.entity.CheckIn;
import com.performiq.checkin.repository.CheckInRepository;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.entity.EmployeeStatus;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.feedback.entity.Feedback;
import com.performiq.feedback.repository.FeedbackRepository;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.keyresult.entity.KeyResult;
import com.performiq.keyresult.repository.KeyResultRepository;
import com.performiq.manager.entity.ManagerAssignmentHistory;
import com.performiq.manager.repository.ManagerAssignmentHistoryRepository;
import com.performiq.validation.dto.ValidationReportDto;
import com.performiq.validation.entity.AnomalyCase;
import com.performiq.validation.entity.AnomalySeverity;
import com.performiq.validation.entity.DetectionStatus;
import com.performiq.validation.repository.AnomalyCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final CheckInRepository checkInRepository;
    private final FeedbackRepository feedbackRepository;
    private final EmployeeRepository employeeRepository;
    private final ManagerAssignmentHistoryRepository managerAssignmentHistoryRepository;
    private final AnomalyCaseRepository anomalyCaseRepository;

    @Transactional
    public ValidationReportDto runDataQualityValidation() {
        log.info("Starting comprehensive data quality and business rule validation scan...");
        List<AnomalyCase> discoveredAnomalies = new ArrayList<>();
        int seq = 1;

        // 1. Goal Progress Derivation Check: Progress must match sum(kr.progress * kr.weight) / sum(kr.weight)
        List<Goal> allGoals = goalRepository.findAll();
        for (Goal goal : allGoals) {
            List<KeyResult> krs = keyResultRepository.findByGoalId(goal.getId());
            if (!krs.isEmpty()) {
                BigDecimal totalWeightedProg = BigDecimal.ZERO;
                BigDecimal totalWeight = BigDecimal.ZERO;
                for (KeyResult kr : krs) {
                    BigDecimal krProg = kr.getProgress() != null ? kr.getProgress() : BigDecimal.ZERO;
                    BigDecimal krWeight = kr.getWeight() != null ? kr.getWeight() : BigDecimal.ONE;
                    totalWeightedProg = totalWeightedProg.add(krProg.multiply(krWeight));
                    totalWeight = totalWeight.add(krWeight);
                }

                if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal expectedGoalProg = totalWeightedProg.divide(totalWeight, 2, RoundingMode.HALF_UP);
                    BigDecimal actualProg = goal.getProgress() != null ? goal.getProgress() : BigDecimal.ZERO;
                    if (actualProg.subtract(expectedGoalProg).abs().compareTo(new BigDecimal("0.05")) > 0) {
                        recordAnomaly(
                                "PROGRESS_CALCULATION_MISMATCH",
                                "GOAL",
                                goal.getBusinessId(),
                                AnomalySeverity.HIGH,
                                "Goal progress must match weighted key results: " + expectedGoalProg + "%",
                                "Discrepancy detected: goal progress is recorded as " + actualProg + "%",
                                discoveredAnomalies,
                                seq++
                        );
                    }
                }
            }

            // Check if goal progress exceeds 100%
            if (goal.getProgress() != null && goal.getProgress().compareTo(new BigDecimal("100.00")) > 0) {
                recordAnomaly(
                        "EXCESSIVE_PROGRESS_CLAMP",
                        "GOAL",
                        goal.getBusinessId(),
                        AnomalySeverity.LOW,
                        "Goal progress must be clamped to 100.00%",
                        "Progress exceeds clamp limit: " + goal.getProgress() + "%",
                        discoveredAnomalies,
                        seq++
                );
            }
        }

        // 2. Check-in Date Boundary Check
        List<CheckIn> allCheckIns = checkInRepository.findAll();
        for (CheckIn cin : allCheckIns) {
            if (cin.getNotes() == null || cin.getNotes().isBlank()) {
                recordAnomaly(
                        "CHECKIN_EMPTY_NOTES",
                        "CHECK_IN",
                        cin.getBusinessId(),
                        AnomalySeverity.LOW,
                        "Check-in notes must contain non-empty narrative",
                        "Check-in notes field is empty or blank",
                        discoveredAnomalies,
                        seq++
                );
            }

            if (cin.getGoal() != null && cin.getGoal().getReviewCycle() != null) {
                var cycle = cin.getGoal().getReviewCycle();
                if (cin.getCheckInDate().isBefore(cycle.getStartDate()) || cin.getCheckInDate().isAfter(cycle.getEndDate())) {
                    recordAnomaly(
                            "CHECKIN_DATE_OUT_OF_BOUNDS",
                            "CHECK_IN",
                            cin.getBusinessId(),
                            AnomalySeverity.MEDIUM,
                            "Check-in date must fall within cycle [" + cycle.getStartDate() + " to " + cycle.getEndDate() + "]",
                            "Check-in date " + cin.getCheckInDate() + " violates cycle boundary",
                            discoveredAnomalies,
                            seq++
                    );
                }
            }
        }

        // 3. Feedback Rating Range Check
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        for (Feedback fb : allFeedbacks) {
            if (fb.getOverallRating() != null && (fb.getOverallRating() < 1 || fb.getOverallRating() > 5)) {
                recordAnomaly(
                        "OUT_OF_RANGE_RATING",
                        "FEEDBACK",
                        fb.getBusinessId(),
                        AnomalySeverity.HIGH,
                        "Feedback overall rating must be within 1 and 5",
                        "Invalid rating found: " + fb.getOverallRating(),
                        discoveredAnomalies,
                        seq++
                );
            }
        }

        // 4. Terminated Employee with Active Goals Check
        List<Employee> terminatedEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == EmployeeStatus.TERMINATED)
                .toList();

        for (Employee emp : terminatedEmployees) {
            List<Goal> empGoals = goalRepository.findByEmployeeId(emp.getId());
            for (Goal g : empGoals) {
                if (g.getStatus() == GoalStatus.ACTIVE) {
                    recordAnomaly(
                            "TERMINATED_EMPLOYEE_ACTIVE_GOAL",
                            "GOAL",
                            g.getBusinessId(),
                            AnomalySeverity.CRITICAL,
                            "Terminated employee cannot own ACTIVE goals",
                            "Employee " + emp.getBusinessId() + " is TERMINATED but goal status is ACTIVE",
                            discoveredAnomalies,
                            seq++
                    );
                }
            }
        }

        // 5. Circular / Self Manager Reporting Check
        List<ManagerAssignmentHistory> assignments = managerAssignmentHistoryRepository.findAll();
        for (ManagerAssignmentHistory mah : assignments) {
            if (mah.getManager() != null && mah.getManager().getEmployee() != null) {
                if (mah.getEmployee().getId().equals(mah.getManager().getEmployee().getId())) {
                    recordAnomaly(
                            "CIRCULAR_REPORTING_HIERARCHY",
                            "MANAGER_ASSIGNMENT_HISTORY",
                            mah.getEmployee().getBusinessId(),
                            AnomalySeverity.CRITICAL,
                            "Employee cannot be self-reporting manager",
                            "Employee " + mah.getEmployee().getBusinessId() + " is assigned to manage themselves",
                            discoveredAnomalies,
                            seq++
                    );
                }
            }
        }

        // 6. Key Result Progress Out of Bounds
        List<KeyResult> allKrs = keyResultRepository.findAll();
        for (KeyResult kr : allKrs) {
            if (kr.getProgress() != null && (kr.getProgress().compareTo(BigDecimal.ZERO) < 0 || kr.getProgress().compareTo(new BigDecimal("100.00")) > 0)) {
                recordAnomaly(
                        "KEY_RESULT_PROGRESS_OUT_OF_BOUNDS",
                        "KEY_RESULT",
                        kr.getBusinessId(),
                        AnomalySeverity.MEDIUM,
                        "Key result progress must be between [0.00, 100.00]",
                        "Progress value is out of bounds: " + kr.getProgress() + "%",
                        discoveredAnomalies,
                        seq++
                );
            }
        }

        log.info("Validation completed. Discovered {} data quality anomalies.", discoveredAnomalies.size());

        // Count benchmark injected anomalies
        long benchmarkInjected = anomalyCaseRepository.count();
        double detectionRate = benchmarkInjected > 0
                ? Math.min(100.0, ((double) discoveredAnomalies.size() / benchmarkInjected) * 100.0)
                : 100.0;

        Map<String, Long> byType = discoveredAnomalies.stream()
                .collect(Collectors.groupingBy(AnomalyCase::getIssueType, Collectors.counting()));

        Map<String, Long> bySeverity = discoveredAnomalies.stream()
                .collect(Collectors.groupingBy(a -> a.getSeverity().name(), Collectors.counting()));

        return ValidationReportDto.builder()
                .totalAnomaliesDetected(discoveredAnomalies.size())
                .benchmarkAnomaliesInjected((int) benchmarkInjected)
                .detectionRatePercentage(Math.round(detectionRate * 100.0) / 100.0)
                .anomaliesByType(byType)
                .anomaliesBySeverity(bySeverity)
                .detectedCases(discoveredAnomalies)
                .build();
    }

    private void recordAnomaly(
            String issueType,
            String affectedEntity,
            String affectedBusinessId,
            AnomalySeverity severity,
            String expected,
            String actual,
            List<AnomalyCase> discovered,
            int seq
    ) {
        String businessId = "DETECTED-" + String.format("%04d", seq);
        AnomalyCase anomaly = AnomalyCase.builder()
                .businessId(businessId)
                .issueType(issueType)
                .affectedEntity(affectedEntity)
                .affectedBusinessId(affectedBusinessId)
                .severity(severity)
                .expectedOutcome(expected)
                .actualOutcome(actual)
                .detectionStatus(DetectionStatus.DETECTED)
                .build();

        discovered.add(anomaly);
    }
}
