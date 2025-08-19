package com.example.lrs.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/logs")
public class LogController {

    private static final String LOG_FILE_PATH = "logs/application.log";
    private static final int MAX_LINES = 1000;

    @GetMapping("/tail")
    public ResponseEntity<Map<String, Object>> tailLogs(
            @RequestParam(defaultValue = "100") int lines,
            @RequestParam(defaultValue = "INFO") String level) {
        
        try {
            Path logPath = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(logPath)) {
                return ResponseEntity.ok(Map.of(
                    "message", "Log file not found",
                    "path", LOG_FILE_PATH,
                    "timestamp", LocalDateTime.now()
                ));
            }

            List<String> allLines = Files.readAllLines(logPath);
            int requestedLines = Math.min(lines, MAX_LINES);
            
            // 마지막 N줄 가져오기
            List<String> tailLines = allLines.stream()
                    .skip(Math.max(0, allLines.size() - requestedLines))
                    .collect(Collectors.toList());

            // 로그 레벨 필터링
            if (!"ALL".equalsIgnoreCase(level)) {
                tailLines = tailLines.stream()
                        .filter(line -> line.contains(level.toUpperCase()))
                        .collect(Collectors.toList());
            }

            return ResponseEntity.ok(Map.of(
                "logs", tailLines,
                "totalLines", allLines.size(),
                "returnedLines", tailLines.size(),
                "level", level,
                "timestamp", LocalDateTime.now()
            ));

        } catch (IOException e) {
            log.error("Error reading log file: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to read log file: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchLogs(
            @RequestParam String query,
            @RequestParam(defaultValue = "100") int maxResults) {
        
        try {
            Path logPath = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(logPath)) {
                return ResponseEntity.ok(Map.of(
                    "message", "Log file not found",
                    "path", LOG_FILE_PATH,
                    "timestamp", LocalDateTime.now()
                ));
            }

            List<String> allLines = Files.readAllLines(logPath);
            List<String> matchingLines = allLines.stream()
                    .filter(line -> line.toLowerCase().contains(query.toLowerCase()))
                    .limit(maxResults)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "logs", matchingLines,
                "query", query,
                "totalMatches", matchingLines.size(),
                "timestamp", LocalDateTime.now()
            ));

        } catch (IOException e) {
            log.error("Error searching log file: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to search log file: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getLogStats() {
        try {
            Path logPath = Paths.get(LOG_FILE_PATH);
            if (!Files.exists(logPath)) {
                return ResponseEntity.ok(Map.of(
                    "message", "Log file not found",
                    "path", LOG_FILE_PATH,
                    "timestamp", LocalDateTime.now()
                ));
            }

            List<String> allLines = Files.readAllLines(logPath);
            
            Map<String, Long> levelCounts = allLines.stream()
                    .filter(line -> line.contains("ERROR") || line.contains("WARN") || 
                                  line.contains("INFO") || line.contains("DEBUG"))
                    .collect(Collectors.groupingBy(
                        line -> {
                            if (line.contains("ERROR")) return "ERROR";
                            if (line.contains("WARN")) return "WARN";
                            if (line.contains("INFO")) return "INFO";
                            if (line.contains("DEBUG")) return "DEBUG";
                            return "OTHER";
                        },
                        Collectors.counting()
                    ));

            return ResponseEntity.ok(Map.of(
                "totalLines", allLines.size(),
                "levelCounts", levelCounts,
                "fileSize", Files.size(logPath),
                "lastModified", Files.getLastModifiedTime(logPath).toString(),
                "timestamp", LocalDateTime.now()
            ));

        } catch (IOException e) {
            log.error("Error getting log stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to get log stats: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testLogging(
            @RequestParam(defaultValue = "INFO") String level,
            @RequestParam(defaultValue = "Test log message") String message) {
        
        switch (level.toUpperCase()) {
            case "ERROR":
                log.error("TEST ERROR: {}", message);
                break;
            case "WARN":
                log.warn("TEST WARN: {}", message);
                break;
            case "INFO":
                log.info("TEST INFO: {}", message);
                break;
            case "DEBUG":
                log.debug("TEST DEBUG: {}", message);
                break;
            default:
                log.info("TEST: {}", message);
        }

        return ResponseEntity.ok(Map.of(
            "message", "Test log written",
            "level", level,
            "content", message,
            "timestamp", LocalDateTime.now()
        ));
    }
}
