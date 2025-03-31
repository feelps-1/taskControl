package dev.crab.dto;

import java.sql.Timestamp;

public record CardTimeReportDTO(
        Long cardId,
        String cardName,
        String fromColumnName,
        String toColumnName,
        Timestamp enteredAt,
        Timestamp leftAt
) {}
