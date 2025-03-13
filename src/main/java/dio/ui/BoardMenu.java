package dio.ui;

import dio.dto.BoardColumnInfoDTO;
import dio.persistence.dao.CardDAO;
import dio.persistence.entity.BoardColumnEntity;
import dio.persistence.entity.BoardEntity;
import dio.persistence.entity.CardEntity;
import dio.service.BoardColumnQueryService;
import dio.service.BoardQueryService;
import dio.service.CardQueryService;
import dio.service.CardService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Scanner;

import static dio.persistence.config.ConnectionConfig.getConnection;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.printf("Board %s, selecione a opção desejada\n", entity.getId());
            var option = -1;
            while (option != 9) {
                System.out.println("1 - Cria um card");
                System.out.println("2 - Mover um card");
                System.out.println("3 - Bloquear card");
                System.out.println("4 - Desbloquear um card");
                System.out.println("5 - Cancalar um card");
                System.out.println("6 - Visualizar board");
                System.out.println("7 - Visualizar coluna com cards");
                System.out.println("8 - Visualizar card");
                System.out.println("9 - Voltar o menu anterior");
                System.out.println("10 - Sair");
                System.out.print("Escolha uma opção: ");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> movedCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> System.out.println("Voltando para o menu anterior");
                    case 10 -> System.exit(0);
                    default -> System.out.println("Escolha uma opção:");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void createCard() throws SQLException {
        var card = new CardEntity();
        System.out.println("Coloque o título do card");
        card.setTitle(scanner.next());
        System.out.println("Coloque a descrição do card");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try (var connection = getConnection()) {
            new CardService(connection).create(card);
        }
    }

    private void movedCardToNextColumn() throws SQLException {
        System.out.println("Coloque o id do card que será movido para a próxima coluna");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try (var connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
        } catch (RuntimeException ex){
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
        System.out.println("Coloque o id do card que será movido para a coluna de cancelamento");
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
                System.out.printf("Board %s %s\n", b.id(), b.name());
                b.columns().forEach(c ->
                        System.out.printf("Coluna %s tipo %s tem %s cards\n", c.name(), c.kind(), c.cardsAmount())
                );
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumn = -1L;
        while (!columnsIds.contains(selectedColumn)) {
            System.out.printf("Escolha uma coluna do board %s\n", entity.getName());
            entity.getBoardColumns().forEach(c -> System.out.printf("%s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumn = scanner.nextLong();
        }
        try (var connection = getConnection()) {
            var column = new BoardColumnQueryService(connection).findById(selectedColumn);
            column.ifPresent(co -> {
                System.out.printf("Coluna %s tipo %s ", co.getName(), co.getKind());
                co.getCards().forEach(ca -> System.out.printf("Card %s - %s\nDescrição: %s\n", ca.getId(), ca.getTitle(), ca.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.println("Coloque o id do card para visualizar");
        var selectedCardId = scanner.nextLong();
        try (var connection = getConnection()) {
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(c -> {
                                System.out.printf("Card %s - %s\n", c.id(), c.title());
                                System.out.printf("Descrição: %s\n", c.description());
                                System.out.println(c.blocked() ? "Está bloqueado. Motivo: " + c.blockReason() : "Não está bloqueado");
                                System.out.printf("Já foi bloqueado %s vezes\n", c.blocksAmount());
                                System.out.printf("Está na coluna %s - %s\n", c.columnId(), c.columnName());
                            },
                            () -> System.out.println("Esse card não existe\n"));
        }
    }
}
