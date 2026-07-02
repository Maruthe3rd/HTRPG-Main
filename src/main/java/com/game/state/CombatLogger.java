package com.game.state;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CombatLogger {
    private static final ExecutorService dbWorker = Executors.newSingleThreadExecutor();

    public static void logTurnAsync(String turnInfo, String actionDetails) {
        dbWorker.submit(() -> {

            System.out.println("Logged to DB: " + turnInfo);
        });
    }
}