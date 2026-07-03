package com.game;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.scenes.MainMenuScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane masterViewport = new StackPane();

        Scene masterScene = new Scene(masterViewport, 1920, 1080);

        primaryStage.setScene(masterScene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setTitle("(Half) Text RPG");


        SceneDirector.initialize(primaryStage, masterViewport);

        ScenePayload initialPayload = new ScenePayload("MAIN_MENU", "unassigned");
        SceneDirector.switchScene(new MainMenuScene(), initialPayload);


        primaryStage.show();
    }
}