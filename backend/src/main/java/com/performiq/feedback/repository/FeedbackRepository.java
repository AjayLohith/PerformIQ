package com.performiq.feedback.repository;

import com.performiq.feedback.entity.Feedback;
import com.performiq.feedback.entity.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    Optional<Feedback> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    List<Feedback> findByEmployeeIdAndReviewCycleId(UUID employeeId, UUID reviewCycleId);
    List<Feedback> findByReviewerIdAndReviewCycleId(UUID reviewerId, UUID reviewCycleId);
    Page<Feedback> findByEmployeeIdAndFeedbackType(UUID employeeId, FeedbackType feedbackType, Pageable pageable);
}
