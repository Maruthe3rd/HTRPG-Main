package com.game.state;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private static final String DB_FILE_NAME = "game_save.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE_NAME;

    private static final String[] REQUIRED_TABLES = {
            "meta_timeline_flags",
            "playthrough_history",
            "paths_choices",
            "hero_state",
            "relationships",
            "inventory"
    };

    private static final String SCHEMA_SCRIPT = """
            CREATE TABLE IF NOT EXISTS meta_timeline_flags (
                flag_key    TEXT PRIMARY KEY,
                flag_value  BOOLEAN NOT NULL DEFAULT 0,
                description TEXT
            );

            CREATE TABLE IF NOT EXISTS playthrough_history (
                run_id         INTEGER PRIMARY KEY AUTOINCREMENT,
                character_name TEXT NOT NULL,
                play_order     INTEGER NOT NULL,
                achieved_end   TEXT NOT NULL,
                run_completed  BOOLEAN DEFAULT 0
            );

            CREATE TABLE IF NOT EXISTS paths_choices (
                choice_id             TEXT PRIMARY KEY,
                character_who_made_it TEXT NOT NULL,
                choice_value          TEXT NOT NULL,
                impacts_future_runs   BOOLEAN DEFAULT 1
            );

            CREATE TABLE IF NOT EXISTS hero_state (
                hero_id          TEXT PRIMARY KEY,
                current_health   INTEGER DEFAULT 100,
                current_mana     INTEGER DEFAULT 100,
                last_known_scene TEXT
            );

            CREATE TABLE IF NOT EXISTS relationships (
                npc_id              TEXT PRIMARY KEY,
                affection_level     INTEGER DEFAULT 0,
                met_in_previous_run BOOLEAN DEFAULT 0
            );

            CREATE TABLE IF NOT EXISTS inventory (
                item_id             TEXT PRIMARY KEY,
                item_name           TEXT NOT NULL,
                quantity            INTEGER DEFAULT 1,
                transcends_timeline BOOLEAN DEFAULT 0
            );
            """;

    private static final class Holder {
        private static final DatabaseManager INSTANCE = new DatabaseManager();
    }

    private final Object connectionLock = new Object();
    private volatile Connection connection;

    private DatabaseManager() {
        try {
            synchronized (connectionLock) {
                openConnection();
                initializeSchemaIfNeeded();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Fatal error while initializing " + DB_FILE_NAME, e);
            throw new DatabaseInitializationException("Could not initialize " + DB_FILE_NAME, e);
        }
    }

    public static DatabaseManager getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() {
        synchronized (connectionLock) {
            try {
                if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                    LOGGER.warning("Database connection was closed or invalid; reconnecting...");
                    openConnection();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Unable to verify or restore the database connection.", e);
                throw new DatabaseInitializationException("Lost connection to " + DB_FILE_NAME, e);
            }
            return connection;
        }
    }

    private void openConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("sqlite-jdbc driver not found on classpath.", e);
        }

        connection = DriverManager.getConnection(JDBC_URL);
        LOGGER.info(() -> "Connected to SQLite database: " + new File(DB_FILE_NAME).getAbsolutePath());

        try (Statement pragma = connection.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON;");
            pragma.execute("PRAGMA journal_mode = WAL;");
            pragma.execute("PRAGMA synchronous = NORMAL;");
        }
    }

    private void initializeSchemaIfNeeded() throws SQLException {
        if (allRequiredTablesPresent()) {
            LOGGER.info("Existing meta-timeline schema detected; skipping creation script.");
            return;
        }

        LOGGER.info("Meta-timeline schema missing or incomplete; running creation script.");
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                for (String ddl : SCHEMA_SCRIPT.split(";")) {
                    String trimmed = ddl.strip();
                    if (!trimmed.isEmpty()) {
                        statement.executeUpdate(trimmed);
                    }
                }
            }
            connection.commit();
            LOGGER.info("Database schema initialized successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to execute schema creation script; rolling back.", e);
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private boolean allRequiredTablesPresent() throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String table : REQUIRED_TABLES) {
                ps.setString(1, table);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void closeConnection() {
        synchronized (connectionLock) {
            if (connection == null) {
                return;
            }
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LOGGER.info("Database connection closed cleanly.");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error while closing the database connection.", e);
            } finally {
                connection = null;
            }
        }
    }

    public static final class DatabaseInitializationException extends RuntimeException {
        public DatabaseInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}