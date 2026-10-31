package com.performiq.review.entity;

import com.performiq.goal.entity.Goal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
import java.util.UUID;

@Entity
@Table(
    name = "review_goals",
    uniqueConstraints = @UniqueConstraint(name = "uq_review_goal", columnNames = {"review_id", "goal_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "goal_score", precision = 5, scale = 2)
    private BigDecimal goalScore;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}
