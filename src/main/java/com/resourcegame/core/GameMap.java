package com.resourcegame.core;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.TileType;
import com.resourcegame.entities.Resource;
import com.resourcegame.utils.ResourceType;
import java.util.Random;
import java.util.EnumSet;

public class GameMap {
    private Tile[][] grid;
    private final int width;
    private final int height;
    private Position startingPosition;
    private Position selectedTile;
    // Define set of base resources that can be harvested
    private static final EnumSet<ResourceType> HARVESTABLE_RESOURCES = EnumSet.of(
        ResourceType.WOOD,
        ResourceType.STONE,
        ResourceType.IRON,
        ResourceType.GOLD,
        ResourceType.FOOD
    );

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Tile[width][height];
        this.selectedTile = null;
        generateMap();
    }

    private void generateMap() {
        Random random = new Random();
        
        // Initialize all tiles as empty
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Tile(TileType.EMPTY);
            }
        }

        // Set starting position at top-left
        startingPosition = new Position(0, 0);
        grid[0][0].setType(TileType.STARTING);

        // Set market at bottom-right
        grid[width-1][height-1].setType(TileType.MARKET);

        // Create a path from start to market
        ensurePathExists();

        // Add random resources while preserving path
        for (int i = 0; i < width * height / 4; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Position pos = new Position(x, y);
            
            // Only place resource if:
            // 1. Tile is empty
            // 2. Not adjacent to start or market
            // 3. Won't block the path
            if (grid[x][y].getType() == TileType.EMPTY &&
                !pos.isAdjacent(startingPosition) &&
                !pos.isAdjacent(new Position(width-1, height-1)) &&
                !wouldBlockPath(pos)) {
                
                grid[x][y].setType(TileType.RESOURCE);
                grid[x][y].setResource(new Resource(getRandomBaseResource()));
            }
        }
    }

    private void ensurePathExists() {
        // Create a simple path from start to market
        int x = 0, y = 0;
        
        // Move right
        while (x < width - 1) {
            grid[x][y].setType(TileType.EMPTY);
            x++;
        }
        
        // Move down
        while (y < height - 1) {
            grid[x][y].setType(TileType.EMPTY);
            y++;
        }
    }

    private boolean wouldBlockPath(Position pos) {
        // Temporarily make the tile unwalkable
        TileType originalType = grid[pos.getX()][pos.getY()].getType();
        grid[pos.getX()][pos.getY()].setType(TileType.RESOURCE);

        // Check if path still exists
        boolean pathExists = pathExists(startingPosition, new Position(width-1, height-1));

        // Restore original tile type
        grid[pos.getX()][pos.getY()].setType(originalType);

        return !pathExists;
    }

    private boolean pathExists(Position start, Position end) {
        boolean[][] visited = new boolean[width][height];
        return findPath(start, end, visited);
    }

    private boolean findPath(Position current, Position end, boolean[][] visited) {
        if (current.getX() < 0 || current.getX() >= width ||
            current.getY() < 0 || current.getY() >= height ||
            visited[current.getX()][current.getY()] ||
            !grid[current.getX()][current.getY()].isWalkable()) {
            return false;
        }

        if (current.getX() == end.getX() && current.getY() == end.getY()) {
            return true;
        }

        visited[current.getX()][current.getY()] = true;

        // Check all four directions
        Position[] neighbors = {
            new Position(current.getX() + 1, current.getY()),
            new Position(current.getX() - 1, current.getY()),
            new Position(current.getX(), current.getY() + 1),
            new Position(current.getX(), current.getY() - 1)
        };

        for (Position next : neighbors) {
            if (findPath(next, end, visited)) {
                return true;
            }
        }

        return false;
    }

    private ResourceType getRandomBaseResource() {
        ResourceType[] baseResources = HARVESTABLE_RESOURCES.toArray(new ResourceType[0]);
        return baseResources[new Random().nextInt(baseResources.length)];
    }

    // Rest of the GameMap methods remain unchanged...

    public Position getSelectedTile() {
        return selectedTile;
    }

    public void setSelectedTile(Position pos) {
        this.selectedTile = pos;
    }

    public Position getStartingPosition() {
        return startingPosition;
    }

    public Tile getTile(Position pos) {
        if (pos.getX() >= 0 && pos.getX() < width && pos.getY() >= 0 && pos.getY() < height) {
            return grid[pos.getX()][pos.getY()];
        }
        return null;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Position getMarketPosition() {
        // Market is at bottom-right corner as defined in generateMap()
        return new Position(width - 1, height - 1);
    }


    public void loadTile(int x, int y, TileType type, ResourceType resourceType) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[x][y] = new Tile(type);
            if (resourceType != null) {
                grid[x][y].setResource(new Resource(resourceType));
            }
        }
    }

    public void clear() {
        // Clear existing tiles before loading
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = new Tile(TileType.EMPTY);
            }
        }
    }
}