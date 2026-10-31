package com.performiq.keyresult.entity;

import com.performiq.common.entity.BaseEntity;
import com.performiq.goal.entity.Goal;
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
@Table(name = "key_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyResult extends BaseEntity {

    @Column(name = "business_id", nullable = false, unique = true, length = 64)
    private String businessId;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 32)
    private MetricType metricType;

    @Column(name = "target_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "current_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "start_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal startValue;

    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weight = new BigDecimal("1.00");

    @Column(name = "unit", nullable = false, length = 32)
    @Builder.Default
    private String unit = "%";

    @Column(name = "progress", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progress = BigDecimal.ZERO;
}
