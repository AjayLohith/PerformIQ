package com.performiq.keyresult.dto;

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
public class UpdateKeyResultValueDto {

    @NotNull(message = "New value is required")
    private BigDecimal currentValue;

    private String note;
}
