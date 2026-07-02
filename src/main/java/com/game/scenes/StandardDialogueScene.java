package com.game.scenes;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class StandardDialogueScene extends ModularScene {

    private Label dialogueLabel;
    private SceneDirector director;
    private StackPane masterViewport;

    public StandardDialogueScene(SceneDirector director, StackPane masterViewport) {
        this.director = director;
        this.masterViewport = masterViewport;
    }

    @Override
    public void init(ScenePayload payload) {
        String initialText = "Default dialogue string.";
        dialogueLabel.setText(initialText);

        System.out.println("Dialogue context mounted successfully.");
    }

    @Override
    public void buildUI() {
        AnchorPane layout = new AnchorPane();
        layout.setPrefSize(1920, 1080);

        dialogueLabel = new Label("Loading narrative stream...");
        layout.getChildren().add(dialogueLabel);
    }

    @Override
    public Scene getScene() {
        return null;
    }
}