package com.performiq.reviewcycle.repository;

import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewCycleRepository extends JpaRepository<ReviewCycle, UUID> {
    Optional<ReviewCycle> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    Page<ReviewCycle> findByStatus(ReviewCycleStatus status, Pageable pageable);
    Optional<ReviewCycle> findFirstByStatus(ReviewCycleStatus status);
}
