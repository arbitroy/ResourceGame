package com.resourcegame.ui;

import com.resourcegame.core.Game;
import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private Game game;
    private JLabel inventoryLabel;
    private JButton harvestButton;
    private JButton craftButton;
    private JButton marketButton;

    public ControlPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(200, 400));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initializeComponents();
    }

    private void initializeComponents() {
        // Inventory Section
        add(createSectionPanel("Inventory", () -> {
            JPanel panel = new JPanel();
            inventoryLabel = new JLabel("Empty");
            panel.add(inventoryLabel);
            return panel;
        }));

        // Actions Section
        add(createSectionPanel("Actions", () -> {
            JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
            
            harvestButton = new JButton("Harvest");
            harvestButton.addActionListener(e -> handleHarvest());
            
            craftButton = new JButton("Craft");
            craftButton.addActionListener(e -> handleCraft());
            
            marketButton = new JButton("Market");
            marketButton.addActionListener(e -> handleMarket());
            
            panel.add(harvestButton);
            panel.add(craftButton);
            panel.add(marketButton);
            return panel;
        }));
    }

    private JPanel createSectionPanel(String title, PanelCreator content) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BorderLayout());
        sectionPanel.setBorder(BorderFactory.createTitledBorder(title));
        sectionPanel.add(content.createPanel(), BorderLayout.CENTER);
        return sectionPanel;
    }

    private void handleHarvest() {
        // TODO: Implement harvest logic
        System.out.println("Harvest attempted");
    }

    private void handleCraft() {
        // TODO: Implement craft logic
        System.out.println("Craft menu opened");
    }

    private void handleMarket() {
        // TODO: Implement market logic
        System.out.println("Market opened");
    }

    public void updateInventoryDisplay(String inventoryContent) {
        inventoryLabel.setText(inventoryContent);
    }
}

@FunctionalInterface
interface PanelCreator {
    JPanel createPanel();
}