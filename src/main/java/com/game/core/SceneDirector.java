package com.game.core;

import com.game.entities.Player;
import com.game.scenes.DialogueTemplate;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;

public class SceneDirector {
    private final StackPane masterViewport;
    private final Map<String, ModularScene> sceneRegistry = new HashMap<>();
    private final ScenePayload globalPayload = new ScenePayload();
    //private final StackPane root;
    private final Map<String, ModularScene> scenes = new HashMap<>();
    private final ScenePayload payload = new ScenePayload();
    private ModularScene currentScene;

    // Test Char
    private final Player hero = new Player("Phoenix Wright");

    public Player getHero() {
        return this.hero;
    }


    public SceneDirector(StackPane masterViewport) {
        this.masterViewport = masterViewport;
    }

    // "Plug in" a new scene module
    public void registerScene(String sceneId, ModularScene sceneInstance) {
        sceneRegistry.put(sceneId, sceneInstance);
    }

    // The single method used to jump around the game
    public void navigateTo(String sceneId) {
        ModularScene nextScene = sceneRegistry.get(sceneId);
        if (nextScene == null) {
            throw new IllegalArgumentException("Director Critical Error: Scene ID '" + sceneId + "' does not exist!");
        }

        // 1. Pack up the old scene
        if (currentScene != null) {
            currentScene.onExit();
        }

        // 2. Swap the JavaFX Viewport
        masterViewport.getChildren().clear();
        masterViewport.getChildren().add(nextScene.getRoot());

        // 3. Wake up the new scene and hand it the player's data
        nextScene.onEnter(globalPayload);
        this.currentScene = nextScene;
    }

    public ScenePayload getPayload() {
        return globalPayload;
    }
}