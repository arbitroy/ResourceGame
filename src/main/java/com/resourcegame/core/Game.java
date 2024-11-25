package com.resourcegame.core;

import com.resourcegame.entities.Factory;
import com.resourcegame.entities.Harvester;
import com.resourcegame.entities.Machine;
import com.resourcegame.entities.MachineManager;
import com.resourcegame.entities.Player;
import com.resourcegame.systems.Market;
import com.resourcegame.systems.Recipe;
import com.resourcegame.systems.CraftingSystem;
import com.resourcegame.utils.Direction;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.Position;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.ui.ControlPanel;
import com.resourcegame.ui.GameUIListener;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private GameMap map;
    private Player player;
    private CraftingSystem craftingSystem;
    private List<GameUIListener> uiListeners;
    private ControlPanel controlPanel;
    private Market market;
    private MachineManager machineManager;

    public Game() {
        this.uiListeners = new ArrayList<>();
        initializeGame();
    }

    private void initializeGame() {
        map = new GameMap(20, 20);
        player = new Player(map.getStartingPosition());
        market = new Market();
        craftingSystem = new CraftingSystem();
        machineManager = new MachineManager(map);
    }

    public void movePlayer(Direction direction) {
        Position currentPos = player.getPosition();
        Position newPos = calculateNewPosition(currentPos, direction);

        if (isValidMove(newPos)) {
            player.setPosition(newPos);
            // Clear any selected tile when moving
            if (map.getSelectedTile() != null) {
                map.setSelectedTile(null);
                if (controlPanel != null) {
                    controlPanel.updateHarvestButton(false);
                }
            }
            notifyUIUpdate();
        }

        if (player.getPosition().isAdjacent(map.getMarketPosition())) {
            controlPanel.updateMarketButton(true);
        } else {
            controlPanel.updateMarketButton(false);
        }
    }

    private Position calculateNewPosition(Position current, Direction direction) {
        switch (direction) {
            case UP:
                return new Position(current.getX(), current.getY() - 1);
            case DOWN:
                return new Position(current.getX(), current.getY() + 1);
            case LEFT:
                return new Position(current.getX() - 1, current.getY());
            case RIGHT:
                return new Position(current.getX() + 1, current.getY());
            default:
                return current;
        }
    }

    private boolean isValidMove(Position newPos) {
        return newPos.getX() >= 0 && newPos.getX() < map.getWidth() &&
                newPos.getY() >= 0 && newPos.getY() < map.getHeight() &&
                map.getTile(newPos).isWalkable();
    }

    public void attemptHarvest() {
        Position playerPos = player.getPosition();
        for (Position adjacent : playerPos.getAdjacentPositions()) {
            Tile tile = map.getTile(adjacent);
            if (tile != null && tile.hasResource() && tile.getResource().canHarvest()) {
                ResourceType resourceType = tile.getResource().getType();
                if (player.getInventory().addResource(resourceType, 1)) {
                    tile.getResource().harvest();
                    if (controlPanel != null) {
                        controlPanel.updateHarvestButton(false);
                    }
                    map.setSelectedTile(null);
                    notifyUIUpdate();
                } else {
                    // Could notify player that inventory is full
                    System.out.println("Inventory is full!");
                }
                break;
            }
        }
    }

    public void harvestResource(Position resourcePos) {
        Tile tile = map.getTile(resourcePos);
        if (tile != null && tile.hasResource() && tile.getResource().canHarvest() &&
                player.getPosition().isAdjacent(resourcePos)) {

            ResourceType resourceType = tile.getResource().getType();
            if (player.getInventory().addResource(resourceType, 1)) {
                tile.getResource().harvest();
                if (controlPanel != null) {
                    controlPanel.updateHarvestButton(false);
                }
                map.setSelectedTile(null);
                notifyUIUpdate();
            } else {
                // Could notify player that inventory is full
                System.out.println("Inventory is full!");
            }
        }
    }

     public boolean placeMachine(MachineType type, Position position) {
        Tile tile = map.getTile(position);
        if (tile != null && tile.isWalkable() && !tile.hasMachine()) {
            Machine machine = machineManager.createMachine(type, position);
            if (machine != null) {
                tile.setMachine(machine);
                notifyUIUpdate();
                return true;
            }
        }
        return false;
    }

    // Add new method for configuring machines
    public void configureMachine(Position position, Object configuration) {
        Machine machine = machineManager.getMachineAt(position);
        if (machine != null) {
            if (machine instanceof Harvester && configuration instanceof ResourceType) {
                ((Harvester) machine).setTargetResource((ResourceType) configuration);
            } else if (machine instanceof Factory && configuration instanceof Recipe) {
                ((Factory) machine).setRecipe((Recipe) configuration);
            }
            notifyUIUpdate();
        }
    }

    // Add new method for removing machines
    public void removeMachine(Position position) {
        Tile tile = map.getTile(position);
        if (tile != null && tile.hasMachine()) {
            machineManager.removeMachine(position);
            tile.setMachine(null);
            notifyUIUpdate();
        }
    }

     // Add machine manager getter
     public MachineManager getMachineManager() {
        return machineManager;
    }

    // Update the game loop or timer
    public void update() {
        machineManager.updateMachines();
        notifyUIUpdate();
    }

    // UI Listener methods
    public void addUIListener(GameUIListener listener) {
        if (listener != null && !uiListeners.contains(listener)) {
            uiListeners.add(listener);
        }
    }

    public void removeUIListener(GameUIListener listener) {
        uiListeners.remove(listener);
    }

    protected void notifyUIUpdate() {
        for (GameUIListener listener : uiListeners) {
            if (listener != null) {
                listener.onGameUpdate();
            }
        }
    }

    // Getters
    public GameMap getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    public void setControlPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public CraftingSystem getCraftingSystem() {
        return craftingSystem;
    }

    public Market getMarket() {
        return market;
    }
}