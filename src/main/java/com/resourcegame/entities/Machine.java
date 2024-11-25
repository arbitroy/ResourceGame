package com.resourcegame.entities;

import com.resourcegame.utils.Position;

public abstract class Machine {
    protected Position position;
    protected Inventory inventory;
    protected boolean isWorking;

    public Machine(Position position) {
        this.position = position;
        this.inventory = new Inventory();
        this.isWorking = false;
    }

    public abstract void update();
}
