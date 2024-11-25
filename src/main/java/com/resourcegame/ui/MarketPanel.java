package com.resourcegame.ui;

import com.resourcegame.systems.Market;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.entities.Inventory;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MarketPanel extends JPanel {
    private Market market;
    private Inventory playerInventory;
    private JLabel moneyLabel;
    private Map<ResourceType, JSpinner> quantitySpinners;

    public MarketPanel(Market market, Inventory playerInventory) {
        this.market = market;
        this.playerInventory = playerInventory;
        this.quantitySpinners = new HashMap<>();
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Market"));
        
        initializeComponents();
    }

    private void initializeComponents() {
        // Money display
        moneyLabel = new JLabel("Money: $" + playerInventory.getMoney());
        add(moneyLabel, BorderLayout.NORTH);

        // Resource trading panel
        JPanel tradingPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        for (ResourceType type : ResourceType.values()) {
            tradingPanel.add(createResourceTradePanel(type));
        }

        JScrollPane scrollPane = new JScrollPane(tradingPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createResourceTradePanel(ResourceType type) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(type.name()));

        // Price labels
        JLabel buyLabel = new JLabel(String.format("Buy: $%d", market.getBuyPrice(type)));
        JLabel sellLabel = new JLabel(String.format("Sell: $%d", market.getSellPrice(type)));
        
        // Quantity spinner
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 100, 1);
        JSpinner quantitySpinner = new JSpinner(spinnerModel);
        quantitySpinners.put(type, quantitySpinner);

        // Buy/Sell buttons
        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(e -> buyResource(type));
        
        JButton sellButton = new JButton("Sell");
        sellButton.addActionListener(e -> sellResource(type));

        // Stock label
        JLabel stockLabel = new JLabel(String.format("Stock: %d", market.getStock(type)));

        // Add components to panel
        panel.add(buyLabel);
        panel.add(sellLabel);
        panel.add(new JLabel("Qty:"));
        panel.add(quantitySpinner);
        panel.add(buyButton);
        panel.add(sellButton);
        panel.add(stockLabel);

        return panel;
    }

    private void buyResource(ResourceType type) {
        int quantity = (Integer) quantitySpinners.get(type).getValue();
        if (market.buyResource(type, playerInventory, quantity)) {
            updateDisplay();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cannot buy resources. Check your money and market stock.", 
                "Transaction Failed", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void sellResource(ResourceType type) {
        int quantity = (Integer) quantitySpinners.get(type).getValue();
        if (market.sellResource(type, playerInventory, quantity)) {
            updateDisplay();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cannot sell resources. Check your inventory.", 
                "Transaction Failed", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    public void updateDisplay() {
        moneyLabel.setText("Money: $" + playerInventory.getMoney());
        // Update other display elements as needed
        revalidate();
        repaint();
    }
}