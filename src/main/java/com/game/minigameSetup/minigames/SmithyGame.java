package com.game.minigameSetup.minigames;

import com.game.minigameSetup.MiniGame;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import static com.game.minigameSetup.ShakeEffect.shakeScreen;

public class SmithyGame implements MiniGame {

    private double timer = 0; // seconds remaining for the hit-feedback display
    private boolean hammerTime = false;
    private boolean showResult = false;
    private String resultText = "";

    private double markerX = 100;
    private double markerSpeed = 300; // px/sec

    // bounds the marker travels between (track stays fixed, zones move within it)
    private static final int TRACK_MIN = 100;
    private static final int TRACK_MAX = 700;

    // width of the zones, kept constant; only their position (zoneCenter) changes
    private static final int PERFECT_WIDTH = 50;
    private static final int OKAY_WIDTH = 250;

    // current center of the target zones, randomized on each space press
    private int zoneCenter = 400;

    // how much the marker speed increases after every strike (px/sec)
    private static final int speedUp = 60;

    private int totalScore = 0; // tracks how good the sword ends up
    private int strikes = 0;    // how many times player has pressed space

    private static final int MAX_STRIKES = 10;
    private boolean finished = false;

    private final Image dwarfDown;
    private final Image dwarfUp;

    private final Node screen; // the node to shake on a good hit (your GamePanel canvas)

    public SmithyGame(Node screen) {
        this.screen = screen;
        dwarfDown = loadSprite("/dwarfArmDown.png");
        dwarfUp = loadSprite("/dwarfArmUp.png");
    }

    // /dwarfArmDown.png and /dwarfArmUp.png (the hammer-swing frames) add later!
    private Image loadSprite(String resourcePath) {
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            url = getClass().getResource("/images/characters/dwarf.png");
        }
        return (url != null) ? new Image(url.toExternalForm()) : null;
    }

    public int getTotalScore() { return totalScore; }
    public int getStrikes() { return strikes; }

    @Override
    public void update(double dt) {
        markerX += markerSpeed * dt;
        if (markerX > 700 || markerX < 100) markerSpeed *= -1;

        if (timer > 0) {
            timer -= dt;
        } else {
            hammerTime = false;
            showResult = false;
        }
        if (strikes >= MAX_STRIKES) finished = true; // signal the host scene to move on
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    // 3 pts/strike max (Perfect), MAX_STRIKES strikes -> up to 30 points total.
    @Override
    public String getResultTier() {
        int maxPossible = MAX_STRIKES * 3;
        if (totalScore >= maxPossible * 0.8) return "HIGH";
        if (totalScore >= maxPossible * 0.35) return "MEDIUM";
        return "LOW";
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Coloured Zones
        int okayStart = zoneCenter - OKAY_WIDTH / 2;
        int perfectStart = zoneCenter - PERFECT_WIDTH / 2;

        gc.setFill(Color.DARKGRAY); gc.fillRect(TRACK_MIN, 500, TRACK_MAX - TRACK_MIN, 40);
        gc.setFill(Color.ORANGE);   gc.fillRect(okayStart, 500, OKAY_WIDTH, 40);
        gc.setFill(Color.GREEN);    gc.fillRect(perfectStart, 500, PERFECT_WIDTH, 40);

        // the moving marker
        gc.setFill(Color.BLACK);
        gc.fillRect(markerX, 500, 10, 40);

        // Dwarf img
        if (hammerTime) {
            gc.drawImage(dwarfDown, 200, 200, 400, 300);
        } else {
            gc.drawImage(dwarfUp, 200, 200, 400, 300);
        }

        // hit or miss txt
        if (showResult) {
            gc.setFill(Color.GREEN);
            gc.fillText(resultText, 400, 150);
        }

        // counter for hits n stuff (add maximum and stuff later)
        gc.setFill(Color.WHITE);
        gc.fillText("Sword quality: " + totalScore, 20, 30);
        gc.fillText("Strikes: " + strikes, 20, 50);
    }

    @Override
    public void onKeyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.SPACE) {
            checkResult();
        }
    }

    private void checkResult() {
        showResult = true;
        hammerTime = true;
        timer = 1.0; // seconds the hit-feedback stays on screen
        strikes++;

        int perfectStart = zoneCenter - PERFECT_WIDTH / 2;
        int perfectEnd = zoneCenter + PERFECT_WIDTH / 2;
        int okayStart = zoneCenter - OKAY_WIDTH / 2;
        int okayEnd = zoneCenter + OKAY_WIDTH / 2;

        if (markerX >= perfectStart && markerX <= perfectEnd) {
            resultText = "Perfect!";
            totalScore += 3;
            shakeScreen(screen, 16, 0.25);
        } else if (markerX >= okayStart && markerX <= okayEnd) {
            resultText = "Okay!";
            totalScore += 1;
            shakeScreen(screen, 5, 0.25);
        } else {
            resultText = "Miss!";
            shakeScreen(screen, 2, 0.25);
        }
        // moves coloured bar
        int minCenter = TRACK_MIN + OKAY_WIDTH / 2;
        int maxCenter = TRACK_MAX - OKAY_WIDTH / 2;
        //decides new middle where everything moves relatively to
        zoneCenter = new java.util.Random().nextInt(minCenter, maxCenter + 1);

        // gas gas gas
        if (markerSpeed > 0) markerSpeed += speedUp;
        if (markerSpeed < 0) markerSpeed -= speedUp;
    }
}