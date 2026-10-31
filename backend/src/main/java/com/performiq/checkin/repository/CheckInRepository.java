package com.performiq.checkin.repository;

import com.performiq.checkin.entity.CheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {
    Optional<CheckIn> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    List<CheckIn> findByGoalIdOrderByCheckInDateDesc(UUID goalId);
    Page<CheckIn> findByEmployeeIdOrderByCheckInDateDesc(UUID employeeId, Pageable pageable);
}
