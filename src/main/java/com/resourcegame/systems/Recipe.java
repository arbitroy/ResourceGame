package com.resourcegame.systems;

import com.resourcegame.utils.ResourceType;
import java.util.Map;
import java.util.HashMap;

public class Recipe {
    private String name;
    private String description;
    private Map<ResourceType, Integer> ingredients;
    private Map<ResourceType, Integer> results;
    private int craftingTime;

    public Recipe(String name, int craftingTime) {
        this.name = name;
        this.craftingTime = craftingTime;
        this.ingredients = new HashMap<>();
        this.results = new HashMap<>();
        this.description = "";
    }

    public void addIngredient(ResourceType type, int amount) {
        ingredients.put(type, amount);
    }

    public void addResult(ResourceType type, int amount) {
        results.put(type, amount);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<ResourceType, Integer> getIngredients() { return ingredients; }
    public Map<ResourceType, Integer> getResults() { return results; }
    public int getCraftingTime() { return craftingTime; }
}