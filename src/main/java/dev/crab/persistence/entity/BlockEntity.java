package dev.crab.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BlockEntity {
    private Long Id;
    private OffsetDateTime blockedAt;
    private String blockReason;
    private OffsetDateTime unblockedAt;
    private String unblockReason;
}
