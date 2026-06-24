package com.game.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CombatView extends AnchorPane {

    // --- CONTROLLER CALLBACK HOOKS ---
    private Consumer<Double> onPhysicalAttackHook;        // Passes drawing accuracy (0.0 to 1.0)
    private BiConsumer<Double, Boolean> onMagicAttackHook; // Passes (speedMultiplier, isCritical)
    private Runnable onConversationHook;                  // Pacifist trigger
    private Runnable onFleeHook;                          // Flee trigger

    // --- UI LAYOUT NODES ---
    private final TextArea fightLogArea = new TextArea();
    private final StackPane dynamicUiViewport = new StackPane();
    private final ImageView enemySpriteViewer = new ImageView();

    // Typeracer Internal State
    private Timeline spellTimer;
    private String normalTargetWord = "";
    private String critTargetWord = "";
    private int currentTypedIndex = 0;
    private boolean isAttemptingCritical = false;

    public CombatView() {
        this.setStyle("-fx-background-color: #14141f;"); // Deep twilight backdrop
        this.setFocusTraversable(true); // Required to catch Typeracer key events

        // 1. Right-Side Enemy Sprite anchored matching the reference layout
        enemySpriteViewer.setFitWidth(450);
        enemySpriteViewer.setPreserveRatio(true);
        AnchorPane.setRightAnchor(enemySpriteViewer, 20.0);
        AnchorPane.setBottomAnchor(enemySpriteViewer, 0.0);

        // 2. Left-Side Main Translucent Dashboard Card
        VBox dashboardCard = new VBox(12);
        dashboardCard.setStyle(
                "-fx-background-color: rgba(18, 26, 33, 0.85); " +
                        "-fx-background-radius: 35; " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #2a3a4a; " +
                        "-fx-border-radius: 35; " +
                        "-fx-border-width: 2;"
        );
        dashboardCard.setPrefSize(480, 440);

        // Top Half: Kampf-Log
        Label logTitle = new Label("Kampf-Log:");
        logTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        logTitle.setTextFill(Color.web("#00FF66"));

        fightLogArea.setEditable(false);
        fightLogArea.setPrefHeight(150);
        fightLogArea.setWrapText(true);
        fightLogArea.setStyle(
                "-fx-control-inner-background: rgba(0,0,0,0.5); " +
                        "-fx-text-fill: #00FF66; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 13px;"
        );

        // Center Divider Line
        Separator divider = new Separator();
        divider.setStyle("-fx-background-color: #00FF66; -fx-opacity: 0.3;");

        // Bottom Half: Dynamic Viewport
        Label uiTitle = new Label("Kampf-UI:");
        uiTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        uiTitle.setTextFill(Color.web("#00FF66"));

        dynamicUiViewport.setPrefHeight(160);
        dynamicUiViewport.setAlignment(Pos.CENTER);
        renderIdleState("Wähle eine Aktion aus der Leiste unten.");

        dashboardCard.getChildren().addAll(logTitle, fightLogArea, divider, uiTitle, dynamicUiViewport);

        // 3. The 4 Bottom Circular Action Buttons
        HBox actionButtonBar = new HBox(15);
        actionButtonBar.setAlignment(Pos.CENTER);

        Button btnFist = createRoundActionButton("👊", "#ff4444");
        btnFist.setOnAction(e -> openPhysicalCanvasMode("Z-SLASH"));

        Button btnMagic = createRoundActionButton("✨", "#00FF66");
        btnMagic.setOnAction(e -> openMagicTyperacerMode("Fireball"));

        Button btnTalk = createRoundActionButton("💬", "#00eafe");
        btnTalk.setOnAction(e -> openConversationMode());

        Button btnFlee = createRoundActionButton("🏃", "#ffaa00");
        btnFlee.setOnAction(e -> openFleeMode());

        actionButtonBar.getChildren().addAll(btnFist, btnMagic, btnTalk, btnFlee);

        // Anchor absolute positions to match reference image
        AnchorPane.setTopAnchor(dashboardCard, 30.0);
        AnchorPane.setLeftAnchor(dashboardCard, 40.0);
        AnchorPane.setBottomAnchor(actionButtonBar, 25.0);
        AnchorPane.setLeftAnchor(actionButtonBar, 90.0);

        this.getChildren().addAll(enemySpriteViewer, dashboardCard, actionButtonBar);
        this.setOnKeyTyped(e -> handleTyperacerKeystroke(e.getCharacter()));
    }

    // --- API PUBLIC SETTERS FOR THE SCENE CONTROLLER ---

    public void setCallbacks(Consumer<Double> onPhys, BiConsumer<Double, Boolean> onMag, Runnable onTalk, Runnable onFlee) {
        this.onPhysicalAttackHook = onPhys;
        this.onMagicAttackHook = onMag;
        this.onConversationHook = onTalk;
        this.onFleeHook = onFlee;
    }

    public void setEnemySprite(Image spriteImage) {
        this.enemySpriteViewer.setImage(spriteImage);
    }

    public void appendLog(String text) {
        fightLogArea.appendText(text + "\n");
    }

    public void resetUi() {
        renderIdleState("Aktion abgeschlossen. Wähle den nächsten Zug.");
    }

    // --- MODE 1: MAGIC TYPERACER (SPEED + CRITICAL LOGIC) ---

    private void openMagicTyperacerMode(String baseSpellName) {
        if (spellTimer != null) spellTimer.stop();
        this.requestFocus(); // Claim keyboard focus

        this.normalTargetWord = baseSpellName;                           // e.g. "Fireball"
        this.critTargetWord = baseSpellName.toUpperCase() + "!";         // e.g. "FIREBALL!"
        this.currentTypedIndex = 0;
        this.isAttemptingCritical = false;

        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);

        TextFlow wordCanvas = new TextFlow();
        wordCanvas.setTextAlignment(TextAlignment.CENTER);
        renderTyperacerLetters(wordCanvas, normalTargetWord, Color.GRAY);

        ProgressBar cutoffBar = new ProgressBar(1.0);
        cutoffBar.setPrefWidth(300);
        cutoffBar.setStyle("-fx-accent: #00FF66; -fx-control-inner-background: #111;");

        Label hint = new Label("Tippe den Zauber! (CAPS + '!' am Ende = Kritisch)");
        hint.setTextFill(Color.web("#8899aa"));
        hint.setFont(Font.font("Courier New", 11));

        container.getChildren().addAll(wordCanvas, cutoffBar, hint);
        dynamicUiViewport.getChildren().setAll(container);

        // Cutoff Timer Setup (4.0 Seconds max window)
        double maxSeconds = 4.0;
        AtomicReference<Double> timeLeft = new AtomicReference<>(maxSeconds);

        spellTimer = new Timeline(new KeyFrame(Duration.millis(50), e -> {
            timeLeft.updateAndGet(v -> v - 0.05);
            double progress = timeLeft.get() / maxSeconds;
            cutoffBar.setProgress(progress);

            if (progress <= 0) {
                spellTimer.stop();
                renderIdleState("ZAUBER FEHLGESCHLAGEN: Zu langsam!");
                appendLog("-> Du hast die magische Konzentration verloren.");
            }
        }));
        spellTimer.setCycleCount(Timeline.INDEFINITE);
        spellTimer.playFromStart();
    }

    private void handleTyperacerKeystroke(String inputChar) {
        if (dynamicUiViewport.getChildren().isEmpty() || !(dynamicUiViewport.getChildren().get(0) instanceof VBox)) return;
        VBox box = (VBox) dynamicUiViewport.getChildren().get(0);
        if (!(box.getChildren().get(0) instanceof TextFlow)) return;

        TextFlow canvas = (TextFlow) box.getChildren().get(0);
        ProgressBar bar = (ProgressBar) box.getChildren().get(1);

        // Check fork towards Critical track
        if (!isAttemptingCritical && inputChar.equals(String.valueOf(critTargetWord.charAt(currentTypedIndex)))) {
            isAttemptingCritical = true;
            renderTyperacerLetters(canvas, critTargetWord, Color.web("#FFaa00")); // Turn untyped preview gold!
            bar.setStyle("-fx-accent: #FFaa00; -fx-control-inner-background: #111;");
        }

        String activeTarget = isAttemptingCritical ? critTargetWord : normalTargetWord;

        if (inputChar.equals(String.valueOf(activeTarget.charAt(currentTypedIndex)))) {
            Text letter = (Text) canvas.getChildren().get(currentTypedIndex);
            letter.setFill(isAttemptingCritical ? Color.web("#FFD700") : Color.web("#00FF66"));
            letter.setUnderline(true);
            currentTypedIndex++;

            if (currentTypedIndex == activeTarget.length()) {
                spellTimer.stop();
                // Speed formula: yields a 1.0x multiplier at the last second up to 2.0x if typed instantly
                double speedMultiplier = 1.0 + bar.getProgress();
                if (onMagicAttackHook != null) {
                    onMagicAttackHook.accept(speedMultiplier, isAttemptingCritical);
                }
            }
        }
    }

    private void renderTyperacerLetters(TextFlow canvas, String word, Color untypedColor) {
        canvas.getChildren().clear();
        for (int i = 0; i < word.length(); i++) {
            Text t = new Text(word.charAt(i) + " ");
            t.setFont(Font.font("Courier New", FontWeight.BOLD, 28));
            t.setFill(i < currentTypedIndex ? (isAttemptingCritical ? Color.web("#FFD700") : Color.web("#00FF66")) : untypedColor);
            if (i < currentTypedIndex) t.setUnderline(true);
            canvas.getChildren().add(t);
        }
    }

    // --- MODE 2: PHYSICAL ATTACK (MARTIAL ARTS VECTOR DRAWING) ---

    private void openPhysicalCanvasMode(String techName) {
        if (spellTimer != null) spellTimer.stop();

        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);

        Label title = new Label("Technik: " + techName + " (Zeichne das Symbol nach!)");
        title.setTextFill(Color.web("#ff4444"));
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 12));

        Canvas drawPad = new Canvas(340, 110);
        GraphicsContext padGc = drawPad.getGraphicsContext2D();

        // Base Pad Background
        padGc.setFill(Color.web("#0c1014"));
        padGc.fillRoundRect(0, 0, 340, 110, 15, 15);

        // Define a Martial Arts "Z-Slash" geometric template
        List<Point2D> zPoints = List.of(
                new Point2D(50, 20), new Point2D(290, 20),
                new Point2D(50, 90), new Point2D(290, 90)
        );

        // Render faint guide template onto the pad
        padGc.setStroke(Color.web("#2a3a4a"));
        padGc.setLineWidth(10);
        padGc.strokeLine(zPoints.get(0).getX(), zPoints.get(0).getY(), zPoints.get(1).getX(), zPoints.get(1).getY());
        padGc.strokeLine(zPoints.get(1).getX(), zPoints.get(1).getY(), zPoints.get(2).getX(), zPoints.get(2).getY());
        padGc.strokeLine(zPoints.get(2).getX(), zPoints.get(2).getY(), zPoints.get(3).getX(), zPoints.get(3).getY());

        // Generate 30 mathematical validation hit-boxes along the guide lines
        Set<Point2D> hitCheckpoints = new HashSet<>();
        for (int i = 0; i <= 10; i++) {
            hitCheckpoints.add(new Point2D(50 + i * 24, 20));          // Top bar
            hitCheckpoints.add(new Point2D(290 - i * 24, 20 + i * 7)); // Diagonal slash
            hitCheckpoints.add(new Point2D(50 + i * 24, 90));          // Bottom bar
        }

        Set<Point2D> successfullyHitBoxes = new HashSet<>();
        padGc.setStroke(Color.web("#ff4444"));
        padGc.setLineWidth(4);

        drawPad.setOnMousePressed(e -> padGc.beginPath());
        drawPad.setOnMouseDragged(e -> {
            padGc.lineTo(e.getX(), e.getY());
            padGc.stroke();
            // Verify if user's brush intersects any checkpoint within a 18px tolerance radius
            hitCheckpoints.forEach(pt -> {
                if (pt.distance(e.getX(), e.getY()) < 18.0) {
                    successfullyHitBoxes.add(pt);
                }
            });
        });

        drawPad.setOnMouseReleased(e -> {
            double rawScore = (double) successfullyHitBoxes.size() / hitCheckpoints.size();
            double finalAccuracy = successfullyHitBoxes.size() < 5 ? 0.0 : Math.min(1.0, rawScore * 1.15); // Slight generous curve

            padGc.setFill(Color.web("#rgba(0,0,0,0.8)"));
            padGc.fillRect(0, 0, 340, 110);
            padGc.setFill(Color.web("#ff4444"));
            padGc.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
            padGc.fillText("Präzision: " + Math.round(finalAccuracy * 100) + "%", 80, 60);

            // Hold result on screen for 900ms before dispatching to controller
            new Timeline(new KeyFrame(Duration.millis(900), evt -> {
                if (onPhysicalAttackHook != null) onPhysicalAttackHook.accept(finalAccuracy);
            })).play();
        });

        container.getChildren().addAll(title, drawPad);
        dynamicUiViewport.getChildren().setAll(container);
    }

    // --- MODES 3 & 4: CONVERSATION AND FLEE ---

    private void openConversationMode() {
        if (spellTimer != null) spellTimer.stop();
        renderPromptBox("Möchtest du versuchen, den Gegner mit Worten zu besänftigen?", "Sprechen", () -> {
            if (onConversationHook != null) onConversationHook.run();
        });
    }

    private void openFleeMode() {
        if (spellTimer != null) spellTimer.stop();
        renderPromptBox("Flucht riskieren? (Gefahr, beim Weglaufen attackiert zu werden)", "Sprinten!", () -> {
            if (onFleeHook != null) onFleeHook.run();
        });
    }

    // --- UI HELPER GENERATORS ---

    private void renderIdleState(String message) {
        Label lbl = new Label(message);
        lbl.setFont(Font.font("Courier New", 14));
        lbl.setTextFill(Color.web("#556677"));
        dynamicUiViewport.getChildren().setAll(lbl);
    }

    private void renderPromptBox(String promptText, String buttonLabel, Runnable action) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);

        Label lbl = new Label(promptText);
        lbl.setWrapText(true);
        lbl.setTextFill(Color.web("#00FF66"));
        lbl.setFont(Font.font("Courier New", 13));

        Button actionBtn = new Button(buttonLabel);
        actionBtn.setStyle("-fx-background-color: #00FF66; -fx-text-fill: black; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        actionBtn.setOnAction(e -> action.run());

        box.getChildren().addAll(lbl, actionBtn);
        dynamicUiViewport.getChildren().setAll(box);
    }

    private Button createRoundActionButton(String emoji, String hoverBorderColor) {
        Button btn = new Button(emoji);
        btn.setStyle(
                "-fx-background-color: #0c1014; " +
                        "-fx-text-fill: " + hoverBorderColor + "; " +
                        "-fx-font-size: 24px; " +
                        "-fx-min-width: 60px; -fx-min-height: 60px; " +
                        "-fx-max-width: 60px; -fx-max-height: 60px; " +
                        "-fx-background-radius: 50em; " +
                        "-fx-border-radius: 50em; " +
                        "-fx-border-color: #2a3a4a; " +
                        "-fx-border-width: 2;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#2a3a4a", hoverBorderColor)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverBorderColor, "#2a3a4a")));
        return btn;
    }
}