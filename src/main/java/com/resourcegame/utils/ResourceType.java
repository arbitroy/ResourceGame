package com.resourcegame.utils;

public enum ResourceType {
    WOOD, STONE, IRON, GOLD, FOOD;

    public int getBaseHarvestTime() {
        switch (this) {
            case WOOD: return 2;
            case STONE: return 3;
            case IRON: return 4;
            case GOLD: return 5;
            case FOOD: return 1;
            default: return 1;
        }
    }
}
