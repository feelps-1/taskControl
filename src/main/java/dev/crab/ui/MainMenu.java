package dev.crab.ui;

import dev.crab.persistence.entity.BoardColumnEntity;
import dev.crab.persistence.entity.BoardColumnKindEnum;
import dev.crab.persistence.entity.BoardEntity;
import dev.crab.service.BoardQueryService;
import dev.crab.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static dev.crab.persistence.config.ConnectionConfig.getConnection;
import static dev.crab.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public void execute() throws SQLException {
        System.out.println("Bem vindo ao gerenciador de boards! Escolha a opção desejada: ");

        var option = -1;

        while (true){
            System.out.println("1 - Criar um novo board");
            System.out.println("2 - Selecionar um board");
            System.out.println("3 - Excluir um board");
            System.out.println("4 - Sair");
            option = scanner.nextInt();
            switch (option){
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> System.out.println("Opção inválida! Selecione outra");
            }
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("Informe o ID do Board que será excluido: ");
        var id = scanner.nextLong();

        try(var connection = getConnection()){
            var service = new BoardService(connection);
            if(service.delete(id)){
                System.out.printf("O board %s foi excluído\n", id);
            } else {
                System.out.printf("Não foi encontrado um board com o id %s", id);
            }
        }
    }

    private void selectBoard() throws SQLException {
        System.out.println("Informe o id do board que deseja selecionar");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findByID(id);
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                    () -> System.out.printf("Não foi encontrado um board com id %s \n", id)
            );
        }
    }

    private void createBoard() throws SQLException {
        var entity = new BoardEntity();
        System.out.println("Informe o nome do seu board: ");
        entity.setName(scanner.next());

        System.out.println("Seu board terá mais colunas que o padrão(3)? Se sim, informe quantas, senão digite 0");

        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da coluna inicial: ");
        var initialColumnName = scanner.next();
        var initialColumn = createColumn(initialColumnName, INITIAL, 0);
        columns.add(initialColumn);

        for (int i = 0; i < additionalColumns; i++) {
            System.out.println("Informe o nome da coluna pendente: ");
            var pendingColumnName = scanner.next();
            var pendingColumn = createColumn(pendingColumnName, PENDING, i+1);
            columns.add(pendingColumn);
        }

        System.out.println("Informe o nome da coluna final: ");
        var finalColumnName = scanner.next();
        var finalColumn = createColumn(finalColumnName, FINAL, additionalColumns+1);
        columns.add(finalColumn);

        System.out.println("Informe o nome da coluna de cancelamento: ");
        var canceledColumnName = scanner.next();
        var canceledColumn = createColumn(canceledColumnName, CANCEL, additionalColumns+2);
        columns.add(canceledColumn);

        entity.setBoardColumns(columns);
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            service.insert(entity);
        }
    }

    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order){
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setKind(kind);
        boardColumn.setOrder(order);
        return boardColumn;
    }
}
