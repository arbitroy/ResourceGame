package com.resourcegame.entities;

public class CraftingProgress {
    private final long startTime;
    private final long duration;
    
    public CraftingProgress(long durationMs) {
        this.startTime = System.currentTimeMillis();
        this.duration = durationMs;
    }
    
    public void update() {
        // No update needed, time-based
    }
    
    public boolean isComplete() {
        return getElapsedTime() >= duration;
    }
    
    public double getProgressPercentage() {
        return Math.min(100.0, (getElapsedTime() / (double) duration) * 100.0);
    }
    
    private long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}
