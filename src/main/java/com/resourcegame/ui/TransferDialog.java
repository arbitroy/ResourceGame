package com.resourcegame.ui;

import com.resourcegame.entities.Factory;
import com.resourcegame.entities.Inventory;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.systems.Recipe;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TransferDialog extends JDialog {
    private final Factory factory;
    private final Inventory playerInventory;
    private JPanel mainPanel;

public TransferDialog(Window owner, Factory factory, Inventory playerInventory) {
    super(owner, "Transfer Resources", ModalityType.APPLICATION_MODAL);
    this.factory = factory;
    this.playerInventory = playerInventory;
    
    initializeComponents();
    
    setSize(400, 500);
    setLocationRelativeTo(owner);
}

    private void initializeComponents() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Get current recipe requirements
        Recipe recipe = factory.getSelectedRecipe();
        if (recipe != null) {
            // Add recipe info section
            addRecipeInfoSection(recipe);
            
            // Add transfer controls for each required resource
            for (Map.Entry<ResourceType, Integer> ingredient : recipe.getIngredients().entrySet()) {
                addResourceTransferPanel(ingredient.getKey(), ingredient.getValue());
            }
        } else {
            mainPanel.add(new JLabel("No recipe selected in factory"));
        }

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Add control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        // Main layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addRecipeInfoSection(Recipe recipe) {
        JPanel recipePanel = new JPanel(new BorderLayout());
        recipePanel.setBorder(BorderFactory.createTitledBorder("Current Recipe"));
        
        JTextArea recipeInfo = new JTextArea();
        recipeInfo.setEditable(false);
        recipeInfo.setText(String.format("Recipe: %s\nCrafting Time: %.1f seconds",
                recipe.getName(),
                recipe.getCraftingTime() / 1000.0));
        recipePanel.add(recipeInfo);
        
        mainPanel.add(recipePanel);
        mainPanel.add(Box.createVerticalStrut(10));
    }

    private void addResourceTransferPanel(ResourceType type, int required) {
        JPanel resourcePanel = new JPanel(new BorderLayout(5, 0));
        resourcePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Resource info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(new JLabel(type.toString()));
        
        int inFactory = factory.getInventory().getResourceCount(type);
        int inPlayer = playerInventory.getResourceCount(type);
        JLabel countLabel = new JLabel(String.format("Required: %d | In Factory: %d | You have: %d",
                required, inFactory, inPlayer));
        infoPanel.add(countLabel);
        
        resourcePanel.add(infoPanel, BorderLayout.CENTER);

        // Transfer controls
        if (inPlayer > 0) {
            JPanel transferPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            // Amount spinner
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
                    Math.min(required - inFactory, inPlayer), // initial value
                    1,                                        // minimum
                    inPlayer,                                 // maximum
                    1                                         // step
            );
            JSpinner amountSpinner = new JSpinner(spinnerModel);
            amountSpinner.setPreferredSize(new Dimension(60, 25));

            JButton transferButton = new JButton("Transfer");
            transferButton.addActionListener(e -> {
                int amount = (Integer) amountSpinner.getValue();
                if (playerInventory.removeResource(type, amount)) {
                    factory.getInventory().addResource(type, amount);
                    dispose();  // Close dialog after transfer
                }
            });

            transferPanel.add(amountSpinner);
            transferPanel.add(transferButton);
            resourcePanel.add(transferPanel, BorderLayout.EAST);
        }

        mainPanel.add(resourcePanel);
        mainPanel.add(Box.createVerticalStrut(5));
    }
}