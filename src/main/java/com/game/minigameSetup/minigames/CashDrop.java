package com.game.minigameSetup.minigames;

import javafx.application.Platform;
import javafx.scene.image.Image;
import com.game.minigameSetup.MiniGame;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.*;

public class CashDrop implements MiniGame {

    private static final int WIDTH = 1920; //1920
    private static final int HEIGHT = 1080; //1800

    // --- player ---
    private static final double PLAYER_Y = 40;          // fixed vertical position near the top
    private static final int PLAYER_WIDTH = 100;
    private static final int PLAYER_HEIGHT = 30;
    private static final double PLAYER_SPEED = 360; // px/sec

    private double playerX = WIDTH / 2.0 - PLAYER_WIDTH / 2.0;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // --- projectiles (travel downward, toward the crowd) ---
    private static final double PROJECTILE_SPEED = 720; // px/sec
    private static final int PROJECTILE_WIDTH = 24;
    private static final int PROJECTILE_HEIGHT = 24;
    private final List<double[]> projectiles = new ArrayList<>(); // {x, y}

    // --- shooting cooldown ---
    private static final double SHOOT_COOLDOWN_SECONDS = 18.0 / 60.0; // ~0.3s
    private double timeSinceLastShot = SHOOT_COOLDOWN_SECONDS; // ready to fire immediately

    // --- crowd (travels upward, toward the player) ---
    private static final double SPAWN_INTERVAL_SECONDS = 30.0 / 60.0; // ~0.7s
    private double timeSinceLastSpawn = 0;
    private static final int MAX_CROWD_SIZE = 40; // population cap

    private static final int PERSON_WIDTH = 60;
    private static final int PERSON_HEIGHT = 60;
    private static final double CROWD_SPEED = 18; // px/sec the crowd creeps up
    private static final double KNOCKBACK_AMOUNT = 50; // how far a hit person gets shoved back down
    private static final int PERSON_HEALTH = 2;
    private final List<double[]> crowd = new ArrayList<>(); // {x, y, health}

    // --- survival timer ---
    private static final int SURVIVE_SECONDS = 30;
    private long startTimeNanos = -1;
    private boolean gameOver = false;
    private boolean won = false;

    private int score = 0;

    private final Image dragonBorn;
    private final Random random = new Random();
    private final Image[] dwarfSprites;

    private final Image coin;

    private  boolean spaceHeld;

    public CashDrop() {
        spawnPerson();

        dragonBorn = new Image(Objects.requireNonNull(getClass().getResource("/images/characters/dragonborn.png")).toExternalForm());

        dwarfSprites = new Image[]{
                new Image(Objects.requireNonNull(getClass().getResource("/images/characters/attackers/Dwarf_0.png")).toExternalForm()),
                new Image(Objects.requireNonNull(getClass().getResource("/images/characters/attackers/Dwarf_1.png")).toExternalForm()),
                new Image(Objects.requireNonNull(getClass().getResource("/images/characters/attackers/Dwarf_2.png")).toExternalForm())
        };
        coin = new Image(Objects.requireNonNull(getClass().getResource("/images/coin.png")).toExternalForm());

    }

    private static final int CROWD_SIZE = 24;
    private static final double SPAWN_Y_MIN = HEIGHT - 200; // spawn band near the bottom
    private static final double SPAWN_Y_MAX = HEIGHT - 20;
    private static final double MIN_SPACING = 26; // avoid spawning two people on top of each other

    private void spawnPerson() {
        if (crowd.size() >= MAX_CROWD_SIZE) return;

        int attempts = 0;
        while (attempts < 30) {
            attempts++;
            double x = Math.random() * (WIDTH - PERSON_WIDTH);
            double y = SPAWN_Y_MIN + Math.random() * (SPAWN_Y_MAX - SPAWN_Y_MIN);

            boolean tooClose = false;
            for (double[] person : crowd) {
                double dx = person[0] - x;
                double dy = person[1] - y;
                if (Math.sqrt(dx * dx + dy * dy) < MIN_SPACING) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                int spriteIndex = random.nextInt(3);
                crowd.add(new double[]{x, y, PERSON_HEALTH, spriteIndex});
                return;


            }
        }
    }

    @Override
    public void update(double dt) {
        timeSinceLastShot += dt;
        if (spaceHeld) shoot();
        if (gameOver) return;



        if (startTimeNanos < 0) startTimeNanos = System.nanoTime();
        double elapsed = (System.nanoTime() - startTimeNanos) / 1_000_000_000.0;
        if (elapsed >= SURVIVE_SECONDS) {
            gameOver = true;
            won = true;
            return;
        }

        // move player, clamped to screen
        if (movingLeft) playerX -= PLAYER_SPEED * dt;
        if (movingRight) playerX += PLAYER_SPEED * dt;
        playerX = Math.max(0, Math.min(WIDTH - PLAYER_WIDTH, playerX));

        // move projectiles downward, remove once they pass the bottom edge
        Iterator<double[]> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            double[] p = projIt.next();
            p[1] += PROJECTILE_SPEED * dt;
            if (p[1] > HEIGHT) projIt.remove();
        }

        // crowd creeps upward
        for (double[] person : crowd) {
            person[1] -= CROWD_SPEED * dt;
        }

        // projectile vs person collisions
        for (double[] p : projectiles) {
            for (double[] person : crowd) {
                if (person[2] <= 0) continue; // already dead
                if (rectsOverlap(p[0], p[1], PROJECTILE_WIDTH, PROJECTILE_HEIGHT,
                        person[0], person[1], PERSON_WIDTH, PERSON_HEIGHT)) {
                    person[2] -= 1;
                    person[1] += KNOCKBACK_AMOUNT;
                    p[1] = HEIGHT + 1;
                    if (person[2] <= 0) score += 10;
                }
            }
        }
        projectiles.removeIf(p -> p[1] > HEIGHT);
        crowd.removeIf(person -> person[2] <= 0);

        timeSinceLastSpawn += dt;
        if (timeSinceLastSpawn >= SPAWN_INTERVAL_SECONDS) {
            timeSinceLastSpawn = 0;
            spawnPerson();
        }

        // lose if anyone reaches the player's row
        for (double[] person : crowd) {
            if (person[1] <= PLAYER_Y + PLAYER_HEIGHT) {
                gameOver = true;
                won = false;
                break;
            }
        }
    }

    private boolean rectsOverlap(double x1, double y1, double w1, double h1,
                                 double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.drawImage(dragonBorn, playerX, PLAYER_Y, 80, 80);

        for (double[] p : projectiles) {
            gc.drawImage(coin, p[0], p[1], PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        for (double[] person : crowd) {
            int spriteIndex = (int) person[3];
            gc.drawImage(
                    dwarfSprites[spriteIndex],
                    person[0],
                    person[1],
                    PERSON_WIDTH,
                    PERSON_HEIGHT
            );
        }

        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 20, 20);
        double remaining = startTimeNanos < 0
                ? SURVIVE_SECONDS
                : Math.max(0, SURVIVE_SECONDS - (System.nanoTime() - startTimeNanos) / 1_000_000_000.0);
        gc.fillText("Time: " + (int) Math.ceil(remaining), 20, 40);

        if (gameOver) {
            gc.setFill(won ? Color.LIME : Color.RED);
            gc.fillText(won ? "Survived! Final score: " + score : "Overrun! Final score: " + score, 280, 300);
        }
    }

    @Override
    public void onKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT, A -> movingLeft = true;
            case RIGHT, D -> movingRight = true;
            case SPACE -> spaceHeld = true;
            case ESCAPE -> Platform.exit();
            default -> {}
        }
    }

    @Override
    public void onKeyRelease(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT, A -> movingLeft = false;
            case RIGHT, D -> movingRight = false;
            case SPACE -> spaceHeld = false;
            default -> {}
        }
    }

    private void shoot() {
        if (gameOver) return;
        if (timeSinceLastShot < SHOOT_COOLDOWN_SECONDS) return;
        timeSinceLastShot = 0;

        double x = playerX + PLAYER_WIDTH / 2.0 - PROJECTILE_WIDTH / 2.0;
        double y = PLAYER_Y + PLAYER_HEIGHT; // spawn just below the player
        projectiles.add(new double[]{x, y});
    }

    public int getScore() { return score; }

    @Override
    public boolean isFinished() {
        return gameOver;
    }
}