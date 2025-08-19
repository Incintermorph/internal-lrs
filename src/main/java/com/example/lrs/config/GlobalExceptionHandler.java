
package com.example.lrs.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,Object>> handleIllegalState(IllegalStateException ex, WebRequest request) {
        HttpStatus status = ex.getMessage()!=null && ex.getMessage().toLowerCase().contains("etag") ?
                HttpStatus.PRECONDITION_FAILED : HttpStatus.CONFLICT;

        log.warn("IllegalStateException occurred: {} | Request: {} | Status: {}",
                ex.getMessage(), request.getDescription(false), status.value());

        return ResponseEntity.status(status).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "path", request.getDescription(false).replace("uri=", "")
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleOther(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {} | Request: {} | Exception type: {}",
                ex.getMessage(), request.getDescription(false), ex.getClass().getSimpleName(), ex);

        return ResponseEntity.status(500).body(Map.of(
                "error", ex.getMessage() != null ? ex.getMessage() : "Internal server error",
                "timestamp", LocalDateTime.now(),
                "status", 500,
                "path", request.getDescription(false).replace("uri=", ""),
                "type", ex.getClass().getSimpleName()
        ));
    }
}
