package dev.crab.persistence.dao;

import dev.crab.persistence.entity.CardMovementEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@AllArgsConstructor
public class CardMovementDAO {
    private final Connection connection;

    public void insert(CardMovementEntity movement) throws SQLException {
        String sql = "INSERT INTO card_movements (card_id, from_column_id, to_column_id, moved_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, movement.getCardId());
            if (movement.getFromColumnId() != null) {
                stmt.setLong(2, movement.getFromColumnId());
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
            }
            stmt.setLong(3, movement.getToColumnId());
            stmt.setTimestamp(4, movement.getMovedAt());

            stmt.executeUpdate();
        }
    }
}
