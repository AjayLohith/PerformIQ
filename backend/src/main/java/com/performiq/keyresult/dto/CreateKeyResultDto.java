package com.performiq.keyresult.dto;

import com.performiq.keyresult.entity.MetricType;
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
public class CreateKeyResultDto {

    @NotNull(message = "Goal ID is required")
    private UUID goalId;

    private String businessId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Metric type is required")
    private MetricType metricType;

    @NotNull(message = "Start value is required")
    private BigDecimal startValue;

    @NotNull(message = "Target value is required")
    private BigDecimal targetValue;

    private BigDecimal currentValue;

    @DecimalMin(value = "0.01", message = "Weight must be positive")
    @Builder.Default
    private BigDecimal weight = new BigDecimal("1.00");

    @Builder.Default
    private String unit = "%";
}
