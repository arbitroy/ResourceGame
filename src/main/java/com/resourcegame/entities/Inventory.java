package com.resourcegame.entities;

import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.ResourceType;
import java.util.HashMap;
import java.util.Map;

public class Inventory {
    private Map<ResourceType, Integer> resources;
    private Map<MachineType, Integer> unplacedMachines; // Storage for machines
    private int capacity;
    private int money;

    public Inventory() {
        this(100); // Default capacity of 100 for player inventory
    }

    public Inventory(int capacity) {
        this.resources = new HashMap<>();
        this.unplacedMachines = new HashMap<>();
        this.capacity = capacity;
        this.money = 100;
    }

    public boolean addResource(ResourceType type, int quantity) {
        if (getTotalItems() + quantity > capacity) {
            return false;
        }
        resources.merge(type, quantity, Integer::sum);
        return true;
    }


    public void addMachine(MachineType type) {
        unplacedMachines.merge(type, 1, Integer::sum);
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

    public int getCapacity() {
        return capacity;
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

    public boolean removeMachine(MachineType type) {
        Integer count = unplacedMachines.get(type);
        if (count != null && count > 0) {
            if (count == 1) {
                unplacedMachines.remove(type);
            } else {
                unplacedMachines.put(type, count - 1);
            }
            return true;
        }
        return false;
    }

    public int getUnplacedMachineCount(MachineType type) {
        return unplacedMachines.getOrDefault(type, 0);
    }

    public Map<MachineType, Integer> getUnplacedMachines() {
        return new HashMap<>(unplacedMachines);
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