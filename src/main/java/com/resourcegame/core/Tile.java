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
        // Resource tiles are not walkable, unless they're special tiles (MARKET or
        // STARTING)
        return type != TileType.BLOCKED &&
                machine == null &&
                (type != TileType.RESOURCE || type == TileType.MARKET || type == TileType.STARTING);
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