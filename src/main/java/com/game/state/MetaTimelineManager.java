package com.game.state;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MetaTimelineManager {

    private static final Logger LOGGER = Logger.getLogger(MetaTimelineManager.class.getName());

    public static final String DRAGONBORN = "Dragonborn";
    public static final String DWARF = "Dwarf";
    public static final String RABBIT_GIRL = "Rabbit-Girl";

    private static final String[] ALL_CHARACTERS = {DRAGONBORN, DWARF, RABBIT_GIRL};

    private final DatabaseManager databaseManager;
    private final Object operationLock = new Object();

    public MetaTimelineManager() {
        this(DatabaseManager.getInstance());
    }

    MetaTimelineManager(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    public boolean hasCharacterCompletedRun(String characterName, String expectedEnd) {
        Objects.requireNonNull(characterName, "characterName");
        Objects.requireNonNull(expectedEnd, "expectedEnd");

        String sql = """
                SELECT 1
                FROM playthrough_history
                WHERE character_name = ?
                  AND achieved_end = ?
                  AND run_completed = 1
                LIMIT 1
                """;

        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, characterName);
                ps.setString(2, expectedEnd);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw wrap("checking whether " + characterName + " reached '" + expectedEnd + "'", e);
            }
        }
    }

    public int getNextPlayOrder() {
        String sql = "SELECT COUNT(*) FROM playthrough_history WHERE run_completed = 1";

        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int completedRuns = rs.next() ? rs.getInt(1) : 0;
                return Math.min(completedRuns + 1, ALL_CHARACTERS.length);
            } catch (SQLException e) {
                throw wrap("determining the next play order", e);
            }
        }
    }

    public void recordChoice(String choiceId, String character, String value) {
        Objects.requireNonNull(choiceId, "choiceId");
        Objects.requireNonNull(character, "character");
        Objects.requireNonNull(value, "value");

        String sql = """
                INSERT INTO paths_choices (choice_id, character_who_made_it, choice_value, impacts_future_runs)
                VALUES (?, ?, ?, 1)
                ON CONFLICT(choice_id) DO UPDATE SET
                    character_who_made_it = excluded.character_who_made_it,
                    choice_value = excluded.choice_value
                """;

        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, choiceId);
                ps.setString(2, character);
                ps.setString(3, value);
                ps.executeUpdate();
                LOGGER.fine(() -> "Recorded choice '" + choiceId + "' by " + character + " = " + value);
            } catch (SQLException e) {
                throw wrap("recording choice '" + choiceId + "'", e);
            }
        }
    }

    public String getPreviousChoiceValue(String choiceId) {
        Objects.requireNonNull(choiceId, "choiceId");

        String sql = "SELECT choice_value FROM paths_choices WHERE choice_id = ?";

        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, choiceId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("choice_value") : null;
                }
            } catch (SQLException e) {
                throw wrap("reading choice '" + choiceId + "'", e);
            }
        }
    }

    public boolean isTrueEndUnlocked() {
        synchronized (operationLock) {
            try {
                return haveAllCharactersReachedAnEnd() && allStrategicFlagsSatisfied();
            } catch (SQLException e) {
                throw wrap("evaluating true-end eligibility", e);
            }
        }
    }

    private boolean haveAllCharactersReachedAnEnd() throws SQLException {
        String sql = """
                SELECT COUNT(DISTINCT character_name)
                FROM playthrough_history
                WHERE character_name IN (?, ?, ?)
                  AND run_completed = 1
                """;

        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            for (int i = 0; i < ALL_CHARACTERS.length; i++) {
                ps.setString(i + 1, ALL_CHARACTERS[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == ALL_CHARACTERS.length;
            }
        }
    }

    private boolean allStrategicFlagsSatisfied() throws SQLException {
        String sql = "SELECT COUNT(*) FROM meta_timeline_flags WHERE flag_value = 0";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return !rs.next() || rs.getInt(1) == 0;
        }
    }

    private Connection connection() {
        return databaseManager.getConnection();
    }

    private static MetaTimelineException wrap(String action, SQLException cause) {
        LOGGER.log(Level.SEVERE, "Database error while " + action, cause);
        return new MetaTimelineException("Database error while " + action, cause);
    }

    public static final class MetaTimelineException extends RuntimeException {
        public MetaTimelineException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}