package com.performiq.keyresult.entity;

import com.performiq.employee.entity.Employee;
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
import java.util.UUID;

@Entity
@Table(name = "key_result_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyResultHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "key_result_id", nullable = false)
    private KeyResult keyResult;

    @Column(name = "old_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal oldValue;

    @Column(name = "new_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal newValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Employee changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;
}
