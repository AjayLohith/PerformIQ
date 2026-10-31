package com.performiq.keyresult.repository;

import com.performiq.keyresult.entity.KeyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KeyResultRepository extends JpaRepository<KeyResult, UUID> {
    Optional<KeyResult> findByBusinessId(String businessId);
    boolean existsByBusinessId(String businessId);
    List<KeyResult> findByGoalId(UUID goalId);
}
