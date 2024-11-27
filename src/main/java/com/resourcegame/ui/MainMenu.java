package com.resourcegame.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class MainMenu extends JFrame {
    private static final String SAVE_FILE = "gamestate.txt";
    
    public MainMenu() {
        setTitle("Resource Management Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        // Title
        JLabel titleLabel = new JLabel("Resource Management Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        // Buttons
        addButton(mainPanel, "Start New Game", e -> startNewGame());
        addButton(mainPanel, "Continue Game", e -> loadGame());
        addButton(mainPanel, "Settings", e -> showSettings());
        addButton(mainPanel, "Exit", e -> System.exit(0));
        
        add(mainPanel);
    }
    
    private void addButton(JPanel panel, String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.addActionListener(listener);
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
    }
    
    private void startNewGame() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            GameUI gameUI = new GameUI();
            gameUI.setVisible(true);
        });
    }
    
    private void loadGame() {
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists()) {
            JOptionPane.showMessageDialog(this, 
                "No saved game found!", 
                "Load Game", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        dispose();
        SwingUtilities.invokeLater(() -> {
            GameUI gameUI = new GameUI();
            gameUI.loadGameState(SAVE_FILE);
            gameUI.setVisible(true);
        });
    }
    
    private void showSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}