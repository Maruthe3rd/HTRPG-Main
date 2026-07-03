package com.game.core;

import com.game.scenes.ModularScene;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;



public final class SceneDirector {

    private static final Logger LOGGER = Logger.getLogger(SceneDirector.class.getName());
    private static final Duration TRANSITION_DURATION = Duration.millis(250);

    private static StackPane gameRoot;

    private static volatile Stage primaryStage;
    private static volatile ModularScene currentScene;
    public static StackPane masterViewport;

    private SceneDirector(StackPane masterViewport) {
        SceneDirector.masterViewport = masterViewport;
    }

    public static void initialize(
            Stage stage,
            StackPane viewport,
            StackPane root
    ) {

        primaryStage = stage;

        masterViewport = viewport;

        gameRoot = root;
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
            if (currentScene != null) {
                currentScene.exit();
            }

            targetScene.init(payload);
            targetScene.buildUI();

            Parent root = targetScene.getRoot();

            installSceneWithFade(root);

            currentScene = targetScene;

            LOGGER.info(() -> "Switched scene to '" + payload.currentSceneFile()
                    + "' for hero '" + payload.activeHeroId() + "'");
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Failed to switch to scene '" + payload.currentSceneFile() + "'", e);
            throw e;
        }
    }

    private static void installSceneWithFade(Parent root) {
        root.setOpacity(0);

        gameRoot.getChildren().setAll(root);

        FadeTransition fade = new FadeTransition(
                TRANSITION_DURATION,
                root
        );

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static ModularScene getCurrentScene() {
        return currentScene;
    }
}