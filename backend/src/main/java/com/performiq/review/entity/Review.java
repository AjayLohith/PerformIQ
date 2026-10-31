package com.performiq.review.entity;

import com.performiq.common.entity.BaseEntity;
import com.performiq.employee.entity.Employee;
import com.performiq.reviewcycle.entity.ReviewCycle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(name = "uq_employee_review_cycle", columnNames = {"employee_id", "review_cycle_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Column(name = "business_id", nullable = false, unique = true, length = 64)
    private String businessId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_cycle_id", nullable = false)
    private ReviewCycle reviewCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "completed_at")
    private Instant completedAt;
}
