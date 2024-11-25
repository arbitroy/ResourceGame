package com.resourcegame.ui;

import com.resourcegame.core.GameMap;
import com.resourcegame.utils.TileType;
import com.resourcegame.utils.Position;
import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {
    private static final int TILE_SIZE = 40;
    private GameMap gameMap;
    private Position playerPosition;

    public MapPanel(GameMap gameMap) {
        this.gameMap = gameMap;
        this.playerPosition = gameMap.getStartingPosition();
        setPreferredSize(new Dimension(
            gameMap.getWidth() * TILE_SIZE,
            gameMap.getHeight() * TILE_SIZE
        ));
        setupMouseListener();
    }

    private void setupMouseListener() {
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int tileX = e.getX() / TILE_SIZE;
                int tileY = e.getY() / TILE_SIZE;
                handleTileClick(new Position(tileX, tileY));
            }
        });
    }

    private void handleTileClick(Position clickedPos) {
        if (gameMap.getTile(clickedPos).isWalkable() && 
            playerPosition.isAdjacent(clickedPos)) {
            playerPosition = clickedPos;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw tiles
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                drawTile(g2d, x, y);
            }
        }

        // Draw player
        drawPlayer(g2d, playerPosition.getX(), playerPosition.getY());
    }

    private void drawTile(Graphics2D g2d, int x, int y) {
        TileType tileType = gameMap.getTile(new Position(x, y)).getType();
        Color tileColor = getTileColor(tileType);
        
        g2d.setColor(tileColor);
        g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void drawPlayer(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.BLUE);
        int padding = 5;
        g2d.fillOval(
            x * TILE_SIZE + padding,
            y * TILE_SIZE + padding,
            TILE_SIZE - 2 * padding,
            TILE_SIZE - 2 * padding
        );
    }

    private Color getTileColor(TileType type) {
        switch (type) {
            case EMPTY: return Color.WHITE;
            case RESOURCE: return Color.GREEN;
            case MARKET: return Color.YELLOW;
            case STARTING: return Color.LIGHT_GRAY;
            case BLOCKED: return Color.DARK_GRAY;
            default: return Color.WHITE;
        }
    }

    public void updatePlayerPosition(Position newPosition) {
        this.playerPosition = newPosition;
        repaint();
    }
}