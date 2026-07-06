package com.game.minigameSetup.minigames;

import com.game.minigameSetup.MiniGame;
import javafx.scene.image.Image;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class CashCatch implements MiniGame {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;

    // --- shooter (top, moves left/right on its own, fires downward) ---
    private static final double SHOOTER_Y = 30;
    private static final int SHOOTER_WIDTH = 70;
    private static final int SHOOTER_HEIGHT = 70;
    private static final double SHOOTER_BASE_SPEED = 180; // px/sec
    private double shooterX = WIDTH / 2.0 - SHOOTER_WIDTH / 2.0;
    private int shooterDir = 1; // 1 = right, -1 = left
    private double shooterSpeed = SHOOTER_BASE_SPEED;
    private double timeUntilSpeedChange = randomSpeedChangeCooldown();

    private double randomSpeedChangeCooldown() {
        return 0.5 + Math.random() * 1.5; // change every 0.5–2.0 seconds
    }

    // --- player (bottom, catches projectiles) ---
    private static final double PLAYER_Y = HEIGHT - 90;
    private static final int PLAYER_WIDTH = 80;
    private static final int PLAYER_HEIGHT = 80;
    private static final double PLAYER_SPEED = 420; // px/sec
    private double playerX = WIDTH / 2.0 - PLAYER_WIDTH / 2.0;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // --- projectiles (coins falling from the shooter) ---
    private static final double PROJECTILE_BASE_SPEED = 270; // px/sec
    private static final int PROJECTILE_WIDTH = 24;
    private static final int PROJECTILE_HEIGHT = 24;
    private final List<double[]> projectiles = new ArrayList<>(); // {x, y}

    // --- firing cadence (random interval between shots) ---
    private static final double SHOOT_INTERVAL_MIN = 0.3;
    private static final double SHOOT_INTERVAL_MAX = 1.4;
    private double timeSinceLastShot = 0;
    private double nextShootInterval = randomInterval();

    private double randomInterval() {
        return SHOOT_INTERVAL_MIN + Math.random() * (SHOOT_INTERVAL_MAX - SHOOT_INTERVAL_MIN);
    }

    // --- timer ---
    private static final int GAME_DURATION_SECONDS = 30;
    private long startTimeNanos = -1;
    private boolean gameOver = false;

    private int score = 0;

    private final Image furry;
    private final Image dragonborn;
    private final Image coin;

    public CashCatch() {
        furry = new Image(Objects.requireNonNull(getClass().getResource("/images/characters/furry.png")).toExternalForm());
        dragonborn = new Image(Objects.requireNonNull(getClass().getResource("/images/characters/dragonborn.png")).toExternalForm());
        coin = new Image(Objects.requireNonNull(getClass().getResource("/images/coin.png")).toExternalForm());
    }

    @Override
    public void update(double dt) {
        if (gameOver) return;

        if (startTimeNanos < 0) startTimeNanos = System.nanoTime();
        double elapsed = (System.nanoTime() - startTimeNanos) / 1_000_000_000.0;
        if (elapsed >= GAME_DURATION_SECONDS) {
            gameOver = true;
            return;
        }

        double difficulty = Math.min(1.0, elapsed / GAME_DURATION_SECONDS);
        double projectileSpeed = PROJECTILE_BASE_SPEED + difficulty * 210; // px/sec

        // move shooter with irregular speed, bounce off walls
        timeUntilSpeedChange -= dt;
        if (timeUntilSpeedChange <= 0) {
            timeUntilSpeedChange = randomSpeedChangeCooldown();
            double baseSpeed = SHOOTER_BASE_SPEED + difficulty * 150;
            shooterSpeed = Math.random() < 0.15
                    ? 0                                          // 15% chance: brief pause
                    : baseSpeed * (0.4 + Math.random() * 1.2);  // otherwise: 40–160% of base speed
        }
        shooterX += shooterSpeed * shooterDir * dt;
        if (shooterX <= 0) {
            shooterX = 0;
            shooterDir = 1;
        } else if (shooterX >= WIDTH - SHOOTER_WIDTH) {
            shooterX = WIDTH - SHOOTER_WIDTH;
            shooterDir = -1;
        }

        // move player, clamped to screen
        if (movingLeft) playerX -= PLAYER_SPEED * dt;
        if (movingRight) playerX += PLAYER_SPEED * dt;
        playerX = Math.max(0, Math.min(WIDTH - PLAYER_WIDTH, playerX));

        // shooter fires at irregular intervals
        timeSinceLastShot += dt;
        if (timeSinceLastShot >= nextShootInterval) {
            timeSinceLastShot = 0;
            nextShootInterval = randomInterval();
            shoot();
        }

        // move projectiles downward
        Iterator<double[]> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            double[] p = projIt.next();
            p[1] += projectileSpeed * dt;

            // caught by player?
            if (rectsOverlap(p[0], p[1], PROJECTILE_WIDTH, PROJECTILE_HEIGHT,
                    playerX, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                score += 10;
                projIt.remove();
                continue;
            }

            // missed -> just remove it, no penalty
            if (p[1] > HEIGHT) {
                projIt.remove();
            }
        }
    }

    private boolean rectsOverlap(double x1, double y1, double w1, double h1,
                                 double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private void shoot() {
        double x = shooterX + SHOOTER_WIDTH / 2.0 - PROJECTILE_WIDTH / 2.0;
        double y = SHOOTER_Y + SHOOTER_HEIGHT;
        projectiles.add(new double[]{x, y});
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.drawImage(dragonborn, shooterX, SHOOTER_Y, SHOOTER_WIDTH, SHOOTER_HEIGHT);
        gc.drawImage(furry, playerX, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (double[] p : projectiles) {
            gc.drawImage(coin, p[0], p[1], PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 20, 20);
        double remaining = startTimeNanos < 0
                ? GAME_DURATION_SECONDS
                : Math.max(0, GAME_DURATION_SECONDS - (System.nanoTime() - startTimeNanos) / 1_000_000_000.0);
        gc.fillText("Time: " + (int) Math.ceil(remaining), 20, 40);
    }

    @Override
    public void onKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT, A -> movingLeft = true;
            case RIGHT, D -> movingRight = true;
            case ESCAPE -> gameOver = true; // bail out; the scene returns to the dialogue
            default -> {}
        }
    }

    @Override
    public void onKeyRelease(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT, A -> movingLeft = false;
            case RIGHT, D -> movingRight = false;
            default -> {}
        }
    }

    public int getScore() { return score; }

    @Override
    public boolean isFinished() {
        return gameOver;
    }

    /** More coins caught = a better outcome for the story branch that follows. */
    @Override
    public String getResultTier() {
        if (score >= 200) return "HIGH";
        if (score >= 90) return "MEDIUM";
        return "LOW";
    }

    @Override
    public double getDesignWidth() { return WIDTH; }

    @Override
    public double getDesignHeight() { return HEIGHT; }
}