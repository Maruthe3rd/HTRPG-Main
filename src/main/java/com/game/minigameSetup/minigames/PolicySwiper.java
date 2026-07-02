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

    int publicOpinion = 0;
    //policies
    public record Policy(String policy, int approvalEffect) {}

    private final List<Policy> policies = List.of(
            new Policy("Increase accessibility of schooling for disadvantaged minorities", 5),
            new Policy("Lower taxes for the rich", -3),
            new Policy("Alcoholic beverages are now taxed", -2),
            new Policy("Raise the minimum wage nationwide", 4),
            new Policy("Conscript peasants into the royal army", -6));

    private int currentIndex = 0; //counts at which policy we are

    //where the card is
    private static final double CARD_CENTER_X = 400;
    private static final double CARD_CENTER_Y = 260;
    private static final double CARD_WIDTH = 420;
    private static final double CARD_HEIGHT = 220;

    //buttons (visual only tho -- arrow keys do the actual thing)
    private static final double BUTTON_RADIUS = 40;
    private static final double X_BUTTON_X = 300, X_BUTTON_Y = 480;
    private static final double CHECK_BUTTON_X = 500, CHECK_BUTTON_Y = 480;

    // swipe animation state - help. ouch.
    private boolean swiping = false;
    private double swipeProgress = 0;   // 0 -> 1
    private int swipeDirection = 0;     // -1 = left/reject, +1 = right/accept
    private double cardOffsetX = 0;
    private double cardRotation = 0;
    private double cardAlpha = 1;

    private int acceptedCount = 0;
    private int rejectedCount = 0;

    private boolean finished = false;

    public PolicySwiper() {
    }

    public int getAcceptedCount() { return acceptedCount; }
    public int getRejectedCount() { return rejectedCount; }

    @Override
    public void update(double dt) {
        if (!swiping) return;

        swipeProgress += 3.0 * dt; // full swipe-out animation takes ~1/3 second
        double eased = swipeProgress * (2 - swipeProgress); // ease-out, starts fast then settles

        cardOffsetX = swipeDirection * eased * 900;
        cardRotation = swipeDirection * eased * 20;
        cardAlpha = Math.max(0, 1 - swipeProgress);

        if (swipeProgress >= 1.0) {
            advanceCard();
        }
        if (getAcceptedCount() + getRejectedCount() == policies.size()) finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
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
        if (swiping) return; // ignore input mid-animation
        swiping = true;
        swipeDirection = direction;
        swipeProgress = 0;
        if (direction > 0) acceptedCount++; else rejectedCount++;
    }

    @Override
    public void onKeyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.LEFT) {
            startSwipe(-1); // reject
            publicOpinion -= policies.get(currentIndex).approvalEffect();
        } else if (e.getCode() == KeyCode.RIGHT) {
            startSwipe(1);  // accept
            publicOpinion += policies.get(currentIndex).approvalEffect();
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        // header
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font(18));
        gc.fillText("Policy Swiper", 20, 20);
        gc.fillText("Rejected: " + rejectedCount + "   Accepted: " + acceptedCount, 20, 45);
        gc.fillText("<- Reject        Accept ->", 20, 70);

        // --- card --- all design with assistance for now
        gc.save();
        gc.setGlobalAlpha(cardAlpha);
        gc.translate(CARD_CENTER_X + cardOffsetX, CARD_CENTER_Y);
        gc.rotate(cardRotation);

        gc.setFill(Color.web("#f5e6c8"));
        gc.fillRoundRect(-CARD_WIDTH / 2, -CARD_HEIGHT / 2, CARD_WIDTH, CARD_HEIGHT, 20, 20);
        gc.setStroke(Color.web("#8a6d3b"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(-CARD_WIDTH / 2, -CARD_HEIGHT / 2, CARD_WIDTH, CARD_HEIGHT, 20, 20);

        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        Font cardFont = Font.font(20);
        gc.setFont(cardFont);

        List<String> lines = wrapText(policies.get(currentIndex).policy, cardFont, CARD_WIDTH - 40);
        double lineHeight = 26;
        double startY = -((lines.size() - 1) * lineHeight) / 2.0;
        for (int i = 0; i < lines.size(); i++) {
            gc.fillText(lines.get(i), 0, startY + i * lineHeight);
        }
        gc.restore();

        // --- buttons (visual reference for what each arrow key does) ---
        gc.setGlobalAlpha(1.0);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font(28));

        gc.setFill(Color.web("#c0392b"));
        gc.fillOval(X_BUTTON_X - BUTTON_RADIUS, X_BUTTON_Y - BUTTON_RADIUS, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
        gc.setFill(Color.WHITE);
        gc.fillText("X", X_BUTTON_X, X_BUTTON_Y);

        gc.setFill(Color.web("#27ae60"));
        gc.fillOval(CHECK_BUTTON_X - BUTTON_RADIUS, CHECK_BUTTON_Y - BUTTON_RADIUS, BUTTON_RADIUS * 2, BUTTON_RADIUS * 2);
        gc.setFill(Color.WHITE);
        gc.fillText("\u2713", CHECK_BUTTON_X, CHECK_BUTTON_Y);

    }



    /** Splits text into lines that fit within maxWidth, using the given font for measurement. */
    private List<String> wrapText(String text, Font font, double maxWidth) {
        List<String> lines = new ArrayList<>();
        Text measurer = new Text();
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