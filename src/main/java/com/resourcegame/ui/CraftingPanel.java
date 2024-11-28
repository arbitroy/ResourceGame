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
    private final Map<String, JPanel> craftingPanels; // Changed to store whole panel
    private Timer updateTimer;
    private int craftingIdCounter = 0;
    private JLabel statusLabel;
    private JTextArea inventoryDisplay;
    private ControlPanel controlPanel;
    private JPanel rightPanel; // Store reference to right panel

    public CraftingPanel(CraftingSystem craftingSystem, Inventory playerInventory, ControlPanel controlPanel) {
        if (craftingSystem == null) {
            throw new IllegalArgumentException("CraftingSystem cannot be null");
        }
        if (playerInventory == null) {
            throw new IllegalArgumentException("Inventory cannot be null");
        }

        this.craftingSystem = craftingSystem;
        this.playerInventory = playerInventory;
        this.controlPanel = controlPanel;
        this.craftingPanels = new HashMap<>();
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

        for (Recipe recipe : craftingSystem.getAllRecipes()) {
            recipeModel.addElement(recipe);
        }

        JScrollPane recipeScrollPane = new JScrollPane(recipeList);
        recipeScrollPane.setPreferredSize(new Dimension(200, 0));
        leftPanel.add(recipeScrollPane, BorderLayout.CENTER);

        // Center Panel - Recipe Details and Inventory Display
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Recipe details at the top
        JPanel recipeDetailsPanel = new JPanel(new BorderLayout());
        recipeDetailsPanel.setBorder(BorderFactory.createTitledBorder("Recipe Details"));

        recipeDetails = new JTextArea();
        recipeDetails.setEditable(false);
        recipeDetails.setWrapStyleWord(true);
        recipeDetails.setLineWrap(true);
        recipeDetails.setMargin(new Insets(5, 5, 5, 5));
        recipeDetails.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane detailsScrollPane = new JScrollPane(recipeDetails);
        recipeDetailsPanel.add(detailsScrollPane, BorderLayout.CENTER);

        // Inventory display at the bottom
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Current Inventory"));

        inventoryDisplay = new JTextArea();
        inventoryDisplay.setEditable(false);
        inventoryDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        updateInventoryDisplay(); // Initialize inventory display

        JScrollPane inventoryScrollPane = new JScrollPane(inventoryDisplay);
        inventoryScrollPane.setPreferredSize(new Dimension(0, 100));
        inventoryPanel.add(inventoryScrollPane, BorderLayout.CENTER);

        // Add both to center panel
        centerPanel.add(recipeDetailsPanel, BorderLayout.CENTER);
        centerPanel.add(inventoryPanel, BorderLayout.SOUTH);

        // Craft Button
        craftButton = new JButton("Craft Selected");
        craftButton.setEnabled(false);
        craftButton.addActionListener(e -> startCrafting());
        centerPanel.add(craftButton, BorderLayout.NORTH);

        // Right Panel - Active Crafting

        this.rightPanel = new JPanel(); // Use the class field
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Active Crafting"));
        rightPanel.setPreferredSize(new Dimension(200, 0));

        // Add scroll capability to right panel
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Main Layout
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightScrollPane, BorderLayout.EAST);

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
        details.append("=== ").append(selected.getName()).append(" ===\n\n");
        details.append(selected.getDescription()).append("\n\n");

        // Input Resources Section
        details.append("┌─ REQUIRED RESOURCES ").append("─".repeat(20)).append("┐\n");
        boolean canCraft = true;
        for (Map.Entry<ResourceType, Integer> ingredient : selected.getIngredients().entrySet()) {
            int available = playerInventory.getResourceCount(ingredient.getKey());
            int required = ingredient.getValue();
            details.append(String.format("│ %-15s %3d  (Have: %d)  %s\n",
                    ingredient.getKey() + ":",
                    required,
                    available,
                    available >= required ? "✓" : "✗"));
            if (available < required)
                canCraft = false;
        }
        details.append("└").append("─".repeat(42)).append("┘\n\n");

        // Output Resources Section
        details.append("┌─ PRODUCES ").append("─".repeat(31)).append("┐\n");
        for (Map.Entry<ResourceType, Integer> result : selected.getResults().entrySet()) {
            details.append(String.format("│ %-15s %3d%25s\n",
                    result.getKey() + ":",
                    result.getValue(),
                    ""));
        }
        details.append("└").append("─".repeat(42)).append("┘\n\n");

        // Crafting Time
        if (!selected.isInstant()) {
            details.append("⏱ Crafting Time: ")
                    .append(String.format("%.1f seconds", selected.getCraftingTime() / 1000.0))
                    .append("\n");
        }

        // Space Check
        int totalResults = selected.getResults().values().stream()
                .mapToInt(Integer::intValue).sum();
        if (!playerInventory.hasSpace(totalResults)) {
            details.append("\n⚠ WARNING: Not enough inventory space for results!");
            canCraft = false;
        }

        if (!canCraft) {
            details.append("\n❌ Cannot craft: Missing resources or insufficient space");
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
                    updateProgressBars();
                    recipeList.repaint();
                    updateRecipeDetails();
                    updateInventoryDisplay();
                });
            }
        }, 0, 100);
    }

    private void updateProgressBars() {
        Iterator<Map.Entry<String, JPanel>> it = craftingPanels.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, JPanel> entry = it.next();
            String craftingId = entry.getKey();
            JPanel panel = entry.getValue();

            float progress = craftingSystem.getCraftingProgress(craftingId);

            if (progress >= 1.0f) {
                // Remove the panel from the right panel first
                rightPanel.remove(panel);
                // Remove from our tracking map
                it.remove();
                // Force a visual refresh
                rightPanel.revalidate();
                rightPanel.repaint();
            } else {
                // Update progress bar if crafting is still in progress
                for (Component component : panel.getComponents()) {
                    if (component instanceof JProgressBar) {
                        JProgressBar progressBar = (JProgressBar) component;
                        progressBar.setValue((int) (progress * 100));
                        break;
                    }
                }
            }
        }
    }

    private void startCrafting() {
        Recipe selected = recipeList.getSelectedValue();
        if (selected == null)
            return;
    
        String craftingId = "craft_" + (++craftingIdCounter);
        if (craftingSystem.startCrafting(selected, playerInventory, craftingId)) {
            if (!selected.isInstant()) {
                addProgressBar(selected, craftingId);
            }
        }
    }

    private void addProgressBar(Recipe recipe, String craftingId) {
        // Create panel for this crafting process
        JPanel progressPanel = new JPanel(new BorderLayout(5, 0));
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)));

        // Recipe name with output preview
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel recipeLabel = new JLabel(recipe.getName());
        recipeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(recipeLabel, BorderLayout.WEST);

        // Add output preview
        StringBuilder output = new StringBuilder("→ ");
        recipe.getResults().forEach((type, amount) -> output.append(amount).append("x ").append(type).append(" "));
        JLabel outputLabel = new JLabel(output.toString());
        outputLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        outputLabel.setForeground(new Color(0, 100, 0));
        headerPanel.add(outputLabel, BorderLayout.EAST);

        progressPanel.add(headerPanel, BorderLayout.NORTH);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        // Store the whole panel
        craftingPanels.put(craftingId, progressPanel);

        // Add to right panel
        if (rightPanel != null) {
            rightPanel.add(progressPanel);
            rightPanel.revalidate();
            rightPanel.repaint();
        }
    }

    public void destroy() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer.purge(); // Add purge to clean up cancelled tasks
        }

        // Clear any remaining progress bars
        if (rightPanel != null) {
            rightPanel.removeAll();
            rightPanel.revalidate();
            rightPanel.repaint();
        }
        craftingPanels.clear();
    }

    private void updateInventoryDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Resources:\n");

        boolean hasItems = false;
        for (ResourceType type : ResourceType.values()) {
            int count = playerInventory.getResourceCount(type);
            if (count > 0) {
                if (hasItems) {
                    sb.append("\n");
                }
                sb.append(String.format("%-10s: %d", type.toString(), count));
                hasItems = true;
            }
        }

        if (!hasItems) {
            sb.append("Empty");
        }

        sb.append("\nInventory Space: ")
                .append(playerInventory.getTotalItems())
                .append("/")
                .append(100); // Assuming max capacity is 100

        inventoryDisplay.setText(sb.toString());
    }

    @Override
    public void onCraftingStarted(Recipe recipe) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Started crafting: " + recipe.getName());
            statusLabel.setForeground(new Color(0, 100, 0));
            updateInventoryDisplay();
            if (controlPanel != null) {
                controlPanel.updateInventoryDisplay(playerInventory.getInventoryDisplay());
            }
        });
    }

    @Override
    public void onCraftingCompleted(Recipe recipe) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Successfully crafted: " + recipe.getName());
            statusLabel.setForeground(new Color(0, 100, 0));

            if (!recipe.isInstant()) {
                // Force refresh of panels for timed recipes only
                updateProgressBars();
            }

            updateInventoryDisplay();
            if (controlPanel != null) {
                controlPanel.updateInventoryDisplay(playerInventory.getInventoryDisplay());
            }
            updateRecipeDetails();
            recipeList.repaint();
        });
    }

    @Override
    public void onCraftingFailed(Recipe recipe, String reason) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Crafting failed: " + reason);
            statusLabel.setForeground(Color.RED);
            updateInventoryDisplay();
            if (controlPanel != null) {
                controlPanel.updateInventoryDisplay(playerInventory.getInventoryDisplay());
            }
            updateRecipeDetails();
            recipeList.repaint();
        });
    }
}