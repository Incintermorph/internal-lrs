
package com.example.lrs.api;

import com.example.lrs.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/xapi/activities/state")
@RequiredArgsConstructor
public class ActivitiesStateController {

    private final DocumentService svc;

    @PutMapping
    public ResponseEntity<Void> putState(
            @RequestParam String activityId,
            @RequestParam String agent,
            @RequestParam String stateId,
            @RequestParam(required=false) String registration,
            @RequestHeader(value="Content-Type") String contentType,
            @RequestHeader(value="If-Match", required=false) String ifMatch,
            @RequestHeader(value="If-None-Match", required=false) String ifNoneMatch,
            @RequestBody byte[] content
    ) {
        log.info("PUT /xapi/activities/state - activityId: {}, stateId: {}, contentType: {}, contentSize: {}",
                activityId, stateId, contentType, content != null ? content.length : 0);
        String etag = svc.upsertState(activityId, agent, registration, stateId, contentType, content, ifMatch, ifNoneMatch);

        return ResponseEntity.noContent().eTag('"'+etag+'"').build();
    }

    @PostMapping
    public ResponseEntity<Void> postState(
            @RequestParam String activityId,
            @RequestParam String agent,
            @RequestParam String stateId,
            @RequestParam(required=false) String registration,
            @RequestHeader(value="Content-Type") String contentType,
            @RequestHeader(value="If-Match", required=false) String ifMatch,
            @RequestHeader(value="If-None-Match", required=false) String ifNoneMatch,
            @RequestBody byte[] content
    ) {
        String etag = svc.upsertState(activityId, agent, registration, stateId, contentType, content, ifMatch, ifNoneMatch);
        return ResponseEntity.noContent().eTag('"'+etag+'"').build();
    }

    @GetMapping
    public ResponseEntity<byte[]> getState(
            @RequestParam String activityId,
            @RequestParam String agent,
            @RequestParam String stateId,
            @RequestParam(required=false) String registration
    ) {
        Map<String,Object> doc = svc.getState(activityId, agent, registration, stateId);
        if (doc == null) return ResponseEntity.notFound().build();
        String ct = (String) doc.get("content_type");
        byte[] content = (byte[]) doc.get("content");
        String etag = (String) doc.get("etag");
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, '"'+etag+'"')
                .contentType(MediaType.parseMediaType(ct))
                .body(content);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteState(
            @RequestParam String activityId,
            @RequestParam String agent,
            @RequestParam String stateId,
            @RequestParam(required=false) String registration
    ) {
        boolean ok = svc.deleteState(activityId, agent, registration, stateId);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
