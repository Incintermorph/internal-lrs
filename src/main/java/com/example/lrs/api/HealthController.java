package com.example.lrs.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "application", "Internal LRS",
            "version", "0.2.0"
        ));
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
            "message", "Internal LRS (xAPI 1.0.3) is running",
            "endpoints", Map.of(
                "health", "/health",
                "h2-console", "/h2-console",
                "statements", "/xapi/statements (currently disabled)",
                "activities-state", "/xapi/activities/state (currently disabled)"
            )
        ));
    }
}
