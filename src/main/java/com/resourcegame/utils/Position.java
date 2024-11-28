package com.resourcegame.utils;

import java.util.ArrayList;
import java.util.List;

import com.resourcegame.core.GameMap;
import com.resourcegame.core.Tile;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<Position> getAdjacentPositions() {
        List<Position> adjacent = new ArrayList<>();
        adjacent.add(new Position(x, y - 1)); // UP
        adjacent.add(new Position(x, y + 1)); // DOWN
        adjacent.add(new Position(x - 1, y)); // LEFT
        adjacent.add(new Position(x + 1, y)); // RIGHT
        return adjacent;
    }

    public boolean isAdjacent(Position other) {
        int dx = Math.abs(x - other.x);
        int dy = Math.abs(y - other.y);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    public boolean hasAdjacentResourceOfType(GameMap gameMap, ResourceType type) {
        for (Position adjacentPos : getAdjacentPositions()) {
            Tile tile = gameMap.getTile(adjacentPos);
            if (tile != null && tile.hasResource() &&
                    (type == null || tile.getResource().getType() == type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}