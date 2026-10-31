package com.performiq.goal.repository;

import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    Optional<Goal> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    List<Goal> findByEmployeeId(UUID employeeId);
    List<Goal> findByEmployeeIdAndReviewCycleId(UUID employeeId, UUID reviewCycleId);
    Page<Goal> findByEmployeeIdAndStatus(UUID employeeId, GoalStatus status, Pageable pageable);
}
