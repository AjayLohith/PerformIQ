package com.performiq.validation.entity;

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
@Table(name = "anomaly_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyCase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "business_id", nullable = false, unique = true, length = 64)
    private String businessId;

    @Column(name = "issue_type", nullable = false, length = 100)
    private String issueType;

    @Column(name = "affected_entity", nullable = false, length = 64)
    private String affectedEntity;

    @Column(name = "affected_business_id", nullable = false, length = 64)
    private String affectedBusinessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 32)
    @Builder.Default
    private AnomalySeverity severity = AnomalySeverity.MEDIUM;

    @Column(name = "expected_outcome", columnDefinition = "TEXT")
    private String expectedOutcome;

    @Column(name = "actual_outcome", columnDefinition = "TEXT")
    private String actualOutcome;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_status", nullable = false, length = 32)
    @Builder.Default
    private DetectionStatus detectionStatus = DetectionStatus.DETECTED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
