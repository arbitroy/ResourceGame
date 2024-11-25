package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineType;
import com.resourcegame.core.GameMap;

public abstract class Machine {
    protected Position position;
    protected Inventory inventory;
    protected boolean isWorking;
    protected MachineType type;
    protected int inventoryCapacity;
    protected long lastProcessTime;
    protected int processingSpeed;
    
    public Machine(Position position, MachineType type) {
        this.position = position;
        this.type = type;
        this.isWorking = false;
        this.lastProcessTime = System.currentTimeMillis();
        
        // Set capacity and speed based on machine type
        if (type.toString().contains("ADVANCED")) {
            this.inventoryCapacity = 200;
            this.processingSpeed = 2; // 2x faster
        } else {
            this.inventoryCapacity = 100;
            this.processingSpeed = 1;
        }
        
        this.inventory = new Inventory();
    }
    
    public abstract void update(GameMap gameMap);
    
    public Position getPosition() {
        return position;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public boolean isWorking() {
        return isWorking;
    }
    
    public MachineType getType() {
        return type;
    }
}