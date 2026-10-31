package com.performiq.goal.dto;

import com.performiq.checkin.entity.CheckIn;
import com.performiq.goal.entity.Goal;
import com.performiq.keyresult.entity.KeyResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDetailsDto {
    private Goal goal;
    private List<KeyResult> keyResults;
    private List<CheckIn> checkIns;
}
