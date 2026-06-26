package com.game.scenes;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class StandardDialogueScene extends ModularScene {

    private Label dialogueLabel;

    public StandardDialogueScene(SceneDirector director, StackPane masterViewport) {
        super(director, masterViewport);
    }

    @Override
    protected Parent initializeLayout() {
        AnchorPane layout = new AnchorPane();
        layout.setPrefSize(1920, 1080);

        dialogueLabel = new Label("Loading narrative stream...");
        layout.getChildren().add(dialogueLabel);

        return layout;
    }

    @Override
    public void onEnter(ScenePayload payload) {
        String initialText = payload.getOrDefault("START_TEXT", "Default dialogue string.");
        dialogueLabel.setText(initialText);

        System.out.println("Dialogue context mounted successfully.");
    }

    @Override
    public void onExit() {
        dialogueLabel.setText("");
    }
}