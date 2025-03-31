package dev.crab.dto;

import java.sql.Timestamp;

public record BlockReportDTO(
        Long cardId,
        String cardName,
        Timestamp blockedAt,
        String blockReason,
        Timestamp unblockedAt,
        String unblockReason
) {}