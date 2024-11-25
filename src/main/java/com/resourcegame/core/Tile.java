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
        return type != TileType.BLOCKED && machine == null;
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
}