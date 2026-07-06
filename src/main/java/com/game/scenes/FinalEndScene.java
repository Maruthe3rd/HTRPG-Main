package com.game.scenes;

import com.game.audio.AudioManager;
import com.game.core.Endings;
import com.game.core.GameCharacter;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.state.DatabaseManager;
import com.game.state.DatabaseManager.PlaythroughRecord;
import com.game.ui.RetroUi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Final overview once every character's timeline has been played. Deliberately
 * light on numbers: it lists the ending each character reached and how many good
 * endings were secured, framed as "x von y".
 */
public class FinalEndScene extends ModularScene {

    @Override
    protected void onEnter(ScenePayload payload) {}

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {
        AudioManager.menuMood();

        DatabaseManager db = DatabaseManager.getInstance();

        // Keep the most recent ending per character (records come oldest-first).
        Map<String, String> latestEnding = new LinkedHashMap<>();
        for (PlaythroughRecord record : db.getCompletedPlaythroughs()) {
            latestEnding.put(record.character(), record.endingKey());
        }

        GameCharacter[] roster = GameCharacter.values();
        int total = roster.length;
        int goodEndings = 0;

        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setStyle("-fx-background-color: " + RetroUi.BG_DARK + ";");

        VBox column = new VBox(24);
        column.setAlignment(Pos.CENTER);
        column.setMaxWidth(1100);
        column.setPadding(new Insets(70));

        Label title = new Label("Der Lauf der Zeitlinien");
        title.setFont(RetroUi.title(84));
        title.setTextFill(Color.web(RetroUi.ACCENT_GREEN));

        VBox rows = new VBox(14);
        rows.setAlignment(Pos.CENTER_LEFT);
        rows.setMaxWidth(900);

        for (GameCharacter c : roster) {
            String key = latestEnding.get(c.displayName());
            Endings.Ending ending = Endings.describe(key);
            boolean good = Endings.isGood(key);
            if (good) goodEndings++;

            Label name = new Label(c.displayName());
            name.setFont(RetroUi.body(28));
            name.setTextFill(Color.WHITE);
            name.setMinWidth(320);

            Label result = new Label(ending.title() + "   " + (good ? "✓ Gutes Ende" : "✗ Schlechtes Ende"));
            result.setFont(RetroUi.body(24));
            result.setTextFill(Color.web(good ? RetroUi.ACCENT_GREEN : RetroUi.ACCENT_RED));

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            HBox row = new HBox(20, name, spacer, result);
            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }

        Label tally = new Label("Gute Enden:  " + goodEndings + " von " + total);
        tally.setFont(RetroUi.body(30));
        tally.setTextFill(Color.web(RetroUi.ACCENT_GOLD));
        VBox.setMargin(tally, new Insets(20, 0, 0, 0));

        Label verdict = new Label(goodEndings == total
                ? "Beide Zeitlinien gerettet — das wahre Ende ist zum Greifen nah."
                : "Manche Fäden sind gerissen. Vielleicht führt eine andere Reihenfolge weiter …");
        verdict.setFont(RetroUi.body(24));
        verdict.setTextFill(Color.web("#B8C0B8"));
        verdict.setWrapText(true);
        verdict.setMaxWidth(1000);
        verdict.setAlignment(Pos.CENTER);

        Label hint = new Label("Eine neue Zeitlinie behält, was du erfahren hast — nur die Reihenfolge beginnt von vorn.");
        hint.setFont(RetroUi.body(20));
        hint.setTextFill(Color.web("#6E786E"));
        hint.setWrapText(true);
        hint.setMaxWidth(1000);
        hint.setAlignment(Pos.CENTER);

        Label timelineMap = RetroUi.menuOption("» Zeitlinien-Karte ansehen", this::goToTimelineMap);
        Label newTimeline = RetroUi.menuOption("» Neue Zeitlinie starten", this::startNewTimeline);
        Label backOption = RetroUi.menuOption("» Zurück zum Menü", this::goToMenu);
        VBox actions = new VBox(12, timelineMap, newTimeline, backOption);
        actions.setAlignment(Pos.CENTER);
        VBox.setMargin(actions, new Insets(24, 0, 0, 0));

        column.getChildren().addAll(title, rows, tally, verdict, hint, actions);
        root.getChildren().add(column);

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) root.requestFocus();
        });
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER, SPACE -> startNewTimeline();
                case ESCAPE -> goToMenu();
                default -> {}
            }
        });

        return root;
    }

    private void goToTimelineMap() {
        SceneDirector.switchScene(new TreeMapScene(), new ScenePayload("TIMELINE_MAP", "unassigned"));
    }

    /** Wipes the run history (keeping meta flags) so the loop can be replayed in a new order. */
    private void startNewTimeline() {
        DatabaseManager.getInstance().resetPlaythroughHistory();
        SceneDirector.switchScene(new CharCreatorScene(), new ScenePayload("CHAR_CREATOR", "unassigned"));
    }

    private void goToMenu() {
        SceneDirector.switchScene(new MainMenuScene(), new ScenePayload("MAIN_MENU", "unassigned"));
    }
}
