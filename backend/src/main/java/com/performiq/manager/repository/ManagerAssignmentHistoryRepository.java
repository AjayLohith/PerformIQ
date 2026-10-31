package com.performiq.manager.repository;

import com.performiq.manager.entity.ManagerAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ManagerAssignmentHistoryRepository extends JpaRepository<ManagerAssignmentHistory, UUID> {
    List<ManagerAssignmentHistory> findByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);
    List<ManagerAssignmentHistory> findByManagerIdOrderByEffectiveFromDesc(UUID managerId);

    @Query("SELECT mah FROM ManagerAssignmentHistory mah WHERE mah.employee.id = :employeeId AND mah.effectiveTo IS NULL")
    Optional<ManagerAssignmentHistory> findCurrentAssignmentByEmployeeId(@Param("employeeId") UUID employeeId);

    @Query("SELECT mah FROM ManagerAssignmentHistory mah WHERE mah.manager.id = :managerId AND mah.effectiveTo IS NULL")
    List<ManagerAssignmentHistory> findCurrentDirectReportsByManagerId(@Param("managerId") UUID managerId);
}
