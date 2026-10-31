package com.performiq.reviewcycle.dto;

import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransitionCycleDto {

    @NotNull(message = "Target status is required")
    private ReviewCycleStatus targetStatus;

    private String reason;

    @Builder.Default
    private Boolean force = false;
}
