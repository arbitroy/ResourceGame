package com.resourcegame.entities;

import com.resourcegame.utils.ResourceType;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<ResourceType, Integer> resources;
    private int capacity;
    private int money;

    public Inventory() {
        this.resources = new HashMap<>();
        this.capacity = 100;
        this.money = 100; // Starting money
    }

    public boolean addResource(ResourceType type, int quantity) {
        if (getTotalItems() + quantity > capacity) {
            return false;
        }
        resources.merge(type, quantity, Integer::sum);
        return true;
    }

    public boolean removeResource(ResourceType type, int quantity) {
        Integer current = resources.get(type);
        if (current == null || current < quantity) {
            return false;
        }
        
        if (current == quantity) {
            resources.remove(type);
        } else {
            resources.put(type, current - quantity);
        }
        return true;
    }

    public void addMoney(int amount) {
        this.money += amount;
    }

    public boolean removeMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public int getMoney() {
        return money;
    }

    public int getTotalItems() {
        return resources.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getResourceCount(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    public boolean hasResource(ResourceType type, int amount) {
        return getResourceCount(type) >= amount;
    }

    public boolean hasSpace(int items) {
        return getTotalItems() + items <= capacity;
    }

    public String getInventoryDisplay() {
        StringBuilder sb = new StringBuilder();
        boolean hasItems = false;
        
        for (ResourceType type : ResourceType.values()) {
            int count = getResourceCount(type);
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
        
        sb.append("\nSpace: ").append(getTotalItems()).append("/").append(capacity);
        
        return sb.toString();
    }
}