package com.performiq.development.entity;

import com.performiq.common.entity.BaseEntity;
import com.performiq.employee.entity.Employee;
import com.performiq.review.entity.Review;
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

import java.time.LocalDate;

@Entity
@Table(name = "development_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevelopmentPlan extends BaseEntity {

    @Column(name = "business_id", nullable = false, unique = true, length = 64)
    private String businessId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "skills_to_improve", columnDefinition = "TEXT")
    private String skillsToImprove;

    @Column(name = "action_items", columnDefinition = "TEXT")
    private String actionItems;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private DevelopmentPlanStatus status = DevelopmentPlanStatus.DRAFT;
}
