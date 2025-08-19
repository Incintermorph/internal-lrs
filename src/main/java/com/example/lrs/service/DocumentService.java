
package com.example.lrs.service;

import com.example.lrs.mapper.DocumentMapper;
import com.example.lrs.util.HashUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service @RequiredArgsConstructor
public class DocumentService {
    private final DocumentMapper mapper;
    private static final ObjectMapper om = new ObjectMapper();

    public String upsertState(String activityId, String agentJson, String registration, String stateId,
                              String contentType, byte[] content, String ifMatch, String ifNoneMatch) {
        log.debug("Upserting state - activityId: {}, stateId: {}, contentType: {}, contentSize: {}",
                activityId, stateId, contentType, content != null ? content.length : 0);
        try {
            agentJson = agentJson != null ? normalizeAgent(agentJson) : null;
            log.debug("Normalized agentJson: {}", agentJson);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            log.debug("Failed to normalize agent: {}" , e);
        }
        String agentSha = agentJson != null ? HashUtil.sha256Hex(agentJson) : null;
        String newEtag = HashUtil.sha256Hex(content);
        log.debug("Generated agentSha: {}, newEtag: {}", agentSha, newEtag);

        Map<String,Object> key = new HashMap<>();
        key.put("docType", "state");
        key.put("activityId", activityId);
        key.put("agentSha", agentSha);
        key.put("registration", registration);
        key.put("stateId", stateId);

        Map<String,Object> existing = mapper.findOne(key);
        log.debug("Existing state found: {}", existing);
        if (existing != null) {
            String currentEtag = (String) existing.get("etag");
            if (ifMatch != null && !stripQuotes(ifMatch).equals(currentEtag)) {
                throw new IllegalStateException("ETag mismatch");
            }
        } else {
            if ("*".equals(ifMatch)) throw new IllegalStateException("Precondition failed (no existing doc)");
        }

        Map<String,Object> p = new HashMap<>(key);
        p.put("agentJson", agentJson);
        p.put("contentType", contentType);
        p.put("content", content);
        p.put("etag", newEtag);
        log.debug("Upserting state with params: {}", p);
        mapper.upsert(p);
        return newEtag;
    }

    
  
public String normalizeAgent(String raw) throws Exception {
    // case 1: 이미 정상 JSON인 경우
    try {
        JsonNode n = om.readTree(raw);
        return om.writeValueAsString(n);  // 정규화 후 리턴
    } catch (Exception ignored) {}

    // case 2: \" 가 섞여 있는 경우 → 언이스케이프
    // 양끝에 큰따옴표가 붙어 있는 경우 제거
    String candidate = raw;
    if (candidate.startsWith("\"") && candidate.endsWith("\"")) {
        candidate = candidate.substring(1, candidate.length()-1);
    }

    // JSON 문자열로 한 번 감싸서 디코딩 → 역슬래시 제거
    String unescaped = om.readValue('"' +
            candidate.replace("\\", "\\\\").replace("\"", "\\\"") +
            '"', String.class);
    log.debug("Candidate: {}", candidate);
    log.debug("Unescaped agent: {}", unescaped);
    // 언이스케이프된 문자열을 다시 JSON으로 파싱
    JsonNode node = om.readTree(unescaped);
    log.debug("Normalized agent: {}", om.writeValueAsString(node));
    return om.writeValueAsString(node);
}

    public Map<String,Object> getState(String activityId, String agentJson, String registration, String stateId) {
        Map<String,Object> key = new HashMap<>();
        key.put("docType", "state");
        key.put("activityId", activityId);
        key.put("agentSha", agentJson!=null?HashUtil.sha256Hex(agentJson):null);
        key.put("registration", registration);
        key.put("stateId", stateId);
        return mapper.findOne(key);
    }

    public boolean deleteState(String activityId, String agentJson, String registration, String stateId) {
        Map<String,Object> key = new HashMap<>();
        key.put("docType", "state");
        key.put("activityId", activityId);
        key.put("agentSha", agentJson!=null?HashUtil.sha256Hex(agentJson):null);
        key.put("registration", registration);
        key.put("stateId", stateId);
        return mapper.delete(key) > 0;
    }

    private String stripQuotes(String s) {
        return s == null ? null : s.replace("\"","").trim();
    }
}
