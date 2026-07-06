package com.game.scenes;

import com.game.core.Endings;
import com.game.core.GameCharacter;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.state.DatabaseManager;
import com.game.ui.RetroUi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Shown right after a character's run ends. Reports the ending that was reached
 * and — since failure is progress in this game — nudges the player onward with
 * an "it's not over yet" beat before the next character selection. Once every
 * character has finished, it leads into the final overview instead.
 */
public class RunEndScene extends ModularScene {

    private String character;
    private String endingKey;

    @Override
    protected void onEnter(ScenePayload payload) {
        character = payload.metadata("CHARACTER", String.class);
        endingKey = payload.metadata("ENDING_KEY", String.class);
    }

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {
        Endings.Ending ending = Endings.describe(endingKey);
        boolean allDone = allCharactersCompleted();

        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setStyle("-fx-background-color: " + RetroUi.BG_DARK + ";");

        VBox column = new VBox(28);
        column.setAlignment(Pos.CENTER);
        column.setMaxWidth(1200);
        column.setPadding(new Insets(80));

        String who = (character != null) ? character.toUpperCase() : "";
        Label subtitle = new Label(who + "  —  " + ending.title());
        subtitle.setFont(RetroUi.body(30));
        subtitle.setTextFill(Color.web(ending.good() ? RetroUi.ACCENT_GREEN : RetroUi.ACCENT_RED));

        Label headline = new Label(allDone ? "Der Kreis schließt sich" : "Es ist noch nicht vorbei");
        headline.setFont(RetroUi.title(92));
        headline.setTextFill(Color.WHITE);
        headline.setWrapText(true);
        headline.setAlignment(Pos.CENTER);

        Label flavor = new Label(ending.flavor());
        flavor.setFont(RetroUi.body(26));
        flavor.setTextFill(Color.web("#B8C0B8"));
        flavor.setWrapText(true);
        flavor.setMaxWidth(1000);
        flavor.setAlignment(Pos.CENTER);

        Runnable proceed = allDone ? this::goToFinal : this::goToSelection;
        Label continueOption = RetroUi.menuOption(
                allDone ? "» Zum Abschluss" : "» Nächster Charakter", proceed);
        VBox.setMargin(continueOption, new Insets(30, 0, 0, 0));

        column.getChildren().addAll(subtitle, headline, flavor, continueOption);
        root.getChildren().add(column);

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) root.requestFocus();
        });
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER, SPACE -> proceed.run();
                default -> {}
            }
        });

        return root;
    }

    private boolean allCharactersCompleted() {
        return GameCharacter.allCompleted(DatabaseManager.getInstance().getCompletedCharacters());
    }

    private void goToSelection() {
        SceneDirector.switchScene(new CharCreatorScene(), new ScenePayload("CHAR_CREATOR", "unassigned"));
    }

    private void goToFinal() {
        SceneDirector.switchScene(new FinalEndScene(), new ScenePayload("FINAL_END", "unassigned"));
    }
}
