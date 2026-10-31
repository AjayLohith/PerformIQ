package com.performiq.evidence.entity;

import com.performiq.checkin.entity.CheckIn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "evidence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "business_id", nullable = false, unique = true, length = 64)
    private String businessId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckIn checkIn;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 32)
    @Builder.Default
    private EvidenceType evidenceType = EvidenceType.URL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
