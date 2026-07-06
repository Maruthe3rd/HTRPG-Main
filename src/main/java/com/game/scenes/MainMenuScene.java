package com.game.scenes;

import com.game.core.GameCharacter;
import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.state.DatabaseManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainMenuScene extends ModularScene {
    public static Font BBTitle = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/MxPlus_ToshibaTxL1_8x16.ttf").toExternalForm(),
            150);

    public static Font BBTiverent = Font.loadFont(
            MainMenuScene.class.getResource("/fonts/pixel/Mx437_ToshibaTxL2_8x8.ttf").toExternalForm(),
            75);


    public MainMenuScene() {
        super();
    }

    @Override
    protected void onEnter(ScenePayload payload) {}

    @Override
    protected void onExit() {}

    @Override
    protected Parent initializeLayout() {

        StackPane root = new StackPane();
        root.setPrefSize(1920, 1080);


        ImageView background = new ImageView();
        try {
            background.setImage(new Image(getClass().getResourceAsStream("/images/menuBG.png")));
            background.setFitWidth(1920);
            background.setFitHeight(1080);
            background.setPreserveRatio(false);
        } catch (Exception e) {
            System.err.println("Upsi hat nicht funktioniert (wo bild!?) :(");
        }

        VBox layoutBox = new VBox(80);
        layoutBox.setAlignment(Pos.CENTER);

        Label title = new Label("(Half) TextRPG");
        title.setFont(BBTitle);
        title.setTextFill(Color.web("#000000"));

        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Label btnBegin = createMenuOption("Begin", () -> {
            // If every character has already been played, the selection would be
            // entirely greyed out — send the player to the final overview instead.
            if (allCharactersCompleted()) {
                SceneDirector.switchScene(new FinalEndScene(), new ScenePayload("FINAL_END", "unassigned"));
            } else {
                ScenePayload charCreatorPayload = new ScenePayload("CHAR_CREATOR", payload.activeHeroId());
                SceneDirector.switchScene(new CharCreatorScene(), charCreatorPayload);
            }
        });

        Label btnResume = createMenuOption("Resume", () -> {
            ScenePayload continuePayload = new ScenePayload("CONTINUE", payload.activeHeroId());
            SceneDirector.switchScene(new ContinueScene(), continuePayload);
        });

        Label btnExit = createMenuOption("Exit", () -> {
            System.out.println("Shutting down engine...");
            Platform.exit();
            System.exit(0);
        });

        buttonBox.getChildren().addAll(btnBegin, btnResume, btnExit);
        layoutBox.getChildren().addAll(title, buttonBox);


        root.getChildren().addAll(background, layoutBox);
        return root;
    }


    private boolean allCharactersCompleted() {
        return GameCharacter.allCompleted(DatabaseManager.getInstance().getCompletedCharacters());
    }

    private Label createMenuOption(String text, Runnable onClickAction) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#00FF66"));
        label.setCursor(Cursor.HAND);
        label.setFont(BBTiverent);

        label.setOnMouseEntered(e -> label.setTextFill(Color.WHITE));
        label.setOnMouseExited(e -> label.setTextFill(Color.web("#00FF66")));
        label.setOnMouseClicked(e -> onClickAction.run());

        return label;
    }
}