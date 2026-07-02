package com.game.scenes;

import com.game.core.ScenePayload;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public abstract class ModularScene {

    protected final StackPane masterViewport;
    protected ScenePayload payload;
    protected Scene scene;

    protected ModularScene(StackPane masterViewport) {
        this.masterViewport = masterViewport;
    }

    protected abstract void onEnter(ScenePayload payload);

    protected abstract void onExit();

    protected abstract Parent initializeLayout();

    public final void init(ScenePayload payload) {
        this.payload = payload;
        onEnter(payload);
    }

    public final void buildUI() {
        Parent root = initializeLayout();
        this.scene = new Scene(root, 1920, 1080);
    }

    public final Scene getScene() {
        return scene;
    }

    public final void exit() {
        onExit();
    }
}