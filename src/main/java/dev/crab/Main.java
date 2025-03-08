package dev.crab;

import dev.crab.persistence.migration.MigrationStrategy;
import dev.crab.ui.MainMenu;

import java.sql.SQLException;

import static dev.crab.persistence.config.ConnectionConfig.getConnection;

public class Main {
    public static void main(String[] args) throws SQLException {

        try (var connection = getConnection()){
            new MigrationStrategy(connection).executeMigration();
        }

        new MainMenu().execute();
    }
}