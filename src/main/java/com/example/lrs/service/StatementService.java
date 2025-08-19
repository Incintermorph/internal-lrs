
package com.example.lrs.service;

import com.example.lrs.mapper.StatementMapper;
import com.example.lrs.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementService {
    private final StatementMapper mapper;

    public String saveOne(String maybeId, String body) throws Exception {
        log.debug("Saving statement - ID: {}, Body length: {}", maybeId, body != null ? body.length() : 0);

        JsonNode node = JsonUtil.parse(body);
        if (node.get("actor")==null || node.get("verb")==null || node.get("object")==null) {
            log.error("Missing required fields in statement: actor={}, verb={}, object={}",
                    node.get("actor") != null, node.get("verb") != null, node.get("object") != null);
            throw new IllegalArgumentException("actor/verb/object are required");
        }
        String id = JsonUtil.ensureStatementId(node, maybeId);
        log.debug("Statement ID assigned: {}", id);

        Map<String,Object> p = new HashMap<>();
        p.put("id", id);
        p.put("fullStatement", body);
        p.put("actorJson", node.get("actor").toString());
        p.put("verbJson", node.get("verb").toString());
        p.put("objectJson", node.get("object").toString());
        p.put("resultJson", node.get("result")!=null? node.get("result").toString(): null);
        p.put("contextJson", node.get("context")!=null? node.get("context").toString(): null);
        p.put("attachmentsJson", node.get("attachments")!=null? node.get("attachments").toString(): null);

        // Extract fields for indexing
        p.put("verbId", node.get("verb").has("id") ? node.get("verb").get("id").asText() : null);
        p.put("actorAccountName", node.get("actor").has("account") && node.get("actor").get("account").has("name") ?
               node.get("actor").get("account").get("name").asText() : null);
        p.put("activityId", node.get("object").has("id") ? node.get("object").get("id").asText() :
                           (node.get("object").has("object") && node.get("object").get("object").has("id") ?
                            node.get("object").get("object").get("id").asText() : null));
        p.put("registration", node.get("context")!=null && node.get("context").has("registration") ?
               node.get("context").get("registration").asText() : null);

        var existing = mapper.findById(id);
        if (existing == null) {
            log.debug("Inserting new statement with ID: {}", id);
            mapper.insert(p);
            log.info("Successfully inserted statement with ID: {}", id);
        } else if (!Objects.equals(existing.getFullStatement(), body)) {
            log.error("Statement ID {} already exists with different content", id);
            throw new IllegalStateException("StatementId already exists with different content");
        } else {
            log.debug("Statement with ID {} already exists with same content, skipping insert", id);
        }
        return id;
    }

    public List<com.example.lrs.domain.Statement> query(OffsetDateTime since, String verbId, String activityId,
                                                        String registration, int limit) {
        int adjustedLimit = Math.min(Math.max(limit,1), 500);
        log.debug("Querying statements - since: {}, verbId: {}, activityId: {}, registration: {}, limit: {}",
                since, verbId, activityId, registration, adjustedLimit);

        List<com.example.lrs.domain.Statement> results = mapper.query(since, verbId, activityId, registration, adjustedLimit);
        log.info("Query returned {} statements", results.size());
        return results;
    }

    public String getById(String id) {
        log.debug("Getting statement by ID: {}", id);
        var s = mapper.findById(id);
        if (s != null) {
            log.debug("Found statement with ID: {}", id);
            return s.getFullStatement();
        } else {
            log.debug("Statement not found with ID: {}", id);
            return null;
        }
    }
}
