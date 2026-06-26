package com.game.entities;

import java.util.HashSet;
import java.util.Set;

public class Player extends GameCharacter { // aka. Hero
    Set<String> flags = new HashSet<>();
    private int atk;
    private int def;
    private int mana;
    private int maxMana;
    private String race;

    public Player(String name) {
        super(name, 10);
        maxMana = mana = atk = def = 10;
    }

    public boolean tryUseMana(int cost) {
        if (this.mana >= cost) {
            this.mana -= cost;
            return true;
        }
        return false;
    }

    public int getMana() { return mana; }
    public int getMaxMana() { return maxMana; }
}