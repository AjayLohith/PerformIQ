package com.performiq.dataimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.performiq.auth.entity.AppUser;
import com.performiq.auth.entity.UserRole;
import com.performiq.auth.repository.AppUserRepository;
import com.performiq.checkin.entity.CheckIn;
import com.performiq.checkin.repository.CheckInRepository;
import com.performiq.common.exception.ResourceNotFoundException;
import com.performiq.common.util.ChecksumUtil;
import com.performiq.common.util.CsvReader;
import com.performiq.competency.entity.Competency;
import com.performiq.competency.repository.CompetencyRepository;
import com.performiq.dataimport.dto.ImportRequestDto;
import com.performiq.dataimport.dto.ImportResponseDto;
import com.performiq.dataimport.dto.ManifestDto;
import com.performiq.dataimport.dto.ManifestFileDto;
import com.performiq.dataimport.entity.DataImportRun;
import com.performiq.dataimport.entity.ImportScale;
import com.performiq.dataimport.entity.ImportStatus;
import com.performiq.dataimport.repository.DataImportRunRepository;
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
import com.performiq.validation.dto.ValidationReportDto;
import com.performiq.validation.entity.AnomalyCase;
import com.performiq.validation.entity.AnomalySeverity;
import com.performiq.validation.entity.DetectionStatus;
import com.performiq.validation.repository.AnomalyCaseRepository;
import com.performiq.validation.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportService {

    @Value("${app.data-import.directory:../data-generator/output}")
    private String defaultImportDir;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DataImportRunRepository dataImportRunRepository;
    private final ReviewCycleRepository reviewCycleRepository;
    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final ManagerAssignmentHistoryRepository managerAssignmentHistoryRepository;
    private final AppUserRepository appUserRepository;
    private final GoalRepository goalRepository;
    private final GoalHistoryRepository goalHistoryRepository;
    private final KeyResultRepository keyResultRepository;
    private final KeyResultHistoryRepository keyResultHistoryRepository;
    private final CheckInRepository checkInRepository;
    private final EvidenceRepository evidenceRepository;
    private final CompetencyRepository competencyRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackCompetencyRepository feedbackCompetencyRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewGoalRepository reviewGoalRepository;
    private final DevelopmentPlanRepository developmentPlanRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final AnomalyCaseRepository anomalyCaseRepository;
    private final ValidationService validationService;

    @Transactional
    public ImportResponseDto executeImport(ImportRequestDto request) {
        long startTime = System.currentTimeMillis();
        String dirString = (request.getDirectoryPath() != null && !request.getDirectoryPath().isBlank())
                ? request.getDirectoryPath()
                : defaultImportDir;

        Path importDirPath = Paths.get(dirString).toAbsolutePath().normalize();
        log.info("Starting dataset import from directory: {}", importDirPath);

        if (!Files.exists(importDirPath) || !Files.isDirectory(importDirPath)) {
            throw new IllegalArgumentException("Import directory does not exist: " + importDirPath);
        }

        Path manifestPath = importDirPath.resolve("manifest.json");
        if (!Files.exists(manifestPath)) {
            throw new IllegalArgumentException("manifest.json not found in import directory: " + importDirPath);
        }

        // 1. Parse Manifest
        ManifestDto manifest;
        try {
            manifest = objectMapper.readValue(Files.readAllBytes(manifestPath), ManifestDto.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse manifest.json: " + e.getMessage(), e);
        }

        // 2. Cryptographic Checksum Verification
        if (Boolean.TRUE.equals(request.getVerifyChecksums()) && manifest.getFiles() != null) {
            log.info("Verifying cryptographic SHA-256 checksums against manifest...");
            for (ManifestFileDto fileDto : manifest.getFiles()) {
                if ("csv".equalsIgnoreCase(fileDto.getFormat())) {
                    Path filePath = importDirPath.resolve(fileDto.getFileName());
                    if (!Files.exists(filePath)) {
                        throw new IllegalStateException("Manifest file missing from directory: " + fileDto.getFileName());
                    }
                    try {
                        String calculated = ChecksumUtil.calculateSha256(filePath);
                        if (!calculated.equalsIgnoreCase(fileDto.getSha256())) {
                            throw new IllegalStateException("Checksum verification failed for " + fileDto.getFileName()
                                    + ". Expected " + fileDto.getSha256() + " but found " + calculated);
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException("Error reading file during checksum check: " + fileDto.getFileName(), e);
                    }
                }
            }
            log.info("Cryptographic manifest verification passed successfully.");
        }

        // 3. Create DataImportRun entity
        ImportScale scale = "FULL".equalsIgnoreCase(manifest.getScale()) ? ImportScale.FULL : ImportScale.STARTER;
        DataImportRun run = DataImportRun.builder()
                .seed(manifest.getSeed() != null ? manifest.getSeed() : "unknown")
                .scale(scale)
                .generatorVersion(manifest.getGeneratorVersion() != null ? manifest.getGeneratorVersion() : "1.0.0")
                .status(ImportStatus.IMPORTING)
                .manifestChecksum(manifest.getBundleChecksum())
                .startedAt(Instant.now())
                .totalRecords(manifest.getTotalRecords() != null ? manifest.getTotalRecords() : 0)
                .build();
        run = dataImportRunRepository.save(run);

        Map<String, Integer> tableCounts = new HashMap<>();

        try {
            // Memory resolution caches for fast O(1) FK lookups
            Map<String, ReviewCycle> cycleMap = new HashMap<>();
            reviewCycleRepository.findAll().forEach(c -> cycleMap.put(c.getBusinessId(), c));

            Map<String, Employee> empMap = new HashMap<>();
            employeeRepository.findAll().forEach(e -> empMap.put(e.getBusinessId(), e));

            Map<String, Manager> mgrMap = new HashMap<>();
            managerRepository.findAll().forEach(m -> mgrMap.put(m.getBusinessId(), m));

            Map<String, Goal> goalMap = new HashMap<>();
            goalRepository.findAll().forEach(g -> goalMap.put(g.getBusinessId(), g));

            Map<String, KeyResult> krMap = new HashMap<>();
            keyResultRepository.findAll().forEach(k -> krMap.put(k.getBusinessId(), k));

            Map<String, CheckIn> cinMap = new HashMap<>();
            checkInRepository.findAll().forEach(c -> cinMap.put(c.getBusinessId(), c));

            // Seed catalog competencies if empty (e.g. In-memory test environment)
            if (competencyRepository.count() == 0) {
                seedDefaultCompetencies();
            }

            Map<String, Competency> compMap = new HashMap<>();
            competencyRepository.findAll().forEach(c -> compMap.put(c.getBusinessId(), c));

            Map<String, Feedback> fbMap = new HashMap<>();
            feedbackRepository.findAll().forEach(f -> fbMap.put(f.getBusinessId(), f));

            Map<String, Review> revMap = new HashMap<>();
            reviewRepository.findAll().forEach(r -> revMap.put(r.getBusinessId(), r));

            Map<String, DevelopmentPlan> planMap = new HashMap<>();
            developmentPlanRepository.findAll().forEach(p -> planMap.put(p.getBusinessId(), p));

            // Ingestion in strict topological order:
            // 1. Review Cycles
            tableCounts.put("review_cycles", ingestReviewCycles(importDirPath, cycleMap));

            // 2. Employees
            tableCounts.put("employees", ingestEmployees(importDirPath, empMap));

            // 3. Managers
            tableCounts.put("managers", ingestManagers(importDirPath, empMap, mgrMap));

            // 4. Manager Assignment History
            tableCounts.put("manager_assignment_history", ingestAssignments(importDirPath, empMap, mgrMap));

            // 5. App Users
            tableCounts.put("app_users", ingestUsers(importDirPath, empMap));

            // 6. Goals
            tableCounts.put("goals", ingestGoals(importDirPath, empMap, cycleMap, goalMap));

            // 7. Key Results
            tableCounts.put("key_results", ingestKeyResults(importDirPath, goalMap, krMap));

            // 8. Goal History & Key Result History
            tableCounts.put("goal_history", ingestGoalHistory(importDirPath, goalMap, empMap, mgrMap));
            tableCounts.put("key_result_history", ingestKrHistory(importDirPath, krMap, empMap, mgrMap));

            // 9. Check-ins
            tableCounts.put("check_ins", ingestCheckIns(importDirPath, goalMap, empMap, mgrMap, cinMap));

            // 10. Evidence
            tableCounts.put("evidence", ingestEvidence(importDirPath, cinMap));

            // 11. Feedbacks & Competencies
            tableCounts.put("feedbacks", ingestFeedbacks(importDirPath, empMap, mgrMap, cycleMap, fbMap));
            tableCounts.put("feedback_competencies", ingestFeedbackCompetencies(importDirPath, fbMap, compMap));

            // 12. Reviews & Review Goals
            tableCounts.put("reviews", ingestReviews(importDirPath, empMap, mgrMap, cycleMap, revMap));
            tableCounts.put("review_goals", ingestReviewGoals(importDirPath, revMap, goalMap));

            // 13. Development Plans & Approvals
            tableCounts.put("development_plans", ingestDevPlans(importDirPath, empMap, revMap, planMap));
            tableCounts.put("approval_history", ingestApprovalHistory(importDirPath, planMap, empMap, mgrMap));

            // 14. Benchmark Anomalies
            tableCounts.put("anomalies", ingestAnomalies(importDirPath));

            // 15. Optional Post-Import Validation
            if (Boolean.TRUE.equals(request.getRunValidation())) {
                ValidationReportDto report = validationService.runDataQualityValidation();
                log.info("Post-import validation completed: {} issues detected.", report.getTotalAnomaliesDetected());
            }

            int validTotal = tableCounts.values().stream().mapToInt(Integer::intValue).sum();
            run.setStatus(ImportStatus.COMPLETED);
            run.setValidRecords(validTotal);
            run.setInvalidRecords(tableCounts.getOrDefault("anomalies", 0));
            run.setCompletedAt(Instant.now());
            dataImportRunRepository.save(run);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Dataset import completed successfully in {} ms. Total records ingested: {}", duration, validTotal);

            return ImportResponseDto.builder()
                    .importRunId(run.getId())
                    .status(ImportStatus.COMPLETED)
                    .scale(scale)
                    .seed(run.getSeed())
                    .generatorVersion(run.getGeneratorVersion())
                    .totalRecords(manifest.getTotalRecords())
                    .validRecords(validTotal)
                    .invalidRecords(run.getInvalidRecords())
                    .manifestChecksum(run.getManifestChecksum())
                    .durationMs(duration)
                    .message("Dataset successfully imported and verified.")
                    .importedTableCounts(tableCounts)
                    .build();

        } catch (Exception ex) {
            log.error("Fatal exception during dataset ingestion: {}", ex.getMessage(), ex);
            run.setStatus(ImportStatus.FAILED);
            run.setCompletedAt(Instant.now());
            dataImportRunRepository.save(run);
            throw new IllegalStateException("Data import failed: " + ex.getMessage(), ex);
        }
    }

    private int ingestReviewCycles(Path dir, Map<String, ReviewCycle> cache) throws IOException {
        Path p = dir.resolve("review_cycles.csv");
        if (!Files.exists(p)) return 0;
        List<ReviewCycle> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                ReviewCycle rc = ReviewCycle.builder()
                        .businessId(bId)
                        .name(row.get("name"))
                        .description(row.get("description"))
                        .startDate(LocalDate.parse(row.get("start_date")))
                        .endDate(LocalDate.parse(row.get("end_date")))
                        .status(ReviewCycleStatus.valueOf(row.get("status")))
                        .build();
                toSave.add(rc);
            }
        }
        reviewCycleRepository.saveAll(toSave).forEach(c -> cache.put(c.getBusinessId(), c));
        return toSave.size();
    }

    private int ingestEmployees(Path dir, Map<String, Employee> cache) throws IOException {
        Path p = dir.resolve("employees.csv");
        if (!Files.exists(p)) return 0;
        List<Employee> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Employee emp = Employee.builder()
                        .businessId(bId)
                        .firstName(row.get("first_name"))
                        .lastName(row.get("last_name"))
                        .email(row.get("email"))
                        .department(row.get("department"))
                        .designation(row.get("designation"))
                        .joiningDate(LocalDate.parse(row.get("joining_date")))
                        .status(EmployeeStatus.valueOf(row.get("status")))
                        .build();
                toSave.add(emp);
            }
        }
        employeeRepository.saveAll(toSave).forEach(e -> cache.put(e.getBusinessId(), e));
        return toSave.size();
    }

    private int ingestManagers(Path dir, Map<String, Employee> empMap, Map<String, Manager> cache) throws IOException {
        Path p = dir.resolve("managers.csv");
        if (!Files.exists(p)) return 0;
        List<Manager> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Employee emp = empMap.get(row.get("employee_business_id"));
                if (emp != null) {
                    Manager m = Manager.builder()
                            .businessId(bId)
                            .employee(emp)
                            .effectiveFrom(LocalDate.parse(row.get("effective_from")))
                            .status(ManagerStatus.valueOf(row.get("status")))
                            .build();
                    toSave.add(m);
                }
            }
        }
        managerRepository.saveAll(toSave).forEach(m -> cache.put(m.getBusinessId(), m));
        return toSave.size();
    }

    private int ingestAssignments(Path dir, Map<String, Employee> empMap, Map<String, Manager> mgrMap) throws IOException {
        Path p = dir.resolve("manager_assignment_history.csv");
        if (!Files.exists(p)) return 0;
        List<ManagerAssignmentHistory> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            Employee emp = empMap.get(row.get("employee_business_id"));
            Manager mgr = mgrMap.get(row.get("manager_business_id"));
            if (emp != null && mgr != null) {
                ManagerAssignmentHistory mah = ManagerAssignmentHistory.builder()
                        .employee(emp)
                        .manager(mgr)
                        .effectiveFrom(LocalDate.parse(row.get("effective_from")))
                        .reason(row.get("reason"))
                        .build();
                toSave.add(mah);
            }
        }
        managerAssignmentHistoryRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestUsers(Path dir, Map<String, Employee> empMap) throws IOException {
        Path p = dir.resolve("app_users.csv");
        if (!Files.exists(p)) return 0;
        List<AppUser> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String username = row.get("username");
            if (!appUserRepository.existsByUsername(username)) {
                Employee emp = empMap.get(row.get("employee_business_id"));
                AppUser u = AppUser.builder()
                        .username(username)
                        .employee(emp)
                        .passwordHash(row.get("password_hash"))
                        .role(UserRole.valueOf(row.get("role")))
                        .enabled(Boolean.parseBoolean(row.get("enabled")))
                        .build();
                toSave.add(u);
            }
        }
        appUserRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestGoals(Path dir, Map<String, Employee> empMap, Map<String, ReviewCycle> cycleMap, Map<String, Goal> cache) throws IOException {
        Path p = dir.resolve("goals.csv");
        if (!Files.exists(p)) return 0;
        List<Goal> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Employee emp = empMap.get(row.get("employee_business_id"));
                ReviewCycle cycle = cycleMap.get(row.get("review_cycle_business_id"));
                if (emp != null && cycle != null) {
                    Goal g = Goal.builder()
                            .businessId(bId)
                            .employee(emp)
                            .reviewCycle(cycle)
                            .title(row.get("title"))
                            .description(row.get("description"))
                            .category(row.get("category"))
                            .status(GoalStatus.valueOf(row.get("status")))
                            .weight(new BigDecimal(row.get("weight")))
                            .progress(new BigDecimal(row.get("progress")))
                            .build();
                    toSave.add(g);
                }
            }
        }
        goalRepository.saveAll(toSave).forEach(g -> cache.put(g.getBusinessId(), g));
        return toSave.size();
    }

    private int ingestKeyResults(Path dir, Map<String, Goal> goalMap, Map<String, KeyResult> cache) throws IOException {
        Path p = dir.resolve("key_results.csv");
        if (!Files.exists(p)) return 0;
        List<KeyResult> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Goal g = goalMap.get(row.get("goal_business_id"));
                if (g != null) {
                    KeyResult kr = KeyResult.builder()
                            .businessId(bId)
                            .goal(g)
                            .title(row.get("title"))
                            .metricType(MetricType.valueOf(row.get("metric_type")))
                            .targetValue(new BigDecimal(row.get("target_value")))
                            .currentValue(new BigDecimal(row.get("current_value")))
                            .startValue(new BigDecimal(row.get("start_value")))
                            .weight(new BigDecimal(row.get("weight")))
                            .unit(row.get("unit"))
                            .progress(new BigDecimal(row.get("progress")))
                            .build();
                    toSave.add(kr);
                }
            }
        }
        keyResultRepository.saveAll(toSave).forEach(k -> cache.put(k.getBusinessId(), k));
        return toSave.size();
    }

    private Employee resolveEmployee(String id, Map<String, Employee> empMap, Map<String, Manager> mgrMap) {
        if (id == null || id.isBlank()) return null;
        Employee emp = empMap.get(id);
        if (emp == null && mgrMap != null && mgrMap.containsKey(id)) {
            emp = mgrMap.get(id).getEmployee();
        }
        return emp;
    }

    private int ingestGoalHistory(Path dir, Map<String, Goal> goalMap, Map<String, Employee> empMap, Map<String, Manager> mgrMap) throws IOException {
        Path p = dir.resolve("goal_history.csv");
        if (!Files.exists(p)) return 0;
        List<GoalHistory> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            Goal g = goalMap.get(row.get("goal_business_id"));
            if (g != null) {
                Employee emp = resolveEmployee(row.get("changed_by_employee_id"), empMap, mgrMap);
                GoalHistory gh = GoalHistory.builder()
                        .goal(g)
                        .fieldChanged(row.get("field_changed"))
                        .oldValue(row.get("old_value"))
                        .newValue(row.get("new_value"))
                        .changedBy(emp)
                        .build();
                toSave.add(gh);
            }
        }
        goalHistoryRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestKrHistory(Path dir, Map<String, KeyResult> krMap, Map<String, Employee> empMap, Map<String, Manager> mgrMap) throws IOException {
        Path p = dir.resolve("key_result_history.csv");
        if (!Files.exists(p)) return 0;
        List<KeyResultHistory> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            KeyResult kr = krMap.get(row.get("key_result_business_id"));
            if (kr != null) {
                Employee emp = resolveEmployee(row.get("changed_by_employee_id"), empMap, mgrMap);
                KeyResultHistory krh = KeyResultHistory.builder()
                        .keyResult(kr)
                        .oldValue(new BigDecimal(row.get("old_value")))
                        .newValue(new BigDecimal(row.get("new_value")))
                        .changedBy(emp)
                        .build();
                toSave.add(krh);
            }
        }
        keyResultHistoryRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestCheckIns(Path dir, Map<String, Goal> goalMap, Map<String, Employee> empMap, Map<String, Manager> mgrMap, Map<String, CheckIn> cache) throws IOException {
        Path p = dir.resolve("check_ins.csv");
        if (!Files.exists(p)) return 0;
        List<CheckIn> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Goal g = goalMap.get(row.get("goal_business_id"));
                Employee emp = resolveEmployee(row.get("employee_business_id"), empMap, mgrMap);
                if (g != null && emp != null) {
                    CheckIn cin = CheckIn.builder()
                            .businessId(bId)
                            .goal(g)
                            .employee(emp)
                            .notes(row.get("notes"))
                            .progressSnapshot(new BigDecimal(row.get("progress_snapshot")))
                            .checkInDate(LocalDate.parse(row.get("check_in_date")))
                            .build();
                    toSave.add(cin);
                }
            }
        }
        checkInRepository.saveAll(toSave).forEach(c -> cache.put(c.getBusinessId(), c));
        return toSave.size();
    }

    private int ingestEvidence(Path dir, Map<String, CheckIn> cinMap) throws IOException {
        Path p = dir.resolve("evidence.csv");
        if (!Files.exists(p)) return 0;
        List<Evidence> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            CheckIn cin = cinMap.get(row.get("check_in_business_id"));
            if (cin != null) {
                Evidence ev = Evidence.builder()
                        .businessId(row.get("business_id"))
                        .checkIn(cin)
                        .title(row.get("title"))
                        .url(row.get("url"))
                        .description(row.get("description"))
                        .evidenceType(EvidenceType.valueOf(row.get("evidence_type")))
                        .build();
                toSave.add(ev);
            }
        }
        evidenceRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestFeedbacks(Path dir, Map<String, Employee> empMap, Map<String, Manager> mgrMap, Map<String, ReviewCycle> cycleMap, Map<String, Feedback> cache) throws IOException {
        Path p = dir.resolve("feedbacks.csv");
        if (!Files.exists(p)) return 0;
        List<Feedback> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Employee emp = resolveEmployee(row.get("employee_business_id"), empMap, mgrMap);
                Employee rev = resolveEmployee(row.get("reviewer_business_id"), empMap, mgrMap);
                ReviewCycle cycle = cycleMap.get(row.get("review_cycle_business_id"));
                if (emp != null && rev != null && cycle != null) {
                    Integer rating = row.get("overall_rating") != null && !row.get("overall_rating").isBlank()
                            ? Integer.parseInt(row.get("overall_rating"))
                            : null;
                    Feedback fb = Feedback.builder()
                            .businessId(bId)
                            .employee(emp)
                            .reviewer(rev)
                            .reviewCycle(cycle)
                            .feedbackType(FeedbackType.valueOf(row.get("feedback_type")))
                            .comments(row.get("comments"))
                            .overallRating(rating)
                            .status(FeedbackStatus.valueOf(row.get("status")))
                            .build();
                    toSave.add(fb);
                }
            }
        }
        feedbackRepository.saveAll(toSave).forEach(f -> cache.put(f.getBusinessId(), f));
        return toSave.size();
    }

    private int ingestFeedbackCompetencies(Path dir, Map<String, Feedback> fbMap, Map<String, Competency> compMap) throws IOException {
        Path p = dir.resolve("feedback_competencies.csv");
        if (!Files.exists(p)) return 0;
        List<FeedbackCompetency> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            Feedback fb = fbMap.get(row.get("feedback_business_id"));
            Competency comp = compMap.get(row.get("competency_business_id"));
            if (fb != null && comp != null) {
                FeedbackCompetency fc = FeedbackCompetency.builder()
                        .feedback(fb)
                        .competency(comp)
                        .rating(Integer.parseInt(row.get("rating")))
                        .comments(row.get("comments"))
                        .build();
                toSave.add(fc);
            }
        }
        feedbackCompetencyRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestReviews(Path dir, Map<String, Employee> empMap, Map<String, Manager> mgrMap, Map<String, ReviewCycle> cycleMap, Map<String, Review> cache) throws IOException {
        Path p = dir.resolve("reviews.csv");
        if (!Files.exists(p)) return 0;
        List<Review> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Employee emp = resolveEmployee(row.get("employee_business_id"), empMap, mgrMap);
                Employee rev = resolveEmployee(row.get("reviewer_business_id"), empMap, mgrMap);
                ReviewCycle cycle = cycleMap.get(row.get("review_cycle_business_id"));
                if (emp != null && rev != null && cycle != null) {
                    BigDecimal score = row.get("overall_score") != null && !row.get("overall_score").isBlank()
                            ? new BigDecimal(row.get("overall_score"))
                            : null;
                    Review r = Review.builder()
                            .businessId(bId)
                            .employee(emp)
                            .reviewer(rev)
                            .reviewCycle(cycle)
                            .status(ReviewStatus.valueOf(row.get("status")))
                            .overallScore(score)
                            .summary(row.get("summary"))
                            .recommendation(row.get("recommendation"))
                            .build();
                    toSave.add(r);
                }
            }
        }
        reviewRepository.saveAll(toSave).forEach(r -> cache.put(r.getBusinessId(), r));
        return toSave.size();
    }

    private int ingestReviewGoals(Path dir, Map<String, Review> revMap, Map<String, Goal> goalMap) throws IOException {
        Path p = dir.resolve("review_goals.csv");
        if (!Files.exists(p)) return 0;
        List<ReviewGoal> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            Review r = revMap.get(row.get("review_business_id"));
            Goal g = goalMap.get(row.get("goal_business_id"));
            if (r != null && g != null) {
                BigDecimal score = row.get("goal_score") != null && !row.get("goal_score").isBlank()
                        ? new BigDecimal(row.get("goal_score"))
                        : null;
                ReviewGoal rg = ReviewGoal.builder()
                        .review(r)
                        .goal(g)
                        .goalScore(score)
                        .comments(row.get("comments"))
                        .build();
                toSave.add(rg);
            }
        }
        reviewGoalRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestDevPlans(Path dir, Map<String, Employee> empMap, Map<String, Review> revMap, Map<String, DevelopmentPlan> cache) throws IOException {
        Path p = dir.resolve("development_plans.csv");
        if (!Files.exists(p)) return 0;
        List<DevelopmentPlan> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!cache.containsKey(bId)) {
                Employee emp = empMap.get(row.get("employee_business_id"));
                Review rev = revMap.get(row.get("review_business_id"));
                if (emp != null) {
                    LocalDate targetDate = row.get("target_date") != null && !row.get("target_date").isBlank()
                            ? LocalDate.parse(row.get("target_date"))
                            : null;
                    DevelopmentPlan dp = DevelopmentPlan.builder()
                            .businessId(bId)
                            .employee(emp)
                            .review(rev)
                            .title(row.get("title"))
                            .description(row.get("description"))
                            .objectives(row.get("objectives"))
                            .skillsToImprove(row.get("skills_to_improve"))
                            .actionItems(row.get("action_items"))
                            .targetDate(targetDate)
                            .status(DevelopmentPlanStatus.valueOf(row.get("status")))
                            .build();
                    toSave.add(dp);
                }
            }
        }
        developmentPlanRepository.saveAll(toSave).forEach(p_ent -> cache.put(p_ent.getBusinessId(), p_ent));
        return toSave.size();
    }

    private int ingestApprovalHistory(Path dir, Map<String, DevelopmentPlan> planMap, Map<String, Employee> empMap, Map<String, Manager> mgrMap) throws IOException {
        Path p = dir.resolve("approval_history.csv");
        if (!Files.exists(p)) return 0;
        List<ApprovalHistory> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            DevelopmentPlan dp = planMap.get(row.get("development_plan_business_id"));
            Employee approver = resolveEmployee(row.get("approver_employee_id"), empMap, mgrMap);
            if (dp != null && approver != null) {
                ApprovalHistory ah = ApprovalHistory.builder()
                        .developmentPlan(dp)
                        .approver(approver)
                        .action(ApprovalAction.valueOf(row.get("action")))
                        .comments(row.get("comments"))
                        .build();
                toSave.add(ah);
            }
        }
        approvalHistoryRepository.saveAll(toSave);
        return toSave.size();
    }

    private int ingestAnomalies(Path dir) throws IOException {
        Path p = dir.resolve("anomalies.csv");
        if (!Files.exists(p)) return 0;
        List<AnomalyCase> toSave = new ArrayList<>();
        for (var row : CsvReader.readCsv(p)) {
            String bId = row.get("business_id");
            if (!anomalyCaseRepository.existsByBusinessId(bId)) {
                AnomalyCase ac = AnomalyCase.builder()
                        .businessId(bId)
                        .issueType(row.get("issue_type"))
                        .affectedEntity(row.get("affected_entity"))
                        .affectedBusinessId(row.get("affected_business_id"))
                        .severity(AnomalySeverity.valueOf(row.get("severity")))
                        .expectedOutcome(row.get("expected_outcome"))
                        .actualOutcome(row.get("actual_outcome"))
                        .detectionStatus(DetectionStatus.valueOf(row.get("detection_status")))
                        .build();
                toSave.add(ac);
            }
        }
        anomalyCaseRepository.saveAll(toSave);
        return toSave.size();
    }

    private void seedDefaultCompetencies() {
        List<Competency> defaultComps = List.of(
                Competency.builder().businessId("COMP-000001").name("Leadership & Influence").category("LEADERSHIP").build(),
                Competency.builder().businessId("COMP-000002").name("Technical Excellence").category("TECHNICAL").build(),
                Competency.builder().businessId("COMP-000003").name("Communication & Clarity").category("INTERPERSONAL").build(),
                Competency.builder().businessId("COMP-000004").name("Problem Solving & Critical Thinking").category("COGNITIVE").build(),
                Competency.builder().businessId("COMP-000005").name("Collaboration & Teamwork").category("INTERPERSONAL").build(),
                Competency.builder().businessId("COMP-000006").name("Delivery & Execution").category("EXECUTION").build(),
                Competency.builder().businessId("COMP-000007").name("Innovation & Creativity").category("COGNITIVE").build(),
                Competency.builder().businessId("COMP-000008").name("Adaptability & Resilience").category("CORE").build()
        );
        competencyRepository.saveAll(defaultComps);
    }

    @Transactional(readOnly = true)
    public DataImportRun getLatestImportRun() {
        return dataImportRunRepository.findFirstByOrderByStartedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No import run records found"));
    }

    @Transactional(readOnly = true)
    public List<DataImportRun> getAllImportRuns() {
        return dataImportRunRepository.findAll();
    }
}
