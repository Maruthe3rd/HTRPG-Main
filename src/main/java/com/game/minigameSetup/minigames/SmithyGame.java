package com.game.minigameSetup.minigames;

import com.game.minigameSetup.MiniGame;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import static com.game.minigameSetup.ShakeEffect.shakeScreen;

/**
 * Hammer-timing minigame. Re-themed to match the rest of the game's
 * retro-terminal look (see DialogueBox/DialogueView/PolicySwiper) and
 * re-laid-out for the real 1920x1080 virtual canvas (GameScaler.VIRTUAL_WIDTH/
 * HEIGHT) instead of the old ~800x600 coordinates, which drew everything
 * small and huddled in the top-left corner of the screen.
 */
public class SmithyGame implements MiniGame {

    // --- shared game theme (matches DialogueView/BasicTemplate/PolicySwiper) ---
    private static final String ACCENT_GREEN     = "#00FF66";
    private static final String ACCENT_GREEN_DIM = "#4F9E6C";
    private static final String ACCENT_GOLD      = "#FFC94D";
    private static final String ACCENT_BLUE      = "#39B6FF";
    private static final String ACCENT_RED       = "#FF4C4C";
    private static final String BG_COLOR         = "#101010";
    private static final String PANEL_BG         = "rgba(10,12,10,0.78)";

    private static final Font TITLE_FONT_SRC = Font.loadFont(
            SmithyGame.class.getResource("/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf").toExternalForm(), 150);
    private static final Font BODY_FONT_SRC = Font.loadFont(
            SmithyGame.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(), 75);

    private static Font titleFont(double size) {
        return (TITLE_FONT_SRC != null) ? Font.font(TITLE_FONT_SRC.getFamily(), size) : Font.font("Monospace", size);
    }

    private static Font bodyFont(double size) {
        return (BODY_FONT_SRC != null) ? Font.font(BODY_FONT_SRC.getFamily(), size) : Font.font("Monospace", size);
    }

    private double timer = 0; // seconds remaining for the hit-feedback display
    private boolean hammerTime = false;
    private boolean showResult = false;
    private String resultText = "";
    private String resultColor = ACCENT_GREEN;

    // --- layout, designed for the real 1920x1080 virtual canvas ---
    private static final double TRACK_MIN = 460;
    private static final double TRACK_MAX = 1460;
    private static final double TRACK_Y = 820;
    private static final double TRACK_HEIGHT = 54;

    private static final double PERFECT_WIDTH = 84;
    private static final double OKAY_WIDTH = 420;

    private static final double MARKER_WIDTH = 18;

    private static final double DWARF_X = 660, DWARF_Y = 140, DWARF_W = 600, DWARF_H = 450;

    private double markerX = TRACK_MIN;
    private double markerSpeed = 500; // px/sec

    // current center of the target zones, randomized on each space press
    private double zoneCenter = (TRACK_MIN + TRACK_MAX) / 2.0;

    // how much the marker speed increases after every strike (px/sec)
    private static final int speedUp = 100;

    private int totalScore = 0; // tracks how good the sword ends up
    private int strikes = 0;    // how many times player has pressed space

    private static final int MAX_STRIKES = 10;
    private boolean finished = false;

    private final Image dwarfDown;
    private final Image dwarfUp;

    private final Node screen;

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
    public double getDesignWidth() { return 1920.0; }

    @Override
    public double getDesignHeight() { return 1080.0; }

    @Override
    public void update(double dt) {
        markerX += markerSpeed * dt;
        if (markerX > TRACK_MAX || markerX < TRACK_MIN) markerSpeed *= -1;

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

    @Override
    public String getResultTier() {
        int maxPossible = MAX_STRIKES * 3;
        if (totalScore >= maxPossible * 0.8) return "HIGH";
        if (totalScore >= maxPossible * 0.35) return "MEDIUM";
        return "LOW";
    }

    @Override
    public void draw(GraphicsContext gc) {
        double w = getDesignWidth();

        gc.setFill(Color.web(BG_COLOR));
        gc.fillRect(0, 0, w, getDesignHeight());

        drawHeader(gc, w);
        drawDwarf(gc);
        drawTrack(gc);
        drawResult(gc, w);
    }

    private void drawHeader(GraphicsContext gc, double w) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        gc.setFill(Color.web(ACCENT_GREEN));
        gc.setFont(titleFont(54));
        gc.fillText("SMITHY", w / 2.0, 40);

        gc.setFill(Color.web(ACCENT_GREEN_DIM));
        gc.setFont(bodyFont(26));
        gc.fillText("PRESS SPACE TO STRIKE", w / 2.0, 108);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web(ACCENT_GOLD));
        gc.fillText("SWORD QUALITY: " + totalScore, 60, 40);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.web(ACCENT_GREEN_DIM));
        gc.fillText("STRIKES: " + strikes + " / " + MAX_STRIKES, w - 60, 40);
    }

    private void drawDwarf(GraphicsContext gc) {
        Image sprite = hammerTime ? dwarfDown : dwarfUp;
        if (sprite == null) return;

        gc.drawImage(sprite, DWARF_X, DWARF_Y, DWARF_W, DWARF_H);
    }

    private void drawTrack(GraphicsContext gc) {
        double okayStart = zoneCenter - OKAY_WIDTH / 2;
        double perfectStart = zoneCenter - PERFECT_WIDTH / 2;

        gc.setFill(Color.web(PANEL_BG));
        gc.fillRoundRect(TRACK_MIN - 30, TRACK_Y - 30, (TRACK_MAX - TRACK_MIN) + 60, TRACK_HEIGHT + 60, 24, 24);

        gc.setFill(Color.web("#2A2A2A"));
        gc.fillRoundRect(TRACK_MIN, TRACK_Y, TRACK_MAX - TRACK_MIN, TRACK_HEIGHT, 10, 10);

        gc.setFill(Color.web(ACCENT_GOLD, 0.85));
        gc.fillRect(okayStart, TRACK_Y, OKAY_WIDTH, TRACK_HEIGHT);

        gc.setFill(Color.web(ACCENT_GREEN));
        gc.fillRect(perfectStart, TRACK_Y, PERFECT_WIDTH, TRACK_HEIGHT);

        gc.setStroke(Color.web(ACCENT_GREEN_DIM));
        gc.setLineWidth(2);
        gc.strokeRoundRect(TRACK_MIN, TRACK_Y, TRACK_MAX - TRACK_MIN, TRACK_HEIGHT, 10, 10);

        // the moving marker
        gc.setFill(Color.web(ACCENT_BLUE));
        gc.fillRoundRect(markerX - MARKER_WIDTH / 2.0, TRACK_Y - 10, MARKER_WIDTH, TRACK_HEIGHT + 20, 6, 6);
    }

    private void drawResult(GraphicsContext gc, double w) {
        if (!showResult) return;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        //gc.setEffect(new Glow(0.7));
        gc.setFill(Color.web(resultColor));
        gc.setFont(titleFont(48));
        gc.fillText(resultText, w / 2.0, TRACK_Y - 90);
        gc.setEffect(null);
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
        timer = 1.0;
        strikes++;

        double perfectStart = zoneCenter - PERFECT_WIDTH / 2;
        double perfectEnd = zoneCenter + PERFECT_WIDTH / 2;
        double okayStart = zoneCenter - OKAY_WIDTH / 2;
        double okayEnd = zoneCenter + OKAY_WIDTH / 2;

        if (markerX >= perfectStart && markerX <= perfectEnd) {
            resultText = "PERFECT!";
            resultColor = ACCENT_GREEN;
            totalScore += 3;
            shakeScreen(screen, 16, 0.25);
        } else if (markerX >= okayStart && markerX <= okayEnd) {
            resultText = "OKAY!";
            resultColor = ACCENT_GOLD;
            totalScore += 1;
            shakeScreen(screen, 5, 0.25);
        } else {
            resultText = "MISS!";
            resultColor = ACCENT_RED;
            shakeScreen(screen, 2, 0.25);
        }
        // moves coloured bar
        double minCenter = TRACK_MIN + OKAY_WIDTH / 2;
        double maxCenter = TRACK_MAX - OKAY_WIDTH / 2;

        zoneCenter = minCenter + new java.util.Random().nextDouble() * (maxCenter - minCenter);

        // gas gas gas
        if (markerSpeed > 0) markerSpeed += speedUp;
        if (markerSpeed < 0) markerSpeed -= speedUp;
    }
}