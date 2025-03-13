package dio.ui;

import dio.persistence.entity.BoardColumnEntity;
import dio.persistence.entity.BoardColumnKindEnum;
import dio.persistence.entity.BoardEntity;
import dio.service.BoardQueryService;
import dio.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static dio.persistence.config.ConnectionConfig.getConnection;
import static dio.persistence.entity.BoardColumnKindEnum.*;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void execute() throws SQLException {
        System.out.println("Bem vindo ao gerenciador de boards");
        var option = -1;
        while(true){
            System.out.println("1 - Cria um board");
            System.out.println("2 - Selecionar um board");
            System.out.println("3 - Excluir um board");
            System.out.println("4 - Sair");
            System.out.print("Escolha uma opção: ");
            option = scanner.nextInt();
            switch (option){
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> System.out.println("Escolha uma opção:");
            }
        }
    }

    private void createBoard() throws SQLException{
        var entity = new BoardEntity();
        System.out.println("Coloque o nome do board:");
        entity.setName(scanner.next());

        System.out.println("O board terá mais colunas? Se sim informe quantas, senão digite '0");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.println("Informe o nome da board:");
        var initialColumnName = scanner.next();
        var initialColumn = createColumn(initialColumnName, INITIAL, 0);
        columns.add(initialColumn);

        for(int i = 0; i < additionalColumns;i++){
            System.out.println("informe o nome da board");
            var pendingColumnName = scanner.next();
            var pendingColumn = createColumn(pendingColumnName, PENDING, i + 1);
            columns.add(pendingColumn);
        }

        System.out.println("informe o nome da última board");
        var lastColumnName = scanner.next();
        var lastColumn = createColumn(lastColumnName, INITIAL, additionalColumns + 1);
        columns.add(lastColumn);

        System.out.println("informe o nome da board de cancelamento");
        var cancelColumnName = scanner.next();
        var cancelColumn = createColumn(cancelColumnName, CANCEL, additionalColumns + 21);
        columns.add(cancelColumn);

        entity.setBoardColumns(columns);
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            service.insert(entity);
        }

    }

    private void selectBoard() throws SQLException {
        System.out.println("Coloque o id do board selecionado");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                            () -> System.out.println("Board não encontrado")
            );
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.println("Coloque o id que será excluido");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            if(service.delete(id)){
                System.out.printf("Board %s excluido\n",id);
            }
            else{
                System.out.println("Board não encontrado\n");
            }
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
