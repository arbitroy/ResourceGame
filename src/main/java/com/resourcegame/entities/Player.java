package com.resourcegame.entities;

import com.resourcegame.utils.Position;

public class Player {
    private Position position;
    private Inventory inventory;

    public Player(Position startPosition) {
        this.position = startPosition;
        this.inventory = new Inventory();
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
