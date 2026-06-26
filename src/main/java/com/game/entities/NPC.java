package com.game.entities;

public class NPC extends GameCharacter {
    private int patience;
    private int friendliness;

    public NPC(String name, int maxHp, int startingPatience, int friendliness) {
        super(name, maxHp);
        this.patience = startingPatience;
        this.friendliness = friendliness;
    }

    public void changePatience(int delta) {
        this.patience = Math.max(0, Math.min(100, this.patience + delta));
    }

    public void changeFriendliness(int delta) {
        this.friendliness = Math.max(0, Math.min(100, this.friendliness + delta));
    }

    public int getPatience() { return patience; }
    public int getFriendliness() { return friendliness; }
}