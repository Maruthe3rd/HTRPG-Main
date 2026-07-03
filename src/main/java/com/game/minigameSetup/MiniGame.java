package com.game.minigameSetup;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;

public interface MiniGame {
    void update(double dt); //dt = delta-time in seconds
    void draw(GraphicsContext gc);
    void onKeyPress(KeyEvent e);
    default void onKeyRelease(KeyEvent e) {}

    default boolean isFinished() { return false; }

    default String getResultTier() { return null; }

    default double getDesignWidth() { return 800.0; }
    default double getDesignHeight() { return 600.0; }
}