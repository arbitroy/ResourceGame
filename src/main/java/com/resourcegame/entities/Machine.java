package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineStatus;
import com.resourcegame.utils.MachineType;
import com.resourcegame.core.GameMap;

public abstract class Machine {
    protected Position position;
    protected Inventory inventory;
    protected boolean isWorking;
    protected MachineType type;
    protected final int inventoryCapacity; 
    protected long lastProcessTime;
    protected int processingSpeed;
    protected MachineStatus status;
    
    
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
        
        this.status = MachineStatus.IDLE;
        this.inventory = new Inventory(this.inventoryCapacity);
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

    public MachineStatus getStatus() {
        return status;
    }
    
    protected void setStatus(MachineStatus newStatus) {
        if (this.status != newStatus) {
            this.status = newStatus;
            isWorking = (newStatus == MachineStatus.WORKING);
        }
    }

    public int getInventoryCapacity() {
        return inventoryCapacity;
    }
    
    public abstract String getStatusMessage();
}