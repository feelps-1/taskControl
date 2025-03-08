package dev.crab.dto;

import java.sql.Timestamp;

public record CardTimeReportDTO(
        Long cardId,
        Long fromColumnId,
        Long toColumnId,
        Timestamp enteredAt,
        Timestamp leftAt,
        Long durationSeconds
) {}
