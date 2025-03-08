package dev.crab.persistence.dao;

import com.mysql.cj.jdbc.StatementImpl;
import dev.crab.dto.BoardColumnDTO;
import dev.crab.persistence.entity.BoardColumnEntity;
import dev.crab.persistence.entity.CardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.crab.persistence.entity.BoardColumnKindEnum.findByName;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class BoardColumnDAO {
    private final Connection connection;

    public BoardColumnEntity insert(final BoardColumnEntity entity) throws SQLException{
        var sql = "INSERT INTO boards_columns (name, column_order, type, board_id) values (?, ?, ?, ?);";
        try (var statement = connection.prepareStatement(sql)){
            var i = 1;
            statement.setString(i ++, entity.getName());
            statement.setInt(i ++, entity.getOrder());
            statement.setString(i ++, entity.getKind().name());
            statement.setLong(i, entity.getBoard().getId());
            statement.executeUpdate();
            if(statement instanceof StatementImpl impl){
                entity.setId(impl.getLastInsertID());
            }
        }
        return entity;
    }

    public List<BoardColumnEntity> findByBoardId(Long boardId) throws SQLException{
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = "SELECT id, name, column_order, type FROM boards_columns WHERE board_id = ? ORDER BY column_order";
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while(resultSet.next()){
                var entity = new BoardColumnEntity();
                entity.setId(resultSet.getLong("id"));
                entity.setName(resultSet.getString("name"));
                entity.setOrder(resultSet.getInt("column_order"));
                entity.setKind(findByName(resultSet.getString("type")));
                entities.add(entity);
            }
            return entities;
        }catch (SQLException e){
            throw e;
        }

    }

    public List<BoardColumnDTO> findByBoardIdWithDetails(Long boardId) throws SQLException{
        List<BoardColumnDTO> dtos = new ArrayList<>();
        var sql = """
                  SELECT bc.id,
                         bc.name,
                         bc.type,
                         (SELECT COUNT(c.id)
                                        FROM cards c
                                        WHERE board_column_id = bc.id) cards_amount
                  FROM boards_columns bc
                  WHERE board_id = ?
                  ORDER BY column_order
                  """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            while(resultSet.next()){
                var dto = new BoardColumnDTO(
                        resultSet.getLong("bc.id"),
                        resultSet.getString("bc.name"),
                        findByName(resultSet.getString("bc.type")),
                        resultSet.getInt("cards_amount")
                );

                dtos.add(dto);
            }
            return dtos;
        }catch (SQLException e){
            throw e;
        }

    }

    public Optional<BoardColumnEntity> findById(Long boardId) throws SQLException{
        List<BoardColumnEntity> entities = new ArrayList<>();
        var sql = """
                  SELECT bc.name,
                         bc.type,
                         c.id,
                         c.title,
                         c.description
                  FROM boards_columns bc
                  LEFT JOIN cards c
                  ON c.board_column_id = bc.id
                  WHERE bc.id = ?
                  """;
        try(var statement = connection.prepareStatement(sql)){
            statement.setLong(1, boardId);
            statement.executeQuery();
            var resultSet = statement.getResultSet();
            if(resultSet.next()){
                var entity = new BoardColumnEntity();
                entity.setName(resultSet.getString("bc.name"));
                entity.setKind(findByName(resultSet.getString("bc.type")));
                do{
                    if(isNull(resultSet.getString("c.title"))){
                        break;
                    }
                    var card = new CardEntity();
                    card.setId(resultSet.getLong("c.id"));
                    card.setTitle(resultSet.getString("c.title"));
                    card.setDescription(resultSet.getString("c.description"));
                    entity.getCards().add(card);
                }while(resultSet.next());
                return Optional.of(entity);
            }

            return Optional.empty();
        }catch (SQLException e){
            throw e;
        }

    }

}
