package com.performiq;

import com.performiq.checkin.repository.CheckInRepository;
import com.performiq.dataimport.dto.ImportRequestDto;
import com.performiq.dataimport.dto.ImportResponseDto;
import com.performiq.dataimport.entity.ImportStatus;
import com.performiq.dataimport.repository.DataImportRunRepository;
import com.performiq.dataimport.service.DataImportService;
import com.performiq.development.repository.DevelopmentPlanRepository;
import com.performiq.employee.repository.EmployeeRepository;
import com.performiq.feedback.repository.FeedbackRepository;
import com.performiq.goal.repository.GoalRepository;
import com.performiq.keyresult.repository.KeyResultRepository;
import com.performiq.review.repository.ReviewRepository;
import com.performiq.reviewcycle.repository.ReviewCycleRepository;
import com.performiq.validation.dto.ValidationReportDto;
import com.performiq.validation.repository.AnomalyCaseRepository;
import com.performiq.validation.service.ValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataImportAndValidationTest {

    @Autowired private DataImportService dataImportService;
    @Autowired private ValidationService validationService;
    @Autowired private DataImportRunRepository dataImportRunRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ReviewCycleRepository reviewCycleRepository;
    @Autowired private GoalRepository goalRepository;
    @Autowired private KeyResultRepository keyResultRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private DevelopmentPlanRepository developmentPlanRepository;
    @Autowired private AnomalyCaseRepository anomalyCaseRepository;

    @Test
    @DisplayName("Verify end-to-end dataset ingestion from output/ with SHA-256 verification and anomaly detection")
    void testEndToEndImportAndValidation() {
        // Point to the generated starter dataset in data-generator/output
        String outputDir = Paths.get("../data-generator/output").toAbsolutePath().normalize().toString();

        ImportRequestDto request = ImportRequestDto.builder()
                .directoryPath(outputDir)
                .format("csv")
                .verifyChecksums(true)
                .runValidation(true)
                .build();

        ImportResponseDto response = dataImportService.executeImport(request);

        // 1. Verify Import Execution
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ImportStatus.COMPLETED);
        assertThat(response.getValidRecords()).isGreaterThan(6000);
        assertThat(response.getManifestChecksum()).isNotBlank();

        // 2. Verify Entity Persistences
        assertThat(employeeRepository.count()).isGreaterThanOrEqualTo(50);
        assertThat(reviewCycleRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(goalRepository.count()).isGreaterThanOrEqualTo(400);
        assertThat(keyResultRepository.count()).isGreaterThanOrEqualTo(1000);
        assertThat(checkInRepository.count()).isGreaterThanOrEqualTo(1600);
        assertThat(feedbackRepository.count()).isGreaterThanOrEqualTo(250);
        assertThat(reviewRepository.count()).isGreaterThanOrEqualTo(60);
        assertThat(developmentPlanRepository.count()).isGreaterThanOrEqualTo(45);
        assertThat(anomalyCaseRepository.count()).isGreaterThanOrEqualTo(10);

        // 3. Verify Validation Engine Anomaly Detection
        ValidationReportDto report = validationService.runDataQualityValidation();
        assertThat(report).isNotNull();
        assertThat(report.getTotalAnomaliesDetected()).isGreaterThanOrEqualTo(5);
        assertThat(report.getAnomaliesByType()).containsKeys(
                "PROGRESS_CALCULATION_MISMATCH",
                "CHECKIN_DATE_OUT_OF_BOUNDS",
                "OUT_OF_RANGE_RATING"
        );
        assertThat(report.getDetectionRatePercentage()).isGreaterThan(0.0);
    }
}
