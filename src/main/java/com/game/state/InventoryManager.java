package com.game.state;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InventoryManager {

    private static final Logger LOGGER = Logger.getLogger(InventoryManager.class.getName());

    private final DatabaseManager databaseManager;
    private final Object operationLock = new Object();

    public InventoryManager() {
        this(DatabaseManager.getInstance());
    }

    InventoryManager(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    public void addItem(String itemId, String name, int qty, boolean transcends) throws Throwable {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(name, "name");
        String sql = """
                INSERT INTO inventory (item_id, item_name, quantity, transcends_timeline)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(item_id) DO UPDATE SET
                    item_name = excluded.item_name,
                    quantity  = inventory.quantity + excluded.quantity
                """;
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, itemId);
                ps.setString(2, name);
                ps.setInt(3, qty);
                ps.setBoolean(4, transcends);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("adding item '" + itemId + "'", e);
            }
        }
    }

    public boolean hasItem(String itemId) throws Throwable {
        Objects.requireNonNull(itemId, "itemId");
        String sql = "SELECT 1 FROM inventory WHERE item_id = ? AND quantity > 0 LIMIT 1";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                throw wrap("checking for item '" + itemId + "'", e);
            }
        }
    }

    public int getQuantity(String itemId) throws Throwable {
        Objects.requireNonNull(itemId, "itemId");
        String sql = "SELECT quantity FROM inventory WHERE item_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setString(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getInt("quantity") : 0;
                }
            } catch (SQLException e) {
                throw wrap("reading quantity for item '" + itemId + "'", e);
            }
        }
    }

    public void removeItem(String itemId, int qty) throws Throwable {
        Objects.requireNonNull(itemId, "itemId");
        String sql = "UPDATE inventory SET quantity = MAX(0, quantity - ?) WHERE item_id = ?";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                ps.setInt(1, qty);
                ps.setString(2, itemId);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw wrap("removing " + qty + " of item '" + itemId + "'", e);
            }
        }
    }

    public void clearCurrentTimelineItems() throws Throwable {
        String sql = "DELETE FROM inventory WHERE transcends_timeline = 0";
        synchronized (operationLock) {
            try (PreparedStatement ps = connection().prepareStatement(sql)) {
                int removed = ps.executeUpdate();
                LOGGER.info(() -> "Cleared " + removed + " non-transcending item(s) after bad ending.");
            } catch (SQLException e) {
                throw wrap("clearing current-timeline inventory items", e);
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