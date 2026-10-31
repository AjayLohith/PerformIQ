package com.performiq.goal.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal extends BaseEntity {

    @Column(name = "business_id", nullable = false, unique = true, length = 64)
    private String businessId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_cycle_id", nullable = false)
    private ReviewCycle reviewCycle;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 64)
    @Builder.Default
    private String category = "INDIVIDUAL";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private GoalStatus status = GoalStatus.DRAFT;

    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weight = new BigDecimal("1.00");

    @Column(name = "progress", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;
}
