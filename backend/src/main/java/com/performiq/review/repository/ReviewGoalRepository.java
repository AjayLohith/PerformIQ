package com.performiq.review.repository;

import com.performiq.review.entity.ReviewGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewGoalRepository extends JpaRepository<ReviewGoal, UUID> {
    List<ReviewGoal> findByReviewId(UUID reviewId);
    Optional<ReviewGoal> findByReviewIdAndGoalId(UUID reviewId, UUID goalId);
}
