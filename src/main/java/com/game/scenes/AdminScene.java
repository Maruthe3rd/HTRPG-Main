package com.game.scenes;

import com.game.audio.AudioManager;
import com.game.core.GameCharacter;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.state.DatabaseManager;
import com.game.ui.RetroUi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Hidden admin/debug panel, reachable from the main menu via a secret key chord
 * (Ctrl+Shift+R). Lets a developer wipe the local save while testing without
 * hunting down game_save.db. The save-persistence logic is untouched — this only
 * empties the stored data.
 */
public class AdminScene extends ModularScene {

    private Label statsLabel;
    private Label status;

    @Override
    protected void onEnter(ScenePayload payload) {}

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {
        AudioManager.menuMood();

        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setStyle("-fx-background-color: " + RetroUi.BG_DARK + ";");

        Label title = new Label("Admin · Debug");
        title.setFont(RetroUi.title(64));
        title.setTextFill(Color.web(RetroUi.ACCENT_GREEN));

        statsLabel = new Label();
        statsLabel.setFont(RetroUi.body(24));
        statsLabel.setTextFill(Color.web("#B8C0B8"));
        statsLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        status = new Label(" ");
        status.setFont(RetroUi.body(22));
        status.setTextFill(Color.web(RetroUi.ACCENT_GOLD));

        Label reset = dangerOption("⚠  Spielstand komplett zurücksetzen", this::resetAll);
        Label back = RetroUi.menuOption("» Zurück zum Menü", this::goToMenu);

        VBox column = new VBox(24, title, statsLabel, reset, status, back);
        column.setAlignment(Pos.CENTER);
        column.setPadding(new Insets(40));
        root.getChildren().add(column);

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) root.requestFocus();
        });
        root.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) goToMenu();
        });

        refreshStats();
        return root;
    }

    private void refreshStats() {
        DatabaseManager db = DatabaseManager.getInstance();
        int runs = db.getCompletedPlaythroughs().size();
        int chars = db.getCompletedCharacters().size();
        int flags = db.countSetMetaFlags();
        int explored = db.getExploredNodes().size();
        statsLabel.setText(
                "Abgeschlossene Läufe: " + runs + "\n"
                        + "Charaktere durchgespielt: " + chars + " / " + GameCharacter.values().length + "\n"
                        + "Gesetzte Meta-Flags: " + flags + "\n"
                        + "Erkundete Knoten: " + explored);
    }

    private void resetAll() {
        DatabaseManager.getInstance().resetAll();
        refreshStats();
        status.setText("✓ Spielstand vollständig zurückgesetzt.");
        AudioManager.playSfx(AudioManager.UI_CLICK);
    }

    private Label dangerOption(String text, Runnable onClick) {
        Label label = new Label(text);
        label.setFont(RetroUi.body(30));
        label.setTextFill(Color.web(RetroUi.ACCENT_RED));
        label.setCursor(Cursor.HAND);
        label.setOnMouseEntered(e -> label.setTextFill(Color.WHITE));
        label.setOnMouseExited(e -> label.setTextFill(Color.web(RetroUi.ACCENT_RED)));
        label.setOnMouseClicked(e -> onClick.run());
        return label;
    }

    private void goToMenu() {
        SceneDirector.switchScene(new MainMenuScene(), new ScenePayload("MAIN_MENU", "unassigned"));
    }
}
