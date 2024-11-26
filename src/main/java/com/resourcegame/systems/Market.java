package com.resourcegame.systems;

import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.entities.Inventory;
import com.resourcegame.entities.MachineManager;

import java.util.*;

public class Market {
    private Map<ResourceType, Integer> buyPrices;
    private Map<ResourceType, Integer> sellPrices;
    private Map<ResourceType, Integer> stock;
    private static final int MAX_STOCK = 1000;
    private Map<MachineType, Integer> machinePrices;
    private MachineManager machineManager;

    public Market() {
        this.buyPrices = new HashMap<>();
        this.sellPrices = new HashMap<>();
        this.stock = new HashMap<>();
        new CraftingSystem();
        initializeMarket();
        this.machinePrices = new HashMap<>();
        initializeMachinePrices();
    }

    private void initializeMarket() {
        for (ResourceType type : ResourceType.values()) {
            buyPrices.put(type, getBasePrice(type));
            sellPrices.put(type, (int) (getBasePrice(type) * 0.7)); // Sell price is 70% of buy price
            stock.put(type, MAX_STOCK / 2); // Start with half stock
        }
    }

    private int getBasePrice(ResourceType type) {
        switch (type) {
            case WOOD:
                return 10;
            case STONE:
                return 15;
            case IRON:
                return 25;
            case GOLD:
                return 50;
            case FOOD:
                return 5;
            default:
                return 10;
        }
    }

    public boolean sellResource(ResourceType type, Inventory playerInventory, int quantity) {
        if (stock.get(type) + quantity > MAX_STOCK) {
            return false;
        }

        int totalPrice = sellPrices.get(type) * quantity;
        if (playerInventory.removeResource(type, quantity)) {
            playerInventory.addMoney(totalPrice);
            stock.put(type, stock.get(type) + quantity);
            updatePrices(type, quantity, true);
            return true;
        }
        return false;
    }

    public boolean buyResource(ResourceType type, Inventory playerInventory, int quantity) {
        if (stock.get(type) < quantity) {
            return false;
        }

        int totalPrice = buyPrices.get(type) * quantity;
        if (playerInventory.getMoney() >= totalPrice) {
            playerInventory.removeMoney(totalPrice);
            playerInventory.addResource(type, quantity);
            stock.put(type, stock.get(type) - quantity);
            updatePrices(type, quantity, false);
            return true;
        }
        return false;
    }

    private void updatePrices(ResourceType type, int quantity, boolean isSelling) {
        // Simple supply-demand price adjustment
        float stockRatio = (float) stock.get(type) / MAX_STOCK;
        int basePrice = getBasePrice(type);

        if (isSelling) {
            // More stock = lower prices
            buyPrices.put(type, (int) (basePrice * (1.5f - stockRatio)));
            sellPrices.put(type, (int) (buyPrices.get(type) * 0.7f));
        } else {
            // Less stock = higher prices
            buyPrices.put(type, (int) (basePrice * (2.0f - stockRatio)));
            sellPrices.put(type, (int) (buyPrices.get(type) * 0.7f));
        }
    }

    public int getBuyPrice(ResourceType type) {
        return buyPrices.get(type);
    }

    public int getSellPrice(ResourceType type) {
        return sellPrices.get(type);
    }

    public int getStock(ResourceType type) {
        return stock.get(type);
    }

    private void initializeMachinePrices() {
        for (MachineType type : MachineType.values()) {
            machinePrices.put(type, type.getBasePrice());
        }
    }
    
    public boolean purchaseMachine(MachineType type, Inventory playerInventory) {
        int price = type.getBasePrice();
        if (playerInventory.getMoney() >= price) {
            if (playerInventory.removeMoney(price)) {
                playerInventory.addMachine(type);
                return true;
            }
        }
        return false;
    }
    
    public boolean finalizeMachinePurchase(MachineType type, Inventory playerInventory) {
        int price = machinePrices.get(type);
        if (playerInventory.getMoney() >= price) {
            if (playerInventory.removeMoney(price)) {
                return true;
            }
        }
        return false;
    }
    
    public int getMachinePrice(MachineType type) {
        return machinePrices.get(type);
    }

    public void setMachineManager(MachineManager machineManager) {
        this.machineManager = machineManager;
    }
}