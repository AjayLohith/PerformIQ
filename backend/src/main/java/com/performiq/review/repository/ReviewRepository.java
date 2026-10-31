package com.performiq.review.repository;

import com.performiq.review.entity.Review;
import com.performiq.review.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    Optional<Review> findByEmployeeIdAndReviewCycleId(UUID employeeId, UUID reviewCycleId);
    List<Review> findByReviewerIdAndReviewCycleId(UUID reviewerId, UUID reviewCycleId);
    Page<Review> findByReviewCycleId(UUID reviewCycleId, Pageable pageable);
    Page<Review> findByReviewCycleIdAndStatus(UUID reviewCycleId, ReviewStatus status, Pageable pageable);
}
