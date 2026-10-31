package com.performiq.feedback.repository;

import com.performiq.feedback.entity.FeedbackCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackCompetencyRepository extends JpaRepository<FeedbackCompetency, UUID> {
    List<FeedbackCompetency> findByFeedbackId(UUID feedbackId);
    Optional<FeedbackCompetency> findByFeedbackIdAndCompetencyId(UUID feedbackId, UUID competencyId);
}
