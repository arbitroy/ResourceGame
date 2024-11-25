package com.resourcegame.entities;

import com.resourcegame.utils.ResourceType;

public class Resource {
    private ResourceType type;
    private long lastHarvestTime;
    private boolean isHarvestable;
    private int harvestCooldown; // milliseconds
    private static final int BASE_COOLDOWN = 2000; // 2 seconds base cooldown

    public Resource(ResourceType type) {
        this.type = type;
        this.harvestCooldown = type.getBaseHarvestTime() * BASE_COOLDOWN;
        this.isHarvestable = true;
        this.lastHarvestTime = 0;
    }

    public boolean canHarvest() {
        if (!isHarvestable) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastHarvestTime >= harvestCooldown) {
                isHarvestable = true;
            }
        }
        return isHarvestable;
    }

    public void harvest() {
        isHarvestable = false;
        lastHarvestTime = System.currentTimeMillis();
    }

    public float getHarvestProgress() {
        if (isHarvestable) return 1.0f;
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastHarvestTime;
        return Math.min(1.0f, (float) elapsed / harvestCooldown);
    }

    public ResourceType getType() {
        return type;
    }
}