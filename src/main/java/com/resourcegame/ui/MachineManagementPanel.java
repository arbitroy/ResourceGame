package com.resourcegame.ui;

import com.resourcegame.core.Game;
import com.resourcegame.entities.Factory;
import com.resourcegame.entities.Harvester;
import com.resourcegame.entities.Machine;
import com.resourcegame.utils.MachineStatus;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.Position;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.systems.Recipe;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MachineManagementPanel extends JPanel {
    private final Game game;
    private final ControlPanel controlPanel;
    private Timer updateTimer;
    private JPanel machineListPanel;
    private JPanel unplacedMachinesPanel;
    private static final int STATUS_UPDATE_INTERVAL = 100; 

    public MachineManagementPanel(Game game, ControlPanel controlPanel) {
        this.game = game;
        this.controlPanel = controlPanel;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initializeComponents();
        setupUpdateTimer();
    }

    private void initializeComponents() {
        // Title
        JLabel titleLabel = new JLabel("Machine Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Create split pane for unplaced and placed machines
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        // Unplaced Machines Panel
        unplacedMachinesPanel = new JPanel();
        unplacedMachinesPanel.setLayout(new BoxLayout(unplacedMachinesPanel, BoxLayout.Y_AXIS));
        JScrollPane unplacedScrollPane = new JScrollPane(unplacedMachinesPanel);
        unplacedScrollPane.setBorder(BorderFactory.createTitledBorder("Available Machines"));

        // Placed Machines Panel
        machineListPanel = new JPanel();
        machineListPanel.setLayout(new BoxLayout(machineListPanel, BoxLayout.Y_AXIS));
        JScrollPane placedScrollPane = new JScrollPane(machineListPanel);
        placedScrollPane.setBorder(BorderFactory.createTitledBorder("Placed Machines"));

        splitPane.setTopComponent(unplacedScrollPane);
        splitPane.setBottomComponent(placedScrollPane);
        splitPane.setDividerLocation(200);

        add(splitPane, BorderLayout.CENTER);

        updateMachineLists();
    }

    private JPanel createPlacedMachinePanel(Machine machine) {
        return createMachinePanel(machine); // Call existing createMachinePanel method
    }

    private String formatMachineType(MachineType type) {
        return type.toString().replace("_", " ").toLowerCase()
                .substring(0, 1).toUpperCase() +
                type.toString().replace("_", " ").toLowerCase().substring(1);
    }

    private void updateMachineList() {
        updateMachineLists();
    }

    private void updateMachineLists() {
        // Update unplaced machines
        unplacedMachinesPanel.removeAll();
        Map<MachineType, Integer> unplacedMachines = game.getPlayer().getInventory().getUnplacedMachines();

        if (unplacedMachines.isEmpty()) {
            JLabel noUnplacedLabel = new JLabel("No machines available to place");
            noUnplacedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            unplacedMachinesPanel.add(noUnplacedLabel);
        } else {
            for (Map.Entry<MachineType, Integer> entry : unplacedMachines.entrySet()) {
                unplacedMachinesPanel.add(createUnplacedMachinePanel(entry.getKey(), entry.getValue()));
                unplacedMachinesPanel.add(Box.createVerticalStrut(10));
            }
        }

        // Update placed machines
        machineListPanel.removeAll();
        List<Machine> placedMachines = game.getMachineManager().getAllMachines();

        if (placedMachines.isEmpty()) {
            JLabel noPlacedLabel = new JLabel("No machines placed on the map");
            noPlacedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            machineListPanel.add(noPlacedLabel);
        } else {
            for (Machine machine : placedMachines) {
                machineListPanel.add(createPlacedMachinePanel(machine));
                machineListPanel.add(Box.createVerticalStrut(10));
            }
        }

        unplacedMachinesPanel.revalidate();
        unplacedMachinesPanel.repaint();
        machineListPanel.revalidate();
        machineListPanel.repaint();
    }

    private JPanel createUnplacedMachinePanel(MachineType type, int count) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Machine info
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(formatMachineType(type) + " (x" + count + ")");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        infoPanel.add(nameLabel, BorderLayout.NORTH);

        // Description
        JLabel descLabel = new JLabel(type.getDescription());
        descLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoPanel.add(descLabel, BorderLayout.CENTER);

        panel.add(infoPanel, BorderLayout.CENTER);

        // Place button
        JButton placeButton = new JButton("Place Machine");
        placeButton.addActionListener(e -> startMachinePlacement(type));
        panel.add(placeButton, BorderLayout.EAST);

        return panel;
    }

    private void startMachinePlacement(MachineType type) {
        // First, check if we still have this machine available
        if (game.getPlayer().getInventory().getUnplacedMachineCount(type) > 0) {
            // Start the placement process using ControlPanel's method
            controlPanel.startMachinePlacement(type);
            
            // Add callback via game for when placement is successful
            game.startMachinePlacement(type, () -> {
                // On successful placement, remove from inventory and update UI
                game.getPlayer().getInventory().removeMachine(type);
                updateMachineLists();
            });
        }
    }

    private JPanel createMachinePanel(Machine machine) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getStatusColor(machine.getStatus())),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Machine Type and Location
        JPanel headerPanel = new JPanel(new BorderLayout());
        String machineTitle = String.format("%s at %s", 
            formatMachineType(machine), 
            machine.getPosition().toString());
        headerPanel.add(new JLabel(machineTitle), BorderLayout.WEST);
        
        // Status indicator
        JLabel statusLabel = new JLabel(machine.getStatusMessage());
        statusLabel.setForeground(getStatusColor(machine.getStatus()));
        headerPanel.add(statusLabel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Machine Controls and Status
        JPanel controlsPanel = new JPanel(new BorderLayout());
        
        // Progress bar for factories
        if (machine instanceof Factory) {
            Factory factory = (Factory) machine;
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setValue((int)(factory.getCraftingProgress() * 100));
            controlsPanel.add(progressBar, BorderLayout.CENTER);
        }

        // Machine-specific controls
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        if (machine instanceof Harvester) {
            addHarvesterControls(buttonsPanel, (Harvester) machine);
        } else if (machine instanceof Factory) {
            addFactoryControls(buttonsPanel, (Factory) machine);
        }

        // Add collect resources button
        JButton collectButton = new JButton("Collect Resources");
        collectButton.setEnabled(machine.getInventory().getTotalItems() > 0);
        collectButton.addActionListener(e -> handleResourceCollection(machine));
        buttonsPanel.add(collectButton);

        controlsPanel.add(buttonsPanel, BorderLayout.EAST);
        panel.add(controlsPanel, BorderLayout.CENTER);

        // Inventory Display
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        JProgressBar inventoryBar = new JProgressBar(0, machine.getInventoryCapacity());
        inventoryBar.setValue(machine.getInventory().getTotalItems());
        inventoryBar.setStringPainted(true);
        inventoryBar.setString(String.format("Inventory: %d/%d", 
            machine.getInventory().getTotalItems(), 
            machine.getInventoryCapacity()));
        inventoryPanel.add(inventoryBar, BorderLayout.CENTER);
        panel.add(inventoryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private Color getStatusColor(MachineStatus status) {
        switch (status) {
            case WORKING:
                return new Color(50, 205, 50);
            case INVENTORY_FULL:
                return new Color(255, 0, 0);
            case INVENTORY_NEARLY_FULL:
                return new Color(255, 165, 0);
            case NEEDS_CONFIG:
                return new Color(255, 215, 0);
            case INSUFFICIENT_RESOURCES:
                return new Color(255, 69, 0);
            case NO_RESOURCES:
                return new Color(128, 128, 128);
            default:
                return new Color(100, 100, 100);
        }
    }

    private void addHarvesterControls(JPanel panel, Harvester harvester) {
        JComboBox<ResourceType> resourceSelect = new JComboBox<>(
            Arrays.stream(ResourceType.values())
                  .filter(r -> r.getBaseHarvestTime() > 0)
                  .toArray(ResourceType[]::new)
        );
        resourceSelect.setSelectedItem(harvester.getTargetResource());
        resourceSelect.addActionListener(e -> harvester.setTargetResource(
            (ResourceType) resourceSelect.getSelectedItem()));
        panel.add(resourceSelect);
    }

    private void addFactoryControls(JPanel panel, Factory factory) {
        JButton recipeButton = new JButton("Set Recipe");
        recipeButton.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            for (Recipe recipe : game.getCraftingSystem().getAllRecipes()) {
                JMenuItem item = new JMenuItem(recipe.getName());
                item.addActionListener(ev -> {
                    factory.setRecipe(recipe);
                    updateMachineList();
                });
                menu.add(item);
            }
            menu.show(recipeButton, 0, recipeButton.getHeight());
        });
        panel.add(recipeButton);
    }


    private void handleResourceCollection(Machine machine) {
        if (machine.getInventory().getTotalItems() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No resources to collect!",
                    "Empty Inventory",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Transfer all resources from machine to player
        for (ResourceType type : ResourceType.values()) {
            int amount = machine.getInventory().getResourceCount(type);
            if (amount > 0) {
                if (game.getPlayer().getInventory().hasSpace(amount)) {
                    machine.getInventory().removeResource(type, amount);
                    game.getPlayer().getInventory().addResource(type, amount);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Not enough space in player inventory!",
                            "Inventory Full",
                            JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }

        // Update UI
        controlPanel.updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
        updateMachineList();
    }

    private void handleMachineRemoval(Machine machine) {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this machine?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Collect any remaining resources first
            handleResourceCollection(machine);
            // Remove the machine
            game.removeMachine(machine.getPosition());
            updateMachineList();
        }
    }

    private String formatMachineType(Machine machine) {
        if (machine instanceof Harvester) {
            return "Harvester" + (machine.getType().toString().contains("ADVANCED") ? " (Advanced)" : " (Basic)");
        } else if (machine instanceof Factory) {
            return "Factory" + (machine.getType().toString().contains("ADVANCED") ? " (Advanced)" : " (Basic)");
        }
        return "Unknown Machine";
    }

    private String formatInventory(Machine machine) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (ResourceType type : ResourceType.values()) {
            int count = machine.getInventory().getResourceCount(type);
            if (count > 0) {
                if (!first)
                    sb.append(", ");
                sb.append(type).append(": ").append(count);
                first = false;
            }
        }

        return sb.length() > 0 ? sb.toString() : "Empty";
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateMachineList());
        updateTimer.start();
    }

    public void destroy() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}