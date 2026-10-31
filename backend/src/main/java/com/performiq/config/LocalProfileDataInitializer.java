package com.performiq.config;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.entity.UserRole;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.competency.entity.Competency;
import com.performiq.competency.repository.CompetencyRepository;
import com.performiq.development.entity.ApprovalAction;
import com.performiq.development.entity.ApprovalHistory;
import com.performiq.development.entity.DevelopmentPlan;
import com.performiq.development.entity.DevelopmentPlanStatus;
import com.performiq.development.repository.ApprovalHistoryRepository;
import com.performiq.development.repository.DevelopmentPlanRepository;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.entity.EmployeeStatus;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.keyresult.entity.KeyResult;
import com.performiq.keyresult.entity.MetricType;
import com.performiq.keyresult.repository.KeyResultRepository;
import com.performiq.manager.entity.Manager;
import com.performiq.manager.entity.ManagerAssignmentHistory;
import com.performiq.manager.entity.ManagerStatus;
import com.performiq.manager.repository.ManagerAssignmentHistoryRepository;
import com.performiq.manager.repository.ManagerRepository;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class LocalProfileDataInitializer implements ApplicationRunner {

    private final CompetencyRepository competencyRepository;
    private final ReviewCycleRepository reviewCycleRepository;
    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final ManagerAssignmentHistoryRepository managerAssignmentHistoryRepository;
    private final AppUserRepository appUserRepository;
    private final GoalRepository goalRepository;
    private final KeyResultRepository keyResultRepository;
    private final DevelopmentPlanRepository developmentPlanRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (competencyRepository.count() > 0) {
            log.info("Local database already seeded. Skipping initialization.");
            return;
        }

        log.info("Seeding in-memory H2 database with PerformIQ enterprise demo fixtures...");

        // 1. Competencies
        List<Competency> competencies = List.of(
                Competency.builder().businessId("COMP-001").name("Leadership & Influence").description("Guides and inspires teams toward strategic business impact.").category("LEADERSHIP").build(),
                Competency.builder().businessId("COMP-002").name("Technical Excellence").description("Demonstrates deep domain mastery, architectural quality, and craft.").category("TECHNICAL").build(),
                Competency.builder().businessId("COMP-003").name("Communication & Clarity").description("Communicates transparently and articulates complex concepts clearly.").category("INTERPERSONAL").build(),
                Competency.builder().businessId("COMP-004").name("Problem Solving & Critical Thinking").description("Solves intricate challenges with structured data-driven analysis.").category("COGNITIVE").build(),
                Competency.builder().businessId("COMP-005").name("Collaboration & Teamwork").description("Partners cross-functionally and fosters psychological safety.").category("INTERPERSONAL").build(),
                Competency.builder().businessId("COMP-006").name("Delivery & Execution").description("Consistently delivers high quality outcomes on schedule.").category("EXECUTION").build(),
                Competency.builder().businessId("COMP-007").name("Innovation & Creativity").description("Champions novel solutions that advance organizational capability.").category("COGNITIVE").build(),
                Competency.builder().businessId("COMP-008").name("Adaptability & Resilience").description("Thrives in ambiguity and pivots gracefully as priorities evolve.").category("CORE").build()
        );
        competencyRepository.saveAll(competencies);

        // 2. Review Cycle
        ReviewCycle cycle = reviewCycleRepository.save(ReviewCycle.builder()
                .businessId("CYCLE-2026-H1")
                .name("2026 First Half Performance Cycle")
                .description("Bi-annual enterprise performance and growth review cycle")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ReviewCycleStatus.ACTIVE)
                .build());

        // 3. Employees
        Employee adminEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-000001")
                .firstName("Platform")
                .lastName("Administrator")
                .email("admin@performiq.com")
                .department("People Operations")
                .designation("Operations & HR Admin")
                .joiningDate(LocalDate.of(2023, 1, 1))
                .status(EmployeeStatus.ACTIVE)
                .build());

        Employee sarahEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-000002")
                .firstName("Sarah")
                .lastName("Chen")
                .email("sarah.chen@performiq.com")
                .department("Engineering")
                .designation("VP of Engineering")
                .joiningDate(LocalDate.of(2022, 3, 15))
                .status(EmployeeStatus.ACTIVE)
                .build());

        Employee marcusEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-000003")
                .firstName("Marcus")
                .lastName("Vance")
                .email("marcus.vance@performiq.com")
                .department("Engineering")
                .designation("Director of Engineering")
                .joiningDate(LocalDate.of(2022, 8, 1))
                .status(EmployeeStatus.ACTIVE)
                .build());

        Employee elenaEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-000004")
                .firstName("Elena")
                .lastName("Rostova")
                .email("elena.rostova@performiq.com")
                .department("Engineering")
                .designation("Staff Software Engineer")
                .joiningDate(LocalDate.of(2023, 2, 10))
                .status(EmployeeStatus.ACTIVE)
                .build());

        Employee alexEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-000005")
                .firstName("Alex")
                .lastName("Rivera")
                .email("alex.rivera@performiq.com")
                .department("Engineering")
                .designation("Senior Staff Engineer")
                .joiningDate(LocalDate.of(2023, 5, 20))
                .status(EmployeeStatus.ACTIVE)
                .build());

        // 4. Managers & Hierarchies
        Manager sarahMgr = managerRepository.save(Manager.builder()
                .businessId("MGR-000001")
                .employee(sarahEmp)
                .effectiveFrom(LocalDate.of(2022, 3, 15))
                .status(ManagerStatus.ACTIVE)
                .build());

        Manager marcusMgr = managerRepository.save(Manager.builder()
                .businessId("MGR-000002")
                .employee(marcusEmp)
                .effectiveFrom(LocalDate.of(2022, 8, 1))
                .status(ManagerStatus.ACTIVE)
                .build());

        managerAssignmentHistoryRepository.save(ManagerAssignmentHistory.builder()
                .employee(marcusEmp)
                .manager(sarahMgr)
                .effectiveFrom(LocalDate.of(2022, 8, 1))
                .reason("Engineering Org Realignment")
                .build());

        managerAssignmentHistoryRepository.save(ManagerAssignmentHistory.builder()
                .employee(elenaEmp)
                .manager(marcusMgr)
                .effectiveFrom(LocalDate.of(2023, 2, 10))
                .reason("Core Services Placement")
                .build());

        managerAssignmentHistoryRepository.save(ManagerAssignmentHistory.builder()
                .employee(alexEmp)
                .manager(marcusMgr)
                .effectiveFrom(LocalDate.of(2023, 5, 20))
                .reason("Infrastructure Placement")
                .build());

        // 5. App Users
        String encodedPass = passwordEncoder.encode("password123");

        appUserRepository.save(AppUser.builder()
                .username("admin")
                .passwordHash(encodedPass)
                .role(UserRole.ADMIN)
                .employee(adminEmp)
                .enabled(true)
                .build());

        appUserRepository.save(AppUser.builder()
                .username("sarah.chen")
                .passwordHash(encodedPass)
                .role(UserRole.MANAGER)
                .employee(sarahEmp)
                .enabled(true)
                .build());

        appUserRepository.save(AppUser.builder()
                .username("marcus.vance")
                .passwordHash(encodedPass)
                .role(UserRole.MANAGER)
                .employee(marcusEmp)
                .enabled(true)
                .build());

        appUserRepository.save(AppUser.builder()
                .username("elena.rostova")
                .passwordHash(encodedPass)
                .role(UserRole.EMPLOYEE)
                .employee(elenaEmp)
                .enabled(true)
                .build());

        appUserRepository.save(AppUser.builder()
                .username("alex.rivera")
                .passwordHash(encodedPass)
                .role(UserRole.EMPLOYEE)
                .employee(alexEmp)
                .enabled(true)
                .build());

        // 6. Elena's Goals & Key Results
        Goal goal1 = goalRepository.save(Goal.builder()
                .businessId("GOAL-000001")
                .employee(elenaEmp)
                .reviewCycle(cycle)
                .title("Architect High-Throughput Event Processing Engine")
                .description("Design and ship distributed Kafka streams pipeline achieving 25,000 evt/sec.")
                .category("TECHNICAL")
                .status(GoalStatus.ACTIVE)
                .weight(new BigDecimal("40.00"))
                .progress(new BigDecimal("70.00"))
                .build());

        keyResultRepository.save(KeyResult.builder()
                .businessId("KR-000001")
                .goal(goal1)
                .title("Ingestion stream throughput peak (evt/sec)")
                .metricType(MetricType.INCREASE)
                .startValue(new BigDecimal("5000.00"))
                .targetValue(new BigDecimal("25000.00"))
                .currentValue(new BigDecimal("19000.00"))
                .weight(new BigDecimal("50.00"))
                .unit("eps")
                .progress(new BigDecimal("70.00"))
                .build());

        keyResultRepository.save(KeyResult.builder()
                .businessId("KR-000002")
                .goal(goal1)
                .title("End-to-end event propagation latency (ms)")
                .metricType(MetricType.DECREASE)
                .startValue(new BigDecimal("350.00"))
                .targetValue(new BigDecimal("45.00"))
                .currentValue(new BigDecimal("120.00"))
                .weight(new BigDecimal("50.00"))
                .unit("ms")
                .progress(new BigDecimal("75.41"))
                .build());

        Goal goal2 = goalRepository.save(Goal.builder()
                .businessId("GOAL-000002")
                .employee(elenaEmp)
                .reviewCycle(cycle)
                .title("Elevate Core Platform Security & Compliance")
                .description("Remediate architectural vulnerability surface and lead SOC2 audit readiness.")
                .category("OPERATIONAL")
                .status(GoalStatus.ACTIVE)
                .weight(new BigDecimal("35.00"))
                .progress(new BigDecimal("85.00"))
                .build());

        keyResultRepository.save(KeyResult.builder()
                .businessId("KR-000003")
                .goal(goal2)
                .title("High-severity static code analysis findings")
                .metricType(MetricType.DECREASE)
                .startValue(new BigDecimal("24.00"))
                .targetValue(new BigDecimal("0.00"))
                .currentValue(new BigDecimal("3.00"))
                .weight(new BigDecimal("100.00"))
                .unit("issues")
                .progress(new BigDecimal("87.50"))
                .build());

        Goal goal3 = goalRepository.save(Goal.builder()
                .businessId("GOAL-000003")
                .employee(elenaEmp)
                .reviewCycle(cycle)
                .title("Engineering Mentorship & Technical Design Reviews")
                .description("Conduct 10 architecture design review sessions and mentor senior engineers.")
                .category("LEADERSHIP")
                .status(GoalStatus.ACTIVE)
                .weight(new BigDecimal("25.00"))
                .progress(new BigDecimal("60.00"))
                .build());

        keyResultRepository.save(KeyResult.builder()
                .businessId("KR-000004")
                .goal(goal3)
                .title("RFC Design reviews facilitated")
                .metricType(MetricType.INCREASE)
                .startValue(new BigDecimal("0.00"))
                .targetValue(new BigDecimal("10.00"))
                .currentValue(new BigDecimal("6.00"))
                .weight(new BigDecimal("100.00"))
                .unit("RFCs")
                .progress(new BigDecimal("60.00"))
                .build());

        // 7. Development Plans
        DevelopmentPlan plan1 = developmentPlanRepository.save(DevelopmentPlan.builder()
                .businessId("PLAN-000001")
                .employee(elenaEmp)
                .title("Principal Engineering Track — Distributed Systems Mastery")
                .description("Deepen expertise in multi-region data replication, consensus algorithms, and chaos engineering.")
                .objectives("Design active-active cross-region failover and publish internal architecture playbooks.")
                .skillsToImprove("Raft Consensus, Chaos Mesh, Distributed Tracing at Scale")
                .actionItems("1. Complete Raft protocol benchmark\n2. Run game-day simulation\n3. Deliver architecture review")
                .targetDate(LocalDate.of(2026, 8, 30))
                .status(DevelopmentPlanStatus.SUBMITTED)
                .build());

        approvalHistoryRepository.save(ApprovalHistory.builder()
                .developmentPlan(plan1)
                .approver(elenaEmp)
                .action(ApprovalAction.SUBMITTED)
                .comments("Submitted for Marcus Vance approval")
                .build());

        log.info("PerformIQ demo seed fixtures loaded successfully for local profile.");
    }
}
