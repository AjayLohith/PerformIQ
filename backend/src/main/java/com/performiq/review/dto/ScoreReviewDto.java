package com.performiq.review.dto;

import com.performiq.review.service.ReviewService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreReviewDto {

    private String summary;
    private String recommendation;
    private List<ReviewService.ReviewGoalScoreDto> goalScores;
}
