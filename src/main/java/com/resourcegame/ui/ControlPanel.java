package com.resourcegame.ui;

import com.resourcegame.core.Game;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.Position;
import com.resourcegame.utils.ResourceType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ControlPanel extends JPanel {
    private Game game;
    private JPanel inventoryPanel;
    private JButton harvestButton;
    private JButton craftButton;
    private JButton marketButton;
    private JLabel moneyLabel;
    private JButton placeMachineButton;
    private static final int TILE_SIZE = 40;

    public ControlPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(200, 400));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initializeComponents();
    }

    private void initializeComponents() {
        // Inventory Section with scrolling
        add(createSectionPanel("Inventory", () -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            inventoryPanel = new JPanel();
            inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
            inventoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JScrollPane scrollPane = new JScrollPane(inventoryPanel);
            scrollPane.setPreferredSize(new Dimension(180, 200));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            panel.add(scrollPane);
            updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
            return panel;
        }));

        // Actions Section
        add(createSectionPanel("Actions", () -> {
            JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5)); // Changed to 4 rows for the new button

            harvestButton = new JButton("Harvest Resource");
            harvestButton.setEnabled(false);
            harvestButton.addActionListener(e -> handleHarvest());

            craftButton = new JButton("Craft Items");
            craftButton.addActionListener(e -> handleCraft());

            marketButton = new JButton("Open Market");
            marketButton.addActionListener(e -> handleMarket());

            placeMachineButton = new JButton("Place Machine");
            placeMachineButton.addActionListener(e -> handleMachinePlacement());

            panel.add(harvestButton);
            panel.add(craftButton);
            panel.add(marketButton);
            panel.add(placeMachineButton);
            return panel;
        }));

        // Money Display
        add(createSectionPanel("Money", () -> {
            JPanel panel = new JPanel(new BorderLayout());
            moneyLabel = new JLabel("$" + game.getPlayer().getInventory().getMoney());
            moneyLabel.setFont(new Font("Arial", Font.BOLD, 16));
            moneyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(moneyLabel, BorderLayout.CENTER);
            return panel;
        }));

        marketButton.setEnabled(false);
        marketButton.setText("Not Near Market");
    }

    private void handleHarvest() {
        Position selectedTile = game.getMap().getSelectedTile();
        if (selectedTile != null) {
            game.harvestResource(selectedTile);
            updateHarvestButton(false);
        }
    }

    public void updateMoneyDisplay(int amount) {
        if (moneyLabel != null) {
            moneyLabel.setText("$" + amount);
        }
    }

    public void updateHarvestButton(boolean enabled) {
        if (harvestButton != null) {
            harvestButton.setEnabled(enabled);
            harvestButton.setText(enabled ? "Harvest Resource" : "No Resource Selected");
        }
    }

    private void handleCraft() {
        if (game.getCraftingSystem() != null) {
            CraftingPanel craftingPanel = new CraftingPanel(
                    game.getCraftingSystem(),
                    game.getPlayer().getInventory(),
                    this // Pass reference to ControlPanel
            );

            // Create dialog
            JDialog dialog = new JDialog(
                    SwingUtilities.getWindowAncestor(this) instanceof Frame
                            ? (Frame) SwingUtilities.getWindowAncestor(this)
                            : null,
                    "Crafting",
                    false);

            dialog.setFocusableWindowState(false);
            dialog.setAutoRequestFocus(false);

            dialog.setContentPane(craftingPanel);
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    craftingPanel.destroy();
                }
            });

            JFrame gameFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (gameFrame != null) {
                gameFrame.requestFocus();
            }

            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Crafting system not initialized",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMarket() {
        // Check if player is adjacent to market tile
        Position playerPos = game.getPlayer().getPosition();
        Position marketPos = game.getMap().getMarketPosition();

        if (!playerPos.isAdjacent(marketPos)) {
            JOptionPane.showMessageDialog(this,
                    "You must be next to the market to trade!",
                    "Market Access",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create and show market dialog
        MarketPanel marketPanel = new MarketPanel(
                game.getMarket(),
                game.getPlayer().getInventory(),
                this // Pass reference to ControlPanel for updates
        );

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this) instanceof Frame ? (Frame) SwingUtilities.getWindowAncestor(this)
                        : null,
                "Market",
                false);

        dialog.setContentPane(marketPanel);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Cleanup when dialog is closed
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                marketPanel.destroy(); // Stop the update timer
            }
        });

        // Make sure focus returns to game
        JFrame gameFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (gameFrame != null) {
            gameFrame.requestFocus();
        }

        dialog.setVisible(true);
    }

    private JPanel createSectionPanel(String title, PanelCreator content) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BorderLayout());
        sectionPanel.setBorder(BorderFactory.createTitledBorder(title));
        sectionPanel.add(content.createPanel(), BorderLayout.CENTER);
        return sectionPanel;
    }

    public void updateMarketButton(boolean nearMarket) {
        if (marketButton != null) {
            marketButton.setEnabled(nearMarket);
            marketButton.setText(nearMarket ? "Open Market" : "Not Near Market");
        }
    }

    public void updateInventoryDisplay(String inventoryContent) {
        if (inventoryPanel != null) {
            inventoryPanel.removeAll();

            // Create individual item panels for each resource
            for (ResourceType type : ResourceType.values()) {
                int count = game.getPlayer().getInventory().getResourceCount(type);
                if (count > 0) {
                    JPanel itemPanel = new JPanel(new BorderLayout());
                    itemPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

                    JLabel itemLabel = new JLabel(String.format("%-10s: %d", type.toString(), count));
                    itemLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

                    itemPanel.add(itemLabel, BorderLayout.WEST);
                    inventoryPanel.add(itemPanel);
                }
            }

            // Add a glue component to push everything to the top
            inventoryPanel.add(Box.createVerticalGlue());

            // Update the money display
            updateMoneyDisplay(game.getPlayer().getInventory().getMoney());

            // Update the display
            inventoryPanel.revalidate();
            inventoryPanel.repaint();
        }
    }


     private void startMachinePlacement(MachineType type) {
        JOptionPane.showMessageDialog(this,
                "Click on an empty tile to place the machine.",
                "Machine Placement",
                JOptionPane.INFORMATION_MESSAGE);

        // Add one-time mouse listener to MapPanel for placement
        MouseAdapter placementListener = new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {  // Fixed MouseEvent import
                MapPanel mapPanel = (MapPanel) e.getComponent();
                int tileX = e.getX() / TILE_SIZE;
                int tileY = e.getY() / TILE_SIZE;
                Position pos = new Position(tileX, tileY);

                if (game.placeMachine(type, pos)) {
                    game.getPlayer().getInventory().removeMoney(type.getBasePrice());
                    updateInventoryDisplay(game.getPlayer().getInventory().getInventoryDisplay());
                    mapPanel.removeMouseListener(this);
                } else {
                    JOptionPane.showMessageDialog(mapPanel,
                            "Cannot place machine here!",
                            "Invalid Location",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        // Get the MapPanel from the game window
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            Component[] components = frame.getContentPane().getComponents();
            for (Component component : components) {
                if (component instanceof MapPanel) {
                    ((MapPanel) component).addMouseListener(placementListener);
                    break;
                }
            }
        }
    }



    private void handleMachinePlacement() {
        if (game.getPlayer().getInventory().getMoney() < MachineType.BASIC_HARVESTER.getBasePrice()) {
            JOptionPane.showMessageDialog(this,
                    "Not enough money to purchase any machines!",
                    "Insufficient Funds",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Place Machine", true);
        dialog.setLayout(new BorderLayout());

        JPanel machinePanel = new JPanel(new GridLayout(0, 1, 5, 5));
        machinePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (MachineType type : MachineType.values()) {
            if (game.getPlayer().getInventory().getMoney() >= type.getBasePrice()) {
                JButton machineButton = new JButton(String.format("%s ($%d)",
                        formatMachineName(type.toString()),
                        type.getBasePrice()));

                machineButton.addActionListener(e -> {
                    dialog.dispose();
                    startMachinePlacement(type);
                });

                machinePanel.add(machineButton);
            }
        }

        dialog.add(machinePanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String formatMachineName(String name) {
        return name.replace("_", " ").toLowerCase()
                .substring(0, 1).toUpperCase() +
                name.replace("_", " ").toLowerCase().substring(1);
    }
}

@FunctionalInterface
interface PanelCreator {
    JPanel createPanel();
}