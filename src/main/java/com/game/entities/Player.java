package com.game.entities;

import java.util.HashSet;
import java.util.Set;

public class Player extends GameCharacter { // aka. Hero
    Set<String> flags = new HashSet<>();
    private String race;

    public Player(String name) {
        super(name, 10);
    }
}