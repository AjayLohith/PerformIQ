package com.performiq.feedback.entity;

import com.performiq.competency.entity.Competency;
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

import java.util.UUID;

@Entity
@Table(
    name = "feedback_competencies",
    uniqueConstraints = @UniqueConstraint(name = "uq_feedback_competency", columnNames = {"feedback_id", "competency_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
}
