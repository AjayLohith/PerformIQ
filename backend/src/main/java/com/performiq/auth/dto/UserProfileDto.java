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
public class UserProfileDto {

    private UUID userId;
    private String username;
    private UserRole role;
    private Boolean enabled;
    private LoginResponseDto.EmployeeSummaryDto employee;
}
