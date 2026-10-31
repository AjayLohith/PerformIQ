package com.performiq;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.entity.UserRole;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.checkin.entity.CheckIn;
import com.performiq.checkin.repository.CheckInRepository;
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
import com.performiq.evidence.entity.Evidence;
import com.performiq.evidence.entity.EvidenceType;
import com.performiq.evidence.repository.EvidenceRepository;
import com.performiq.feedback.entity.Feedback;
import com.performiq.feedback.entity.FeedbackCompetency;
import com.performiq.feedback.entity.FeedbackStatus;
import com.performiq.feedback.entity.FeedbackType;
import com.performiq.feedback.repository.FeedbackCompetencyRepository;
import com.performiq.feedback.repository.FeedbackRepository;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalHistory;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalHistoryRepository;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.keyresult.entity.KeyResult;
import com.performiq.keyresult.entity.KeyResultHistory;
import com.performiq.keyresult.entity.MetricType;
import com.performiq.keyresult.repository.KeyResultHistoryRepository;
import com.performiq.keyresult.repository.KeyResultRepository;
import com.performiq.manager.entity.Manager;
import com.performiq.manager.entity.ManagerAssignmentHistory;
import com.performiq.manager.entity.ManagerStatus;
import com.performiq.manager.repository.ManagerAssignmentHistoryRepository;
import com.performiq.manager.repository.ManagerRepository;
import com.performiq.review.entity.Review;
import com.performiq.review.entity.ReviewGoal;
import com.performiq.review.entity.ReviewStatus;
import com.performiq.review.repository.ReviewGoalRepository;
import com.performiq.review.repository.ReviewRepository;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
import com.performiq.validation.entity.AnomalyCase;
import com.performiq.validation.entity.AnomalySeverity;
import com.performiq.validation.entity.DetectionStatus;
import com.performiq.validation.repository.AnomalyCaseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityRelationshipTest {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ManagerRepository managerRepository;
    @Autowired private ManagerAssignmentHistoryRepository managerAssignmentHistoryRepository;
    @Autowired private ReviewCycleRepository reviewCycleRepository;
    @Autowired private GoalRepository goalRepository;
    @Autowired private GoalHistoryRepository goalHistoryRepository;
    @Autowired private KeyResultRepository keyResultRepository;
    @Autowired private KeyResultHistoryRepository keyResultHistoryRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private EvidenceRepository evidenceRepository;
    @Autowired private CompetencyRepository competencyRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private FeedbackCompetencyRepository feedbackCompetencyRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewGoalRepository reviewGoalRepository;
    @Autowired private DevelopmentPlanRepository developmentPlanRepository;
    @Autowired private ApprovalHistoryRepository approvalHistoryRepository;
    @Autowired private AnomalyCaseRepository anomalyCaseRepository;
    @Autowired private AppUserRepository appUserRepository;

    @Test
    @DisplayName("Verify end-to-end entity persistence, associations, and queries across all 21 entities")
    void testFullEntityGraph() {
        // 1. Create Employee (Manager)
        Employee mgrEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-000001")
                .firstName("Sarah")
                .lastName("Connor")
                .email("sconnor@performiq.com")
                .department("Engineering")
                .designation("Engineering Lead")
                .joiningDate(LocalDate.of(2022, 1, 15))
                .status(EmployeeStatus.ACTIVE)
                .build());

        // 2. Create Employee (Direct Report)
        Employee devEmp = employeeRepository.save(Employee.builder()
                .businessId("EMP-000002")
                .firstName("John")
                .lastName("Doe")
                .email("jdoe@performiq.com")
                .department("Engineering")
                .designation("Software Engineer")
                .joiningDate(LocalDate.of(2023, 3, 1))
                .status(EmployeeStatus.ACTIVE)
                .build());

        // 3. Create Manager profile for Sarah
        Manager manager = managerRepository.save(Manager.builder()
                .businessId("MGR-000001")
                .employee(mgrEmp)
                .effectiveFrom(LocalDate.of(2022, 1, 15))
                .status(ManagerStatus.ACTIVE)
                .build());

        // 4. Assign John to Sarah
        ManagerAssignmentHistory assignment = managerAssignmentHistoryRepository.save(ManagerAssignmentHistory.builder()
                .employee(devEmp)
                .manager(manager)
                .effectiveFrom(LocalDate.of(2023, 3, 1))
                .reason("Direct team placement")
                .build());

        assertThat(managerAssignmentHistoryRepository.findCurrentDirectReportsByManagerId(manager.getId())).hasSize(1);

        // 5. Create Review Cycle
        ReviewCycle cycle = reviewCycleRepository.save(ReviewCycle.builder()
                .businessId("CYCLE-2026-H1")
                .name("2026 H1 Cycle")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ReviewCycleStatus.ACTIVE)
                .build());

        // 6. Create Goal
        Goal goal = goalRepository.save(Goal.builder()
                .businessId("GOAL-EMP-000002-001")
                .employee(devEmp)
                .reviewCycle(cycle)
                .title("Deliver Microservice Decoupling")
                .weight(new BigDecimal("2.00"))
                .progress(BigDecimal.ZERO)
                .status(GoalStatus.ACTIVE)
                .build());

        // 7. Track Goal History
        goalHistoryRepository.save(GoalHistory.builder()
                .goal(goal)
                .fieldChanged("status")
                .oldValue("DRAFT")
                .newValue("ACTIVE")
                .changedBy(mgrEmp)
                .build());

        // 8. Create Key Result
        KeyResult kr = keyResultRepository.save(KeyResult.builder()
                .businessId("KR-GOAL-EMP-000002-001-01")
                .goal(goal)
                .title("Reduce API Latency to < 50ms")
                .metricType(MetricType.DECREASE)
                .startValue(new BigDecimal("120.00"))
                .currentValue(new BigDecimal("80.00"))
                .targetValue(new BigDecimal("50.00"))
                .weight(new BigDecimal("1.00"))
                .unit("ms")
                .progress(new BigDecimal("57.14"))
                .build());

        // 9. Key Result History
        keyResultHistoryRepository.save(KeyResultHistory.builder()
                .keyResult(kr)
                .oldValue(new BigDecimal("120.00"))
                .newValue(new BigDecimal("80.00"))
                .changedBy(devEmp)
                .build());

        // 10. Check-in & Evidence
        CheckIn checkIn = checkInRepository.save(CheckIn.builder()
                .businessId("CHECKIN-000001")
                .goal(goal)
                .employee(devEmp)
                .notes("Deployed connection pool optimizations")
                .progressSnapshot(new BigDecimal("57.14"))
                .checkInDate(LocalDate.now())
                .build());

        Evidence evidence = evidenceRepository.save(Evidence.builder()
                .businessId("EVID-000001")
                .checkIn(checkIn)
                .title("Grafana Dashboard Snapshot")
                .url("https://metrics.internal/dashboards/latency")
                .evidenceType(EvidenceType.METRIC)
                .build());

        assertThat(evidenceRepository.findByCheckInId(checkIn.getId())).hasSize(1);

        // 11. Competency
        Competency comp = competencyRepository.save(Competency.builder()
                .businessId("COMP-000001")
                .name("Technical Excellence")
                .category("TECHNICAL")
                .build());

        // 12. Feedback & Competency rating
        Feedback feedback = feedbackRepository.save(Feedback.builder()
                .businessId("FEEDBACK-000001")
                .employee(devEmp)
                .reviewer(mgrEmp)
                .reviewCycle(cycle)
                .feedbackType(FeedbackType.MANAGER)
                .comments("Outstanding execution on backend architecture")
                .overallRating(5)
                .status(FeedbackStatus.SUBMITTED)
                .build());

        feedbackCompetencyRepository.save(FeedbackCompetency.builder()
                .feedback(feedback)
                .competency(comp)
                .rating(5)
                .comments("Demonstrated master-level system design")
                .build());

        // 13. Review & ReviewGoal
        Review review = reviewRepository.save(Review.builder()
                .businessId("REV-2026-000001")
                .employee(devEmp)
                .reviewer(mgrEmp)
                .reviewCycle(cycle)
                .status(ReviewStatus.COMPLETED)
                .overallScore(new BigDecimal("92.50"))
                .summary("Exceeded expectations across all Q1/Q2 goals.")
                .build());

        reviewGoalRepository.save(ReviewGoal.builder()
                .review(review)
                .goal(goal)
                .goalScore(new BigDecimal("90.00"))
                .comments("Goal delivered ahead of schedule")
                .build());

        // 14. Development Plan & Approval History
        DevelopmentPlan devPlan = developmentPlanRepository.save(DevelopmentPlan.builder()
                .businessId("DEVP-000001")
                .employee(devEmp)
                .review(review)
                .title("Staff Engineer Readiness")
                .skillsToImprove("System architecture presentation")
                .actionItems("Lead technical design review for project delta")
                .targetDate(LocalDate.of(2026, 12, 31))
                .status(DevelopmentPlanStatus.APPROVED)
                .build());

        approvalHistoryRepository.save(ApprovalHistory.builder()
                .developmentPlan(devPlan)
                .approver(mgrEmp)
                .action(ApprovalAction.APPROVED)
                .comments("Approved. Good growth roadmap.")
                .build());

        // 15. Anomaly Case
        anomalyCaseRepository.save(AnomalyCase.builder()
                .businessId("ANOM-000001")
                .issueType("ORPHAN_GOAL_DETECTION")
                .affectedEntity("GOAL")
                .affectedBusinessId(goal.getBusinessId())
                .severity(AnomalySeverity.LOW)
                .detectionStatus(DetectionStatus.DETECTED)
                .build());

        // 16. AppUser
        AppUser appUser = appUserRepository.save(AppUser.builder()
                .employee(devEmp)
                .username("jdoe@performiq.com")
                .passwordHash("$2b$10$pwQ14mbhxYbXMMhT3AN7yuqvQmLkI6AIfsFcvp44.KX7fvucYeQQO")
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .build());

        // Assertions
        Optional<Employee> foundEmp = employeeRepository.findByBusinessId("EMP-000002");
        assertThat(foundEmp).isPresent();
        assertThat(foundEmp.get().getDepartment()).isEqualTo("Engineering");

        Optional<AppUser> foundUser = appUserRepository.findByUsername("jdoe@performiq.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.EMPLOYEE);

        List<Goal> goals = goalRepository.findByEmployeeId(devEmp.getId());
        assertThat(goals).hasSize(1);
        assertThat(goals.get(0).getTitle()).isEqualTo("Deliver Microservice Decoupling");

        Optional<DevelopmentPlan> plan = developmentPlanRepository.findByBusinessId("DEVP-000001");
        assertThat(plan).isPresent();
        assertThat(plan.get().getStatus()).isEqualTo(DevelopmentPlanStatus.APPROVED);
    }
}
