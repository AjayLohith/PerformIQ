package com.performiq.checkin.controller;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.checkin.dto.RecordCheckInDto;
import com.performiq.checkin.entity.CheckIn;
import com.performiq.checkin.repository.CheckInRepository;
import com.performiq.checkin.service.CheckInService;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.evidence.entity.Evidence;
import com.performiq.evidence.repository.EvidenceRepository;
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
@RequestMapping("/check-ins")
@RequiredArgsConstructor
@Tag(name = "Check-ins", description = "Periodic goal check-ins, progress snapshots, and attached evidence records")
public class CheckInController {

    private final CheckInService checkInService;
    private final CheckInRepository checkInRepository;
    private final EvidenceRepository evidenceRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping
    @Operation(summary = "Get check-ins for a goal", description = "Returns chronological check-in history for a specific goal")
    public ResponseEntity<List<CheckIn>> getCheckInsForGoal(@RequestParam UUID goalId) {
        return ResponseEntity.ok(checkInRepository.findByGoalIdOrderByCheckInDateDesc(goalId));
    }

    @GetMapping("/{id}/evidence")
    @Operation(summary = "Get evidence items for a check-in", description = "Returns attached deliverables, links, and evidence for a specific check-in")
    public ResponseEntity<List<Evidence>> getEvidenceForCheckIn(@PathVariable UUID id) {
        return ResponseEntity.ok(evidenceRepository.findByCheckInId(id));
    }

    @PostMapping
    @Operation(summary = "Record a check-in", description = "Records a check-in on a goal with snapshot progress, notes, and optional evidence links")
    public ResponseEntity<CheckIn> recordCheckIn(@Valid @RequestBody RecordCheckInDto request,
                                                 Authentication authentication) {
        Employee actor = getActor(authentication);
        if (actor == null) {
            throw new ResourceNotFoundException("Authenticated employee profile not found");
        }

        CheckIn checkIn = checkInService.recordCheckIn(
                request.getGoalId(), actor, request.getBusinessId(), request.getNotes(),
                request.getProgressSnapshot(), request.getCheckInDate(), request.getEvidenceList());

        return ResponseEntity.ok(checkIn);
    }

    private Employee getActor(Authentication authentication) {
        if (authentication == null) return null;
        String name = authentication.getName();
        return appUserRepository.findByUsername(name)
                .or(() -> !name.contains("@") ? appUserRepository.findByUsername(name + "@performiq.com") : java.util.Optional.empty())
                .map(AppUser::getEmployee)
                .orElse(null);
    }
}
