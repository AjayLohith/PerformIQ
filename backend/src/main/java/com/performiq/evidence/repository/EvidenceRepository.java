package com.performiq.evidence.repository;

import com.performiq.evidence.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
    Optional<Evidence> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    List<Evidence> findByCheckInId(UUID checkInId);
}
