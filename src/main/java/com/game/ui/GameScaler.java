package com.game.ui;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class GameScaler {

    public static final double VIRTUAL_WIDTH = 1920.0;
    public static final double VIRTUAL_HEIGHT = 1080.0;

    private GameScaler() {}

    public static void bind(Stage stage, StackPane gameRoot) {

        gameRoot.scaleXProperty().bind(
                Bindings.min(
                        stage.widthProperty().divide(VIRTUAL_WIDTH),
                        stage.heightProperty().divide(VIRTUAL_HEIGHT)
                )
        );

        gameRoot.scaleYProperty().bind(gameRoot.scaleXProperty());
    }
}