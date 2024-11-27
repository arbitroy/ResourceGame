package com.resourcegame.ui;

import com.resourcegame.core.Game;
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

    public GameUI() {
        this.game = new Game();
        this.game.addUIListener(this); // Register for game updates
        initializeUI();
        setupControls();
        setupGameTimer();
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
                        game.attemptHarvest();
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

    public void loadGameState(String filename) {
        try {
            GameState.loadGame(game, filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load game: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

   

    @Override
    public void onGameUpdate() {
        // Update UI components when game state changes
        mapPanel.updatePlayerPosition(game.getPlayer().getPosition());
        controlPanel.updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
        repaint();
    }

    @Override
    public void dispose() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        super.dispose();
    }

}