package com.performiq.dataimport.dto;

import com.performiq.dataimport.entity.ImportScale;
import com.performiq.dataimport.entity.ImportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResponseDto {

    private UUID importRunId;
    private ImportStatus status;
    private ImportScale scale;
    private String seed;
    private String generatorVersion;
    private Integer totalRecords;
    private Integer validRecords;
    private Integer invalidRecords;
    private String manifestChecksum;
    private Long durationMs;
    private String message;
    private Map<String, Integer> importedTableCounts;
}
