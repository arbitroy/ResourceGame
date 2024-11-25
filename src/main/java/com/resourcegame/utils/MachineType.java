package com.resourcegame.utils;

public enum MachineType {
    BASIC_HARVESTER(100, "Automatically harvests resources from adjacent tiles"),
    ADVANCED_HARVESTER(200, "Faster harvesting speed and larger storage"),
    BASIC_FACTORY(150, "Automatically crafts items using basic recipes"),
    ADVANCED_FACTORY(300, "Faster crafting speed and can handle complex recipes");

    private final int basePrice;
    private final String description;

    MachineType(int basePrice, String description) {
        this.basePrice = basePrice;
        this.description = description;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public String getDescription() {
        return description;
    }
}