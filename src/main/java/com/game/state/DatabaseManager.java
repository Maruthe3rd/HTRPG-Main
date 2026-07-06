package com.game.state;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
            "inventory",
            "explored_nodes"
    };

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
                String schema = loadSchemaScript();

                for (String ddl : schema.split(";")) {
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

    public void setMetaFlag(String key, boolean value) {
        String sql = """
        INSERT INTO meta_timeline_flags(flag_key, flag_value)
        VALUES(?, ?)
        ON CONFLICT(flag_key)
        DO UPDATE SET flag_value = excluded.flag_value;
    """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setBoolean(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Records a finished run. {@code play_order} is derived from how many runs
     * were already completed, and {@code endingKey} is the real ending reached
     * (e.g. METAKEY_DWARF_GOOD_END) so the end screens can report what happened.
     */
    public void savePlaythrough(String character, String endingKey) {
        String sql = """
        INSERT INTO playthrough_history(character_name, play_order, achieved_end, run_completed)
        VALUES(?, (SELECT COUNT(*) FROM playthrough_history WHERE run_completed = 1) + 1, ?, 1)
    """;

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, character);
            ps.setString(2, endingKey);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** All finished runs, oldest first, for the continue/end overview screens. */
    public List<PlaythroughRecord> getCompletedPlaythroughs() {
        String sql = """
        SELECT character_name, achieved_end, play_order
        FROM playthrough_history
        WHERE run_completed = 1
        ORDER BY run_id
    """;

        List<PlaythroughRecord> records = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                records.add(new PlaythroughRecord(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    /** Distinct character display-names that have at least one finished run. */
    public Set<String> getCompletedCharacters() {
        String sql = "SELECT DISTINCT character_name FROM playthrough_history WHERE run_completed = 1";

        Set<String> characters = new LinkedHashSet<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                characters.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return characters;
    }

    /**
     * Starts a fresh loop: forgets which characters were played so they become
     * selectable again, while deliberately <em>keeping</em> the meta-timeline
     * flags — that accumulated memory is what a new play-order builds upon.
     */
    public void resetPlaythroughHistory() {
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM playthrough_history");
            LOGGER.info("Playthrough history cleared for a new timeline (meta flags kept).");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasCharacterCompleted(String character) {
        String sql = "SELECT 1 FROM playthrough_history WHERE character_name = ? AND run_completed = 1 LIMIT 1";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, character);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** A finished run: which character, which ending key they reached, and the play order. */
    public record PlaythroughRecord(String character, String endingKey, int playOrder) {}

    /** Records that a story node has been visited (for the end-of-game timeline map). */
    public void markExplored(String storyFile, String nodeId) {
        if (storyFile == null || nodeId == null) return;
        String sql = "INSERT OR IGNORE INTO explored_nodes(story_file, node_id) VALUES(?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, storyFile);
            ps.setString(2, nodeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** All visited nodes as "storyFile#nodeId" keys (matches StoryGraph node keys). */
    public Set<String> getExploredNodes() {
        String sql = "SELECT story_file, node_id FROM explored_nodes";
        Set<String> keys = new LinkedHashSet<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                keys.add(rs.getString(1) + "#" + rs.getString(2));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }

    public boolean hasMetaFlag(String key) {
        String sql = "SELECT flag_value FROM meta_timeline_flags WHERE flag_key = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMetaFlag(String key) {
        String sql = "SELECT flag_value FROM meta_timeline_flags WHERE flag_key = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static final class DatabaseInitializationException extends RuntimeException {
        public DatabaseInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private String loadSchemaScript() {
        try (InputStream in = getClass().getResourceAsStream("/SQL/schema.sql")) {
            if (in == null) throw new RuntimeException("schema.sql not found");

            return new String(in.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}