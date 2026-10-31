package com.performiq.validation.dto;

import com.performiq.validation.entity.AnomalyCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationReportDto {

    private int totalAnomaliesDetected;
    private int benchmarkAnomaliesInjected;
    private double detectionRatePercentage;
    private Map<String, Long> anomaliesByType;
    private Map<String, Long> anomaliesBySeverity;
    private List<AnomalyCase> detectedCases;
}
