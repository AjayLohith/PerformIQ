package com.performiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.performiq.auth.dto.LoginRequestDto;
import com.performiq.auth.dto.LoginResponseDto;
import com.performiq.auth.entity.AppUser;
import com.performiq.auth.entity.UserRole;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.entity.EmployeeStatus;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RestApiSecurityAndEndpointsTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ReviewCycleRepository reviewCycleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AppUser adminUser;
    private AppUser employeeUser;
    private Employee employee;
    private ReviewCycle activeCycle;

    @BeforeEach
    void setupUsers() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        employee = employeeRepository.save(Employee.builder()
                .businessId("EMP-API-001")
                .firstName("Elena")
                .lastName("Rostova")
                .email("elena.api@example.com")
                .department("Engineering")
                .designation("Staff Engineer")
                .joiningDate(LocalDate.of(2022, 3, 1))
                .status(EmployeeStatus.ACTIVE)
                .build());

        Employee adminEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-API-ADMIN")
                .firstName("System")
                .lastName("Admin")
                .email("admin.api@example.com")
                .department("HR Operations")
                .designation("Platform Administrator")
                .joiningDate(LocalDate.of(2020, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .build());

        adminUser = appUserRepository.save(AppUser.builder()
                .username("admin.test")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .employee(adminEmp)
                .build());

        employeeUser = appUserRepository.save(AppUser.builder()
                .username("elena.test")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .employee(employee)
                .build());

        activeCycle = reviewCycleRepository.save(ReviewCycle.builder()
                .businessId("CYCLE-API-2026")
                .name("2026 Active Cycle")
                .description("Active cycle for API tests")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ReviewCycleStatus.ACTIVE)
                .build());
    }

    private String obtainToken(String username, String password) throws Exception {
        LoginRequestDto req = new LoginRequestDto(username, password);
        String resJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponseDto loginResponse = objectMapper.readValue(resJson, LoginResponseDto.class);
        return loginResponse.getToken();
    }

    @Test
    @DisplayName("POST /auth/login - Success with valid credentials and failure with bad password")
    void testLoginFlow() throws Exception {
        // Valid login
        LoginRequestDto validReq = new LoginRequestDto("admin.test", "password123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin.test"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Invalid login
        LoginRequestDto invalidReq = new LoginRequestDto("admin.test", "wrongpassword");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /auth/me - Authenticated user can retrieve their profile")
    void testGetAuthenticatedUserProfile() throws Exception {
        String token = obtainToken("elena.test", "password123");

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("elena.test"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.employee.businessId").value("EMP-API-001"));
    }

    @Test
    @DisplayName("RBAC: /admin/audit-logs is protected from unauthenticated and non-admin users")
    void testAdminAuditLogsRbac() throws Exception {
        // 1. Unauthenticated -> 401 or 403
        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isForbidden());

        // 2. Authenticated as EMPLOYEE -> 403 Forbidden
        String employeeToken = obtainToken("elena.test", "password123");
        mockMvc.perform(get("/admin/audit-logs")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());

        // 3. Authenticated as ADMIN -> 200 OK
        String adminToken = obtainToken("admin.test", "password123");
        mockMvc.perform(get("/admin/audit-logs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /employees and GET /review-cycles/active - Publicly accessible for authenticated users")
    void testCoreReadEndpoints() throws Exception {
        String token = obtainToken("elena.test", "password123");

        // Employees list
        mockMvc.perform(get("/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // Active cycle
        mockMvc.perform(get("/review-cycles/active")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
