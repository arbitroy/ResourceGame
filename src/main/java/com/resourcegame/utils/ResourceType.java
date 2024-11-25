package com.resourcegame.utils;

public enum ResourceType {
    // Base Resources
    WOOD, STONE, IRON, GOLD, FOOD,
    
    // Crafted Resources
    WOODEN_PLANKS,    // Crafted from wood
    STONE_TOOLS,      // Crafted from stone and wood
    METAL_ALLOY,      // Crafted from iron and stone
    PRESERVED_FOOD,   // Crafted from food
    BUILDING_MATERIALS, // Crafted from stone and wood
    LUXURY_ITEMS;      // Crafted from gold and iron
    
    public int getBaseHarvestTime() {
        switch (this) {
            case WOOD: return 2;
            case STONE: return 3;
            case IRON: return 4;
            case GOLD: return 5;
            case FOOD: return 1;
            default: return 0; // Crafted items aren't harvestable
        }
    }
}