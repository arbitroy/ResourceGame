package com.resourcegame.ui;

import com.resourcegame.systems.CraftingSystem;
import com.resourcegame.systems.Recipe;
import com.resourcegame.entities.Inventory;
import com.resourcegame.utils.ResourceType;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import com.resourcegame.systems.CraftingSystem.CraftingListener;

public class CraftingPanel extends JPanel implements CraftingListener {
    private final CraftingSystem craftingSystem;
    private final Inventory playerInventory;
    private JList<Recipe> recipeList;
    private DefaultListModel<Recipe> recipeModel;
    private JTextArea recipeDetails;
    private JButton craftButton;
    private final Map<String, JProgressBar> craftingProgress;
    private Timer updateTimer;
    private int craftingIdCounter = 0;
    private JLabel statusLabel;

    public CraftingPanel(CraftingSystem craftingSystem, Inventory playerInventory) {
        if (craftingSystem == null) {
            throw new IllegalArgumentException("CraftingSystem cannot be null");
        }
        if (playerInventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null");
        }

        this.craftingSystem = craftingSystem;
        this.playerInventory = playerInventory;
        this.craftingProgress = new HashMap<>();
        craftingSystem.addCraftingListener(this);
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initializeComponents();
        setupUpdateTimer();

        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void initializeComponents() {
        // Left Panel - Recipe List
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Recipes"));
        
        recipeModel = new DefaultListModel<>();
        recipeList = new JList<>(recipeModel);
        recipeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recipeList.setCellRenderer(new RecipeListRenderer());
        recipeList.addListSelectionListener(e -> updateRecipeDetails());
        
        // Add all recipes to the list
        for (Recipe recipe : craftingSystem.getAllRecipes()) {
            recipeModel.addElement(recipe);
        }
        
        JScrollPane recipeScrollPane = new JScrollPane(recipeList);
        recipeScrollPane.setPreferredSize(new Dimension(200, 0));
        leftPanel.add(recipeScrollPane, BorderLayout.CENTER);

        // Center Panel - Recipe Details
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Recipe Details"));
        
        recipeDetails = new JTextArea();
        recipeDetails.setEditable(false);
        recipeDetails.setWrapStyleWord(true);
        recipeDetails.setLineWrap(true);
        recipeDetails.setMargin(new Insets(5, 5, 5, 5));
        recipeDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane detailsScrollPane = new JScrollPane(recipeDetails);
        centerPanel.add(detailsScrollPane, BorderLayout.CENTER);

        // Craft Button
        craftButton = new JButton("Craft Selected");
        craftButton.setEnabled(false);
        craftButton.addActionListener(e -> startCrafting());
        centerPanel.add(craftButton, BorderLayout.SOUTH);

        // Right Panel - Active Crafting
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Active Crafting"));
        rightPanel.setPreferredSize(new Dimension(200, 0));

        // Main Layout
        setLayout(new BorderLayout());
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Select first recipe
        if (recipeModel.getSize() > 0) {
            recipeList.setSelectedIndex(0);
        }
    }

    private class RecipeListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                    int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            Recipe recipe = (Recipe) value;
            setText(recipe.getName());
            
            if (!craftingSystem.canCraft(recipe, playerInventory)) {
                setForeground(Color.GRAY);
            } else {
                setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            }
            
            return this;
        }
    }

    private void updateRecipeDetails() {
        Recipe selected = recipeList.getSelectedValue();
        if (selected == null) {
            recipeDetails.setText("Select a recipe to view details.");
            craftButton.setEnabled(false);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Recipe: ").append(selected.getName()).append("\n\n");
        details.append(selected.getDescription()).append("\n\n");
        
        details.append("Required Resources:\n");
        boolean canCraft = true;
        for (Map.Entry<ResourceType, Integer> ingredient : selected.getIngredients().entrySet()) {
            int available = playerInventory.getResourceCount(ingredient.getKey());
            int required = ingredient.getValue();
            details.append(String.format("  %-15s %3d  (Have: %d)  %s\n",
                ingredient.getKey() + ":", 
                required,
                available,
                available >= required ? "✓" : "✗"));
            if (available < required) canCraft = false;
        }
        
        details.append("\nProduces:\n");
        for (Map.Entry<ResourceType, Integer> result : selected.getResults().entrySet()) {
            details.append(String.format("  %-15s %3d\n",
                result.getKey() + ":",
                result.getValue()));
        }
        
        details.append("\nCrafting Time: ")
               .append(String.format("%.1f seconds", selected.getCraftingTime() / 1000.0));

        int totalResults = selected.getResults().values().stream()
            .mapToInt(Integer::intValue).sum();
        if (!playerInventory.hasSpace(totalResults)) {
            details.append("\n\nWARNING: Not enough inventory space for results!");
            canCraft = false;
        }

        if (!canCraft) {
            details.append("\n\nCannot craft: Missing resources or insufficient space");
        }

        recipeDetails.setText(details.toString());
        craftButton.setEnabled(canCraft);
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    recipeList.repaint(); // Update recipe list colors
                    updateRecipeDetails();
                    updateProgressBars();
                });
            }
        }, 0, 100);
    }

    private void updateProgressBars() {
        Iterator<Map.Entry<String, JProgressBar>> it = craftingProgress.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, JProgressBar> entry = it.next();
            float progress = craftingSystem.getCraftingProgress(entry.getKey());
            
            if (progress >= 1.0f) {
                Component rightPanel = getComponent(2);
                if (rightPanel instanceof JPanel) {
                    ((JPanel) rightPanel).remove(entry.getValue().getParent());
                    ((JPanel) rightPanel).revalidate();
                    repaint();
                }
                it.remove();
            } else {
                entry.getValue().setValue((int)(progress * 100));
            }
        }
    }

    private void startCrafting() {
        Recipe selected = recipeList.getSelectedValue();
        if (selected == null) return;

        String craftingId = "craft_" + (++craftingIdCounter);
        if (craftingSystem.startCrafting(selected, playerInventory, craftingId)) {
            addProgressBar(selected, craftingId);
        }
    }

    private void addProgressBar(Recipe recipe, String craftingId) {
        JPanel progressPanel = new JPanel(new BorderLayout(5, 0));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        craftingProgress.put(craftingId, progressBar);

        JLabel recipeLabel = new JLabel(recipe.getName());
        progressPanel.add(recipeLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        Component rightPanel = getComponent(2);
        if (rightPanel instanceof JPanel) {
            ((JPanel) rightPanel).add(progressPanel);
            ((JPanel) rightPanel).revalidate();
            repaint();
        }
    }

    public void destroy() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    @Override
    public void onCraftingStarted(Recipe recipe) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Started crafting: " + recipe.getName());
            statusLabel.setForeground(new Color(0, 100, 0));
        });
    }

    @Override
    public void onCraftingCompleted(Recipe recipe) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Successfully crafted: " + recipe.getName());
            statusLabel.setForeground(new Color(0, 100, 0));
            updateRecipeDetails();
            recipeList.repaint();
        });
    }

    @Override
    public void onCraftingFailed(Recipe recipe, String reason) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Crafting failed: " + reason);
            statusLabel.setForeground(Color.RED);
            updateRecipeDetails();
            recipeList.repaint();
        });
    }
}