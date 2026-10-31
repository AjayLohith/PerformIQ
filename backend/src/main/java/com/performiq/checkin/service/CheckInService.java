package com.performiq.checkin.service;

import com.performiq.audit.service.AuditService;
import com.performiq.checkin.entity.CheckIn;
import com.performiq.checkin.repository.CheckInRepository;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.evidence.entity.Evidence;
import com.performiq.evidence.entity.EvidenceType;
import com.performiq.evidence.repository.EvidenceRepository;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalHistory;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalHistoryRepository;
import com.performiq.goal.repository.GoalRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final EvidenceRepository evidenceRepository;
    private final GoalRepository goalRepository;
    private final GoalHistoryRepository goalHistoryRepository;
    private final AuditService auditService;

    @Data
    @Builder
    public static class EvidenceDto {
        private String businessId;
        private String title;
        private String url;
        private String description;
        private EvidenceType evidenceType;
    }

    @Transactional
    public CheckIn recordCheckIn(UUID goalId, Employee employee, String businessId, String notes,
                                 BigDecimal progressSnapshot, LocalDate checkInDate, List<EvidenceDto> evidenceList) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        if (goal.getStatus() == GoalStatus.CANCELLED) {
            throw new BadRequestException("Cannot submit check-in for a CANCELLED goal");
        }

        BigDecimal clampedProgress = progressSnapshot != null
                ? progressSnapshot.max(BigDecimal.ZERO).min(new BigDecimal("100.00")).setScale(2, RoundingMode.HALF_UP)
                : goal.getProgress();

        CheckIn checkIn = CheckIn.builder()
                .businessId(businessId != null ? businessId : "CHK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .goal(goal)
                .employee(employee)
                .notes(notes)
                .progressSnapshot(clampedProgress)
                .checkInDate(checkInDate != null ? checkInDate : LocalDate.now())
                .build();

        CheckIn savedCheckIn = checkInRepository.save(checkIn);

        // Save evidence items if provided
        if (evidenceList != null && !evidenceList.isEmpty()) {
            for (EvidenceDto ed : evidenceList) {
                Evidence ev = Evidence.builder()
                        .businessId(ed.getBusinessId() != null ? ed.getBusinessId() : "EVI-" + UUID.randomUUID().toString().substring(0, 8))
                        .checkIn(savedCheckIn)
                        .title(ed.getTitle())
                        .url(ed.getUrl())
                        .description(ed.getDescription())
                        .evidenceType(ed.getEvidenceType() != null ? ed.getEvidenceType() : EvidenceType.URL)
                        .build();
                evidenceRepository.save(ev);
            }
        }

        // Update goal progress & transition to COMPLETED if reached 100%
        BigDecimal oldGoalProgress = goal.getProgress();
        goal.setProgress(clampedProgress);
        if (goal.getStatus() == GoalStatus.DRAFT) {
            goal.setStatus(GoalStatus.ACTIVE);
        }
        if (clampedProgress.compareTo(new BigDecimal("100.00")) == 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        goalRepository.save(goal);

        // Record history and audit
        GoalHistory gh = GoalHistory.builder()
                .goal(goal)
                .fieldChanged("PROGRESS")
                .oldValue(oldGoalProgress.toString())
                .newValue(clampedProgress.toString())
                .changedBy(employee)
                .build();
        goalHistoryRepository.save(gh);

        auditService.recordAudit(employee, "RECORD_CHECK_IN", "Goal", goal.getBusinessId(),
                oldGoalProgress.toString(), clampedProgress.toString(), notes);

        return savedCheckIn;
    }
}
