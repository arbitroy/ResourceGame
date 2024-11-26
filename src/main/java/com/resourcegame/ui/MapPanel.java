package com.resourcegame.ui;

import com.resourcegame.core.Game;
import com.resourcegame.core.GameMap;
import com.resourcegame.core.Tile;
import com.resourcegame.utils.TileType;
import com.resourcegame.utils.Position;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.entities.Factory;
import com.resourcegame.entities.Harvester;
import com.resourcegame.entities.Inventory;
import com.resourcegame.entities.Machine;
import com.resourcegame.entities.Resource;
import com.resourcegame.systems.Recipe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MapPanel extends JPanel {
    private static final int TILE_SIZE = 40;
    private GameMap gameMap;
    private Position playerPosition;
    private Game game;
    private String statusMessage; // For displaying feedback
    private long statusMessageTime;
    private static final long MESSAGE_DURATION = 2000; // 2 seconds

    public MapPanel(GameMap gameMap, Game game) {
        this.gameMap = gameMap;
        this.game = game;
        this.playerPosition = gameMap.getStartingPosition();
        this.statusMessage = null;
        setPreferredSize(new Dimension(
                gameMap.getWidth() * TILE_SIZE,
                gameMap.getHeight() * TILE_SIZE));
        setupMouseListener();
    }

    private void setupMouseListener() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tileX = e.getX() / TILE_SIZE;
                int tileY = e.getY() / TILE_SIZE;
                Position clickedPos = new Position(tileX, tileY);
                Tile clickedTile = gameMap.getTile(clickedPos);

                if (clickedTile != null) {
                    if (clickedTile.hasMachine()) {
                        handleMachineClick(clickedTile.getMachine());
                    } else if (clickedTile.hasResource()) {
                        handleResourceClick(clickedPos, clickedTile, e);
                    }
                }
                repaint();
            }
        };
        addMouseListener(mouseAdapter);
    }

    private void handleResourceClick(Position clickedPos, Tile clickedTile, MouseEvent e) {
        if (!clickedTile.hasResource())
            return;

        if (!playerPosition.isAdjacent(clickedPos)) {
            showStatusMessage("Move closer to harvest this resource!");
            gameMap.setSelectedTile(null);
            game.getControlPanel().updateHarvestButton(false);
        } else if (!clickedTile.getResource().canHarvest()) {
            showStatusMessage("Resource is regenerating...");
            gameMap.setSelectedTile(null);
            game.getControlPanel().updateHarvestButton(false);
        } else if (SwingUtilities.isMiddleMouseButton(e) ||
                (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))) {
            // Middle click or double left click to harvest instantly
            game.harvestResource(clickedPos);
            gameMap.setSelectedTile(null);
            game.getControlPanel().updateHarvestButton(false);

            // Show feedback
            showStatusMessage("Resource harvested!");
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            // Single left click to select
            gameMap.setSelectedTile(clickedPos);
            game.getControlPanel().updateHarvestButton(true);
            showStatusMessage("Resource selected. Double-click or press Harvest to collect.");
        }
    }

    private void transferItems(Position machinePos, Machine machine) {
        JPopupMenu menu = new JPopupMenu();
        Inventory machineInventory = machine.getInventory();
        Inventory playerInventory = game.getPlayer().getInventory();

        // Transfer FROM machine TO player
        if (machineInventory.getTotalItems() > 0) {
            JMenu takeMenu = new JMenu("Take Items");

            for (ResourceType type : ResourceType.values()) {
                int count = machineInventory.getResourceCount(type);
                if (count > 0) {
                    JMenuItem item = new JMenuItem(String.format("%s (%d)", type, count));
                    item.addActionListener(e -> {
                        int amount = promptForAmount(count);
                        if (amount > 0 && playerInventory.hasSpace(amount)) {
                            machineInventory.removeResource(type, amount);
                            playerInventory.addResource(type, amount);
                            game.getControlPanel().updateInventoryDisplay(
                                    playerInventory.getInventoryDisplay());
                            repaint();
                        }
                    });
                    takeMenu.add(item);
                }
            }
            menu.add(takeMenu);
        }

        // Transfer FROM player TO machine (only for factories)
        if (machine instanceof Factory) {
            JMenu giveMenu = new JMenu("Give Items");

            for (ResourceType type : ResourceType.values()) {
                int count = playerInventory.getResourceCount(type);
                if (count > 0) {
                    JMenuItem item = new JMenuItem(String.format("%s (%d)", type, count));
                    item.addActionListener(e -> {
                        int amount = promptForAmount(count);
                        if (amount > 0 && machineInventory.hasSpace(amount)) {
                            playerInventory.removeResource(type, amount);
                            machineInventory.addResource(type, amount);
                            game.getControlPanel().updateInventoryDisplay(
                                    playerInventory.getInventoryDisplay());
                            repaint();
                        }
                    });
                    giveMenu.add(item);
                }
            }
            menu.add(giveMenu);
        }

        Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, this);
        menu.show(this, p.x, p.y);
    }

    private int promptForAmount(int maxAmount) {
        String input = JOptionPane.showInputDialog(
                this,
                String.format("Enter amount (1-%d):", maxAmount),
                "Transfer Amount",
                JOptionPane.QUESTION_MESSAGE);

        try {
            int amount = Integer.parseInt(input);
            return Math.min(Math.max(1, amount), maxAmount);
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    private void handleMachineClick(Machine machine) {
        if (machine instanceof Harvester) {
            showHarvesterConfig((Harvester) machine);
        } else if (machine instanceof Factory) {
            showFactoryConfig((Factory) machine);
        }
    }

    private void showHarvesterConfig(Harvester harvester) {
        JPopupMenu popup = new JPopupMenu();

        for (ResourceType type : ResourceType.values()) {
            if (type.getBaseHarvestTime() > 0) {
                JMenuItem item = new JMenuItem(type.toString());
                item.addActionListener(e -> {
                    harvester.setTargetResource(type);
                    repaint();
                });
                popup.add(item);
            }
        }

        JMenuItem removeItem = new JMenuItem("Remove Machine");
        removeItem.addActionListener(e -> {
            game.removeMachine(harvester.getPosition());
            repaint();
        });
        popup.addSeparator();
        popup.add(removeItem);

        Point p = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(p, this);
        popup.show(this, p.x, p.y);
    }

    private void showFactoryConfig(Factory factory) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Configure Factory", true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add recipe selection
        for (Recipe recipe : game.getCraftingSystem().getAllRecipes()) {
            JButton recipeButton = new JButton(recipe.getName());
            recipeButton.addActionListener(e -> {
                factory.setRecipe(recipe);
                dialog.dispose();
                repaint();
            });
            panel.add(recipeButton);
        }

        // Add remove button
        JButton removeButton = new JButton("Remove Machine");
        removeButton.addActionListener(e -> {
            game.removeMachine(factory.getPosition());
            dialog.dispose();
            repaint();
        });
        panel.add(removeButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showStatusMessage(String message) {
        statusMessage = message;
        statusMessageTime = System.currentTimeMillis();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw tiles
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                Position currentPos = new Position(x, y);
                drawTile(g2d, x, y);

                // Draw selection highlight
                if (gameMap.getSelectedTile() != null &&
                        gameMap.getSelectedTile().equals(currentPos)) {
                    drawSelectionHighlight(g2d, x, y);
                }

                // Draw adjacent tile indicators
                if (playerPosition.isAdjacent(currentPos)) {
                    drawAdjacentIndicator(g2d, x, y);
                }
            }
        }

        // Draw player
        drawPlayer(g2d, playerPosition.getX(), playerPosition.getY());
        drawGridLines(g2d);

        // Draw status message if active
        if (statusMessage != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - statusMessageTime < MESSAGE_DURATION) {
                drawStatusMessage(g2d);
            } else {
                statusMessage = null;
            }
        }
    }

    private void drawStatusMessage(Graphics2D g2d) {
        if (statusMessage == null)
            return;

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(statusMessage);
        int messageHeight = fm.getHeight();

        int x = (getWidth() - messageWidth) / 2;
        int y = getHeight() - 30;

        // Draw message background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(x - 10, y - messageHeight, messageWidth + 20, messageHeight + 10, 10, 10);

        // Draw message text
        g2d.setColor(Color.WHITE);
        g2d.drawString(statusMessage, x, y);
    }

    private void drawAdjacentIndicator(Graphics2D g2d, int x, int y) {
        Tile tile = gameMap.getTile(new Position(x, y));
        if (tile.hasResource() && tile.getResource().canHarvest()) {
            g2d.setColor(new Color(255, 255, 255, 50));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[] { 5 }, 0));
            g2d.drawRect(x * TILE_SIZE + 2, y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
        }
    }

    private void drawSelectionHighlight(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(255, 255, 0, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(
                x * TILE_SIZE + 2,
                y * TILE_SIZE + 2,
                TILE_SIZE - 4,
                TILE_SIZE - 4);
    }

    private void drawTile(Graphics2D g2d, int x, int y) {
        Tile tile = gameMap.getTile(new Position(x, y));
        TileType tileType = tile.getType();
        Color tileColor = getTileColor(tileType);

        // Draw base tile
        g2d.setColor(tileColor);
        g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw resource indicator if it's a resource tile
        if (tileType == TileType.RESOURCE && tile.hasResource()) {
            drawResourceIndicator(g2d, x, y, tile.getResource());
        }

        // Draw machine if present
        if (tile.hasMachine()) {
            drawMachine(g2d, x, y, tile.getMachine());
        }

        if (tileType == TileType.MARKET) {
            boolean isAdjacentToMarket = playerPosition.isAdjacent(new Position(x, y));
            drawMarketIndicator(g2d, x, y, isAdjacentToMarket);
        }
    }

    private void drawResourceIndicator(Graphics2D g2d, int x, int y, Resource resource) {
        int padding = 8;
        int size = TILE_SIZE - (padding * 2);

        // Draw resource symbol
        g2d.setColor(new Color(34, 139, 34)); // Darker green
        g2d.fillOval(
                x * TILE_SIZE + padding,
                y * TILE_SIZE + padding,
                size,
                size);

        // Draw harvest progress if not harvestable
        if (!resource.canHarvest()) {
            g2d.setColor(new Color(0, 0, 0, 100));
            float progress = 1.0f - resource.getHarvestProgress();
            int arcExtent = (int) (360 * progress);
            g2d.fillArc(
                    x * TILE_SIZE + padding,
                    y * TILE_SIZE + padding,
                    size,
                    size,
                    90,
                    arcExtent);
        }

        // Draw resource type indicator
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String resourceType = resource.getType().toString().substring(0, 1);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x * TILE_SIZE + (TILE_SIZE - fm.stringWidth(resourceType)) / 2;
        int textY = y * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2;
        g2d.drawString(resourceType, textX, textY);
    }

    private void drawMarketIndicator(Graphics2D g2d, int x, int y, boolean isAdjacent) {
        int padding = 8;
        // Draw base market symbol
        g2d.setColor(new Color(184, 134, 11)); // Market color
        g2d.fillRect(
                x * TILE_SIZE + padding,
                y * TILE_SIZE + padding,
                TILE_SIZE - (padding * 2),
                TILE_SIZE - (padding * 2));

        // Draw 'M' for market
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("M",
                x * TILE_SIZE + (TILE_SIZE - fm.stringWidth("M")) / 2,
                y * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2);

        // Draw highlight if adjacent
        if (isAdjacent) {
            g2d.setColor(new Color(255, 215, 0, 100)); // Semi-transparent gold
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(
                    x * TILE_SIZE + 2,
                    y * TILE_SIZE + 2,
                    TILE_SIZE - 4,
                    TILE_SIZE - 4);
        }
    }

    private void drawMachine(Graphics2D g2d, int x, int y, Machine machine) {
        int padding = 8;
        int size = TILE_SIZE - (padding * 2);
    
        // Draw machine base
        Color machineColor = getMachineBaseColor(machine);
        g2d.setColor(machineColor);
        g2d.fillRect(
                x * TILE_SIZE + padding,
                y * TILE_SIZE + padding,
                size,
                size);
    
        // Draw status indicator
        drawMachineStatus(g2d, x, y, machine);
    
        // Draw inventory fill level
        if (machine.getInventory().getTotalItems() > 0) {
            int fillHeight = (int)((float)machine.getInventory().getTotalItems() 
                                  / machine.getInventoryCapacity() * size);
            g2d.setColor(new Color(255, 255, 255, 80));
            g2d.fillRect(
                    x * TILE_SIZE + padding,
                    y * TILE_SIZE + padding + (size - fillHeight),
                    3, // width of fill bar
                    fillHeight);
        }
    
        // Draw machine type indicator
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String typeIndicator = getMachineTypeIndicator(machine);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(typeIndicator,
                x * TILE_SIZE + (TILE_SIZE - fm.stringWidth(typeIndicator)) / 2,
                y * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2);
    
        // Draw progress bar for factories
        if (machine instanceof Factory) {
            drawFactoryProgress(g2d, x, y, (Factory) machine);
        }
    }

    private Color getMachineBaseColor(Machine machine) {
        if (machine instanceof Harvester) {
            return machine.getType().toString().contains("ADVANCED") ? 
                   new Color(70, 130, 180) :  // Steel blue for advanced
                   new Color(100, 149, 237);  // Cornflower blue for basic
        } else {
            return machine.getType().toString().contains("ADVANCED") ? 
                   new Color(139, 69, 19) :   // Saddle brown for advanced
                   new Color(160, 82, 45);    // Sienna for basic
        }
    }

    private void drawMachineStatus(Graphics2D g2d, int x, int y, Machine machine) {
        Color statusColor;
        switch (machine.getStatus()) {
            case WORKING:
                statusColor = new Color(50, 205, 50); // Lime green
                break;
            case INVENTORY_FULL:
                statusColor = new Color(255, 0, 0);   // Red
                break;
            case INVENTORY_NEARLY_FULL:
                statusColor = new Color(255, 165, 0); // Orange
                break;
            case NEEDS_CONFIG:
                statusColor = new Color(255, 215, 0); // Yellow
                break;
            case INSUFFICIENT_RESOURCES:
                statusColor = new Color(255, 69, 0);  // Red-Orange
                break;
            default:
                statusColor = new Color(128, 128, 128); // Gray
        }

        g2d.setColor(statusColor);
        g2d.fillOval(
                x * TILE_SIZE + TILE_SIZE - 10,
                y * TILE_SIZE + 5,
                6,
                6);
    }

    private void drawFactoryProgress(Graphics2D g2d, int x, int y, Factory factory) {
        float progress = factory.getCraftingProgress();
        if (progress > 0) {
            int barHeight = 3;
            int barY = y * TILE_SIZE + TILE_SIZE - barHeight - 2;
            
            // Draw background
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(x * TILE_SIZE + 4, barY, TILE_SIZE - 8, barHeight);
            
            // Draw progress
            g2d.setColor(new Color(50, 205, 50));
            g2d.fillRect(x * TILE_SIZE + 4, barY, 
                    (int)((TILE_SIZE - 8) * progress), barHeight);
        }
    }

    private String getMachineTypeIndicator(Machine machine) {
        if (machine instanceof Harvester) {
            Harvester harvester = (Harvester) machine;
            ResourceType target = harvester.getTargetResource();
            return target != null ? target.toString().substring(0, 1) : "H";
        } else {
            return "F";
        }
    }

    private void drawPlayer(Graphics2D g2d, int x, int y) {
        int padding = 5;
        // Draw player shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(
                x * TILE_SIZE + padding + 2,
                y * TILE_SIZE + padding + 2,
                TILE_SIZE - (padding * 2),
                TILE_SIZE - (padding * 2));
        // Draw player
        g2d.setColor(new Color(30, 144, 255)); // Dodger blue
        g2d.fillOval(
                x * TILE_SIZE + padding,
                y * TILE_SIZE + padding,
                TILE_SIZE - (padding * 2),
                TILE_SIZE - (padding * 2));
    }

    private void drawGridLines(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 50)); // Semi-transparent black

        // Draw vertical lines
        for (int x = 0; x <= gameMap.getWidth(); x++) {
            g2d.drawLine(
                    x * TILE_SIZE,
                    0,
                    x * TILE_SIZE,
                    gameMap.getHeight() * TILE_SIZE);
        }

        // Draw horizontal lines
        for (int y = 0; y <= gameMap.getHeight(); y++) {
            g2d.drawLine(
                    0,
                    y * TILE_SIZE,
                    gameMap.getWidth() * TILE_SIZE,
                    y * TILE_SIZE);
        }
    }

    private Color getTileColor(TileType type) {
        switch (type) {
            case EMPTY:
                return new Color(245, 245, 245); // Light gray
            case RESOURCE:
                return new Color(144, 238, 144); // Light green
            case MARKET:
                return new Color(255, 223, 186); // Light orange
            case STARTING:
                return new Color(176, 196, 222); // Light steel blue
            case BLOCKED:
                return new Color(169, 169, 169); // Dark gray
            default:
                return Color.WHITE;
        }
    }

    public void updatePlayerPosition(Position newPosition) {
        this.playerPosition = newPosition;

        // Clear selection if we moved away from selected resource
        Position selectedTile = gameMap.getSelectedTile();
        if (selectedTile != null && !newPosition.isAdjacent(selectedTile)) {
            gameMap.setSelectedTile(null);
            game.getControlPanel().updateHarvestButton(false);
            showStatusMessage("Moved away from selected resource");
        }
        repaint();
    }
}