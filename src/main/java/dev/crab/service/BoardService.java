package dev.crab.service;

import dev.crab.dto.BoardReportDTO;
import dev.crab.dto.CardTimeReportDTO;
import dev.crab.persistence.dao.BoardColumnDAO;
import dev.crab.persistence.dao.BoardDAO;
import dev.crab.persistence.dao.CardMovementDAO;
import dev.crab.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

@AllArgsConstructor
public class BoardService {
    private final Connection connection;

    public boolean delete(final Long id) throws SQLException {
        var dao = new BoardDAO(connection);
        try{
            if(!dao.exists(id)){
                return false;
            }
            dao.delete(id);
            connection.commit();
            return true;
        }catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public BoardEntity insert(final BoardEntity entity) throws SQLException {
        var dao = new BoardDAO(connection);
        var boardColumnDAO = new BoardColumnDAO(connection);
        try{
            dao.insert(entity);
            var columns = entity.getBoardColumns().stream().map(c -> {
                c.setBoard(entity);
                return c;
            }).toList();
            for(var column : columns) {
                boardColumnDAO.insert(column);
            }
            connection.commit();
        } catch (SQLException e){
            connection.rollback();
            throw e;
        }
        return entity;
    }

    public BoardReportDTO getBoardReport(Long boardId) throws SQLException {
        var boardReport = new BoardDAO(connection);

        var timeReport = boardReport.getBoardTimeReport(boardId);
        var blockReport = boardReport.getBoardBlockReport(boardId);

        return new BoardReportDTO(boardId, timeReport, blockReport);
    }
}
