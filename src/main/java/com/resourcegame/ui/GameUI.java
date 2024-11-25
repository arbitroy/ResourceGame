package com.resourcegame.ui;

import com.resourcegame.core.Game;
import com.resourcegame.utils.Direction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GameUI extends JFrame implements GameUIListener {
    private Game game;
    private MapPanel mapPanel;
    private ControlPanel controlPanel;

    public GameUI() {
        this.game = new Game();
        this.game.addUIListener(this); // Register for game updates
        initializeUI();
        setupControls();
    }

    private void initializeUI() {
        setTitle("Resource Management Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mapPanel = new MapPanel(game.getMap(), game); // Pass game reference
        controlPanel = new ControlPanel(game);
        game.setControlPanel(controlPanel);

        add(mapPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setFocusable(true);
    }

    private void setupControls() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        game.movePlayer(Direction.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                        game.movePlayer(Direction.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                        game.movePlayer(Direction.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                        game.movePlayer(Direction.RIGHT);
                        break;
                    case KeyEvent.VK_SPACE:
                        game.attemptHarvest();
                        break;
                }
            }
        });
    }

    @Override
    public void onGameUpdate() {
        // Update UI components when game state changes
        mapPanel.updatePlayerPosition(game.getPlayer().getPosition());
        controlPanel.updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
        repaint();
    }

    // Add this main method to run the game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameUI gameUI = new GameUI();
            gameUI.setVisible(true);
        });
    }
}