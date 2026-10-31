package com.performiq.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Simple health endpoint to verify the API is running.
 * Accessible at GET /api/v1/health
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "application", "PerformIQ",
            "timestamp", Instant.now().toString()
        ));
    }
}
