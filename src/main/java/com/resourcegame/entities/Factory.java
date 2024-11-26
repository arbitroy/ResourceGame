package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineStatus;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.core.GameMap;
import com.resourcegame.systems.Recipe;
import java.util.Map;
import java.util.HashMap;

public class Factory extends Machine {
    private Recipe selectedRecipe;
    private Map<ResourceType, Integer> reservedResources;
    private CraftingProgress currentCraft;
    
    public Factory(Position position, MachineType type) {
        super(position, type);
        this.reservedResources = new HashMap<>();
        this.status = MachineStatus.IDLE;
    }
    
    @Override
    public void update(GameMap gameMap) {
        if (selectedRecipe == null) {
            setStatus(MachineStatus.NEEDS_CONFIG);
            return;
        }
        
        // Check if we have space for the results
        int totalResultItems = selectedRecipe.getResults().values().stream()
            .mapToInt(Integer::intValue).sum();
            
        if (inventory.getTotalItems() + totalResultItems > inventoryCapacity) {
            setStatus(MachineStatus.INVENTORY_FULL);
            return;
        }
        
        // Check and start new crafting if not currently crafting
        if (currentCraft == null) {
            if (hasRequiredResources()) {
                startCrafting();
                setStatus(MachineStatus.WORKING);
            } else {
                setStatus(MachineStatus.INSUFFICIENT_RESOURCES);
                return;
            }
        }
        
        // Update current crafting progress
        if (currentCraft != null) {
            currentCraft.update();
            if (currentCraft.isComplete()) {
                completeCrafting();
                currentCraft = null;
            }
        }
    }
    
    private void startCrafting() {
        // Reserve resources
        for (Map.Entry<ResourceType, Integer> ingredient : selectedRecipe.getIngredients().entrySet()) {
            ResourceType type = ingredient.getKey();
            int amount = ingredient.getValue();
            inventory.removeResource(type, amount);
            reservedResources.put(type, amount);
        }
        
        currentCraft = new CraftingProgress(selectedRecipe.getCraftingTime() / processingSpeed);
    }
    
    private void completeCrafting() {
        // Add results to inventory
        for (Map.Entry<ResourceType, Integer> result : selectedRecipe.getResults().entrySet()) {
            inventory.addResource(result.getKey(), result.getValue());
        }
        reservedResources.clear();
    }
    
    @Override
    public String getStatusMessage() {
        if (currentCraft != null) {
            return String.format("Crafting %s (%.1f%%)", 
                selectedRecipe.getName(), 
                currentCraft.getProgressPercentage());
        }
        
        switch (status) {
            case NEEDS_CONFIG:
                return "Needs recipe configuration";
            case INVENTORY_FULL:
                return "Output inventory full";
            case INSUFFICIENT_RESOURCES:
                return "Missing required resources";
            default:
                return "Idle";
        }
    }
    
    public float getCraftingProgress() {
        return (float) (currentCraft != null ? currentCraft.getProgressPercentage() / 100f : 0f);
    }
    
    private boolean hasRequiredResources() {
        for (Map.Entry<ResourceType, Integer> ingredient : selectedRecipe.getIngredients().entrySet()) {
            if (!inventory.hasResource(ingredient.getKey(), ingredient.getValue())) {
                return false;
            }
        }
        return true;
    }

    public void setRecipe(Recipe recipe) {
        this.selectedRecipe = recipe;
        setStatus(recipe != null ? MachineStatus.IDLE : MachineStatus.NEEDS_CONFIG);
        // Clear any in-progress crafting when recipe changes
        if (currentCraft != null) {
            // Return any reserved resources
            for (Map.Entry<ResourceType, Integer> entry : reservedResources.entrySet()) {
                inventory.addResource(entry.getKey(), entry.getValue());
            }
            reservedResources.clear();
            currentCraft = null;
        }
    }

    public Recipe getSelectedRecipe() {
        return selectedRecipe;
    }
}