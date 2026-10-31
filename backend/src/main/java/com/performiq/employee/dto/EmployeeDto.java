package com.performiq.employee.dto;

import com.performiq.employee.entity.EmployeeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private UUID id;
    private String businessId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String designation;
    private LocalDate joiningDate;
    private EmployeeStatus status;
    private ManagerSummaryDto currentManager;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerSummaryDto {
        private UUID managerId;
        private String managerBusinessId;
        private String name;
        private String email;
        private String designation;
    }
}
