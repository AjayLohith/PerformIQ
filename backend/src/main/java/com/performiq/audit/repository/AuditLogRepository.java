package com.performiq.audit.repository;

import com.performiq.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId, Pageable pageable);
    Page<AuditLog> findByActorIdOrderByCreatedAtDesc(UUID actorId, Pageable pageable);
}
