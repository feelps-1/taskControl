package dev.crab.persistence.dao;

import com.mysql.cj.jdbc.StatementImpl;
import dev.crab.dto.BlockReportDTO;
import dev.crab.dto.CardTimeReportDTO;
import dev.crab.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class BoardDAO {
    private final Connection connection;

    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        var sql = "INSERT INTO boards (name) values (?);";
        try(var statement = connection.prepareStatement(sql)){
            statement.setString(1, entity.getName());
            statement.executeUpdate();
            if(statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;
    }

    public void delete(final Long id) throws SQLException {
        var sql = "DELETE FROM boards where id = ?";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
             statement.executeUpdate();
        }
    }

    public Optional<BoardEntity> findByID(final Long id) throws SQLException {
        var sql = "SELECT id, name FROM boards where id = ?";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if(resultSet.next()){
                var entity = new BoardEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                return Optional.of(entity);
            }
            return Optional.empty();
        }
    }

    public boolean exists(final Long id) throws SQLException {
        var sql = "SELECT 1 FROM boards where id = ?";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, id);
            statement.executeQuery();
            return statement.getResultSet().next();
        }
    }

    public List<CardTimeReportDTO> getBoardTimeReport(Long boardId) throws SQLException {
        String sql = """
                        SELECT cm.card_id,\s
                           c.title AS card_name,
                           bc_from.name AS from_column_name,\s
                           bc_to.name AS to_column_name,
                           cm.moved_at AS entered_at,
                           LEAD(cm.moved_at) OVER (PARTITION BY cm.card_id ORDER BY cm.moved_at) AS left_at
                        FROM card_movements cm
                        JOIN cards c ON cm.card_id = c.id
                        JOIN boards_columns bc_from ON cm.from_column_id = bc_from.id
                        JOIN boards_columns bc_to ON cm.to_column_id = bc_to.id
                        WHERE c.board_column_id IN (SELECT id FROM boards_columns WHERE board_id = ?)
                        ORDER BY cm.card_id, cm.moved_at;
                    """;

        List<CardTimeReportDTO> report = new ArrayList<>();
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, boardId);
            stmt.executeQuery();
            var rs = stmt.getResultSet();
            while (rs.next()) {
                report.add(new CardTimeReportDTO(
                        rs.getLong("card_id"),
                        rs.getString("card_name"),
                        rs.getString("from_column_name"),
                        rs.getString("to_column_name"),
                        rs.getTimestamp("entered_at"),
                        rs.getTimestamp("left_at")
                ));
            }
        }
        return report;
    }


    public List<BlockReportDTO> getBoardBlockReport(Long boardId) throws SQLException {
        String sql = """
                SELECT
                  b.card_id,
                  c.title AS card_name,
                  b.blocked_at,
                  b.block_reason,
                  b.unblocked_at,
                  b.unblock_reason
              FROM blocks b
              JOIN cards c ON b.card_id = c.id
              JOIN boards_columns bc ON c.board_column_id = bc.id
              WHERE bc.board_id = ?
              ORDER BY b.blocked_at;
            """;
        List<BlockReportDTO> report = new ArrayList<>();
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, boardId);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                report.add(new BlockReportDTO(
                        rs.getLong("card_id"),
                        rs.getString("card_name"),
                        rs.getTimestamp("blocked_at"),
                        rs.getString("block_reason"),
                        rs.getTimestamp("unblocked_at"),
                        rs.getString("unblock_reason")
                ));
            }
        }
        return report;
    }
}
