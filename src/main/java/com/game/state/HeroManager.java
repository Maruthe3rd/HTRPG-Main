package com.game.state;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HeroManager {

    private static final Logger LOGGER = Logger.getLogger(HeroManager.class.getName());

    private static final int DEFAULT_HEALTH = 100;
    private static final int DEFAULT_MANA = 100;

    private final DatabaseManager databaseManager;
    private final Object operationLock = new Object();

    public HeroManager() {
        this(DatabaseManager.getInstance());
    }

    HeroManager(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    public record HeroSnapshot(String heroId, int health, int mana, String currentScene) {}

    public void setHealth(String heroId, int health) throws Throwable {
        requireHeroId(heroId);
        String sql = """
                INSERT INTO hero_state (hero_id, current_health)
                VALUES (?, ?)
                ON CONFLICT(hero_id) DO UPDATE SET current_health = excluded.current_health
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                ps.setInt(2, health);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("setting health for hero '" + heroId + "'", e);
            }
        }
    }

    public int getHealth(String heroId) throws Throwable {
        requireHeroId(heroId);
        String sql = "SELECT current_health FROM hero_state WHERE hero_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt("current_health") : DEFAULT_HEALTH;
                }
            } catch (SQLException e) {
                throw wrap("reading health for hero '" + heroId + "'", e);
            }
        }
    }

    public void adjustHealth(String heroId, int delta) throws Throwable {
        requireHeroId(heroId);
        String sql = """
                INSERT INTO hero_state (hero_id, current_health)
                VALUES (?, MAX(0, ? + ?))
                ON CONFLICT(hero_id) DO UPDATE SET
                    current_health = MAX(0, hero_state.current_health + ?)
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                ps.setInt(2, DEFAULT_HEALTH);
                ps.setInt(3, delta);
                ps.setInt(4, delta);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("adjusting health for hero '" + heroId + "' by " + delta, e);
            }
        }
    }

    public void setMana(String heroId, int mana) throws Throwable {
        requireHeroId(heroId);
        String sql = """
                INSERT INTO hero_state (hero_id, current_mana)
                VALUES (?, ?)
                ON CONFLICT(hero_id) DO UPDATE SET current_mana = excluded.current_mana
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                ps.setInt(2, mana);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("setting mana for hero '" + heroId + "'", e);
            }
        }
    }

    public int getMana(String heroId) throws Throwable {
        requireHeroId(heroId);
        String sql = "SELECT current_mana FROM hero_state WHERE hero_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt("current_mana") : DEFAULT_MANA;
                }
            } catch (SQLException e) {
                throw wrap("reading mana for hero '" + heroId + "'", e);
            }
        }
    }

    public void adjustMana(String heroId, int delta) throws Throwable {
        requireHeroId(heroId);
        String sql = """
                INSERT INTO hero_state (hero_id, current_mana)
                VALUES (?, MAX(0, ? + ?))
                ON CONFLICT(hero_id) DO UPDATE SET
                    current_mana = MAX(0, hero_state.current_mana + ?)
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                ps.setInt(2, DEFAULT_MANA);
                ps.setInt(3, delta);
                ps.setInt(4, delta);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("adjusting mana for hero '" + heroId + "' by " + delta, e);
            }
        }
    }

    public void setCurrentScene(String heroId, String sceneJsonId) throws Throwable {
        requireHeroId(heroId);
        Objects.requireNonNull(sceneJsonId, "sceneJsonId");
        String sql = """
                INSERT INTO hero_state (hero_id, last_known_scene)
                VALUES (?, ?)
                ON CONFLICT(hero_id) DO UPDATE SET last_known_scene = excluded.last_known_scene
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                ps.setString(2, sceneJsonId);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("setting current scene for hero '" + heroId + "'", e);
            }
        }
    }

    public String getCurrentScene(String heroId) throws Throwable {
        requireHeroId(heroId);
        String sql = "SELECT last_known_scene FROM hero_state WHERE hero_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getString("last_known_scene") : null;
                }
            } catch (SQLException e) {
                throw wrap("reading current scene for hero '" + heroId + "'", e);
            }
        }
    }

    public Optional<HeroSnapshot> getHeroSnapshot(String heroId) throws Throwable {
        requireHeroId(heroId);
        String sql = "SELECT current_health, current_mana, last_known_scene FROM hero_state WHERE hero_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, heroId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new HeroSnapshot(
                            heroId,
                            rs.getInt("current_health"),
                            rs.getInt("current_mana"),
                            rs.getString("last_known_scene")));
                }
            } catch (SQLException e) {
                throw wrap("reading state snapshot for hero '" + heroId + "'", e);
            }
        }
    }

    private static void requireHeroId(String heroId) {
        Objects.requireNonNull(heroId, "heroId");
    }

    private Connection connection() {
        return databaseManager.getConnection();
    }

    private static Throwable wrap(String action, SQLException cause) {
        LOGGER.log(Level.SEVERE, "Database error while " + action, cause);
        return new Throwable("Database error while " + action, cause);
    }
}