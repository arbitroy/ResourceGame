package com.resourcegame.utils;

public enum MachineType {
    BASIC_HARVESTER(100, "Automatically harvests resources from adjacent tiles", 0.0f, 1),
    FRAGILE_HARVESTER(75, "Cheaper harvester that requires maintenance", 0.3f, 1),
    ADVANCED_HARVESTER(200, "Faster harvesting speed and larger storage", 0.0f, 2),
    BASIC_FACTORY(150, "Automatically crafts items using basic recipes", 0.0f, 1),
    FRAGILE_FACTORY(100, "Cheaper factory that requires maintenance", 0.3f, 1),
    ADVANCED_FACTORY(300, "Faster crafting speed and can handle complex recipes", 0.0f, 2);

    private final int basePrice;
    private final String description;
    private final float breakdownChance; // Chance of breakdown per operation
    private final int configurationLimit; // How many times can be reconfigured, 0 = unlimited

    MachineType(int basePrice, String description, float breakdownChance, int configurationLimit) {
        this.basePrice = basePrice;
        this.description = description;
        this.breakdownChance = breakdownChance;
        this.configurationLimit = configurationLimit;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public String getDescription() {
        return description;
    }

    public float getBreakdownChance() {
        return breakdownChance;
    }

    public int getConfigurationLimit() {
        return configurationLimit;
    }

    public boolean isFragile() {
        return breakdownChance > 0;
    }
}