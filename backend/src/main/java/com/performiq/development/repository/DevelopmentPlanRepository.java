package com.performiq.development.repository;

import com.performiq.development.entity.DevelopmentPlan;
import com.performiq.development.entity.DevelopmentPlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DevelopmentPlanRepository extends JpaRepository<DevelopmentPlan, UUID> {
    Optional<DevelopmentPlan> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    List<DevelopmentPlan> findByEmployeeId(UUID employeeId);
    Page<DevelopmentPlan> findByEmployeeIdAndStatus(UUID employeeId, DevelopmentPlanStatus status, Pageable pageable);
    Optional<DevelopmentPlan> findByReviewId(UUID reviewId);
}
