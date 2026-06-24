/*

MAYBE UNNÖTIG!

 */


package com.game.core;

import javafx.scene.layout.StackPane;
import javafx.scene.Parent;

public class SceneManager {
    private static final StackPane root = new StackPane();

    public static StackPane getRoot() {
        return root;
    }

    public static void switchTo(Parent view) {
        root.getChildren().clear();
        root.getChildren().add(view);
    }
}