package com.resourcegame.ui;

import com.resourcegame.core.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

public class SettingsDialog extends JDialog {
    private GameSettings settings;
    private JCheckBox soundCheckbox;
    private JCheckBox musicCheckbox;
    private JSlider volumeSlider;
    private JSpinner autosaveSpinner;
    private boolean settingsChanged = false;
    
    public SettingsDialog(Frame owner) {
        super(owner, "Settings", true);
        settings = GameSettings.getInstance();
        initializeComponents();
        loadCurrentSettings();
        
        setSize(500, 450);  // Increased size
        setLocationRelativeTo(owner);
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Game Settings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Audio Settings Section
        JPanel audioPanel = new JPanel();
        audioPanel.setLayout(new GridLayout(5, 1, 10, 10));  // Increased spacing
        audioPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Audio Settings"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        soundCheckbox = new JCheckBox("Sound Effects");
        soundCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        
        musicCheckbox = new JCheckBox("Background Music");
        musicCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JPanel volumePanel = new JPanel(new BorderLayout(10, 0));
        JLabel volumeLabel = new JLabel("Volume: ");
        volumeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setMajorTickSpacing(20);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setFont(new Font("Arial", Font.PLAIN, 12));
        volumePanel.add(volumeLabel, BorderLayout.WEST);
        volumePanel.add(volumeSlider, BorderLayout.CENTER);
        
        audioPanel.add(soundCheckbox);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        audioPanel.add(musicCheckbox);
        audioPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        audioPanel.add(volumePanel);
        
        // Game Settings Section
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(3, 1, 10, 10));  // Increased spacing
        gamePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Game Settings"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        JPanel autosavePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JLabel autosaveLabel = new JLabel("Autosave Interval (minutes): ");
        autosaveLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(5, 1, 60, 1);
        autosaveSpinner = new JSpinner(spinnerModel);
        autosaveSpinner.setFont(new Font("Arial", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) autosaveSpinner.getEditor()).getTextField().setColumns(3);
        autosavePanel.add(autosaveLabel);
        autosavePanel.add(autosaveSpinner);
        
        gamePanel.add(autosavePanel);
        
        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.setPreferredSize(new Dimension(120, 30));
        saveButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        saveButton.addActionListener(e -> {
            saveSettings();
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add change listeners
        ChangeListener changeListener = e -> settingsChanged = true;
        soundCheckbox.addChangeListener(changeListener);
        musicCheckbox.addChangeListener(changeListener);
        volumeSlider.addChangeListener(changeListener);
        autosaveSpinner.addChangeListener(changeListener);
        
        // Add all panels to main panel with spacing
        mainPanel.add(audioPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(gamePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(buttonPanel);
        
        // Add the main panel to a scroll pane in case window is resized
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane);
        
        // Set minimum size
        setMinimumSize(new Dimension(400, 400));
    }
    
    private void loadCurrentSettings() {
        soundCheckbox.setSelected(settings.isSoundEnabled());
        musicCheckbox.setSelected(settings.isMusicEnabled());
        volumeSlider.setValue(settings.getVolumeLevel());
        autosaveSpinner.setValue(settings.getAutosaveInterval());
        settingsChanged = false;
    }
    
    private void saveSettings() {
        if (!settingsChanged) {
            return;
        }
        
        settings.setSoundEnabled(soundCheckbox.isSelected());
        settings.setMusicEnabled(musicCheckbox.isSelected());
        settings.setVolumeLevel(volumeSlider.getValue());
        settings.setAutosaveInterval((Integer) autosaveSpinner.getValue());
        
        settings.saveSettings();
        applySettings();
    }
    
    private void applySettings() {
        // Update game components based on new settings
        if (!settings.isSoundEnabled()) {
            // Mute sound effects
        }
        
        if (!settings.isMusicEnabled()) {
            // Stop background music
        }
        
        // Apply volume level
        float volume = settings.getVolumeLevel() / 100f;
        // Update volume for sound system
        
        // Update autosave timer
        if (getOwner() instanceof GameUI) {
            ((GameUI) getOwner()).updateAutosaveInterval(settings.getAutosaveInterval());
        }
    }
}