
package com.example.lrs.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

public class JsonUtil {
    private static final ObjectMapper om = new ObjectMapper();
    public static JsonNode parse(String json) throws Exception { return om.readTree(json); }
    public static String ensureStatementId(JsonNode stmt, String suppliedId) {
        if (suppliedId != null && !suppliedId.isBlank()) return suppliedId;
        JsonNode id = stmt.get("id");
        if (id != null && id.isTextual()) return id.asText();
        return UUID.randomUUID().toString();
    }
}
