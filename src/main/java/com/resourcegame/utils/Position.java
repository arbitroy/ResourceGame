package com.resourcegame.utils;

import java.util.ArrayList;
import java.util.List;

public class Position {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public List<Position> getAdjacentPositions() {
        List<Position> adjacent = new ArrayList<>();
        adjacent.add(new Position(x, y - 1));  // UP
        adjacent.add(new Position(x, y + 1));  // DOWN
        adjacent.add(new Position(x - 1, y));  // LEFT
        adjacent.add(new Position(x + 1, y));  // RIGHT
        return adjacent;
    }

    public boolean isAdjacent(Position other) {
        int dx = Math.abs(x - other.x);
        int dy = Math.abs(y - other.y);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}