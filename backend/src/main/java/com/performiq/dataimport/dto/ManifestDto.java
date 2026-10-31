package com.performiq.dataimport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ManifestDto {

    @JsonProperty("manifest_version")
    private String manifestVersion;

    @JsonProperty("generator_version")
    private String generatorVersion;

    @JsonProperty("scale")
    private String scale;

    @JsonProperty("seed")
    private String seed;

    @JsonProperty("generated_at")
    private String generatedAt;

    @JsonProperty("total_tables")
    private Integer totalTables;

    @JsonProperty("total_records")
    private Integer totalRecords;

    @JsonProperty("record_counts")
    private Map<String, Integer> recordCounts;

    @JsonProperty("files")
    private List<ManifestFileDto> files;

    @JsonProperty("bundle_checksum")
    private String bundleChecksum;
}
