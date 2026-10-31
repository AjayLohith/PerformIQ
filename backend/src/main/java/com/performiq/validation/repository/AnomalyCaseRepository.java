package com.performiq.validation.repository;

import com.performiq.validation.entity.AnomalyCase;
import com.performiq.validation.entity.AnomalySeverity;
import com.performiq.validation.entity.DetectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnomalyCaseRepository extends JpaRepository<AnomalyCase, UUID> {
    Optional<AnomalyCase> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    Page<AnomalyCase> findByDetectionStatus(DetectionStatus status, Pageable pageable);
    Page<AnomalyCase> findBySeverity(AnomalySeverity severity, Pageable pageable);
    Page<AnomalyCase> findByAffectedEntity(String affectedEntity, Pageable pageable);
}
