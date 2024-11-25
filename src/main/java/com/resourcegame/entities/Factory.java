package com.resourcegame.entities;

import com.resourcegame.utils.Position;
import com.resourcegame.utils.MachineType;
import com.resourcegame.utils.ResourceType;
import com.resourcegame.core.GameMap;
import com.resourcegame.systems.Recipe;
import java.util.Map;
import java.util.HashMap;

public class Factory extends Machine {
    private Recipe selectedRecipe;
    private Map<String, Long> activeCraftingProcesses;
    
    public Factory(Position position, MachineType type) {
        super(position, type);
        this.activeCraftingProcesses = new HashMap<>();
    }
    
    public void setRecipe(Recipe recipe) {
        this.selectedRecipe = recipe;
    }
    
    @Override
    public void update(GameMap gameMap) {
        if (selectedRecipe == null) return;
        
        // Check if we have space for the results
        int totalResultItems = selectedRecipe.getResults().values().stream()
            .mapToInt(Integer::intValue).sum();
            
        if (inventory.getTotalItems() + totalResultItems > inventoryCapacity) {
            isWorking = false;
            return;
        }
        
        // Check if we have the required ingredients
        boolean hasIngredients = true;
        for (Map.Entry<ResourceType, Integer> ingredient : selectedRecipe.getIngredients().entrySet()) {
            if (!inventory.hasResource(ingredient.getKey(), ingredient.getValue())) {
                hasIngredients = false;
                break;
            }
        }
        
        long currentTime = System.currentTimeMillis();
        if (hasIngredients && currentTime - lastProcessTime >= selectedRecipe.getCraftingTime() / processingSpeed) {
            craftItem();
            lastProcessTime = currentTime;
            isWorking = true;
        } else {
            isWorking = false;
        }
    }
    
    private void craftItem() {
        // Remove ingredients
        for (Map.Entry<ResourceType, Integer> ingredient : selectedRecipe.getIngredients().entrySet()) {
            inventory.removeResource(ingredient.getKey(), ingredient.getValue());
        }
        
        // Add results
        for (Map.Entry<ResourceType, Integer> result : selectedRecipe.getResults().entrySet()) {
            inventory.addResource(result.getKey(), result.getValue());
        }
    }
}