package com.game.scenes;

import com.game.core.SceneDirector;
import com.game.core.ScenePayload;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public abstract class ModularScene {

    protected final SceneDirector director;
    protected final StackPane masterViewport;
    protected Parent root;

    public ModularScene(SceneDirector director, StackPane masterViewport) {
        this.director = director;
        this.masterViewport = masterViewport;
        this.root = initializeLayout();
    }

    protected abstract Parent initializeLayout();

    public abstract void onEnter(ScenePayload payload);

    public abstract void onExit();

    public Parent getRoot() {
        return this.root;
    }
}