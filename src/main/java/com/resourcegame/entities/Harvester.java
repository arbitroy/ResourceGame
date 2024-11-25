package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.core.GameMap;
import com.resourcegame.core.Tile;
import java.util.List;

public class Harvester extends Machine {
    private ResourceType targetResource;
    private static final int BASE_HARVEST_INTERVAL = 1000; // 1 second base interval
    
    public Harvester(Position position, MachineType type) {
        super(position, type);
    }
    
    public void setTargetResource(ResourceType resource) {
        this.targetResource = resource;
    }
    
    @Override
    public void update(GameMap gameMap) {
        if (targetResource == null) return;
        
        // Check if inventory is full
        if (inventory.getTotalItems() >= inventoryCapacity) {
            isWorking = false;
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        int harvestInterval = BASE_HARVEST_INTERVAL / processingSpeed;
        
        if (currentTime - lastProcessTime >= harvestInterval) {
            harvestAdjacentResources(gameMap);
            lastProcessTime = currentTime;
        }
    }
    
    private void harvestAdjacentResources(GameMap gameMap) {
        List<Position> adjacent = position.getAdjacentPositions();
        for (Position pos : adjacent) {
            Tile tile = gameMap.getTile(pos);
            if (tile != null && tile.hasResource() && 
                tile.getResource().getType() == targetResource &&
                tile.getResource().canHarvest()) {
                
                // Attempt to harvest
                if (inventory.hasSpace(1)) {
                    inventory.addResource(targetResource, 1);
                    tile.getResource().harvest();
                    isWorking = true;
                } else {
                    isWorking = false;
                    break;
                }
            }
        }
    }
}