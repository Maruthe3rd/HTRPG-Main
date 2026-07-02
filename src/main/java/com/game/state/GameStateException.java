package com.game.state;

public final class GameStateException extends RuntimeException {
    public GameStateException(String message, Throwable cause) {
        super(message, cause);
    }
}