package com.resourcegame.ui;

import com.resourcegame.core.Game;
import com.resourcegame.core.GameSettings;
import com.resourcegame.utils.Direction;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameUI extends JFrame implements GameUIListener {
    private Game game;
    private MapPanel mapPanel;
    private ControlPanel controlPanel;
    private Timer gameTimer;
    private Timer autosaveTimer;

    public GameUI() {
        this.game = new Game();
        this.game.addUIListener(this); // Register for game updates
        initializeUI();
        setupControls();
        setupGameTimer();
        setupAutosaveTimer();
    }

    
    private void initializeUI() {
        setTitle("Resource Management Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createMapPanel();
        controlPanel = new ControlPanel(game);
        game.setControlPanel(controlPanel);

        add(mapPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveGameState("gamestate.txt");
            }
        });

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
                        // Try to interact with adjacent machine first
                        if (!game.interactWithAdjacent()) {
                            // If no machine interaction, try harvesting
                            game.attemptHarvest();
                        }
                        break;
                }
            }
        });
    }

    private void setupGameTimer() {
        gameTimer = new Timer(100, e -> game.update()); // Update every 100ms
        gameTimer.start();
    }

    public void saveGameState(String filename) {
        try {
            GameState.saveGame(game, filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save game: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createMapPanel() {
        mapPanel = new MapPanel(game.getMap(), game);
        game.addUIListener(this); // Ensure UI listener is registered
    }

    public void loadGameState(String filename) {
        try {
            GameState.loadGame(game, filename);
            
            // Recreate UI components
            remove(mapPanel); // Remove old map panel
            createMapPanel(); // Create new map panel
            add(mapPanel, BorderLayout.CENTER);
            
            // Update control panel
            if (controlPanel != null) {
                controlPanel.updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
                controlPanel.updateMarketButton(
                    game.getPlayer().getPosition().isAdjacent(game.getMap().getMarketPosition())
                );
            }
            
            // Force a complete UI refresh
            revalidate();
            repaint();
            requestFocus(); // Ensure keyboard focus is restored

            // Trigger an immediate UI update
            onGameUpdate();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load game: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupAutosaveTimer() {
        int interval = GameSettings.getInstance().getAutosaveInterval();
        autosaveTimer = new Timer(interval * 60 * 1000, e -> saveGameState("gamestate.txt"));
        autosaveTimer.start();
    }

    public void updateAutosaveInterval(int minutes) {
        if (autosaveTimer != null) {
            autosaveTimer.stop();
            autosaveTimer.setDelay(minutes * 60 * 1000);
            autosaveTimer.start();
        }
    }

    @Override
    public void onGameUpdate() {
        // Update UI components when game state changes
        if (mapPanel != null) {
            mapPanel.updatePlayerPosition(game.getPlayer().getPosition());
        }
        if (controlPanel != null) {
            controlPanel.updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
        }
        repaint();
    }

    @Override
    public void dispose() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (autosaveTimer != null) {
            autosaveTimer.stop();
        }
        super.dispose();
    }

}