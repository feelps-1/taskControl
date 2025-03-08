package dev.crab.service;

import dev.crab.dto.BoardColumnInfoDTO;
import dev.crab.dto.CardDetailsDTO;
import dev.crab.exceptions.CardBlockedException;
import dev.crab.exceptions.EntityNotFoundException;
import dev.crab.exceptions.FinishedCardException;
import dev.crab.exceptions.UnblockedCardException;
import dev.crab.persistence.dao.BlockDAO;
import dev.crab.persistence.dao.CardDAO;
import dev.crab.persistence.dao.CardMovementDAO;
import dev.crab.persistence.entity.CardEntity;
import dev.crab.persistence.entity.CardMovementEntity;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import static dev.crab.persistence.converter.OffSetDateTimeConverter.toTimestamp;
import static dev.crab.persistence.entity.BoardColumnKindEnum.FINAL;

@AllArgsConstructor
public class CardService {
    private final Connection connection;

    public CardEntity insert(final CardEntity entity) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            dao.insert(entity);
            connection.commit();
            return entity;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void moveToNextColumn(final Long cardId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var movementDAO = new CardMovementDAO(connection);
            var dto = validateCardState(dao, cardId, boardColumnsInfo);
            var currentColumn = getCurrentColumn(dto.columnId(), boardColumnsInfo);
            var nextColumn = boardColumnsInfo.stream()
                    .filter(bc -> bc.order() == currentColumn.order() + 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("O card informado está cancelado!"));
            dao.moveToColumn(nextColumn.id(), cardId);

            var movement = new CardMovementEntity(null, cardId, dto.columnId(), nextColumn.id(), toTimestamp(OffsetDateTime.now()));
            movementDAO.insert(movement);

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void cancel(final Long cardId, final Long cancelColumnId, final List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        try {
            var dao = new CardDAO(connection);
            var movementDAO = new CardMovementDAO(connection);
            var dto = validateCardState(dao, cardId, boardColumnsInfo);
            var currentColumn = getCurrentColumn(dto.columnId(), boardColumnsInfo);
            if (currentColumn.id().equals(cancelColumnId)) {
                throw new IllegalStateException("O card já está cancelado!");
            }
            dao.moveToColumn(cancelColumnId, cardId);

            var movement = new CardMovementEntity(null, cardId, dto.columnId(), cancelColumnId, toTimestamp(OffsetDateTime.now()));
            movementDAO.insert(movement);

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        }
    }

    public void block(final Long cardId, final String blockReason, List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException{
        try{
            var dao = new CardDAO(connection);
            var dto = validateCardState(dao, cardId, boardColumnsInfo);
            var currentColumn = getCurrentColumn(dto.columnId(), boardColumnsInfo);
            var blockDAO = new BlockDAO(connection);
            blockDAO.block(blockReason, cardId);
            connection.commit();
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        }

    }

    public void unblock(final Long cardId, final String unblockReason) throws SQLException{
        try{
            var dao = new CardDAO(connection);
            var optional = dao.findById(cardId);
            var dto = optional.orElseThrow(() -> new EntityNotFoundException("O card de id %s não foi encontrado!".formatted(cardId)));
            if(!dto.blocked()){
                throw new UnblockedCardException("Não é possível desbloquear um card já desbloquado!");
            }
            var blockDAO = new BlockDAO(connection);
            blockDAO.unblock(unblockReason, cardId);
            connection.commit();
        } catch (SQLException ex){
            connection.rollback();
            throw ex;
        }
    }

    private CardDetailsDTO validateCardState(CardDAO dao, Long cardId, List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        var optional = dao.findById(cardId);
        var dto = optional.orElseThrow(() -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId)));
        if (dto.blocked()) {
            throw new CardBlockedException("O card %s está bloqueado!".formatted(cardId));
        }
        var currentColumn = getCurrentColumn(dto.columnId(), boardColumnsInfo);
        if (currentColumn.kind().equals(FINAL)) {
            throw new FinishedCardException("O card já está finalizado!");
        }
        return dto;
    }

    private BoardColumnInfoDTO getCurrentColumn(Long columnId, List<BoardColumnInfoDTO> boardColumnsInfo) {
        return boardColumnsInfo.stream()
                .filter(bc -> bc.id().equals(columnId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("O card informado não pertence a esse board!"));
    }
}
