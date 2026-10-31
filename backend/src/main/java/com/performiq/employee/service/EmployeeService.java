package com.performiq.employee.service;

import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.dto.EmployeeDto;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.entity.EmployeeStatus;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.manager.entity.Manager;
import com.performiq.manager.entity.ManagerAssignmentHistory;
import com.performiq.manager.repository.ManagerAssignmentHistoryRepository;
import com.performiq.manager.repository.ManagerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final ManagerAssignmentHistoryRepository managerAssignmentHistoryRepository;

    @Transactional(readOnly = true)
    public Page<EmployeeDto> listEmployees(String department, EmployeeStatus status, Pageable pageable) {
        Page<Employee> page;
        if (department != null && !department.isBlank()) {
            page = employeeRepository.findByDepartment(department, pageable);
        } else if (status != null) {
            page = employeeRepository.findByStatus(status, pageable);
        } else {
            page = employeeRepository.findAll(pageable);
        }

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return toDto(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeByBusinessId(String businessId) {
        Employee employee = employeeRepository.findByBusinessId(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with business ID: " + businessId));
        return toDto(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getDirectReports(UUID employeeId) {
        // Find if this employee is a Manager
        Optional<Manager> mgrOpt = managerRepository.findByEmployeeId(employeeId);
        if (mgrOpt.isEmpty()) {
            return List.of();
        }

        List<ManagerAssignmentHistory> assignments =
                managerAssignmentHistoryRepository.findCurrentDirectReportsByManagerId(mgrOpt.get().getId());

        return assignments.stream()
                .map(a -> toDto(a.getEmployee()))
                .toList();
    }

    private EmployeeDto toDto(Employee emp) {
        EmployeeDto.ManagerSummaryDto mgrSummary = null;
        Optional<ManagerAssignmentHistory> currentAssignment =
                managerAssignmentHistoryRepository.findCurrentAssignmentByEmployeeId(emp.getId());

        if (currentAssignment.isPresent() && currentAssignment.get().getManager() != null) {
            Manager mgr = currentAssignment.get().getManager();
            Employee mgrEmp = mgr.getEmployee();
            if (mgrEmp != null) {
                mgrSummary = EmployeeDto.ManagerSummaryDto.builder()
                        .managerId(mgr.getId())
                        .managerBusinessId(mgr.getBusinessId())
                        .name(mgrEmp.getFirstName() + " " + mgrEmp.getLastName())
                        .email(mgrEmp.getEmail())
                        .designation(mgrEmp.getDesignation())
                        .build();
            }
        }

        return EmployeeDto.builder()
                .id(emp.getId())
                .businessId(emp.getBusinessId())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .email(emp.getEmail())
                .department(emp.getDepartment())
                .designation(emp.getDesignation())
                .joiningDate(emp.getJoiningDate())
                .status(emp.getStatus())
                .currentManager(mgrSummary)
                .build();
    }
}
