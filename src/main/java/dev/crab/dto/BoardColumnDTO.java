package dev.crab.dto;

import dev.crab.persistence.entity.BoardColumnKindEnum;

public record BoardColumnDTO(Long id, String name, BoardColumnKindEnum kind, int cardAmount) {
}
