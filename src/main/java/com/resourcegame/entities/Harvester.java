package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineStatus;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.core.GameMap;
import com.resourcegame.core.Tile;
import java.util.List;

public class Harvester extends Machine {
    private ResourceType targetResource;
    private static final int BASE_HARVEST_INTERVAL = 1000; // 1 second base interval
    private static final double FULL_INVENTORY_THRESHOLD = 0.9; // 90% full
    
    public Harvester(Position position, MachineType type) {
        super(position, type);
        this.status = MachineStatus.IDLE;
    }
    
    @Override
    public void update(GameMap gameMap) {
        if (targetResource == null) {
            setStatus(MachineStatus.NEEDS_CONFIG);
            return;
        }
        
        // Check inventory status
        double fillPercentage = (double) inventory.getTotalItems() / inventoryCapacity;
        if (fillPercentage >= 1.0) {
            setStatus(MachineStatus.INVENTORY_FULL);
            return;
        } else if (fillPercentage >= FULL_INVENTORY_THRESHOLD) {
            setStatus(MachineStatus.INVENTORY_NEARLY_FULL);
        }
        
        long currentTime = System.currentTimeMillis();
        int harvestInterval = BASE_HARVEST_INTERVAL / processingSpeed;
        
        if (currentTime - lastProcessTime >= harvestInterval) {
            if (harvestAdjacentResources(gameMap)) {
                setStatus(MachineStatus.WORKING);
            } else {
                setStatus(MachineStatus.NO_RESOURCES);
            }
            lastProcessTime = currentTime;
        }
    }
    
    private boolean harvestAdjacentResources(GameMap gameMap) {
        boolean harvestedAny = false;
        List<Position> adjacent = position.getAdjacentPositions();
        
        for (Position pos : adjacent) {
            Tile tile = gameMap.getTile(pos);
            if (tile != null && tile.hasResource() && 
                tile.getResource().getType() == targetResource &&
                tile.getResource().canHarvest()) {
                
                if (inventory.hasSpace(1)) {
                    inventory.addResource(targetResource, 1);
                    tile.getResource().harvest();
                    harvestedAny = true;
                } else {
                    break;
                }
            }
        }
        
        return harvestedAny;
    }

    @Override
    public String getStatusMessage() {
        switch (status) {
            case NEEDS_CONFIG:
                return "Needs target resource";
            case INVENTORY_FULL:
                return "Inventory full! (" + inventory.getTotalItems() + "/" + inventoryCapacity + ")";
            case INVENTORY_NEARLY_FULL:
                return "Inventory almost full! (" + inventory.getTotalItems() + "/" + inventoryCapacity + ")";
            case NO_RESOURCES:
                return "No harvestable " + targetResource + " nearby";
            case WORKING:
                return "Harvesting " + targetResource;
            default:
                return "Idle";
        }
    }

    public void setTargetResource(ResourceType resource) {
        this.targetResource = resource;
        setStatus(resource != null ? MachineStatus.IDLE : MachineStatus.NEEDS_CONFIG);
    }

    public ResourceType getTargetResource() {
        return targetResource;
    }
}