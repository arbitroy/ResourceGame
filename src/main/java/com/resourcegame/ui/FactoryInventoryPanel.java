package com.resourcegame.ui;

import com.resourcegame.entities.Factory;
import com.resourcegame.entities.Inventory;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.systems.Recipe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

public class FactoryInventoryPanel extends JPanel {
    private final Factory factory;
    private final Inventory playerInventory;
    private final ControlPanel controlPanel;
    private Timer updateTimer;
    private JPanel inventoryPanel;
    private JPanel recipePanel;

    public FactoryInventoryPanel(Factory factory, Inventory playerInventory, ControlPanel controlPanel) {
        this.factory = factory;
        this.playerInventory = playerInventory;
        this.controlPanel = controlPanel;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initializeComponents();
        setupUpdateTimer();
    }

    private void initializeComponents() {
        // Recipe info panel
        recipePanel = new JPanel(new BorderLayout());
        recipePanel.setBorder(BorderFactory.createTitledBorder("Current Recipe"));

        // Add recipe selection button
        JPanel recipeHeaderPanel = new JPanel(new BorderLayout());
        JButton selectRecipeButton = new JButton("Change Recipe");
        selectRecipeButton.addActionListener(e -> showRecipeSelectionDialog());
        recipeHeaderPanel.add(selectRecipeButton, BorderLayout.EAST);
        recipePanel.add(recipeHeaderPanel, BorderLayout.NORTH);

        // Add recipe info
        Recipe currentRecipe = factory.getSelectedRecipe();
        if (currentRecipe != null) {
            JTextArea recipeInfo = new JTextArea();
            recipeInfo.setEditable(false);
            recipeInfo.setText(formatRecipeInfo(currentRecipe));
            recipePanel.add(new JScrollPane(recipeInfo), BorderLayout.CENTER);
        } else {
            recipePanel.add(new JLabel("No recipe selected"), BorderLayout.CENTER);
        }
        add(recipePanel, BorderLayout.NORTH);

        // Inventory panel
        inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Factory Inventory"));
        updateInventoryDisplay();
        add(new JScrollPane(inventoryPanel), BorderLayout.CENTER);

        // Controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton transferButton = new JButton("Transfer Resources");
        transferButton.addActionListener(e -> showTransferDialog());

        JButton collectButton = new JButton("Collect All");
        collectButton.addActionListener(e -> collectAllResources());

        controlsPanel.add(transferButton);
        controlsPanel.add(collectButton);
        add(controlsPanel, BorderLayout.SOUTH);
    }

    private String formatRecipeInfo(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        sb.append("Recipe: ").append(recipe.getName()).append("\n\n");
        sb.append("Inputs:\n");
        for (Map.Entry<ResourceType, Integer> input : recipe.getIngredients().entrySet()) {
            sb.append("- ").append(input.getKey()).append(": ").append(input.getValue()).append("\n");
        }
        sb.append("\nOutputs:\n");
        for (Map.Entry<ResourceType, Integer> output : recipe.getResults().entrySet()) {
            sb.append("- ").append(output.getKey()).append(": ").append(output.getValue()).append("\n");
        }
        return sb.toString();
    }

    private void showRecipeSelectionDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog;
        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, "Select Recipe", true);
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner, "Select Recipe", true);
        } else {
            dialog = new JDialog();
            dialog.setTitle("Select Recipe");
        }
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add recipes
        for (Recipe recipe : controlPanel.getGame().getCraftingSystem().getAllRecipes()) {
            JPanel recipePanel = new JPanel(new BorderLayout(10, 5));
            recipePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            // Recipe name
            JPanel headerPanel = new JPanel(new BorderLayout());
            JLabel nameLabel = new JLabel(recipe.getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            headerPanel.add(nameLabel, BorderLayout.WEST);

            // Current recipe indicator
            if (factory.getSelectedRecipe() != null &&
                    factory.getSelectedRecipe().getName().equals(recipe.getName())) {
                JLabel currentLabel = new JLabel("(Current)");
                currentLabel.setForeground(new Color(0, 128, 0));
                headerPanel.add(currentLabel, BorderLayout.EAST);
            }

            recipePanel.add(headerPanel, BorderLayout.NORTH);

            // Recipe details
            JTextArea details = new JTextArea(formatRecipeInfo(recipe));
            details.setEditable(false);
            details.setOpaque(false);
            details.setLineWrap(true);
            details.setWrapStyleWord(true);
            recipePanel.add(details, BorderLayout.CENTER);

            // Select button
            JButton selectButton = new JButton("Select Recipe");
            selectButton.addActionListener(e -> {
                factory.setRecipe(recipe);
                dialog.dispose();
                updateRecipeDisplay();
            });
            recipePanel.add(selectButton, BorderLayout.SOUTH);

            mainPanel.add(recipePanel);
            mainPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(400, 500));
        dialog.add(scrollPane);

        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void updateRecipeDisplay() {
        recipePanel.removeAll();

        // Re-add recipe selection button
        JPanel recipeHeaderPanel = new JPanel(new BorderLayout());
        JButton selectRecipeButton = new JButton("Change Recipe");
        selectRecipeButton.addActionListener(e -> showRecipeSelectionDialog());
        recipeHeaderPanel.add(selectRecipeButton, BorderLayout.EAST);
        recipePanel.add(recipeHeaderPanel, BorderLayout.NORTH);

        // Update recipe info
        Recipe currentRecipe = factory.getSelectedRecipe();
        if (currentRecipe != null) {
            JTextArea recipeInfo = new JTextArea();
            recipeInfo.setEditable(false);
            recipeInfo.setText(formatRecipeInfo(currentRecipe));
            recipePanel.add(new JScrollPane(recipeInfo), BorderLayout.CENTER);
        } else {
            recipePanel.add(new JLabel("No recipe selected"), BorderLayout.CENTER);
        }

        recipePanel.revalidate();
        recipePanel.repaint();
    }

    private void updateInventoryDisplay() {
        inventoryPanel.removeAll();

        // Add inventory items
        for (ResourceType type : ResourceType.values()) {
            int count = factory.getInventory().getResourceCount(type);
            if (count > 0) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.add(new JLabel(type.toString() + ": " + count), BorderLayout.WEST);

                JButton collectButton = new JButton("Collect");
                collectButton.addActionListener(e -> collectResource(type));
                itemPanel.add(collectButton, BorderLayout.EAST);

                inventoryPanel.add(itemPanel);
            }
        }

        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    private void showTransferDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        TransferDialog dialog = new TransferDialog(owner, factory, playerInventory);
        dialog.setVisible(true);
        updateInventoryDisplay();
    }

    private void collectResource(ResourceType type) {
        int amount = factory.getInventory().getResourceCount(type);
        if (amount > 0 && playerInventory.hasSpace(amount)) {
            factory.getInventory().removeResource(type, amount);
            playerInventory.addResource(type, amount);
            updateInventoryDisplay();
            controlPanel.updateInventoryDisplay(playerInventory.getInventoryDisplay());
        } else {
            JOptionPane.showMessageDialog(this,
                    "Not enough space in your inventory!",
                    "Transfer Failed",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void collectAllResources() {
        boolean collected = false;
        for (ResourceType type : ResourceType.values()) {
            int amount = factory.getInventory().getResourceCount(type);
            if (amount > 0) {
                if (playerInventory.hasSpace(amount)) {
                    factory.getInventory().removeResource(type, amount);
                    playerInventory.addResource(type, amount);
                    collected = true;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Not enough space to collect all resources!",
                            "Collection Incomplete",
                            JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }
        if (collected) {
            updateInventoryDisplay();
            controlPanel.updateInventoryDisplay(playerInventory.getInventoryDisplay());
        }
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> {
            updateInventoryDisplay();
        });
        updateTimer.start();
    }

    public void destroy() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}