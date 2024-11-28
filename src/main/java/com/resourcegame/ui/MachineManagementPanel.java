package com.resourcegame.ui;

import com.resourcegame.core.Game;
import com.resourcegame.entities.*;
import com.resourcegame.systems.*;
import com.resourcegame.utils.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class MachineManagementPanel extends JPanel {
    private final Game game;
    private final ControlPanel controlPanel;
    private Timer updateTimer;
    private static final int UPDATE_INTERVAL = 1000;
    private JPanel machineListPanel;
    private JPanel unplacedMachinesPanel;
    private JPanel statsPanel;
    private JTabbedPane tabbedPane;
    private JLabel totalMachinesLabel;
    private JComboBox<String> filterComboBox;
    private JTextField searchField;
    private MachineStatistics statistics;
    private String currentSearchTerm = "";


    public MachineManagementPanel(Game game, ControlPanel controlPanel) {
        this.game = game;
        this.controlPanel = controlPanel;
        this.statistics = new MachineStatistics();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initializeComponents();
        setupUpdateTimer();
    }
    private void initializeComponents() {
        // Header Panel with better spacing
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // Overview Tab
        JPanel overviewPanel = createOverviewPanel();
        tabbedPane.addTab("Overview", new ImageIcon(), overviewPanel);

        // Active Machines Tab
        JPanel activeMachinesPanel = createActiveMachinesPanel();
        tabbedPane.addTab("Active Machines", new ImageIcon(), activeMachinesPanel);

        // Available Machines Tab
        JPanel availableMachinesPanel = createAvailableMachinesPanel();
        tabbedPane.addTab("Available Machines", new ImageIcon(), availableMachinesPanel);

        add(tabbedPane, BorderLayout.CENTER);
        
        // Set minimum size to prevent congestion
        setMinimumSize(new Dimension(800, 600));
    }

    private void updateMachineList() {
        machineListPanel.removeAll();
        unplacedMachinesPanel.removeAll();
            
        // Add placed machines to Active Machines tab
        List<Machine> placedMachines = game.getMachineManager().getAllMachines();
        for (Machine machine : placedMachines) {
            if (matchesSearch(machine) && matchesFilter(machine)) {
                machineListPanel.add(createMachinePanel(machine));
                machineListPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        // Add unplaced machines to Available Machines tab
        Map<MachineType, Integer> unplacedMachines = game.getPlayer().getInventory().getUnplacedMachines();
        for (Map.Entry<MachineType, Integer> entry : unplacedMachines.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                Machine tempMachine = entry.getKey().toString().contains("HARVESTER") ?
                    new Harvester(new Position(-1, -1), entry.getKey()) :
                    new Factory(new Position(-1, -1), entry.getKey());
                
                if (matchesSearch(tempMachine) && matchesFilter(tempMachine)) {
                    unplacedMachinesPanel.add(createMachinePanel(tempMachine));
                    unplacedMachinesPanel.add(Box.createVerticalStrut(10));
                }
            }
        }
    
        machineListPanel.revalidate();
        machineListPanel.repaint();
        unplacedMachinesPanel.revalidate();
        unplacedMachinesPanel.repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();

        // Title and Count Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Machine Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalMachinesLabel = new JLabel();
        totalMachinesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalStrut(20));
        titlePanel.add(totalMachinesLabel);

        // Add title panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        headerPanel.add(titlePanel, gbc);

        // Controls Panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel filterLabel = new JLabel("Filter: ");
        filterComboBox = new JComboBox<>(new String[] {
                "All Machines", "Harvesters", "Factories",
                "Needs Maintenance", "Working"
        });
        filterComboBox.setPreferredSize(new Dimension(150, 25));
        filterComboBox.addActionListener(e -> updateMachineList());

        // Search Panel with better layout
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(150, 25));
        searchField.putClientProperty("JTextField.placeholderText", "Search machines...");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            public void changedUpdate(DocumentEvent e) { updateSearch(); }
        });

        // Add filter and search components
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel controlsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsWrapper.add(filterLabel);
        controlsWrapper.add(filterComboBox);
        controlsWrapper.add(new JLabel("Search: "));
        controlsWrapper.add(searchField);
        headerPanel.add(controlsWrapper, gbc);

        return headerPanel;
    }


    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Statistics Panel
        statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Production Statistics"),
                new EmptyBorder(10, 10, 10, 10)));

        statsPanel.add(createStatCard("Total Production", "0", "items/min"));
        statsPanel.add(createStatCard("Active Machines", "0", "machines"));
        statsPanel.add(createStatCard("Maintenance Required", "0", "machines"));
        statsPanel.add(createStatCard("Resources Collected", "0", "items"));

        panel.add(statsPanel, BorderLayout.NORTH);

        // Quick Actions Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        actionsPanel.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder("Quick Actions"),
                new EmptyBorder(10, 10, 10, 10)));

        JButton maintainAllButton = new JButton("Maintain All");
        maintainAllButton.addActionListener(e -> performBulkMaintenance());

        JButton collectAllButton = new JButton("Collect All Resources");
        collectAllButton.addActionListener(e -> performBulkCollection());

        actionsPanel.add(maintainAllButton);
        actionsPanel.add(collectAllButton);

        panel.add(actionsPanel, BorderLayout.CENTER);
        return panel;
    }

    private void handleMaintenance(Machine machine) {
        int cost = calculateMaintenanceCost(machine);
        int response = JOptionPane.showConfirmDialog(
                this,
                String.format("Maintenance cost: $%d\nProceed with maintenance?", cost),
                "Confirm Maintenance",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            if (game.getPlayer().getInventory().getMoney() >= cost) {
                game.getPlayer().getInventory().removeMoney(cost);
                machine.performMaintenance();
                updateMachineList();
                controlPanel.updateMoneyDisplay(game.getPlayer().getInventory().getMoney());
                JOptionPane.showMessageDialog(
                        this,
                        "Maintenance completed successfully!",
                        "Maintenance",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Insufficient funds for maintenance!",
                        "Maintenance Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int calculateMaintenanceCost(Machine machine) {
        // Base cost is 10% of machine's original price
        int baseCost = (int) (machine.getType().getBasePrice() * 0.1);
        // Add additional cost based on operations since last maintenance
        int operationsCost = machine.getOperationsSinceMaintenance() * 2;
        return baseCost + operationsCost;
    }

    private JPanel createStatCard(String title, String value, String unit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 10, 10, 10)));
        card.setBackground(new Color(250, 250, 250));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(new Color(70, 130, 180));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel unitLabel = new JLabel(unit);
        unitLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        unitLabel.setForeground(new Color(150, 150, 150));
        unitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(3));
        card.add(unitLabel);

        return card;
    }

    private JPanel createActiveMachinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        machineListPanel = new JPanel();
        machineListPanel.setLayout(new BoxLayout(machineListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(machineListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAvailableMachinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        unplacedMachinesPanel = new JPanel();
        unplacedMachinesPanel.setLayout(new BoxLayout(unplacedMachinesPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(unplacedMachinesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private String formatMachineName(Machine machine) {
        if (machine instanceof Harvester) {
            return "Harvester" + (machine.getType().toString().contains("ADVANCED") ? " (Advanced)"
                    : machine.getType().toString().contains("FRAGILE") ? " (Fragile)" : " (Basic)");
        } else if (machine instanceof Factory) {
            return "Factory" + (machine.getType().toString().contains("ADVANCED") ? " (Advanced)"
                    : machine.getType().toString().contains("FRAGILE") ? " (Fragile)" : " (Basic)");
        }
        return "Unknown Machine";
    }

    private void addHarvesterControls(JPanel panel, Harvester harvester) {
        JButton configButton = new JButton("Set Resource Target");
        configButton.setEnabled(harvester.canBeReconfigured());
        configButton.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(panel);
            JDialog dialog;

            if (parentWindow instanceof Frame) {
                dialog = new JDialog((Frame) parentWindow, "Select Resource", true);
            } else if (parentWindow instanceof Dialog) {
                dialog = new JDialog((Dialog) parentWindow, "Select Resource", true);
            } else {
                dialog = new JDialog();
                dialog.setTitle("Select Resource");
                dialog.setModal(true);
            }

            JPanel resourcePanel = new JPanel();
            resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.Y_AXIS));

            for (ResourceType type : ResourceType.values()) {
                if (type.getBaseHarvestTime() > 0) {
                    JButton resourceButton = new JButton(type.toString());
                    if (type == harvester.getTargetResource()) {
                        resourceButton.setBackground(new Color(200, 255, 200));
                    }
                    resourceButton.addActionListener(event -> {
                        harvester.setTargetResource(type);
                        dialog.dispose();
                        updateMachineList();
                    });
                    resourcePanel.add(resourceButton);
                    resourcePanel.add(Box.createVerticalStrut(5));
                }
            }

            dialog.add(resourcePanel);
            dialog.pack();
            dialog.setLocationRelativeTo(panel);
            dialog.setVisible(true);
        });

        panel.add(configButton);
    }

    private void addFactoryControls(JPanel panel, Factory factory) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton configButton = new JButton("Set Recipe");
        configButton.setEnabled(factory.canBeReconfigured());
        configButton.addActionListener(e -> showRecipeDialog(panel, factory));

        JButton transferButton = new JButton("Transfer Resources");
        transferButton.addActionListener(e -> showTransferDialog(panel, factory));

        buttonPanel.add(configButton);
        buttonPanel.add(transferButton);
        panel.add(buttonPanel);
    }


    private void addPlaceMachineButton(JPanel buttonsPanel, Machine machine) {
        JButton placeButton = new JButton("Place Machine");
        placeButton.setBackground(new Color(46, 204, 113));
        placeButton.setForeground(Color.WHITE);
        placeButton.addActionListener(e -> {
            // Close the management panel
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JDialog) {
                window.dispose();
            }
            
            // Remove the machine from inventory before placement
            game.getPlayer().getInventory().removeMachine(machine.getType());
            
            // Start placement process
            controlPanel.startMachinePlacement(machine.getType());
        });
        buttonsPanel.add(placeButton);
    }


    private void showRecipeDialog(JPanel panel, Factory factory) {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        JDialog dialog;

        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Select Recipe", true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "Select Recipe", true);
        } else {
            dialog = new JDialog();
            dialog.setTitle("Select Recipe");
            dialog.setModal(true);
        }

        JPanel recipePanel = new JPanel();
        recipePanel.setLayout(new BoxLayout(recipePanel, BoxLayout.Y_AXIS));

        for (Recipe recipe : game.getCraftingSystem().getAllRecipes()) {
            JPanel recipeItemPanel = new JPanel(new BorderLayout(10, 5));
            recipeItemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Recipe name and selection button
            JButton recipeButton = new JButton(recipe.getName());
            if (recipe.equals(factory.getSelectedRecipe())) {
                recipeButton.setBackground(new Color(200, 255, 200));
            }
            recipeButton.addActionListener(event -> {
                factory.setRecipe(recipe);
                dialog.dispose();
                updateMachineList();
            });
            recipeItemPanel.add(recipeButton, BorderLayout.NORTH);

            // Recipe description
            JTextArea description = new JTextArea(recipe.getDescription());
            description.setFont(new Font("Arial", Font.ITALIC, 12));
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setEditable(false);
            description.setOpaque(false);
            description.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            recipeItemPanel.add(description, BorderLayout.CENTER);

            // Recipe requirements
            JPanel requirementsPanel = new JPanel(new GridLayout(0, 1));
            requirementsPanel.setBorder(BorderFactory.createTitledBorder("Requirements"));

            for (Map.Entry<ResourceType, Integer> ingredient : recipe.getIngredients().entrySet()) {
                requirementsPanel.add(new JLabel(String.format("• %s: %d",
                        ingredient.getKey(), ingredient.getValue())));
            }
            recipeItemPanel.add(requirementsPanel, BorderLayout.SOUTH);

            recipePanel.add(recipeItemPanel);
            recipePanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scrollPane = new JScrollPane(recipePanel);
        scrollPane.setPreferredSize(new Dimension(400, Math.min(500, recipePanel.getPreferredSize().height)));
        dialog.add(scrollPane);
        dialog.pack();
        dialog.setLocationRelativeTo(panel);
        dialog.setVisible(true);
    }

    private void showTransferDialog(JPanel panel, Factory factory) {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        JDialog dialog;

        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Transfer Resources", true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "Transfer Resources", true);
        } else {
            dialog = new JDialog();
            dialog.setTitle("Transfer Resources");
            dialog.setModal(true);
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (factory.getSelectedRecipe() == null) {
            mainPanel.add(new JLabel("Please set a recipe first!"));
        } else {
            Recipe recipe = factory.getSelectedRecipe();

            // Recipe info panel
            JPanel recipeInfoPanel = new JPanel(new BorderLayout());
            recipeInfoPanel.add(new JLabel("Recipe: " + recipe.getName()), BorderLayout.NORTH);
            mainPanel.add(recipeInfoPanel);
            mainPanel.add(Box.createVerticalStrut(10));

            // Add each required resource
            for (Map.Entry<ResourceType, Integer> ingredient : recipe.getIngredients().entrySet()) {
                ResourceType type = ingredient.getKey();
                int required = ingredient.getValue();
                int inFactory = factory.getInventory().getResourceCount(type);
                int inPlayer = game.getPlayer().getInventory().getResourceCount(type);

                JPanel resourcePanel = new JPanel(new BorderLayout(5, 0));
                resourcePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                // Resource info
                JPanel infoPanel = new JPanel(new BorderLayout());
                infoPanel.add(new JLabel(type.toString()), BorderLayout.NORTH);
                JLabel statusLabel = new JLabel(String.format("Required: %d  |  In Factory: %d  |  Available: %d",
                        required, inFactory, inPlayer));
                statusLabel.setForeground(inFactory >= required ? new Color(0, 150, 0) : Color.BLACK);
                infoPanel.add(statusLabel, BorderLayout.CENTER);
                resourcePanel.add(infoPanel, BorderLayout.CENTER);

                // Transfer controls
                if (inPlayer > 0) {
                    JPanel transferPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JSpinner amountSpinner = new JSpinner(new SpinnerNumberModel(
                            Math.min(required - inFactory, inPlayer), // initial value
                            1, // min
                            inPlayer, // max
                            1 // step
                    ));
                    amountSpinner.setPreferredSize(new Dimension(60, 25));

                    JButton transferBtn = new JButton("Transfer");
                    transferBtn.addActionListener(e -> {
                        int amount = (Integer) amountSpinner.getValue();
                        game.getPlayer().getInventory().removeResource(type, amount);
                        factory.getInventory().addResource(type, amount);
                        dialog.dispose();

                        // Update displays
                        controlPanel.updateInventoryDisplay(
                                game.getPlayer().getInventory().getInventoryDisplay());
                        updateMachineList();
                    });

                    transferPanel.add(amountSpinner);
                    transferPanel.add(transferBtn);
                    resourcePanel.add(transferPanel, BorderLayout.EAST);
                }

                mainPanel.add(resourcePanel);
            }
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setPreferredSize(new Dimension(500, Math.min(400, mainPanel.getPreferredSize().height)));
        dialog.add(scrollPane);

        dialog.pack();
        dialog.setLocationRelativeTo(panel);
        dialog.setVisible(true);
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
        boolean collectedAny = false;
        StringBuilder collectionReport = new StringBuilder("Collected:\n");

        for (ResourceType type : ResourceType.values()) {
            int amount = machine.getInventory().getResourceCount(type);
            if (amount > 0) {
                if (game.getPlayer().getInventory().hasSpace(amount)) {
                    machine.getInventory().removeResource(type, amount);
                    game.getPlayer().getInventory().addResource(type, amount);
                    statistics.recordResourceCollection(type, amount);
                    collectionReport.append(String.format("%s: %d\n", type, amount));
                    collectedAny = true;
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Not enough space in player inventory!",
                            "Inventory Full",
                            JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }

        if (collectedAny) {
            JOptionPane.showMessageDialog(this,
                    collectionReport.toString(),
                    "Collection Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // Update UI
        controlPanel.updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
        updateMachineList();
    }

    private JPanel createMachinePanel(Machine machine) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getStatusColor(machine.getStatus())),
                new EmptyBorder(5, 5, 5, 5)));

        // Header Panel with type, location, and config info
        JPanel headerPanel = new JPanel(new BorderLayout());
        String machineTitle = String.format("%s at %s",
                formatMachineName(machine),
                machine.getPosition().toString());
        JLabel nameLabel = new JLabel(machineTitle);
        headerPanel.add(nameLabel, BorderLayout.WEST);

        // Configuration limit info
        if (machine.getType().getConfigurationLimit() > 0) {
            JLabel configLabel = new JLabel(String.format("Configs left: %d",
                    machine.getRemainingConfigurations()));
            configLabel.setForeground(machine.getRemainingConfigurations() > 0 ? new Color(0, 100, 0) : Color.RED);
            headerPanel.add(configLabel, BorderLayout.EAST);
        }

        panel.add(headerPanel, BorderLayout.NORTH);

        // Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        JLabel statusLabel = new JLabel(machine.getStatusMessage());
        statusLabel.setForeground(getStatusColor(machine.getStatus()));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Maintenance Info for fragile machines
        if (machine.getType().isFragile()) {
            JPanel maintenancePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            int threshold = Machine.getMaintenanceThreshold(machine.getType());
            JLabel opsLabel = new JLabel(String.format("Operations: %d/%d",
                    machine.getOperationsSinceMaintenance(),
                    threshold));

            JButton maintainButton = new JButton("Maintain");
            maintainButton.setEnabled(machine.needsMaintenance());
            maintainButton.addActionListener(e -> handleMaintenance(machine));

            if (machine.needsMaintenance()) {
                opsLabel.setForeground(Color.RED);
                maintainButton.setBackground(new Color(255, 99, 71));
                maintainButton.setForeground(Color.WHITE);
            } else if (machine.getOperationsSinceMaintenance() > threshold * 0.7) {
                opsLabel.setForeground(new Color(255, 140, 0));
            }

            maintenancePanel.add(opsLabel);
            maintenancePanel.add(maintainButton);
            statusPanel.add(maintenancePanel, BorderLayout.EAST);
        }

        panel.add(statusPanel, BorderLayout.CENTER);

        // Controls Panel
        JPanel controlsPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Add Place Machine button for unplaced machines
       if (machine.getPosition().getX() == -1) {
        addPlaceMachineButton(buttonsPanel, machine);
    } else {
        if (machine instanceof Harvester) {
            addHarvesterControls(buttonsPanel, (Harvester) machine);
        } else if (machine instanceof Factory) {
            addFactoryControls(buttonsPanel, (Factory) machine);
        }

        JButton collectButton = new JButton("Collect Resources");
        collectButton.setEnabled(machine.getInventory().getTotalItems() > 0);
        collectButton.addActionListener(e -> handleResourceCollection(machine));
        buttonsPanel.add(collectButton);
    }

        controlsPanel.add(buttonsPanel, BorderLayout.EAST);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void performBulkMaintenance() {
        List<Machine> machines = game.getMachineManager().getAllMachines();
        int totalCost = 0;
        List<Machine> needMaintenance = new ArrayList<>();

        for (Machine machine : machines) {
            if (machine.needsMaintenance()) {
                totalCost += calculateMaintenanceCost(machine);
                needMaintenance.add(machine);
            }
        }

        if (needMaintenance.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No machines need maintenance!",
                    "Bulk Maintenance",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(this,
                String.format("Maintain %d machines for $%d?",
                        needMaintenance.size(), totalCost),
                "Confirm Bulk Maintenance",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            if (game.getPlayer().getInventory().getMoney() >= totalCost) {
                game.getPlayer().getInventory().removeMoney(totalCost);

                for (Machine machine : needMaintenance) {
                    machine.performMaintenance();
                }

                updateMachineList();
                controlPanel.updateMoneyDisplay(
                        game.getPlayer().getInventory().getMoney());

                JOptionPane.showMessageDialog(this,
                        String.format("Successfully maintained %d machines!",
                                needMaintenance.size()),
                        "Bulk Maintenance",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Insufficient funds for bulk maintenance!",
                        "Maintenance Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performBulkCollection() {
        List<Machine> machines = game.getMachineManager().getAllMachines();
        Map<ResourceType, Integer> totalResources = new HashMap<>();
        int totalItems = 0;

        // Calculate total resources to collect
        for (Machine machine : machines) {
            Inventory machineInv = machine.getInventory();
            for (ResourceType type : ResourceType.values()) {
                int amount = machineInv.getResourceCount(type);
                if (amount > 0) {
                    totalResources.merge(type, amount, Integer::sum);
                    totalItems += amount;
                }
            }
        }

        if (totalItems == 0) {
            JOptionPane.showMessageDialog(this,
                    "No resources to collect!",
                    "Bulk Collection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Check player inventory space
        if (!game.getPlayer().getInventory().hasSpace(totalItems)) {
            JOptionPane.showMessageDialog(this,
                    "Not enough inventory space to collect all resources!",
                    "Collection Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Perform collection
        StringBuilder collectionReport = new StringBuilder("Collected:\n");
        for (Machine machine : machines) {
            Inventory machineInv = machine.getInventory();
            for (ResourceType type : ResourceType.values()) {
                int amount = machineInv.getResourceCount(type);
                if (amount > 0) {
                    machineInv.removeResource(type, amount);
                    game.getPlayer().getInventory().addResource(type, amount);
                    statistics.recordResourceCollection(type, amount);
                    collectionReport.append(String.format("%s: %d\n", type, amount));
                }
            }
        }

        updateMachineList();
        controlPanel.updateInventoryDisplay(
                game.getPlayer().getInventory().getInventoryDisplay());

        JOptionPane.showMessageDialog(this,
                collectionReport.toString(),
                "Collection Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateSearch() {
        currentSearchTerm = searchField.getText().toLowerCase().trim();
        updateMachineList();
    }

    private boolean matchesSearch(Machine machine) {
        if (currentSearchTerm.isEmpty())
            return true;

        String searchContent = String.format("%s %s %s",
                formatMachineName(machine),
                machine.getPosition().toString(),
                machine.getStatusMessage()).toLowerCase();

        return searchContent.contains(currentSearchTerm);
    }

    private boolean matchesFilter(Machine machine) {
        String filter = (String) filterComboBox.getSelectedItem();
        if (filter == null || filter.equals("All Machines"))
            return true;

        switch (filter) {
            case "Harvesters":
                return machine instanceof Harvester;
            case "Factories":
                return machine instanceof Factory;
            case "Needs Maintenance":
                return machine.needsMaintenance();
            case "Working":
                return machine.isWorking();
            default:
                return true;
        }
    }

    private void updateStatistics() {
        updateStatCard("Total Production",
                String.format("%.1f", statistics.getTotalProductionRatePerMinute()),
                "items/min");

        updateStatCard("Resources Collected",
                String.valueOf(statistics.getTotalResourcesCollected()),
                "items");

        int activeMachines = (int) game.getMachineManager().getAllMachines()
                .stream().filter(Machine::isWorking).count();
        updateStatCard("Active Machines",
                String.valueOf(activeMachines),
                "machines");

        int maintenanceNeeded = (int) game.getMachineManager().getAllMachines()
                .stream().filter(Machine::needsMaintenance).count();
        updateStatCard("Maintenance Required",
                String.valueOf(maintenanceNeeded),
                "machines");
    }

    private void updateStatCard(String title, String value, String unit) {
        for (Component comp : statsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                JLabel titleLabel = findLabel(card, title);
                if (titleLabel != null) {
                    JLabel valueLabel = findValueLabel(card);
                    if (valueLabel != null) {
                        valueLabel.setText(value);
                    }
                    break;
                }
            }
        }
    }

    private JLabel findLabel(JPanel panel, String text) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel && ((JLabel) comp).getText().equals(text)) {
                return (JLabel) comp;
            }
        }
        return null;
    }

    private JLabel findValueLabel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                Font font = label.getFont();
                if (font.getSize() == 24 && font.isBold()) {
                    return label;
                }
            }
        }
        return null;
    }

    private void setupUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        updateTimer = new Timer(UPDATE_INTERVAL, e -> {
            updateMachineList();
            updateStatistics();
            updateMachineStatistics();
        });
        updateTimer.start();
    }

    private void updateMachineStatistics() {
        List<Machine> placedMachines = game.getMachineManager().getAllMachines();
        Map<MachineType, Integer> unplacedMachines = game.getPlayer().getInventory().getUnplacedMachines();
        
        int totalPlaced = placedMachines.size();
        int totalUnplaced = unplacedMachines.values().stream().mapToInt(Integer::intValue).sum();
        int needsMaintenance = (int) placedMachines.stream().filter(Machine::needsMaintenance).count();
        int working = (int) placedMachines.stream().filter(Machine::isWorking).count();
    
        totalMachinesLabel.setText(String.format(
            "Total Machines: %d | Working: %d | Needs Maintenance: %d | Available: %d",
            totalPlaced + totalUnplaced, working, needsMaintenance, totalUnplaced));
    }

    public void destroy() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
    }

    // Utility method to get status colors
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
            case CONFIG_LIMIT_REACHED:
                return new Color(128, 0, 128);
            case NEEDS_MAINTENANCE:
                return new Color(255, 69, 0);
            case INSUFFICIENT_RESOURCES:
                return new Color(255, 69, 0);
            case NO_RESOURCES:
                return new Color(128, 128, 128);
            default:
                return new Color(100, 100, 100);
        }
    }

}