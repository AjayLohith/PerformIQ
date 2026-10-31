package com.performiq.goal.dto;

import com.performiq.goal.entity.GoalStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGoalDto {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Review cycle ID is required")
    private UUID reviewCycleId;

    private String businessId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Builder.Default
    private String category = "INDIVIDUAL";

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than zero")
    @DecimalMax(value = "100.00", message = "Weight cannot exceed 100%")
    private BigDecimal weight;
}
