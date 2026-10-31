package com.performiq.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.performiq.audit.entity.AuditLog;
import com.performiq.audit.repository.AuditLogRepository;
import com.performiq.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLog recordAudit(Employee actor, String action, String entityType, String entityId,
                                String previousValue, String newValue, String reason) {
        log.debug("Recording audit log: action={}, entityType={}, entityId={}, actor={}",
                action, entityType, entityId, actor != null ? actor.getBusinessId() : "SYSTEM");

        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .previousValue(previousValue)
                .newValue(newValue)
                .reason(reason)
                .build();

        return auditLogRepository.save(auditLog);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AuditLog recordObjectAudit(Employee actor, String action, String entityType, String entityId,
                                      Object previousObject, Object newObject, String reason) {
        String prevJson = null;
        String newJson = null;
        try {
            if (previousObject != null) {
                prevJson = objectMapper.writeValueAsString(previousObject);
            }
            if (newObject != null) {
                newJson = objectMapper.writeValueAsString(newObject);
            }
        } catch (Exception e) {
            log.warn("Could not serialize audit objects to JSON: {}", e.getMessage());
            prevJson = String.valueOf(previousObject);
            newJson = String.valueOf(newObject);
        }

        return recordAudit(actor, action, entityType, entityId, prevJson, newJson, reason);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForEntity(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForActor(UUID actorId, Pageable pageable) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable);
    }
}
