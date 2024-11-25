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

    public String getInventoryDisplay() {
        StringBuilder sb = new StringBuilder("<html>");
        resources.forEach((type, count) -> 
            sb.append(type).append(": ").append(count).append("<br>")
        );
        if (resources.isEmpty()) {
            sb.append("Empty");
        }
        sb.append("</html>");
        return sb.toString();
    }

    public boolean addResource(ResourceType type) {
        if (getTotalItems() >= capacity) {
            return false;
        }
        resources.merge(type, 1, Integer::sum);
        return true;
    }
}