package dio;

import dio.persistence.migration.MigrationStrategy;
import dio.ui.MainMenu;

import java.sql.SQLException;

import static dio.persistence.config.ConnectionConfig.getConnection;

public class Main {

    public static void main(String[] args) throws SQLException {

        try(var connection = getConnection()){
            new MigrationStrategy(connection).executeMigration();
        }
        new MainMenu().execute();

    }
}