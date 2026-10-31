package com.performiq.dataimport.controller;

import com.performiq.dataimport.dto.ImportRequestDto;
import com.performiq.dataimport.dto.ImportResponseDto;
import com.performiq.dataimport.entity.DataImportRun;
import com.performiq.dataimport.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/import")
@RequiredArgsConstructor
@Tag(name = "Data Import", description = "Admin endpoints for synthetic dataset ingestion and manifest verification")
public class DataImportController {

    private final DataImportService dataImportService;

    @PostMapping
    @Operation(summary = "Trigger synthetic dataset import", description = "Verifies SHA-256 manifest and ingests CSV datasets in topological order")
    public ResponseEntity<ImportResponseDto> triggerImport(
            @RequestBody(required = false) ImportRequestDto request
    ) {
        ImportRequestDto req = request != null ? request : new ImportRequestDto();
        ImportResponseDto response = dataImportService.executeImport(req);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Get latest import run status", description = "Returns details of the most recently initiated import run")
    public ResponseEntity<DataImportRun> getLatestStatus() {
        return ResponseEntity.ok(dataImportService.getLatestImportRun());
    }

    @GetMapping("/runs")
    @Operation(summary = "List all import runs", description = "Returns historical log of import runs")
    public ResponseEntity<List<DataImportRun>> getAllRuns() {
        return ResponseEntity.ok(dataImportService.getAllImportRuns());
    }
}
