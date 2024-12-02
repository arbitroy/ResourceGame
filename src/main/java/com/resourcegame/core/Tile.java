package com.resourcegame.core;

import com.resourcegame.utils.TileType;
import com.resourcegame.entities.Resource;
import com.resourcegame.entities.Machine;

public class Tile {
    private TileType type;
    private Resource resource;
    private Machine machine;

    public Tile(TileType type) {
        this.type = type;
    }

    public boolean isWalkable() {
        // Always blocked if there's a machine
        if (machine != null) {
            return false;
        }
        
        // Always blocked if it's a blocked tile
        if (type == TileType.BLOCKED) {
            return false;
        }
        
        // Special tiles are always walkable
        if (type == TileType.MARKET || type == TileType.STARTING) {
            return true;
        }
        
        // For resource tiles, check if the resource is regenerating
        if (type == TileType.RESOURCE && hasResource()) {
            // Can walk on the tile if the resource is regenerating (not harvestable)
            return !resource.canHarvest();
        }
        
        // Empty tiles are walkable
        return true;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public boolean hasResource() {
        return resource != null;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public boolean hasMachine() {
        return machine != null;
    }
}