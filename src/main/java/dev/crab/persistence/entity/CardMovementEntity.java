package dev.crab.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class CardMovementEntity {
    private Long id;
    private Long cardId;
    private Long fromColumnId;
    private Long toColumnId;
    private Timestamp movedAt;
}
