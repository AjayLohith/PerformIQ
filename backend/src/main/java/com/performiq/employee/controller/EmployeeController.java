package com.performiq.employee.controller;

import com.performiq.employee.dto.EmployeeDto;
import com.performiq.employee.entity.EmployeeStatus;
import com.performiq.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee directory, organizational profile, and direct reporting hierarchy")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "List employees", description = "Returns a paginated list of employees with optional department or status filtering")
    public ResponseEntity<Page<EmployeeDto>> listEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(employeeService.listEmployees(department, status, pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Returns comprehensive employee profile including current manager assignment")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/business-id/{businessId}")
    @Operation(summary = "Get employee by business ID", description = "Returns employee profile by business ID (e.g., EMP-000001)")
    public ResponseEntity<EmployeeDto> getEmployeeByBusinessId(@PathVariable String businessId) {
        return ResponseEntity.ok(employeeService.getEmployeeByBusinessId(businessId));
    }

    @GetMapping("/{id}/direct-reports")
    @Operation(summary = "Get direct reports", description = "Returns the active direct reports assigned to this manager")
    public ResponseEntity<List<EmployeeDto>> getDirectReports(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getDirectReports(id));
    }
}
