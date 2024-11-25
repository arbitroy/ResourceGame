package com.resourcegame.systems;

import com.resourcegame.entities.Inventory;
import com.resourcegame.utils.ResourceType;
import java.util.*;

public class CraftingSystem {
    private List<Recipe> recipes;

    public CraftingSystem() {
        this.recipes = new ArrayList<>();
        initializeRecipes();
    }

    private void initializeRecipes() {
        // Will be implemented later with actual recipes
    }
}