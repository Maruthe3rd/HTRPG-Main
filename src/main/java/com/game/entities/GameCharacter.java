package com.game.entities;

public abstract class GameCharacter {
    protected String name;
    protected int hp;
    protected int maxHp;

    public GameCharacter(String name, int maxHp) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public void takeDamage(int amount) {
        this.hp = Math.max(0, this.hp - amount);
    }

    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }

    public boolean isDead() {
        return this.hp <= 0;
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
}