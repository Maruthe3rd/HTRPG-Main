package com.game.entity;

public class Player extends GameCharacter {
    private int atk;
    private int def;
    private int mana;
    private int maxMana;

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