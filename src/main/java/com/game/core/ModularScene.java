package com.game.core;

import javafx.scene.layout.AnchorPane;

public abstract class ModularScene {
    protected final AnchorPane root = new AnchorPane();
    protected final SceneDirector director;

    public ModularScene(SceneDirector director) {
        this.director = director;
        buildUI();
    }

    public AnchorPane getRoot() {
        return root;
    }

    // --- MODULAR LIFECYCLE HOOKS ---

    // 1. Called the exact millisecond this scene is pushed to the screen
    public abstract void onEnter(ScenePayload payload);

    // 2. Called the exact millisecond before this scene is destroyed/swapped
    public abstract void onExit();

    // 3. Construct your JavaFX Nodes here
    protected abstract void buildUI();
}