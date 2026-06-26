package com.game;
import com.game.core.SceneDirector;
import com.game.scenes.MainMenuScene;
import com.game.scenes.StandardDialogueScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneDirector director = new SceneDirector();

        Scene scene = new Scene(director.getMasterViewport(), 1920, 1080);
        primaryStage.setTitle("(Half) Text RPG");
        primaryStage.setScene(scene);
        primaryStage.show();
        director.registerScene("MAIN_MENU", new MainMenuScene(director, director.getMasterViewport()));
        director.registerScene("DIALOGUE_SCENE", new StandardDialogueScene(director, director.getMasterViewport()));
        director.navigateTo("MAIN_MENU");
    }
}