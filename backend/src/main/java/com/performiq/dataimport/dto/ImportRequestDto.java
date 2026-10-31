package com.performiq.dataimport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportRequestDto {

    /**
     * Optional directory override; defaults to app.data-import.directory from configuration.
     */
    private String directoryPath;

    /**
     * Format to ingest: 'csv' or 'json' (defaults to 'csv' for fast streaming ingestion).
     */
    @Builder.Default
    private String format = "csv";

    /**
     * Whether to cryptographically verify SHA-256 hashes against manifest.json before ingesting.
     */
    @Builder.Default
    private Boolean verifyChecksums = true;

    /**
     * Whether to run post-import data quality validation rules.
     */
    @Builder.Default
    private Boolean runValidation = true;
}
