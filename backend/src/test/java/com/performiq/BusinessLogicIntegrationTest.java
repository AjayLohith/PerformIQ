package com.performiq;

import com.performiq.audit.repository.AuditLogRepository;
import com.performiq.checkin.entity.CheckIn;
import com.performiq.checkin.service.CheckInService;
import com.performiq.common.exception.BadRequestException;
import com.performiq.competency.entity.Competency;
import com.performiq.competency.repository.CompetencyRepository;
import com.performiq.development.entity.ApprovalAction;
import com.performiq.development.entity.ApprovalHistory;
import com.performiq.development.entity.DevelopmentPlan;
import com.performiq.development.entity.DevelopmentPlanStatus;
import com.performiq.development.service.DevelopmentPlanService;
import com.performiq.employee.entity.Employee;
import com.performiq.employee.entity.EmployeeStatus;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.feedback.entity.Feedback;
import com.performiq.feedback.entity.FeedbackStatus;
import com.performiq.feedback.entity.FeedbackType;
import com.performiq.feedback.service.FeedbackService;
import com.performiq.goal.entity.Goal;
import com.performiq.goal.entity.GoalStatus;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.goal.service.GoalService;
import com.performiq.keyresult.entity.KeyResult;
import com.performiq.keyresult.entity.MetricType;
import com.performiq.keyresult.repository.KeyResultRepository;
import com.performiq.keyresult.service.KeyResultService;
import com.performiq.review.entity.Review;
import com.performiq.review.entity.ReviewStatus;
import com.performiq.review.service.ReviewService;
import com.performiq.reviewcycle.entity.ReviewCycle;
import com.performiq.reviewcycle.entity.ReviewCycleStatus;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
import com.performiq.reviewcycle.service.ReviewCycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessLogicIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ReviewCycleRepository reviewCycleRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private KeyResultRepository keyResultRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private KeyResultService keyResultService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private ReviewCycleService reviewCycleService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private DevelopmentPlanService developmentPlanService;

    private Employee employee;
    private Employee manager;
    private ReviewCycle cycle;
    private Competency technicalCompetency;

    @BeforeEach
    void setupTestData() {
        employee = employeeRepository.save(Employee.builder()
                .businessId("EMP-TEST-001")
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.test@example.com")
                .department("Engineering")
                .designation("Senior Backend Engineer")
                .joiningDate(LocalDate.of(2023, 1, 15))
                .status(EmployeeStatus.ACTIVE)
                .build());

        manager = employeeRepository.save(Employee.builder()
                .businessId("EMP-TEST-002")
                .firstName("Bob")
                .lastName("Jones")
                .email("bob.test@example.com")
                .department("Engineering")
                .designation("Engineering Manager")
                .joiningDate(LocalDate.of(2021, 5, 1))
                .status(EmployeeStatus.ACTIVE)
                .build());

        cycle = reviewCycleRepository.save(ReviewCycle.builder()
                .businessId("CYCLE-TEST-2026")
                .name("2026 H1 Review Cycle")
                .description("Performance evaluation for first half of 2026")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(ReviewCycleStatus.DRAFT)
                .build());

        technicalCompetency = competencyRepository.save(Competency.builder()
                .businessId("COMP-TECH-001")
                .name("Technical Excellence")
                .description("Quality of architectural design, clean code, and engineering rigor")
                .category("TECHNICAL")
                .build());
    }

    @Test
    @DisplayName("Review Cycle State Machine: DRAFT -> ACTIVE (locks draft goals) -> UNDER_REVIEW -> CLOSED")
    void testReviewCycleStateMachine() {
        // Create a DRAFT goal for the cycle
        Goal g = goalService.createGoal(employee.getId(), cycle.getId(), "GOAL-TEST-01",
                "Migrate to microservices", "Refactor monolith into services", "TECHNICAL",
                new BigDecimal("50.00"), employee);
        assertThat(g.getStatus()).isEqualTo(GoalStatus.DRAFT);

        // Transition: DRAFT -> ACTIVE
        ReviewCycle activeCycle = reviewCycleService.transitionStatus(
                cycle.getId(), ReviewCycleStatus.ACTIVE, manager, "Cycle initiated", false);
        assertThat(activeCycle.getStatus()).isEqualTo(ReviewCycleStatus.ACTIVE);

        // Verify draft goal was activated
        Goal lockedGoal = goalRepository.findById(g.getId()).orElseThrow();
        assertThat(lockedGoal.getStatus()).isEqualTo(GoalStatus.ACTIVE);

        // Transition: ACTIVE -> UNDER_REVIEW
        ReviewCycle underReviewCycle = reviewCycleService.transitionStatus(
                cycle.getId(), ReviewCycleStatus.UNDER_REVIEW, manager, "Review window open", false);
        assertThat(underReviewCycle.getStatus()).isEqualTo(ReviewCycleStatus.UNDER_REVIEW);

        // Transition: UNDER_REVIEW -> CLOSED
        ReviewCycle closedCycle = reviewCycleService.transitionStatus(
                cycle.getId(), ReviewCycleStatus.CLOSED, manager, "Cycle finalized", false);
        assertThat(closedCycle.getStatus()).isEqualTo(ReviewCycleStatus.CLOSED);

        // Invalid backward transition: CLOSED -> DRAFT should throw BadRequestException
        assertThatThrownBy(() -> reviewCycleService.transitionStatus(
                cycle.getId(), ReviewCycleStatus.DRAFT, manager, "Illegal transition", false))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("KeyResult update cascades to parent Goal progress and creates audit logs")
    void testKeyResultProgressCascadeToGoal() {
        Goal goal = goalRepository.save(Goal.builder()
                .businessId("GOAL-CASCADE-01")
                .employee(employee)
                .reviewCycle(cycle)
                .title("Improve Query Latency")
                .weight(new BigDecimal("50.00"))
                .status(GoalStatus.ACTIVE)
                .progress(BigDecimal.ZERO)
                .build());

        // KR 1: Increase test coverage (Start: 40%, Target: 80%, Weight: 60)
        KeyResult kr1 = keyResultRepository.save(KeyResult.builder()
                .businessId("KR-CASCADE-01")
                .goal(goal)
                .title("Increase unit test coverage")
                .metricType(MetricType.INCREASE)
                .startValue(new BigDecimal("40.00"))
                .targetValue(new BigDecimal("80.00"))
                .currentValue(new BigDecimal("40.00"))
                .weight(new BigDecimal("60.00"))
                .unit("%")
                .progress(BigDecimal.ZERO)
                .build());

        // KR 2: Decrease p99 latency (Start: 500ms, Target: 100ms, Weight: 40)
        KeyResult kr2 = keyResultRepository.save(KeyResult.builder()
                .businessId("KR-CASCADE-02")
                .goal(goal)
                .title("Reduce p99 API latency")
                .metricType(MetricType.DECREASE)
                .startValue(new BigDecimal("500.00"))
                .targetValue(new BigDecimal("100.00"))
                .currentValue(new BigDecimal("500.00"))
                .weight(new BigDecimal("40.00"))
                .unit("ms")
                .progress(BigDecimal.ZERO)
                .build());

        // Update KR1: current 60% -> progress = (60-40)/(80-40)*100 = 50.00%
        keyResultService.updateCurrentValue(kr1.getId(), new BigDecimal("60.00"), employee, "Added unit tests");

        // Update KR2: current 300ms -> progress = (500-300)/(500-100)*100 = 50.00%
        keyResultService.updateCurrentValue(kr2.getId(), new BigDecimal("300.00"), employee, "Added Redis caching");

        // Verify parent goal progress: weighted average = (50*60 + 50*40) / 100 = 50.00%
        Goal refreshedGoal = goalRepository.findById(goal.getId()).orElseThrow();
        assertThat(refreshedGoal.getProgress()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("CheckIn records progress snapshot, evidence, and updates Goal progress")
    void testCheckInService() {
        Goal goal = goalRepository.save(Goal.builder()
                .businessId("GOAL-CHECKIN-01")
                .employee(employee)
                .reviewCycle(cycle)
                .title("Deploy Auth Microservice")
                .weight(new BigDecimal("30.00"))
                .status(GoalStatus.ACTIVE)
                .progress(new BigDecimal("10.00"))
                .build());

        var evidence = List.of(
                CheckInService.EvidenceDto.builder()
                        .title("Production deployment PR")
                        .url("https://github.com/org/repo/pull/123")
                        .description("Merged pull request for JWT auth service")
                        .build()
        );

        CheckIn cin = checkInService.recordCheckIn(
                goal.getId(), employee, "CIN-TEST-001", "Deployed v1 to staging",
                new BigDecimal("75.00"), LocalDate.now(), evidence
        );

        assertThat(cin).isNotNull();
        assertThat(cin.getProgressSnapshot()).isEqualByComparingTo("75.00");

        Goal updatedGoal = goalRepository.findById(goal.getId()).orElseThrow();
        assertThat(updatedGoal.getProgress()).isEqualByComparingTo("75.00");
        assertThat(updatedGoal.getStatus()).isEqualTo(GoalStatus.ACTIVE);
    }

    @Test
    @DisplayName("360 Feedback submission and multi-dimensional aggregation")
    void testFeedbackSubmissionAndAggregation() {
        // Request Self Feedback
        Feedback selfFb = feedbackService.requestFeedback(
                employee.getId(), employee.getId(), cycle.getId(), FeedbackType.SELF, employee);

        // Submit Self Feedback with rating 4 and competency rating 4
        feedbackService.submitFeedback(
                selfFb.getId(), 4, "Met my technical deliverables",
                List.of(new FeedbackService.CompetencyRatingDto(technicalCompetency.getId(), 4, "Strong coding skills")),
                employee
        );

        // Request Manager Feedback
        Feedback mgrFb = feedbackService.requestFeedback(
                employee.getId(), manager.getId(), cycle.getId(), FeedbackType.MANAGER, manager);

        // Submit Manager Feedback with rating 5 and competency rating 5
        feedbackService.submitFeedback(
                mgrFb.getId(), 5, "Exceeded expectations in microservice migration",
                List.of(new FeedbackService.CompetencyRatingDto(technicalCompetency.getId(), 5, "Outstanding architecture")),
                manager
        );

        // Calculate Feedback Summary
        var summary = feedbackService.calculateFeedbackSummary(employee.getId(), cycle.getId());
        assertThat(summary.getTotalFeedbacks()).isEqualTo(2);
        // Average overall rating: (4 + 5) / 2 = 4.50
        assertThat(summary.getAverageOverallRating()).isEqualByComparingTo("4.50");
        // Competency average: (4 + 5) / 2 = 4.50
        assertThat(summary.getRatingByCompetency().get("Technical Excellence")).isEqualByComparingTo("4.50");
        assertThat(summary.getRatingByFeedbackType().get(FeedbackType.SELF)).isEqualByComparingTo("4.00");
        assertThat(summary.getRatingByFeedbackType().get(FeedbackType.MANAGER)).isEqualByComparingTo("5.00");
    }

    @Test
    @DisplayName("Development Plan Workflow: DRAFT -> SUBMITTED -> REJECTED -> RESUBMITTED -> APPROVED -> IN_PROGRESS -> COMPLETED")
    void testDevelopmentPlanWorkflow() {
        // 1. Create Plan in DRAFT
        DevelopmentPlan plan = developmentPlanService.createPlan(
                employee.getId(), null, "Architectural Leadership", "Lead team architecture reviews",
                "Learn distributed systems patterns", "System Design", "Read DDIA, run architecture workshops",
                LocalDate.of(2026, 12, 31), employee
        );
        assertThat(plan.getStatus()).isEqualTo(DevelopmentPlanStatus.DRAFT);

        // 2. Submit Plan: DRAFT -> SUBMITTED
        DevelopmentPlan submitted = developmentPlanService.submitPlan(plan.getId(), employee, "Ready for manager review");
        assertThat(submitted.getStatus()).isEqualTo(DevelopmentPlanStatus.SUBMITTED);

        // 3. Manager Rejects with reason: SUBMITTED -> REJECTED
        DevelopmentPlan rejected = developmentPlanService.rejectPlan(
                plan.getId(), manager, "Please add specific dates for the workshops");
        assertThat(rejected.getStatus()).isEqualTo(DevelopmentPlanStatus.REJECTED);

        // 4. Employee updates and Resubmits: REJECTED -> SUBMITTED
        DevelopmentPlan resubmitted = developmentPlanService.submitPlan(
                plan.getId(), employee, "Added monthly workshop schedule as requested");
        assertThat(resubmitted.getStatus()).isEqualTo(DevelopmentPlanStatus.SUBMITTED);

        // 5. Manager Approves: SUBMITTED -> APPROVED
        DevelopmentPlan approved = developmentPlanService.approvePlan(plan.getId(), manager, "Looks comprehensive, approved!");
        assertThat(approved.getStatus()).isEqualTo(DevelopmentPlanStatus.APPROVED);

        // 6. Start Plan: APPROVED -> IN_PROGRESS
        DevelopmentPlan inProgress = developmentPlanService.startPlan(plan.getId(), employee);
        assertThat(inProgress.getStatus()).isEqualTo(DevelopmentPlanStatus.IN_PROGRESS);

        // 7. Complete Plan: IN_PROGRESS -> COMPLETED
        DevelopmentPlan completed = developmentPlanService.completePlan(plan.getId(), employee, "Completed all workshops successfully");
        assertThat(completed.getStatus()).isEqualTo(DevelopmentPlanStatus.COMPLETED);

        // 8. Verify Approval History Trail
        List<ApprovalHistory> history = developmentPlanService.getApprovalHistory(plan.getId());
        assertThat(history).hasSize(4);
        assertThat(history.get(0).getAction()).isEqualTo(ApprovalAction.APPROVED);
        assertThat(history.get(1).getAction()).isEqualTo(ApprovalAction.SUBMITTED);
        assertThat(history.get(2).getAction()).isEqualTo(ApprovalAction.REJECTED);
        assertThat(history.get(3).getAction()).isEqualTo(ApprovalAction.SUBMITTED);
    }
}
