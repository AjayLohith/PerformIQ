package com.performiq.development.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDevPlanDto {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    private UUID reviewId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Objectives are required")
    private String objectives;

    private String skillsToImprove;

    private String actionItems;

    private LocalDate targetDate;
}
