package com.resourcegame.ui;

import com.resourcegame.utils.*;
import com.resourcegame.core.Game;
import com.resourcegame.core.GameMap;
import com.resourcegame.core.Tile;
import com.resourcegame.entities.*;
import com.resourcegame.systems.Market;
import com.resourcegame.systems.Recipe;

import java.io.*;

public class GameState {
    private static final String DELIMITER = ",";

    public static void saveGame(Game game, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Save map dimensions first
            GameMap gameMap = game.getMap();
            writer.println("MAP_DIMENSIONS");
            writer.println(gameMap.getWidth() + DELIMITER + gameMap.getHeight());

            // Save map tiles
            writer.println("MAP_TILES");
            for (int y = 0; y < gameMap.getHeight(); y++) {
                for (int x = 0; x < gameMap.getWidth(); x++) {
                    Position pos = new Position(x, y);
                    Tile tile = gameMap.getTile(pos);
                    writer.println(tile.getType() + DELIMITER +
                            (tile.hasResource() ? tile.getResource().getType() : "NONE"));
                }
            }

            // Save player data
            Player player = game.getPlayer();
            writer.println("PLAYER");
            writer.println(player.getPosition().getX() + DELIMITER + player.getPosition().getY());

            // Save inventory
            writer.println("INVENTORY");
            Inventory inv = player.getInventory();
            writer.println(inv.getMoney());
            for (ResourceType type : ResourceType.values()) {
                int count = inv.getResourceCount(type);
                if (count > 0) {
                    writer.println(type + DELIMITER + count);
                }
            }

            // Save machines
            writer.println("MACHINES");
            for (Machine machine : game.getMachineManager().getAllMachines()) {
                Position pos = machine.getPosition();
                writer.println(machine.getType() + DELIMITER +
                        pos.getX() + DELIMITER +
                        pos.getY());

                // Save machine-specific data
                if (machine instanceof Harvester) {
                    Harvester harvester = (Harvester) machine;
                    ResourceType target = harvester.getTargetResource();
                    writer.println("HARVESTER_CONFIG" + DELIMITER +
                            (target != null ? target.toString() : "NONE"));
                } else if (machine instanceof Factory) {
                    Factory factory = (Factory) machine;
                    Recipe recipe = factory.getSelectedRecipe();
                    writer.println("FACTORY_CONFIG" + DELIMITER +
                            (recipe != null ? recipe.getName() : "NONE"));
                }

                // Save machine inventory
                Inventory machineInv = machine.getInventory();
                for (ResourceType type : ResourceType.values()) {
                    int count = machineInv.getResourceCount(type);
                    if (count > 0) {
                        writer.println("MACHINE_INV" + DELIMITER +
                                type + DELIMITER + count);
                    }
                }
                writer.println("END_MACHINE");
            }

            // Save unplaced machines in inventory
            writer.println("UNPLACED_MACHINES");
            for (MachineType type : MachineType.values()) {
                int count = player.getInventory().getUnplacedMachineCount(type);
                if (count > 0) {
                    writer.println(type + DELIMITER + count);
                }
            }

            // Save market data
            writer.println("MARKET");
            Market market = game.getMarket();
            for (ResourceType type : ResourceType.values()) {
                writer.println(type + DELIMITER +
                        market.getStock(type) + DELIMITER +
                        market.getBuyPrice(type) + DELIMITER +
                        market.getSellPrice(type));
            }
        }
    }


    private static void loadMapTile(GameMap gameMap, String[] parts, int x, int y) {
        TileType tileType = TileType.valueOf(parts[0]);
        ResourceType resourceType = parts[1].equals("NONE") ? null : ResourceType.valueOf(parts[1]);
        gameMap.loadTile(x, y, tileType, resourceType);
    }

    public static void loadGame(Game game, String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String section = "";
            Machine currentMachine = null;
            GameMap gameMap = game.getMap(); // Use existing map
            int mapWidth = 0, mapHeight = 0;
            int currentRow = 0;

            while ((line = reader.readLine()) != null) {
                if (line.equals("MAP_DIMENSIONS") || line.equals("MAP_TILES") || 
                    line.equals("PLAYER") || line.equals("INVENTORY") ||
                    line.equals("MACHINES") || line.equals("MARKET") || 
                    line.equals("UNPLACED_MACHINES")) {
                    section = line;
                    currentRow = 0;
                    continue;
                }

                String[] parts = line.split(DELIMITER);

                switch (section) {
                    case "MAP_DIMENSIONS":
                        mapWidth = Integer.parseInt(parts[0]);
                        mapHeight = Integer.parseInt(parts[1]);
                        if (game.getMap() == null || 
                            game.getMap().getWidth() != mapWidth || 
                            game.getMap().getHeight() != mapHeight) {
                            gameMap = new GameMap(mapWidth, mapHeight);
                            game.setMap(gameMap);
                        } else {
                            gameMap.clear(); // Clear existing map before loading
                        }
                        break;

                    case "MAP_TILES":
                        if (currentRow < mapHeight * mapWidth) {
                            int x = currentRow % mapWidth;
                            int y = currentRow / mapWidth;
                            loadMapTile(gameMap, parts, x, y);
                            currentRow++;
                        }
                        break;

                    case "PLAYER":
                        if (parts.length == 2) {
                            int x = Integer.parseInt(parts[0]);
                            int y = Integer.parseInt(parts[1]);
                            game.getPlayer().setPosition(new Position(x, y));
                        }
                        break;

                    case "INVENTORY":
                        try {
                            int money = Integer.parseInt(line);
                            game.getPlayer().getInventory().addMoney(money - 100); // Adjust for starting money
                        } catch (NumberFormatException e) {
                            ResourceType type = ResourceType.valueOf(parts[0]);
                            int count = Integer.parseInt(parts[1]);
                            game.getPlayer().getInventory().addResource(type, count);
                        }
                        break;

                    case "MACHINES":
                        if (line.equals("END_MACHINE")) {
                            currentMachine = null;
                        } else if (line.startsWith("HARVESTER_CONFIG")) {
                            if (currentMachine instanceof Harvester && !parts[1].equals("NONE")) {
                                ((Harvester) currentMachine).setTargetResource(ResourceType.valueOf(parts[1]));
                            }
                        } else if (line.startsWith("FACTORY_CONFIG")) {
                            if (currentMachine instanceof Factory && !parts[1].equals("NONE")) {
                                Recipe recipe = findRecipe(game, parts[1]);
                                if (recipe != null) {
                                    ((Factory) currentMachine).setRecipe(recipe);
                                }
                            }
                        } else if (line.startsWith("MACHINE_INV")) {
                            if (currentMachine != null) {
                                ResourceType type = ResourceType.valueOf(parts[1]);
                                int count = Integer.parseInt(parts[2]);
                                currentMachine.getInventory().addResource(type, count);
                            }
                        } else {
                            // New machine entry
                            MachineType type = MachineType.valueOf(parts[0]);
                            int x = Integer.parseInt(parts[1]);
                            int y = Integer.parseInt(parts[2]);
                            Position pos = new Position(x, y);
                            currentMachine = game.getMachineManager().createMachine(type, pos);
                            if (currentMachine != null) {
                                gameMap.getTile(pos).setMachine(currentMachine);
                            }
                        }
                        break;

                    case "UNPLACED_MACHINES":
                        MachineType type = MachineType.valueOf(parts[0]);
                        int count = Integer.parseInt(parts[1]);
                        for (int i = 0; i < count; i++) {
                            game.getPlayer().getInventory().addMachine(type);
                        }
                        break;

                    case "MARKET":
                        // Market data loading implementation
                        break;
                }
            }
        }
    }

    private static void handlePlayerLoad(Game game, String[] parts) {
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        game.getPlayer().setPosition(new Position(x, y));
    }

    private static void handleInventoryLoad(Game game, String[] parts, String line) {
        try {
            int money = Integer.parseInt(line);
            game.getPlayer().getInventory().addMoney(money - 100); // Adjust for starting money
        } catch (NumberFormatException e) {
            ResourceType type = ResourceType.valueOf(parts[0]);
            int count = Integer.parseInt(parts[1]);
            game.getPlayer().getInventory().addResource(type, count);
        }
    }

    private static Machine handleMachineLoad(Game game, String[] parts, String line, Machine currentMachine) {
        if (line.equals("END_MACHINE")) {
            return null;
        }

        if (line.startsWith("HARVESTER_CONFIG")) {
            if (currentMachine instanceof Harvester && !parts[1].equals("NONE")) {
                ((Harvester) currentMachine).setTargetResource(ResourceType.valueOf(parts[1]));
            }
        } else if (line.startsWith("FACTORY_CONFIG")) {
            if (currentMachine instanceof Factory && !parts[1].equals("NONE")) {
                Recipe recipe = findRecipe(game, parts[1]);
                if (recipe != null) {
                    ((Factory) currentMachine).setRecipe(recipe);
                }
            }
        } else if (line.startsWith("MACHINE_INV")) {
            if (currentMachine != null) {
                ResourceType type = ResourceType.valueOf(parts[1]);
                int count = Integer.parseInt(parts[2]);
                currentMachine.getInventory().addResource(type, count);
            }
        } else {
            MachineType type = MachineType.valueOf(parts[0]);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            Position pos = new Position(x, y);
            return game.getMachineManager().createMachine(type, pos);
        }

        return currentMachine;
    }

    private static void handleMarketLoad(Game game, String[] parts) {
        // Market state is handled internally by the Market class
        // Add implementation if needed
    }

    private static Recipe findRecipe(Game game, String recipeName) {
        return game.getCraftingSystem().getAllRecipes().stream()
                .filter(r -> r.getName().equals(recipeName))
                .findFirst()
                .orElse(null);
    }
}
