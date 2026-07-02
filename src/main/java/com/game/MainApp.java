package com.game;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.scenes.MainMenuScene;
import javafx.application.Application;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneDirector.initialize(primaryStage);

        StackPane masterViewport = new StackPane();
        primaryStage.setTitle("(Half) Text RPG");

        ScenePayload initialPayload = new ScenePayload("MAIN_MENU", "unassigned");
        SceneDirector.switchScene(new MainMenuScene(masterViewport), initialPayload);
    }
}