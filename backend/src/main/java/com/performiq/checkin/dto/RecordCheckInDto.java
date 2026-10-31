package com.performiq.checkin.dto;

import com.performiq.checkin.service.CheckInService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordCheckInDto {

    @NotNull(message = "Goal ID is required")
    private UUID goalId;

    private String businessId;

    @NotBlank(message = "Check-in notes are required")
    private String notes;

    private BigDecimal progressSnapshot;

    private LocalDate checkInDate;

    private List<CheckInService.EvidenceDto> evidenceList;
}
