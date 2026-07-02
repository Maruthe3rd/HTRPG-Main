package com.game.core;

import com.game.scenes.ModularScene;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;



public final class SceneDirector {

    private static final Logger LOGGER = Logger.getLogger(SceneDirector.class.getName());
    private static final Duration TRANSITION_DURATION = Duration.millis(250);

    private static volatile Stage primaryStage;
    private static volatile ModularScene currentScene;

    private SceneDirector() {
    }

    public static void initialize(Stage stage) {
        primaryStage = Objects.requireNonNull(stage, "stage");
    }

    public static void switchScene(ModularScene targetScene, ScenePayload payload) {
        Objects.requireNonNull(targetScene, "targetScene");
        Objects.requireNonNull(payload, "payload");

        if (primaryStage == null) {
            throw new IllegalStateException(
                    "SceneDirector.initialize(Stage) must be called before switchScene().");
        }

        Runnable performSwitch = () -> doSwitch(targetScene, payload);

        if (Platform.isFxApplicationThread()) {
            performSwitch.run();
        } else {
            Platform.runLater(performSwitch);
        }
    }

    private static void doSwitch(ModularScene targetScene, ScenePayload payload) {
        try {
            targetScene.init(payload);
            targetScene.buildUI();

            Scene builtScene = targetScene.getScene();
            Objects.requireNonNull(builtScene, "getScene() returned null after buildUI()");

            installSceneWithFade(builtScene);
            currentScene = targetScene;

            LOGGER.info(() -> "Switched scene to '" + payload.currentSceneFile()
                    + "' for hero '" + payload.activeHeroId() + "'");
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to switch to scene '" + payload.currentSceneFile() + "'", e);
            throw e;
        }
    }

    private static void installSceneWithFade(Scene builtScene) {
        Parent root = builtScene.getRoot();

        root.setOpacity(0.0);
        primaryStage.setScene(builtScene);
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }

        FadeTransition fadeIn = new FadeTransition(TRANSITION_DURATION, root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static ModularScene getCurrentScene() {
        return currentScene;
    }
}