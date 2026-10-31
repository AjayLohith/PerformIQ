package com.performiq.goal.controller;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.checkin.repository.CheckInRepository;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import com.performiq.goal.dto.CreateGoalDto;
import com.performiq.goal.dto.GoalDetailsDto;
import com.performiq.goal.dto.UpdateGoalProgressDto;
import com.performiq.goal.dto.UpdateGoalStatusDto;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.goal.service.GoalService;
import com.performiq.keyresult.repository.KeyResultRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Performance goals, weighting, progress tracking, and lifecycle transitions")
public class GoalController {

    private final GoalService goalService;
    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final CheckInRepository checkInRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping
    @Operation(summary = "Get goals", description = "Returns all goals for an employee within a given review cycle")
    public ResponseEntity<List<Goal>> getGoals(
            @RequestParam UUID employeeId,
            @RequestParam(required = false) UUID reviewCycleId
    ) {
        List<Goal> goals = reviewCycleId != null
                ? goalRepository.findByEmployeeIdAndReviewCycleId(employeeId, reviewCycleId)
                : goalRepository.findByEmployeeId(employeeId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal details", description = "Returns detailed goal information including child key results and check-in history")
    public ResponseEntity<GoalDetailsDto> getGoalDetails(@PathVariable UUID id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        var keyResults = keyResultRepository.findByGoalId(id);
        var checkIns = checkInRepository.findByGoalIdOrderByCheckInDateDesc(id);

        return ResponseEntity.ok(GoalDetailsDto.builder()
                .goal(goal)
                .keyResults(keyResults)
                .checkIns(checkIns)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a goal", description = "Creates a goal in DRAFT status after validating weight constraint (total <= 100%)")
    public ResponseEntity<Goal> createGoal(@Valid @RequestBody CreateGoalDto request,
                                           Authentication authentication) {
        Employee actor = getActor(authentication);
        Goal goal = goalService.createGoal(
                request.getEmployeeId(), request.getReviewCycleId(), request.getBusinessId(),
                request.getTitle(), request.getDescription(), request.getCategory(),
                request.getWeight(), actor);
        return ResponseEntity.ok(goal);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update goal status", description = "Transitions goal lifecycle status (DRAFT -> ACTIVE -> COMPLETED / CANCELLED)")
    public ResponseEntity<Goal> updateGoalStatus(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateGoalStatusDto request,
                                                 Authentication authentication) {
        Employee actor = getActor(authentication);
        Goal updated = goalService.updateGoalStatus(id, request.getStatus(), actor, request.getReason());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/progress")
    @Operation(summary = "Update direct goal progress", description = "Updates direct progress percentage (clamped to [0, 100]) if goal has no child key results")
    public ResponseEntity<Goal> updateGoalProgress(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateGoalProgressDto request,
                                                   Authentication authentication) {
        Employee actor = getActor(authentication);
        Goal updated = goalService.updateDirectProgress(id, request.getProgress(), actor, request.getNotes());
        return ResponseEntity.ok(updated);
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
