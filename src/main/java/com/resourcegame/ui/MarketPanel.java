package com.resourcegame.ui;

import com.resourcegame.systems.Market;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.entities.Inventory;
import com.resourcegame.entities.Machine;
import com.resourcegame.core.Game;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;
import java.util.HashMap;
import java.util.Map;

public class MarketPanel extends JPanel {
    private final Market market;
    private final Inventory playerInventory;
    private final ControlPanel controlPanel;
    private final JLabel moneyLabel;
    private final Map<ResourceType, JSpinner> quantitySpinners;
    private final Map<ResourceType, JLabel> stockLabels;
    private final Map<ResourceType, JLabel> priceLabels;
    private final Map<ResourceType, JLabel> inventoryLabels;
    private Timer updateTimer;
    private JTabbedPane tabbedPane;
    private Map<MachineType, JButton> machineButtons;
    public MarketPanel(Game game, Market market, Inventory playerInventory, ControlPanel controlPanel) {
        this.market = market;
        this.playerInventory = playerInventory;
        this.controlPanel = controlPanel;
        this.quantitySpinners = new HashMap<>();
        this.stockLabels = new HashMap<>();
        this.priceLabels = new HashMap<>();
        this.inventoryLabels = new HashMap<>();
        this.machineButtons = new HashMap<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header Panel with money display
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        moneyLabel = new JLabel("Money: $" + playerInventory.getMoney(), SwingConstants.RIGHT);
        moneyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(moneyLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Resources Tab
        JPanel resourcesPanel = createResourcesPanel();
        tabbedPane.addTab("Resources", new ImageIcon(), resourcesPanel, "Buy and sell resources");

        // Machines Tab
        JPanel machinesPanel = createMachinesPanel();
        tabbedPane.addTab("Machines", new ImageIcon(), machinesPanel, "Purchase machines");

        add(tabbedPane, BorderLayout.CENTER);
        setupUpdateTimer();
    }

    private JPanel createResourcesPanel() {
        JPanel tradingPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        tradingPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (ResourceType type : ResourceType.values()) {
            if (type.getBaseHarvestTime() > 0) {
                tradingPanel.add(createResourcePanel(type));
            }
        }

        JScrollPane scrollPane = new JScrollPane(tradingPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel resourcesPanel = new JPanel(new BorderLayout());
        resourcesPanel.add(scrollPane, BorderLayout.CENTER);
        return resourcesPanel;
    }

    private JPanel createMachinesPanel() {
        JPanel machinesPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        machinesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (MachineType type : MachineType.values()) {
            machinesPanel.add(createMachineCard(type));
        }

        JScrollPane scrollPane = new JScrollPane(machinesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(scrollPane, BorderLayout.CENTER);
        return containerPanel;
    }

    private JPanel createMachineCard(MachineType type) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));
    
        // Header with name and specs
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(formatMachineName(type.name()));
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(nameLabel, BorderLayout.WEST);
    
        // Price section with comparison if fragile
        JPanel pricePanel = new JPanel(new GridLayout(type.isFragile() ? 2 : 1, 1));
        if (type.isFragile()) {
            MachineType regularVersion = getRegularVersion(type);
            JLabel savingsLabel = new JLabel(String.format("Save $%d",
                    regularVersion.getBasePrice() - type.getBasePrice()));
            savingsLabel.setForeground(new Color(0, 128, 0));
            savingsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            pricePanel.add(savingsLabel);
        }
    
        JLabel priceLabel = new JLabel("Price: $" + type.getBasePrice());
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        pricePanel.add(priceLabel);
        headerPanel.add(pricePanel, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);
    
        // Description and specs panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
    
        JTextArea description = new JTextArea(type.getDescription());
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setOpaque(false);
        description.setEditable(false);
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsPanel.add(description);
        detailsPanel.add(Box.createVerticalStrut(10));
    
        // Specifications panel
        JPanel specsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        specsPanel.setBorder(BorderFactory.createTitledBorder("Specifications"));
    
        String capacityText = String.format("Storage: %d items",
                type.toString().contains("ADVANCED") ? 200 : 100);
        specsPanel.add(new JLabel(capacityText));
    
        String speedText = String.format("Speed: %sx",
                type.toString().contains("ADVANCED") ? "2" : "1");
        specsPanel.add(new JLabel(speedText));
    
        String configText = type.getConfigurationLimit() == 0 ? "Configurations: Unlimited"
                : String.format("Configurations: %d", type.getConfigurationLimit());
        specsPanel.add(new JLabel(configText));
    
        // Maintenance info for fragile machines
        if (type.isFragile()) {
            JPanel maintenancePanel = new JPanel(new GridLayout(3, 1));
            maintenancePanel.setBorder(BorderFactory.createTitledBorder("Maintenance Details"));
    
            JLabel breakdownLabel = new JLabel(String.format("Breakdown chance: %.0f%%",
                    type.getBreakdownChance() * 100));
            JLabel maintenanceFreqLabel = new JLabel(String.format("Maintenance every %d operations",
                    Machine.FRAGILE_MAINTENANCE_THRESHOLD));
    
            int baseMaintCost = (int) (type.getBasePrice() * 0.1);
            JLabel maintCostLabel = new JLabel(String.format("Base maintenance cost: $%d",
                    baseMaintCost));
    
            maintenancePanel.add(breakdownLabel);
            maintenancePanel.add(maintenanceFreqLabel);
            maintenancePanel.add(maintCostLabel);
            specsPanel.add(maintenancePanel);
        }
    
        detailsPanel.add(specsPanel);
        card.add(detailsPanel, BorderLayout.CENTER);
    
        // Purchase button panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
    
        if (type.isFragile()) {
            JLabel warningLabel = new JLabel("⚠ Requires maintenance every " + 
                Machine.FRAGILE_MAINTENANCE_THRESHOLD + " operations");
            warningLabel.setForeground(new Color(255, 140, 0));
            warningLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            buttonPanel.add(warningLabel, BorderLayout.NORTH);
        }
    
        JButton buyButton = new JButton("Purchase Machine");
        buyButton.setBackground(new Color(46, 204, 113));
        buyButton.setForeground(Color.WHITE);
        buyButton.addActionListener(e -> handleMachinePurchase(type));
        machineButtons.put(type, buyButton);
        buttonPanel.add(buyButton, BorderLayout.CENTER);
    
        card.add(buttonPanel, BorderLayout.SOUTH);
        return card;
    }

    private MachineType getRegularVersion(MachineType fragileType) {
        switch (fragileType) {
            case FRAGILE_HARVESTER:
                return MachineType.BASIC_HARVESTER;
            case FRAGILE_FACTORY:
                return MachineType.BASIC_FACTORY;
            default:
                return fragileType;
        }
    }

    private String formatMachineName(String name) {
        return name.replace("_", " ").toLowerCase()
                .substring(0, 1).toUpperCase() +
                name.replace("_", " ").toLowerCase().substring(1);
    }

    private void handleMachinePurchase(MachineType type) {
        int price = type.getBasePrice();
    
        if (playerInventory.getMoney() >= price) {
            if (market.purchaseMachine(type, playerInventory)) {
                updateDisplay();
                
                int response = JOptionPane.showConfirmDialog(
                    this,
                    "Machine purchased! Would you like to place it now?",
                    "Place Machine",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
    
                if (response == JOptionPane.YES_OPTION) {
                    // Close the market panel first
                    Window window = SwingUtilities.getWindowAncestor(this);
                    if (window instanceof JDialog) {
                        window.dispose();
                    }
                    
                    // Start placement process
                    SwingUtilities.invokeLater(() -> {
                        controlPanel.startMachinePlacement(type);
                    });
                } else {
                    showNotification("Machine added to inventory. You can place it later from the Machine Management panel.", true);
                }
    
                // Update displays
                if (controlPanel != null) {
                    controlPanel.updateInventoryDisplay(playerInventory.getInventoryDisplay());
                    controlPanel.updateMoneyDisplay(playerInventory.getMoney());
                }
            } else {
                showNotification("Failed to purchase machine", false);
            }
        } else {
            showNotification("Not enough money to purchase this machine", false);
        }
    }

    private JPanel createResourcePanel(ResourceType type) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));

        // Resource header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel nameLabel = new JLabel(type.toString());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(nameLabel, BorderLayout.WEST);

        // Stock display
        JLabel stockLabel = new JLabel("Stock: " + market.getStock(type));
        stockLabels.put(type, stockLabel);
        headerPanel.add(stockLabel, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        // Center panel with prices and controls
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 5));

        // Buy price
        JLabel buyPriceLabel = new JLabel("Buy: $" + market.getBuyPrice(type));
        priceLabels.put(type, buyPriceLabel);
        centerPanel.add(buyPriceLabel);

        // Sell price
        centerPanel.add(new JLabel("Sell: $" + market.getSellPrice(type)));

        // Quantity spinner
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
        JSpinner quantitySpinner = new JSpinner(spinnerModel);
        quantitySpinners.put(type, quantitySpinner);
        quantitySpinner.addChangeListener(e -> updateButtonStates(type));
        centerPanel.add(quantitySpinner);

        // Inventory count
        JLabel inventoryLabel = new JLabel("Owned: " + playerInventory.getResourceCount(type));
        inventoryLabels.put(type, inventoryLabel);
        centerPanel.add(inventoryLabel);

        card.add(centerPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Buy button
        JButton buyButton = new JButton("Buy");
        buyButton.setBackground(new Color(46, 204, 113));
        buyButton.setForeground(Color.WHITE);
        buyButton.addActionListener(e -> handleBuy(type));

        // Sell button
        JButton sellButton = new JButton("Sell");
        sellButton.setBackground(new Color(231, 76, 60));
        sellButton.setForeground(Color.WHITE);
        sellButton.addActionListener(e -> handleSell(type));

        buttonPanel.add(buyButton);
        buttonPanel.add(sellButton);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private void handleBuy(ResourceType type) {
        int quantity = (Integer) quantitySpinners.get(type).getValue();
        int totalCost = market.getBuyPrice(type) * quantity;

        if (playerInventory.getMoney() >= totalCost && market.getStock(type) >= quantity) {
            if (market.buyResource(type, playerInventory, quantity)) {
                updateDisplay();
                showNotification("Successfully bought " + quantity + " " + type, true);
            } else {
                showNotification("Failed to buy " + type, false);
            }
        } else {
            showNotification("Not enough money or market stock", false);
        }
    }

    private void handleSell(ResourceType type) {
        int quantity = (Integer) quantitySpinners.get(type).getValue();

        if (playerInventory.getResourceCount(type) >= quantity) {
            if (market.sellResource(type, playerInventory, quantity)) {
                updateDisplay();
                showNotification("Successfully sold " + quantity + " " + type, true);
            } else {
                showNotification("Failed to sell " + type, false);
            }
        } else {
            showNotification("Not enough resources to sell", false);
        }
    }

    private void showNotification(String message, boolean success) {
        JOptionPane.showMessageDialog(
                this,
                message,
                success ? "Success" : "Error",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void updateButtonStates(ResourceType type) {
        int quantity = (Integer) quantitySpinners.get(type).getValue();
        int playerResourceCount = playerInventory.getResourceCount(type);

        // Find the buy and sell buttons for this resource
        Container resourcePanel = quantitySpinners.get(type).getParent().getParent();
        for (Component comp : resourcePanel.getComponents()) {
            if (comp instanceof JPanel && ((JPanel) comp).getLayout() instanceof GridLayout) {
                for (Component button : ((JPanel) comp).getComponents()) {
                    if (button instanceof JButton) {
                        JButton btn = (JButton) button;
                        if (btn.getText().equals("Buy")) {
                            btn.setEnabled(playerInventory.getMoney() >= market.getBuyPrice(type) * quantity
                                    && market.getStock(type) >= quantity);
                        } else if (btn.getText().equals("Sell")) {
                            btn.setEnabled(playerResourceCount >= quantity);
                        }
                    }
                }
            }
        }
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateDisplay());
        updateTimer.start();
    }

    public void updateDisplay() {
        // Update money display
        moneyLabel.setText("Money: $" + playerInventory.getMoney());

        // Update each resource card
        for (ResourceType type : ResourceType.values()) {
            if (type.getBaseHarvestTime() > 0) {
                JLabel stockLabel = stockLabels.get(type);
                if (stockLabel != null) {
                    stockLabel.setText("Stock: " + market.getStock(type));
                }

                JLabel priceLabel = priceLabels.get(type);
                if (priceLabel != null) {
                    priceLabel.setText("Buy: $" + market.getBuyPrice(type));
                }

                JLabel inventoryLabel = inventoryLabels.get(type);
                if (inventoryLabel != null) {
                    inventoryLabel.setText("Owned: " + playerInventory.getResourceCount(type));
                }

                updateButtonStates(type);
            }
        }

        // Update machine buttons
        for (MachineType type : MachineType.values()) {
            JButton button = machineButtons.get(type);
            if (button != null) {
                button.setEnabled(playerInventory.getMoney() >= type.getBasePrice());
            }
        }

        // Update control panel displays
        if (controlPanel != null) {
            controlPanel.updateInventoryDisplay(playerInventory.getInventoryDisplay());
            controlPanel.updateMoneyDisplay(playerInventory.getMoney());
        }

        revalidate();
        repaint();
    }

    public void destroy() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}