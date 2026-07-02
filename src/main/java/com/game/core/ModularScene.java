package com.game.core;

import javafx.scene.Scene;

public interface ModularScene {

    void init(ScenePayload payload);

    Scene buildUI();
}