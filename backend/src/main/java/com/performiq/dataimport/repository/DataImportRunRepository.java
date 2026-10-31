package com.performiq.dataimport.repository;

import com.performiq.dataimport.entity.DataImportRun;
import com.performiq.dataimport.entity.ImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataImportRunRepository extends JpaRepository<DataImportRun, UUID> {
    Optional<DataImportRun> findFirstByOrderByStartedAtDesc();
    Page<DataImportRun> findByStatus(ImportStatus status, Pageable pageable);
}
