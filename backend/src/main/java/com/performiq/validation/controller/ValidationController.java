package com.performiq.validation.controller;

import com.performiq.validation.dto.ValidationReportDto;
import com.performiq.validation.entity.AnomalyCase;
import com.performiq.validation.repository.AnomalyCaseRepository;
import com.performiq.validation.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/validation")
@RequiredArgsConstructor
@Tag(name = "Data Validation & Quality", description = "Endpoints for integrity audits, anomaly detection, and data health reporting")
public class ValidationController {

    private final ValidationService validationService;
    private final AnomalyCaseRepository anomalyCaseRepository;

    @GetMapping("/report")
    @Operation(summary = "Run data quality validation scan", description = "Executes rule engine across all entities and compiles anomaly detection report")
    public ResponseEntity<ValidationReportDto> getValidationReport() {
        return ResponseEntity.ok(validationService.runDataQualityValidation());
    }

    @GetMapping("/anomalies")
    @Operation(summary = "List benchmark anomaly cases", description = "Retrieves ground-truth anomaly cases logged during import")
    public ResponseEntity<List<AnomalyCase>> getAllAnomalies() {
        return ResponseEntity.ok(anomalyCaseRepository.findAll());
    }
}
