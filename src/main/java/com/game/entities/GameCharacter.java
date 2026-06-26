package com.game.entities;

public abstract class GameCharacter {
    protected String name;
    protected int maxHP;
    protected int hp;

    public GameCharacter(String name, int maxHP) {
        this.name = name;
        this.maxHP = maxHP;
    }
    public int getHp() { return hp; }
    public void takeDamage(int amount) { this.hp -= amount; }
}