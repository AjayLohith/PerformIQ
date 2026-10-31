package com.performiq.auth.dto;

import com.performiq.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UUID userId;
    private String username;
    private UserRole role;
    private EmployeeSummaryDto employee;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmployeeSummaryDto {
        private UUID id;
        private String businessId;
        private String firstName;
        private String lastName;
        private String email;
        private String department;
        private String designation;
    }
}
