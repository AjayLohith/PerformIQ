package com.performiq.review.dto;

import com.performiq.review.entity.Review;
import com.performiq.review.entity.ReviewGoal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailsDto {
    private Review review;
    private List<ReviewGoal> reviewGoals;
}
