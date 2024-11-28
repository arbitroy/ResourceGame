package com.resourcegame.systems;

import com.resourcegame.utils.ResourceType;
import java.util.*;

public class MachineStatistics {
    private Map<ResourceType, Integer> totalResourcesCollected;
    private Map<String, Integer> productionRates;
    private int totalItemsProduced;
    private long lastUpdateTime;
    private static final int RATE_WINDOW_MS = 60000; // 1 minute window for rate calculation

    public MachineStatistics() {
        this.totalResourcesCollected = new EnumMap<>(ResourceType.class);
        this.productionRates = new HashMap<>();
        this.totalItemsProduced = 0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void recordResourceCollection(ResourceType type, int amount) {
        totalResourcesCollected.merge(type, amount, Integer::sum);
        updateProductionRate(type.toString(), amount);
    }

    public void recordItemProduction(String itemName, int amount) {
        totalItemsProduced += amount;
        updateProductionRate(itemName, amount);
    }

    private void updateProductionRate(String item, int amount) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastUpdateTime;
        
        if (timeDiff >= RATE_WINDOW_MS) {
            // Reset rates if window has passed
            productionRates.clear();
            lastUpdateTime = currentTime;
        }
        
        productionRates.merge(item, amount, Integer::sum);
    }

    public int getTotalResourcesCollected() {
        return totalResourcesCollected.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<ResourceType, Integer> getResourceBreakdown() {
        return Collections.unmodifiableMap(totalResourcesCollected);
    }

    public int getTotalItemsProduced() {
        return totalItemsProduced;
    }

    public double getProductionRatePerMinute(String item) {
        return productionRates.getOrDefault(item, 0) * (60000.0 / RATE_WINDOW_MS);
    }

    public double getTotalProductionRatePerMinute() {
        return productionRates.values().stream().mapToInt(Integer::intValue).sum() 
               * (60000.0 / RATE_WINDOW_MS);
    }

    public void reset() {
        totalResourcesCollected.clear();
        productionRates.clear();
        totalItemsProduced = 0;
        lastUpdateTime = System.currentTimeMillis();
    }
}