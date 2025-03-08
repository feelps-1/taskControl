package dev.crab.dto;

import java.sql.Timestamp;

public record BlockReportDTO(
        Long cardId,
        Timestamp blockedAt,
        String blockReason,
        Timestamp unblockedAt,
        String unblockReason,
        Long durationSeconds
) {}