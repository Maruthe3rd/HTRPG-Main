package com.game.entities;

public class Enemy extends GameCharacter {
    private int attackPower;

    public Enemy(String name, int maxHp, int attackPower) {
        super(name, maxHp);
        this.attackPower = attackPower;
    }

    public int getAttackPower() { return attackPower; }
}