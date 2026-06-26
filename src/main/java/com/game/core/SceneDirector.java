package com.game.core;
import com.game.entities.Player;
import com.game.scenes.ModularScene;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;

public class SceneDirector {
    private final StackPane masterViewport = new StackPane();
    private final Map<String, ModularScene> scenes = new HashMap<>();
    private final ScenePayload payload = new ScenePayload();
    private Player hero;

    public SceneDirector() {
        this.hero = new Player("Hero");
    }

    public void registerScene(String name, ModularScene scene) {
        scenes.put(name, scene);
    }

    public void navigateTo(String name) {
        masterViewport.getChildren().clear();
        ModularScene nextScene = scenes.get(name);
        if (nextScene != null) {
            nextScene.onEnter(payload);
            masterViewport.getChildren().add(nextScene.getRoot());
        }
    }

    public StackPane getMasterViewport() { return masterViewport; }
    public Player getHero() { return hero; }
    public ScenePayload getPayload() { return payload; }
}