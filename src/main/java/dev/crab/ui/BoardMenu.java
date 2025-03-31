package dev.crab.ui;

import dev.crab.dto.BlockReportDTO;
import dev.crab.dto.BoardColumnInfoDTO;
import dev.crab.dto.CardTimeReportDTO;
import dev.crab.persistence.converter.OffSetDateTimeConverter;
import dev.crab.persistence.entity.BoardColumnEntity;
import dev.crab.persistence.entity.BoardEntity;
import dev.crab.persistence.entity.CardEntity;
import dev.crab.service.*;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Scanner;

import static dev.crab.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("Bem vindo ao board %s, selecione a operação desejada\n", entity.getId());
            var option = -1;
            while (option != 10) {
                System.out.println("1 - Criar um card");
                System.out.println("2 - Mover um card");
                System.out.println("3 - Bloquear um card");
                System.out.println("4 - Desbloquear um card");
                System.out.println("5 - Cancelar um card");
                System.out.println("6 - Ver board");
                System.out.println("7 - Ver coluna com cards");
                System.out.println("8 - Ver card");
                System.out.println("9 - Ver relatório do board");
                System.out.println("10 - Voltar para o menu anterior");
                System.out.println("11 - Sair");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> getBoardReport();
                    case 10 -> System.out.println("Voltando para o menu anterior");
                    case 11 -> System.exit(0);
                    default -> System.out.println("Opção inválida, informe uma opção do menu");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void getBoardReport() throws SQLException {
        System.out.printf("Segue o relatório do board %s - %s%n", entity.getId(), entity.getName());

        try (var connection = getConnection()) {
            var boardReport = new BoardService(connection).getBoardReport(entity.getId());

            printTimeReport(boardReport.timeReport());

            printBlockReport(boardReport.blockReport());
        }
    }

    private void printTimeReport(List<CardTimeReportDTO> timeReports) {
        if (timeReports.isEmpty()) {
            System.out.println("Nenhum movimento de cartão registrado neste board.");
            return;
        }

        System.out.println("\nMovimentações dos Cartões:");

        for (CardTimeReportDTO report : timeReports) {
            printTimeReportEntry(report);
        }
    }

    private void printTimeReportEntry(CardTimeReportDTO report) {
        OffsetDateTime enteredAtOffset = OffSetDateTimeConverter.toOffsetDateTime(report.enteredAt());
        OffsetDateTime leftAtOffset = OffSetDateTimeConverter.toOffsetDateTime(report.leftAt());

        ZonedDateTime entered = toZonedDateTime(enteredAtOffset);
        ZonedDateTime left = toZonedDateTime(leftAtOffset);

        String leftAt = left != null ? left.format(FORMATTER) : "N/A (ainda na coluna)";
        Duration duration = calculateDuration(enteredAtOffset, leftAtOffset);

        System.out.printf(
                """
                        Cartão %d - %s - Da coluna %s para %s
                        Entrou em: %s | Saiu em: %s | Tempo: %s%n
                        """,
                report.cardId(),
                report.cardName(),
                report.fromColumnName(),
                report.toColumnName(),
                entered != null ? entered.format(FORMATTER) : "Data desconhecida",
                leftAt,
                formatDuration(duration)
        );
    }

    private void printBlockReport(List<BlockReportDTO> blocks) {
        System.out.println("\nRelatório de Bloqueios:");

        if (blocks.isEmpty()) {
            System.out.println("Nenhum bloqueio registrado neste board.");
            return;
        }

        for (BlockReportDTO block : blocks) {
            printBlockReportEntry(block);
        }
    }

    private void printBlockReportEntry(BlockReportDTO block) {
        String status = block.unblockedAt() != null ? "DESBLOQUEADO" : "BLOQUEADO";
        OffsetDateTime blockAtOffset = OffSetDateTimeConverter.toOffsetDateTime(block.blockedAt());
        OffsetDateTime unblockOffset = OffSetDateTimeConverter.toOffsetDateTime(block.unblockedAt());

        ZonedDateTime blockAt = toZonedDateTime(blockAtOffset);
        ZonedDateTime unblock = toZonedDateTime(unblockOffset);

        String outputBlockedAt = blockAt != null ? blockAt.format(FORMATTER) : "Data desconhecida";
        String outputUnblockedAt = unblock != null ? unblock.format(FORMATTER) : "Ainda bloqueado";
        Duration duration = calculateDuration(blockAtOffset, unblockOffset);

        System.out.printf(
                """
                        [Cartão %d - %s] %s
                        ▸ Bloqueado em: %s
                        ▸ Motivo do bloqueio: %s
                        ▸ Desbloqueio: %s
                        ▸ Motivo do desbloqueio: %s
                        ▸ Tempo total bloqueado: %s
                        ------------------------------
                        """,
                block.cardId(),
                block.cardName(),
                status,
                outputBlockedAt,
                block.blockReason(),
                outputUnblockedAt,
                block.unblockReason(),
                formatDuration(duration)
        );
    }

    private ZonedDateTime toZonedDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()) : null;
    }

    private Duration calculateDuration(OffsetDateTime start, OffsetDateTime end) {
        return start != null ? (end != null ? Duration.between(start, end) : Duration.between(start, OffsetDateTime.now())) : Duration.ZERO;
    }

    private String formatDuration(Duration duration) {
        return "%d dias, %02d:%02d".formatted(duration.toDays(), duration.toHoursPart(), duration.toMinutesPart());
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        System.out.println("Informe o título do card");
        card.setTitle(scanner.next());
        System.out.println("Informe a descrição do card");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try (var connection = getConnection()) {
            new CardService(connection).insert(card);
        }
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.println("Informe o id do card que deseja mover para a próxima coluna");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        System.out.println("Informe o id do card que será bloqueado");
        var cardId = scanner.nextLong();
        System.out.println("Informe o motivo do bloqueio do card");
        var reason = scanner.next();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        System.out.println("Informe o id do card que será desbloqueado");
        var cardId = scanner.nextLong();
        System.out.println("Informe o motivo do desbloqueio do card");
        var reason = scanner.next();
        try (var connection = getConnection()) {
            new CardService(connection).unblock(cardId, reason);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        System.out.println("Informe o id do card que deseja mover para a coluna de cancelamento");
        var cardId = scanner.nextLong();
        var cancelColumn = entity.getCancelColumn();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).cancel(cardId, cancelColumn.getId(), boardColumnsInfo);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try (var connection = getConnection()) {
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                System.out.printf("Board [%s,%s]\n", b.id(), b.name());
                b.columns().forEach(c ->
                        System.out.printf("Coluna [%s] tipo: [%s] tem %s cards\n", c.name(), c.kind(), c.cardAmount())
                );
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumnId = -1L;
        while (!columnsIds.contains(selectedColumnId)) {
            System.out.printf("Escolha uma coluna do board %s pelo id\n", entity.getName());
            entity.getBoardColumns().forEach(c -> System.out.printf("%s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumnId = scanner.nextLong();
        }
        try (var connection = getConnection()) {
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(co -> {
                System.out.printf("Coluna %s tipo %s\n", co.getName(), co.getKind());
                co.getCards().forEach(ca -> System.out.printf("Card %s - %s\nDescrição: %s\n",
                        ca.getId(), ca.getTitle(), ca.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.println("Informe o id do card que deseja visualizar");
        var selectedCardId = scanner.nextLong();
        try (var connection = getConnection()) {
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                System.out.printf("Card %s - %s.\n", c.id(), c.title());
                                System.out.printf("Descrição: %s\n", c.description());
                                System.out.println(c.blocked() ?
                                        "Está bloqueado. Motivo: " + c.blockReason() :
                                        "Não está bloqueado");
                                System.out.printf("Já foi bloqueado %s vezes\n", c.blocksAmount());
                                System.out.printf("Está no momento na coluna %s - %s\n", c.columnId(), c.columnName());
                            },
                            () -> System.out.printf("Não existe um card com o id %s\n", selectedCardId));
        }

    }
}
