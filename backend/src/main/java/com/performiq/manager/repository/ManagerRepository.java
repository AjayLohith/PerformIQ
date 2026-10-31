package com.performiq.manager.repository;

import com.performiq.manager.entity.Manager;
import com.performiq.manager.entity.ManagerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, UUID> {
    Optional<Manager> findByBusinessId(String businessId);
    Optional<Manager> findByEmployeeId(UUID employeeId);
    boolean existsByBusinessId(String businessId);
    Page<Manager> findByStatus(ManagerStatus status, Pageable pageable);
}
