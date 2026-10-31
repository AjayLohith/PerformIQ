package com.performiq.audit.controller;

import com.performiq.audit.entity.AuditLog;
import com.performiq.audit.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "Admin endpoints for inspecting security, state transition, and entity mutation audit trails")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "List audit logs (Admin)", description = "Returns a paginated list of system mutation and state transition audit logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLog> result;
        if (entityType != null && entityId != null) {
            result = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageRequest);
        } else if (actorId != null) {
            result = auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageRequest);
        } else {
            result = auditLogRepository.findAll(pageRequest);
        }

        return ResponseEntity.ok(result);
    }
}
