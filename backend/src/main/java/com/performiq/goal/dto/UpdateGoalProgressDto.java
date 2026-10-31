package com.performiq.goal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGoalProgressDto {

    @NotNull(message = "Progress value is required")
    @DecimalMin(value = "0.00", message = "Progress cannot be negative")
    @DecimalMax(value = "100.00", message = "Progress cannot exceed 100%")
    private BigDecimal progress;

    private String notes;
}
