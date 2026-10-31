package com.performiq.keyresult.repository;

import com.performiq.keyresult.entity.KeyResultHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KeyResultHistoryRepository extends JpaRepository<KeyResultHistory, UUID> {
    List<KeyResultHistory> findByKeyResultIdOrderByChangedAtDesc(UUID keyResultId);
}
