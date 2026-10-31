package com.performiq.goal.repository;

import com.performiq.goal.entity.GoalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalHistoryRepository extends JpaRepository<GoalHistory, UUID> {
    List<GoalHistory> findByGoalIdOrderByChangedAtDesc(UUID goalId);
}
