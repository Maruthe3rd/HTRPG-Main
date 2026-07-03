package com.game.scenes;

import com.game.MainApp;
import com.game.core.ScenePayload;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public abstract class ModularScene {

    protected ScenePayload payload;
    protected Parent root;

    protected ModularScene() {}

    protected abstract void onEnter(ScenePayload payload);

    protected abstract void onExit();

    protected abstract Parent initializeLayout();

    public final void init(ScenePayload payload) {
        this.payload = payload;
        onEnter(payload);
    }

    public final void buildUI() {
        this.root = initializeLayout();
    }

    public final Parent getRoot() {return root;}

    public final void exit() {
        onExit();
    }
}