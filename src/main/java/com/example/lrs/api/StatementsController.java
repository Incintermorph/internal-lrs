
package com.example.lrs.api;

import com.example.lrs.domain.Statement;
import com.example.lrs.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/xapi/statements")
@RequiredArgsConstructor
public class StatementsController {

    private final StatementService svc;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> postStatement(@RequestBody String body) throws Exception {
        log.info("POST /xapi/statements - Body length: {}", body != null ? body.length() : 0);
        String id = svc.saveOne(null, body);
        log.info("POST /xapi/statements - Created statement with ID: {}", id);
        return ResponseEntity.ok(Collections.singletonList(id));
    }

    @PutMapping(params = "statementId", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> putStatement(@RequestParam String statementId, @RequestBody String body) throws Exception {
        String id = svc.saveOne(statementId, body);
        return ResponseEntity.ok(Collections.singletonList(id));
    }

    @GetMapping(produces = "application/json")
    public Map<String,Object> getStatements(
            @RequestParam(value = "since", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
            @RequestParam(value = "verb", required = false) String verb,
            @RequestParam(value = "activity", required = false) String activityId,
            @RequestParam(value = "registration", required = false) String registration,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit
    ) {
        List<Statement> stmts = svc.query(since, verb, activityId, registration, limit);
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("statements", stmts.stream().map(Statement::getFullStatement).toList());
        return result;
    }

    @GetMapping(params = "statementId", produces = "application/json")
    public ResponseEntity<String> getStatementById(
    @RequestParam(value = "statementId", required = false) String statementId
    ) {
        log.info("GET /xapi/statements - statementId: {}", statementId);
        String json = svc.getById(statementId);
        return json != null ? ResponseEntity.ok(json) : ResponseEntity.notFound().build();
    }
}
