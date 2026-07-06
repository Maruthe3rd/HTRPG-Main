package com.game.scenes;

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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * The "Resume/Continue" screen reachable from the main menu: an overview of the
 * runs finished so far and their endings, with a way onward — into the next
 * character, or the final overview once every timeline is done.
 */
public class ContinueScene extends ModularScene {

    @Override
    protected void onEnter(ScenePayload payload) {}

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {
        DatabaseManager db = DatabaseManager.getInstance();
        List<PlaythroughRecord> runs = db.getCompletedPlaythroughs();
        boolean allDone = GameCharacter.allCompleted(db.getCompletedCharacters());

        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);
        root.setStyle("-fx-background-color: " + RetroUi.BG_DARK + ";");
        root.getChildren().add(RetroUi.background("/images/menuBG.png"));

        VBox column = new VBox(22);
        column.setAlignment(Pos.CENTER);
        column.setMaxWidth(1100);
        column.setPadding(new Insets(70));

        Label title = new Label("Spielstände");
        title.setFont(RetroUi.title(84));
        title.setTextFill(Color.web(RetroUi.ACCENT_GREEN));

        VBox rows = new VBox(12);
        rows.setAlignment(Pos.CENTER_LEFT);
        rows.setMaxWidth(900);

        if (runs.isEmpty()) {
            Label empty = new Label("Noch keine Läufe abgeschlossen.");
            empty.setFont(RetroUi.body(28));
            empty.setTextFill(Color.web("#B8C0B8"));
            rows.getChildren().add(empty);
        } else {
            for (PlaythroughRecord run : runs) {
                Endings.Ending ending = Endings.describe(run.endingKey());
                boolean good = Endings.isGood(run.endingKey());

                Label index = new Label(run.playOrder() + ".");
                index.setFont(RetroUi.body(26));
                index.setTextFill(Color.web(RetroUi.ACCENT_GOLD));
                index.setMinWidth(50);

                Label name = new Label(run.character());
                name.setFont(RetroUi.body(26));
                name.setTextFill(Color.WHITE);
                name.setMinWidth(320);

                Label result = new Label(ending.title() + (good ? "  ✓" : "  ✗"));
                result.setFont(RetroUi.body(24));
                result.setTextFill(Color.web(good ? RetroUi.ACCENT_GREEN : RetroUi.ACCENT_RED));

                HBox row = new HBox(20, index, name, result);
                row.setAlignment(Pos.CENTER_LEFT);
                rows.getChildren().add(row);
            }
        }

        String primaryLabel;
        Runnable primaryAction;
        if (allDone) {
            primaryLabel = "» Zum Abschluss";
            primaryAction = this::goToFinal;
        } else if (runs.isEmpty()) {
            primaryLabel = "» Neues Spiel";
            primaryAction = this::goToSelection;
        } else {
            primaryLabel = "» Nächster Charakter";
            primaryAction = this::goToSelection;
        }

        Label primary = RetroUi.menuOption(primaryLabel, primaryAction);
        Label back = RetroUi.menuOption("» Zurück", this::goToMenu);
        VBox actions = new VBox(12, primary, back);
        actions.setAlignment(Pos.CENTER);
        VBox.setMargin(actions, new Insets(30, 0, 0, 0));

        column.getChildren().addAll(title, rows, actions);
        root.getChildren().add(column);

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) root.requestFocus();
        });
        root.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER, SPACE -> primaryAction.run();
                case ESCAPE -> goToMenu();
                default -> {}
            }
        });

        return root;
    }

    private void goToSelection() {
        SceneDirector.switchScene(new CharCreatorScene(), new ScenePayload("CHAR_CREATOR", "unassigned"));
    }

    private void goToFinal() {
        SceneDirector.switchScene(new FinalEndScene(), new ScenePayload("FINAL_END", "unassigned"));
    }

    private void goToMenu() {
        SceneDirector.switchScene(new MainMenuScene(), new ScenePayload("MAIN_MENU", "unassigned"));
    }
}
