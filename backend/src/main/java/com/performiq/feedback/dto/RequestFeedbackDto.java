package com.performiq.feedback.dto;

import com.performiq.feedback.entity.FeedbackType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestFeedbackDto {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Reviewer ID is required")
    private UUID reviewerId;

    @NotNull(message = "Review cycle ID is required")
    private UUID reviewCycleId;

    @NotNull(message = "Feedback type is required")
    private FeedbackType feedbackType;
}
