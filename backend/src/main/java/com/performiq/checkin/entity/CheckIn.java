package com.performiq.checkin.entity;

import com.performiq.employee.entity.Employee;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "business_id", nullable = false, unique = true, length = 64)
    private String businessId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "notes", nullable = false, columnDefinition = "TEXT")
    private String notes;

    @Column(name = "progress_snapshot", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressSnapshot;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
