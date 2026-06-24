package com.game.entities;

public class Item {
    // SQL-Implementation pending
    private String name;
    private int id;
    private int amount;
    private int damage;
    private int defence;

    public Item(String name, int id, int amount) {
        this.name = name;
        this.id = id;
        this.amount = amount;
    }

}
