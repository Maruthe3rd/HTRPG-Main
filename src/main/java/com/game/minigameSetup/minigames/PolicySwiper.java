package com.game.minigameSetup.minigames;

import com.game.minigameSetup.MiniGame;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class PolicySwiper implements MiniGame {

    private static final String ACCENT_GREEN     = "#00FF66";
    private static final String ACCENT_GREEN_DIM = "#4F9E6C";
    private static final String ACCENT_RED       = "#FF4C4C";
    private static final String ACCENT_RED_DIM   = "#9E4F4F";
    private static final String BG_COLOR         = "#101010";
    private static final String PANEL_BG         = "rgba(10,12,10,0.78)";

    private static final Font TITLE_FONT_SRC = Font.loadFont(
            PolicySwiper.class.getResource("/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf").toExternalForm(), 150);
    private static final Font BODY_FONT_SRC = Font.loadFont(
            PolicySwiper.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(), 75);

    private static Font titleFont(double size) {
        return (TITLE_FONT_SRC != null) ? Font.font(TITLE_FONT_SRC.getFamily(), size) : Font.font("Monospace", size);
    }

    private static Font bodyFont(double size) {
        return (BODY_FONT_SRC != null) ? Font.font(BODY_FONT_SRC.getFamily(), size) : Font.font("Monospace", size);
    }

    int publicOpinion = 0;
    //policies
    public record Policy(String policy, int approvalEffect) {}

    private final List<Policy> policies = List.of(
            new Policy("Den Zugang zur Schulbildung für benachteiligte Minderheiten verbessern", 5),
            new Policy("Niedrigere Steuern für die Reichen", -3),
            new Policy("Alkoholische Getränke werden nun besteuert", -2),
            new Policy("Den Mindestlohn landesweit anheben", 4),
            new Policy("Bauern in die königliche Armee einziehen", -6));

    private int currentIndex = 0; //counts at which policy we are

    private static final double CARD_CENTER_X = 960;
    private static final double CARD_CENTER_Y = 500;
    private static final double CARD_WIDTH = 900;
    private static final double CARD_HEIGHT = 420;

    //buttons (visual only tho -- arrow keys do the actual thing)
    private static final double BUTTON_RADIUS = 70;
    private static final double X_BUTTON_X = 800, X_BUTTON_Y = 830;
    private static final double CHECK_BUTTON_X = 1120, CHECK_BUTTON_Y = 830;

    // swipe animation state - help. ouch.
    private boolean swiping = false;
    private double swipeProgress = 0;   // 0 -> 1
    private int swipeDirection = 0;     // -1 = left/reject, +1 = right/accept
    private double cardOffsetX = 0;
    private double cardRotation = 0;
    private double cardAlpha = 1;
    private String resultTier = "LOW";

    private int acceptedCount = 0;
    private int rejectedCount = 0;

    private boolean finished = false;

    public PolicySwiper() {
    }

    public int getAcceptedCount() { return acceptedCount; }
    public int getRejectedCount() { return rejectedCount; }

    @Override
    public double getDesignWidth() { return 1920.0; }

    @Override
    public double getDesignHeight() { return 1080.0; }

    @Override
    public void update(double dt) {
        if (!swiping) return;

        swipeProgress += 3.0 * dt;
        double eased = swipeProgress * (2 - swipeProgress);

        cardOffsetX = swipeDirection * eased * 1400;
        cardRotation = swipeDirection * eased * 20;
        cardAlpha = Math.max(0, 1 - swipeProgress);

        if (swipeProgress >= 1.0) {
            advanceCard();
        }
        if (getAcceptedCount() + getRejectedCount() == policies.size()) finished = true;
    }

    @Override
    public boolean isFinished() {
        if (getAcceptedCount() + getRejectedCount() == policies.size()) {
            finished = true;
            resultTier = getResultTier();
        }
        return finished;
    }

    @Override
    public String getResultTier() {
        int bestPossible = policies.stream().mapToInt(p -> Math.abs(p.approvalEffect())).sum();
        if (publicOpinion >= bestPossible * 0.6) return "HIGH";
        if (publicOpinion >= 0) return "MEDIUM";
        return "LOW";
    }

    private void advanceCard() {
        currentIndex = (currentIndex + 1) % policies.size();
        cardOffsetX = 0;
        cardRotation = 0;
        cardAlpha = 1;
        swiping = false;
        swipeProgress = 0;
    }

    private void startSwipe(int direction) {
        if (swiping) return;
        swiping = true;
        swipeDirection = direction;
        swipeProgress = 0;
        if (direction > 0) acceptedCount++; else rejectedCount++;
    }

    @Override
    public void onKeyPress(KeyEvent e) {
        if (swiping || finished) return; // ignore input mid-animation so opinion isn't counted twice

        if (e.getCode() == KeyCode.LEFT) {
            publicOpinion -= policies.get(currentIndex).approvalEffect();
            startSwipe(-1); // reject
        } else if (e.getCode() == KeyCode.RIGHT) {
            publicOpinion += policies.get(currentIndex).approvalEffect();
            startSwipe(1);  // accept
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        double w = getDesignWidth();

        gc.setFill(Color.web(BG_COLOR));
        gc.fillRect(0, 0, w, getDesignHeight());

        drawHeader(gc, w);
        drawApprovalMeter(gc, w);
        drawCard(gc);
        drawButtons(gc);
    }

    private void drawHeader(GraphicsContext gc, double w) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);

        gc.setFill(Color.web(ACCENT_GREEN));
        gc.setFont(titleFont(54));
        gc.fillText("POLICY SWIPER", w / 2.0, 40);

        gc.setFill(Color.web(ACCENT_GREEN_DIM));
        gc.setFont(bodyFont(26));
        gc.fillText("\u2190 REJECT          ACCEPT \u2192", w / 2.0, 108);

        // card progress + tally, top corners like an in-world HUD
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.web(ACCENT_RED_DIM));
        gc.fillText("REJECTED: " + rejectedCount, 60, 40);

        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(Color.web(ACCENT_GREEN_DIM));
        gc.fillText("ACCEPTED: " + acceptedCount, w - 60, 40);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.web(ACCENT_GREEN_DIM));
        gc.setFont(bodyFont(20));
        gc.fillText("BILL " + (currentIndex + 1) + " / " + policies.size(), w / 2.0, 150);
    }

    // small bar showing where public opinion currently sits, purely for feedback --
    // keeps the exact per-policy numbers hidden so the swiper stays a gut-call, not a spreadsheet.
    private void drawApprovalMeter(GraphicsContext gc, double w) {
        int worst = policies.stream().mapToInt(p -> Math.abs(p.approvalEffect())).sum();
        double meterW = 500, meterH = 18;
        double meterX = w / 2.0 - meterW / 2.0, meterY = 178;

        gc.setFill(Color.web("#000000", 0.55));
        gc.fillRoundRect(meterX, meterY, meterW, meterH, 10, 10);

        double clamped = Math.max(-worst, Math.min(worst, publicOpinion));
        double fillFrac = (clamped + worst) / (2.0 * worst); // 0..1, 0.5 = neutral
        gc.setFill(Color.web(clamped >= 0 ? ACCENT_GREEN : ACCENT_RED));
        gc.fillRoundRect(meterX, meterY, meterW * fillFrac, meterH, 10, 10);

        gc.setStroke(Color.web(ACCENT_GREEN_DIM));
        gc.setLineWidth(2);
        gc.strokeRoundRect(meterX, meterY, meterW, meterH, 10, 10);
    }

    private void drawCard(GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(cardAlpha);
        gc.translate(CARD_CENTER_X + cardOffsetX, CARD_CENTER_Y);
        gc.rotate(cardRotation);

        gc.setFill(Color.web(PANEL_BG));
        gc.fillRoundRect(-CARD_WIDTH / 2, -CARD_HEIGHT / 2, CARD_WIDTH, CARD_HEIGHT, 28, 28);

        gc.setStroke(Color.web(ACCENT_GREEN));
        gc.setLineWidth(4);
        gc.strokeRoundRect(-CARD_WIDTH / 2, -CARD_HEIGHT / 2, CARD_WIDTH, CARD_HEIGHT, 28, 28);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        Font cardFont = bodyFont(34);
        gc.setFont(cardFont);

        List<String> lines = wrapText(policies.get(currentIndex).policy(), cardFont, CARD_WIDTH - 100);
        double lineHeight = 44;
        double startY = -((lines.size() - 1) * lineHeight) / 2.0;
        for (int i = 0; i < lines.size(); i++) {
            gc.fillText(lines.get(i), 0, startY + i * lineHeight);
        }
        gc.restore();
    }

    private void drawButtons(GraphicsContext gc) {
        gc.setGlobalAlpha(1.0);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(titleFont(46));

        gc.setFill(Color.web("#1a0000"));
        gc.fillOval(X_BUTTON_X - BUTTON_RADIUS, X_BUTTON_Y - BUTTON_RADIUS, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
        gc.setStroke(Color.web(ACCENT_RED));
        gc.setLineWidth(4);
        gc.strokeOval(X_BUTTON_X - BUTTON_RADIUS, X_BUTTON_Y - BUTTON_RADIUS, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
        gc.setFill(Color.web(ACCENT_RED));
        gc.fillText("X", X_BUTTON_X, X_BUTTON_Y + 4);

        gc.setFill(Color.web("#001a08"));
        gc.fillOval(CHECK_BUTTON_X - BUTTON_RADIUS, CHECK_BUTTON_Y - BUTTON_RADIUS, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
        gc.setStroke(Color.web(ACCENT_GREEN));
        gc.setLineWidth(4);
        gc.strokeOval(CHECK_BUTTON_X - BUTTON_RADIUS, CHECK_BUTTON_Y - BUTTON_RADIUS, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
        gc.setFill(Color.web(ACCENT_GREEN));
        gc.fillText("\u2713", CHECK_BUTTON_X, CHECK_BUTTON_Y + 4);
    }

    private final Text measurer = new Text(); // reused across frames to avoid per-frame allocation

    /** Splits text into lines that fit within maxWidth, using the given font for measurement. */
    private List<String> wrapText(String text, Font font, double maxWidth) {
        List<String> lines = new ArrayList<>();
        measurer.setFont(font);

        StringBuilder currentLine = new StringBuilder();
        for (String word : text.split(" ")) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            measurer.setText(testLine);
            double width = measurer.getLayoutBounds().getWidth();

            if (width > maxWidth && !currentLine.isEmpty()) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }
}