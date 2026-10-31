package com.performiq.auth.service;

import com.performiq.auth.dto.LoginRequestDto;
import com.performiq.auth.dto.LoginResponseDto;
import com.performiq.auth.dto.UserProfileDto;
import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.auth.security.JwtTokenProvider;
import com.performiq.common.exception.BadRequestException;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        String lookup = request.getUsername();
        AppUser user = appUserRepository.findByUsername(lookup)
                .or(() -> !lookup.contains("@") ? appUserRepository.findByUsername(lookup + "@performiq.com") : java.util.Optional.empty())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid username or password");
        }

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BadRequestException("Account is disabled. Please contact administrator.");
        }

        String token = jwtTokenProvider.generateToken(user);

        return LoginResponseDto.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .employee(toEmployeeSummary(user.getEmployee()))
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(String username) {
        String lookup = username;
        AppUser user = appUserRepository.findByUsername(lookup)
                .or(() -> !lookup.contains("@") ? appUserRepository.findByUsername(lookup + "@performiq.com") : java.util.Optional.empty())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return UserProfileDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .employee(toEmployeeSummary(user.getEmployee()))
                .build();
    }

    private LoginResponseDto.EmployeeSummaryDto toEmployeeSummary(Employee employee) {
        if (employee == null) return null;
        return LoginResponseDto.EmployeeSummaryDto.builder()
                .id(employee.getId())
                .businessId(employee.getBusinessId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .build();
    }
}
