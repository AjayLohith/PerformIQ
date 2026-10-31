package com.performiq.dataimport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "data_import_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataImportRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "seed", nullable = false, length = 64)
    private String seed;

    @Enumerated(EnumType.STRING)
    @Column(name = "scale", nullable = false, length = 32)
    private ImportScale scale;

    @Column(name = "generator_version", nullable = false, length = 32)
    private String generatorVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private ImportStatus status = ImportStatus.PENDING;

    @Column(name = "total_records", nullable = false)
    @Builder.Default
    private Integer totalRecords = 0;

    @Column(name = "valid_records", nullable = false)
    @Builder.Default
    private Integer validRecords = 0;

    @Column(name = "invalid_records", nullable = false)
    @Builder.Default
    private Integer invalidRecords = 0;

    @Column(name = "manifest_checksum", length = 128)
    private String manifestChecksum;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
