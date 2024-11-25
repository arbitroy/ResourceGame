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

        // Add random base resources
        for (int i = 0; i < width * height / 4; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            if (grid[x][y].getType() == TileType.EMPTY) {
                grid[x][y].setType(TileType.RESOURCE);
                grid[x][y].setResource(new Resource(getRandomBaseResource()));
            }
        }
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
}