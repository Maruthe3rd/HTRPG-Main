package com.game;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import com.game.scenes.MainMenuScene;
import com.game.ui.GameScaler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane masterViewport = new StackPane();
        StackPane gameRoot = new StackPane();

        Rectangle clip = new Rectangle(1920, 1080);

        gameRoot.setClip(clip);

        masterViewport.getChildren().add(gameRoot);

        Scene masterScene = new Scene(masterViewport, 1920, 1080);

        primaryStage.setScene(masterScene);

        GameScaler.bind(primaryStage, gameRoot);

        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setTitle("(Half) Text RPG");


        SceneDirector.initialize(
                primaryStage,
                masterViewport,
                gameRoot
        );

        ScenePayload initialPayload = new ScenePayload("MAIN_MENU", "unassigned");
        SceneDirector.switchScene(new MainMenuScene(), initialPayload);


        primaryStage.show();
    }
}