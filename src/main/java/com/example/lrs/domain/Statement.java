
package com.example.lrs.domain;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class Statement {
    private String id;
    private String fullStatement;
    private String verbId;
    private String actorAccountName;
    private String activityId;
    private String registration;
    private boolean voided;
    private OffsetDateTime stored;
}
