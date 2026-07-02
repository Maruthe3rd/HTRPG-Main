package com.game.scenes;

import com.game.core.ScenePayload;
import javafx.scene.Scene;

public abstract class ModularScene {

    protected ScenePayload payload;

    protected Scene scene;

    public abstract void init(ScenePayload payload);

    public abstract void buildUI();

    public abstract Scene getScene();
}