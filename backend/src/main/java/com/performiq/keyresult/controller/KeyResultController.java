package com.performiq.keyresult.controller;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.employee.entity.Employee;
import com.performiq.keyresult.dto.CreateKeyResultDto;
import com.performiq.keyresult.dto.UpdateKeyResultValueDto;
import com.performiq.keyresult.entity.KeyResult;
import com.performiq.keyresult.repository.KeyResultRepository;
import com.performiq.keyresult.service.KeyResultService;
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
@RequestMapping("/key-results")
@RequiredArgsConstructor
@Tag(name = "Key Results", description = "Measurable Key Results for OKR goals, progress calculation, and value updates")
public class KeyResultController {

    private final KeyResultService keyResultService;
    private final KeyResultRepository keyResultRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping
    @Operation(summary = "Get key results for a goal", description = "Returns all key results associated with the given goal ID")
    public ResponseEntity<List<KeyResult>> getKeyResults(@RequestParam UUID goalId) {
        return ResponseEntity.ok(keyResultRepository.findByGoalId(goalId));
    }

    @PostMapping
    @Operation(summary = "Create key result", description = "Creates a new measurable key result and recalculates parent goal progress")
    public ResponseEntity<KeyResult> createKeyResult(@Valid @RequestBody CreateKeyResultDto request,
                                                     Authentication authentication) {
        Employee actor = getActor(authentication);
        KeyResult kr = keyResultService.createKeyResult(
                request.getGoalId(), request.getBusinessId(), request.getTitle(),
                request.getMetricType(), request.getStartValue(), request.getTargetValue(),
                request.getCurrentValue(), request.getWeight(), request.getUnit(), actor);
        return ResponseEntity.ok(kr);
    }

    @RequestMapping(value = "/{id}/value", method = {org.springframework.web.bind.annotation.RequestMethod.PATCH, org.springframework.web.bind.annotation.RequestMethod.PUT})
    @Operation(summary = "Update key result current value", description = "Updates the current value, computes clamped progress, logs history, and cascades to parent goal")
    public ResponseEntity<KeyResult> updateValue(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateKeyResultValueDto request,
                                                 Authentication authentication) {
        Employee actor = getActor(authentication);
        KeyResult kr = keyResultService.updateCurrentValue(id, request.getCurrentValue(), actor, request.getNote());
        return ResponseEntity.ok(kr);
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
