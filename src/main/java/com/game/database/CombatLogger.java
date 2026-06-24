package com.game.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CombatLogger {
    // Thread pool dedicated exclusively to isolated DB writes
    private static final ExecutorService dbWorker = Executors.newSingleThreadExecutor();

    public static void logTurnAsync(String turnInfo, String actionDetails) {
        dbWorker.submit(() -> {
            String insertSQL = "INSERT INTO combat_log (turn_info, action_details) VALUES (?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

                pstmt.setString(1, turnInfo);
                pstmt.setString(2, actionDetails);
                pstmt.executeUpdate();

            } catch (Exception e) {
                System.err.println("Database write failure: " + e.getMessage());
            }
        });
    }
}