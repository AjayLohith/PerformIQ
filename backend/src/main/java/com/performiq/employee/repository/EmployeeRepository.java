package com.performiq.employee.repository;

import com.performiq.employee.entity.Employee;
import com.performiq.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByBusinessId(String businessId);
    Optional<Employee> findByEmail(String email);
    boolean existsByBusinessId(String businessId);
    boolean existsByEmail(String email);
    Page<Employee> findByDepartment(String department, Pageable pageable);
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);
}
