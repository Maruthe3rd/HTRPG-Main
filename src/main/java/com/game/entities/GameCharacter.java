package com.game.entities;

public abstract class GameCharacter {
    protected String name;

    public GameCharacter(String name, int maxHP) {
        this.name = name;
    }
}