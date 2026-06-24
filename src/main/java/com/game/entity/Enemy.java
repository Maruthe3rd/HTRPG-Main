package com.game.entity;

public class Enemy extends GameCharacter {
    private String spellWeakness; // e.g. "FIREBALL"
    private int attackPower;

    public Enemy(String name, int maxHp, String spellWeakness, int attackPower) {
        super(name, maxHp);
        this.spellWeakness = spellWeakness;
        this.attackPower = attackPower;
    }

    public String getSpellWeakness() { return spellWeakness; }
    public int getAttackPower() { return attackPower; }
}