package com.resourcegame.ui;

import com.resourcegame.core.Game;
import com.resourcegame.core.GameMap;
import com.resourcegame.core.Tile;
import com.resourcegame.utils.TileType;
import com.resourcegame.utils.Position;
import com.resourcegame.entities.Resource;
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
            gameMap.getHeight() * TILE_SIZE
        ));
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
                
                if (clickedTile != null && clickedTile.hasResource()) {
                    if (!playerPosition.isAdjacent(clickedPos)) {
                        showStatusMessage("Move closer to harvest this resource!");
                        gameMap.setSelectedTile(null);
                        game.getControlPanel().updateHarvestButton(false);
                    } else if (!clickedTile.getResource().canHarvest()) {
                        showStatusMessage("Resource is regenerating...");
                        gameMap.setSelectedTile(null);
                        game.getControlPanel().updateHarvestButton(false);
                    } else if (e.getClickCount() == 2) {
                        // Double click to harvest
                        game.harvestResource(clickedPos);
                        gameMap.setSelectedTile(null);
                        game.getControlPanel().updateHarvestButton(false);
                    } else {
                        // Single click to select
                        gameMap.setSelectedTile(clickedPos);
                        game.getControlPanel().updateHarvestButton(true);
                        showStatusMessage("Resource selected. Double-click or press Harvest to collect.");
                    }
                } else {
                    gameMap.setSelectedTile(null);
                    game.getControlPanel().updateHarvestButton(false);
                }
                repaint();
            }
        };
        addMouseListener(mouseAdapter);
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
        if (statusMessage == null) return;

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
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
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
            TILE_SIZE - 4
        );
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
        
        // Draw market indicator if it's a market tile
        if (tileType == TileType.MARKET) {
            drawMarketIndicator(g2d, x, y);
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
            size
        );

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
                arcExtent
            );
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

    private void drawMarketIndicator(Graphics2D g2d, int x, int y) {
        int padding = 8;
        g2d.setColor(new Color(184, 134, 11)); // Darker yellow
        g2d.fillRect(
            x * TILE_SIZE + padding,
            y * TILE_SIZE + padding,
            TILE_SIZE - (padding * 2),
            TILE_SIZE - (padding * 2)
        );
        
        // Draw 'M' for market
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("M", 
            x * TILE_SIZE + (TILE_SIZE - fm.stringWidth("M")) / 2,
            y * TILE_SIZE + (TILE_SIZE + fm.getAscent()) / 2
        );
    }

    private void drawPlayer(Graphics2D g2d, int x, int y) {
        int padding = 5;
        // Draw player shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(
            x * TILE_SIZE + padding + 2,
            y * TILE_SIZE + padding + 2,
            TILE_SIZE - (padding * 2),
            TILE_SIZE - (padding * 2)
        );
        // Draw player
        g2d.setColor(new Color(30, 144, 255)); // Dodger blue
        g2d.fillOval(
            x * TILE_SIZE + padding,
            y * TILE_SIZE + padding,
            TILE_SIZE - (padding * 2),
            TILE_SIZE - (padding * 2)
        );
    }

    private void drawGridLines(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 50)); // Semi-transparent black
        
        // Draw vertical lines
        for (int x = 0; x <= gameMap.getWidth(); x++) {
            g2d.drawLine(
                x * TILE_SIZE, 
                0, 
                x * TILE_SIZE, 
                gameMap.getHeight() * TILE_SIZE
            );
        }
        
        // Draw horizontal lines
        for (int y = 0; y <= gameMap.getHeight(); y++) {
            g2d.drawLine(
                0, 
                y * TILE_SIZE, 
                gameMap.getWidth() * TILE_SIZE, 
                y * TILE_SIZE
            );
        }
    }

    private Color getTileColor(TileType type) {
        switch (type) {
            case EMPTY: return new Color(245, 245, 245); // Light gray
            case RESOURCE: return new Color(144, 238, 144); // Light green
            case MARKET: return new Color(255, 223, 186); // Light orange
            case STARTING: return new Color(176, 196, 222); // Light steel blue
            case BLOCKED: return new Color(169, 169, 169); // Dark gray
            default: return Color.WHITE;
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