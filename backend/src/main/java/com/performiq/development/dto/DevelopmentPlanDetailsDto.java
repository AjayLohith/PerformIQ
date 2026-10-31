package com.performiq.development.dto;

import com.performiq.development.entity.ApprovalHistory;
import com.performiq.development.entity.DevelopmentPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevelopmentPlanDetailsDto {
    private DevelopmentPlan plan;
    private List<ApprovalHistory> approvalHistory;
}
