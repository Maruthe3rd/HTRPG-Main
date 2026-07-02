package com.game.state;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RelationshipManager {

    private static final Logger LOGGER = Logger.getLogger(RelationshipManager.class.getName());

    private static final int DEFAULT_AFFECTION = 0;

    private final DatabaseManager databaseManager;
    private final Object operationLock = new Object();

    public RelationshipManager() {
        this(DatabaseManager.getInstance());
    }

    RelationshipManager(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    public void adjustAffection(String npcId, int delta) throws Throwable {
        Objects.requireNonNull(npcId, "npcId");
        String sql = """
                INSERT INTO relationships (npc_id, affection_level)
                VALUES (?, ?)
                ON CONFLICT(npc_id) DO UPDATE SET
                    affection_level = relationships.affection_level + excluded.affection_level
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, npcId);
                ps.setInt(2, delta);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("adjusting affection for npc '" + npcId + "' by " + delta, e);
            }
        }
    }

    public int getAffection(String npcId) throws Throwable {
        Objects.requireNonNull(npcId, "npcId");
        String sql = "SELECT affection_level FROM relationships WHERE npc_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, npcId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt("affection_level") : DEFAULT_AFFECTION;
                }
            } catch (SQLException e) {
                throw wrap("reading affection for npc '" + npcId + "'", e);
            }
        }
    }

    public void markMetInPreviousRun(String npcId, boolean met) throws Throwable {
        Objects.requireNonNull(npcId, "npcId");
        String sql = """
                INSERT INTO relationships (npc_id, met_in_previous_run)
                VALUES (?, ?)
                ON CONFLICT(npc_id) DO UPDATE SET met_in_previous_run = excluded.met_in_previous_run
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, npcId);
                ps.setBoolean(2, met);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("setting met_in_previous_run for npc '" + npcId + "'", e);
            }
        }
    }

    public boolean hasMetInPreviousRun(String npcId) throws Throwable {
        Objects.requireNonNull(npcId, "npcId");
        String sql = "SELECT met_in_previous_run FROM relationships WHERE npc_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, npcId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getBoolean("met_in_previous_run");
                }
            } catch (SQLException e) {
                throw wrap("reading met_in_previous_run for npc '" + npcId + "'", e);
            }
        }
    }

    private Connection connection() {
        return databaseManager.getConnection();
    }

    private static Throwable wrap(String action, SQLException cause) {
        LOGGER.log(Level.SEVERE, "Database error while " + action, cause);
        return new Throwable("Database error while " + action, cause);
    }
}