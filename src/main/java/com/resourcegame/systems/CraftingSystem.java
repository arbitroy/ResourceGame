package com.resourcegame.systems;

import com.resourcegame.entities.Inventory;
import com.resourcegame.utils.ResourceType;
import java.util.*;
import java.util.concurrent.*;

public class CraftingSystem {
    private List<Recipe> recipes;
    private Map<String, CraftingProcess> activeProcesses;
    private ScheduledExecutorService craftingExecutor;
    private List<CraftingListener> craftingListeners;
    

    public interface CraftingListener {
        void onCraftingCompleted(Recipe recipe);

        void onCraftingFailed(Recipe recipe, String reason);

        void onCraftingStarted(Recipe recipe);
    }

    public CraftingSystem() {
        this.recipes = new ArrayList<>();
        this.activeProcesses = new ConcurrentHashMap<>();
        this.craftingExecutor = Executors.newScheduledThreadPool(1);
        this.craftingListeners = new ArrayList<>();
        initializeRecipes();
    }

    private static class CraftingProcess {
        private final Recipe recipe;
        private final Inventory inventory;
        private final long startTime;
        private final Map<ResourceType, Integer> removedResources;

        public CraftingProcess(Recipe recipe, Inventory inventory, String id,
                Map<ResourceType, Integer> removedResources) {
            this.recipe = recipe;
            this.inventory = inventory;
            this.startTime = System.currentTimeMillis();
            this.removedResources = new HashMap<>(removedResources);
        }

        public Recipe getRecipe() {
            return recipe;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public long getStartTime() {
            return startTime;
        }

        public Map<ResourceType, Integer> getRemovedResources() {
            return removedResources;
        }
    }

    private void initializeRecipes() {
        // Wood Processing
        Recipe plankRecipe = new Recipe("Wooden Planks", 2000, true);
        plankRecipe.addIngredient(ResourceType.WOOD, 2);
        plankRecipe.addResult(ResourceType.WOODEN_PLANKS, 1);
        plankRecipe.setDescription("Process raw wood into sturdy wooden planks for construction");
        recipes.add(plankRecipe);

        // Stone Tool Crafting
        Recipe stoneToolRecipe = new Recipe("Stone Tools", 3000, false);
        stoneToolRecipe.addIngredient(ResourceType.WOOD, 1); // Handle
        stoneToolRecipe.addIngredient(ResourceType.STONE, 2); // Tool head
        stoneToolRecipe.addResult(ResourceType.STONE_TOOLS, 1);
        stoneToolRecipe.setDescription("Craft basic tools using stone heads and wooden handles");
        recipes.add(stoneToolRecipe);

        // Metal Processing
        Recipe metalAlloyRecipe = new Recipe("Metal Alloy", 5000, false);
        metalAlloyRecipe.addIngredient(ResourceType.IRON, 2);
        metalAlloyRecipe.addIngredient(ResourceType.STONE, 1); // Flux material
        metalAlloyRecipe.addResult(ResourceType.METAL_ALLOY, 1);
        metalAlloyRecipe.setDescription("Combine iron and stone flux to create a stronger metal alloy");
        recipes.add(metalAlloyRecipe);

        // Food Preservation
        Recipe preservedFoodRecipe = new Recipe("Preserved Food", 4000, false);
        preservedFoodRecipe.addIngredient(ResourceType.FOOD, 3);
        preservedFoodRecipe.addIngredient(ResourceType.WOOD, 1); // For smoking/drying
        preservedFoodRecipe.addResult(ResourceType.PRESERVED_FOOD, 2);
        preservedFoodRecipe.setDescription("Preserve food using traditional smoking and drying techniques");
        recipes.add(preservedFoodRecipe);

        // Construction Materials
        Recipe buildingMaterialRecipe = new Recipe("Building Materials", 6000, true);
        buildingMaterialRecipe.addIngredient(ResourceType.STONE, 2);
        buildingMaterialRecipe.addIngredient(ResourceType.WOODEN_PLANKS, 2); // Requires processed wood
        buildingMaterialRecipe.addResult(ResourceType.BUILDING_MATERIALS, 1);
        buildingMaterialRecipe.setDescription("Create advanced building materials by combining processed resources");
        recipes.add(buildingMaterialRecipe);

        // Luxury Items
        Recipe luxuryItemRecipe = new Recipe("Luxury Items", 8000, false);
        luxuryItemRecipe.addIngredient(ResourceType.GOLD, 1);
        luxuryItemRecipe.addIngredient(ResourceType.METAL_ALLOY, 1); // Requires processed metal
        luxuryItemRecipe.addResult(ResourceType.LUXURY_ITEMS, 1);
        luxuryItemRecipe.setDescription("Craft valuable luxury items using precious metals and alloys");
        recipes.add(luxuryItemRecipe);
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }

    public boolean canCraft(Recipe recipe, Inventory inventory) {
        if (recipe == null || inventory == null) {
            return false;
        }

        // Calculate total result items
        int totalResultItems = recipe.getResults().values().stream()
                .mapToInt(Integer::intValue).sum();

        // Check inventory space
        if (!inventory.hasSpace(totalResultItems)) {
            return false;
        }

        // Check ingredients
        for (Map.Entry<ResourceType, Integer> ingredient : recipe.getIngredients().entrySet()) {
            if (!inventory.hasResource(ingredient.getKey(), ingredient.getValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean startCrafting(Recipe recipe, Inventory inventory, String craftingId) {

        if (!canCraft(recipe, inventory)) {
            notifyCraftingFailed(recipe, "Not enough resources or inventory space");
            return false;
        }

        if (recipe.isInstant()) {
            // For instant recipes, complete the crafting immediately
            if (completeCrafting(recipe, inventory)) {
                notifyCraftingCompleted(recipe);
                return true;
            } else {
                notifyCraftingFailed(recipe, "Failed to add results to inventory");
                return false;
            }
        } else {
            // For timed recipes, start the crafting process
            try {
                Map<ResourceType, Integer> removedResources = new HashMap<>();
                
                // Remove ingredients
                for (Map.Entry<ResourceType, Integer> ingredient : recipe.getIngredients().entrySet()) {
                    ResourceType type = ingredient.getKey();
                    int amount = ingredient.getValue();
                    
                    if (!inventory.removeResource(type, amount)) {
                        rollbackIngredients(inventory, removedResources);
                        notifyCraftingFailed(recipe, "Failed to remove resources");
                        return false;
                    }
                    removedResources.put(type, amount);
                }
    
                CraftingProcess process = new CraftingProcess(recipe, inventory, craftingId, removedResources);
                activeProcesses.put(craftingId, process);
    
                craftingExecutor.schedule(() -> {
                    completeCrafting(craftingId);
                }, recipe.getCraftingTime(), TimeUnit.MILLISECONDS);
    
                notifyCraftingStarted(recipe);
                return true;
    
            } catch (Exception e) {
                System.out.println("Error starting craft: " + e.getMessage());
                notifyCraftingFailed(recipe, "Unexpected error: " + e.getMessage());
                return false;
            }
        }

    }

    private boolean completeCrafting(Recipe recipe, Inventory inventory) {
        boolean success = true;
    
        // Remove ingredients
        for (Map.Entry<ResourceType, Integer> ingredient : recipe.getIngredients().entrySet()) {
            ResourceType type = ingredient.getKey();
            int amount = ingredient.getValue();
            
            if (!inventory.removeResource(type, amount)) {
                success = false;
                break;
            }
        }
    
        if (success) {
            // Add results to inventory
            for (Map.Entry<ResourceType, Integer> result : recipe.getResults().entrySet()) {
                if (!inventory.addResource(result.getKey(), result.getValue())) {
                    success = false;
                    break;
                }
            }
        }
    
        return success;
    }


    private void completeCrafting(String craftingId) {

        CraftingProcess process = activeProcesses.remove(craftingId);
        if (process == null) {
            System.out.println("No process found to complete for ID: " + craftingId);
            return;
        }

        Recipe recipe = process.getRecipe();
        Inventory inventory = process.getInventory();
        boolean success = true;

        // Add results to inventory
        for (Map.Entry<ResourceType, Integer> result : recipe.getResults().entrySet()) {
            if (!inventory.addResource(result.getKey(), result.getValue())) {
                success = false;
                break;
            }
        }

        if (success) {

            notifyCraftingCompleted(recipe);
        } else {
            System.out.println("Failed to complete crafting: " + recipe.getName());
            // Return the original ingredients if adding results failed
            rollbackIngredients(inventory, process.getRemovedResources());
            notifyCraftingFailed(recipe, "Failed to add results to inventory");
        }
    }

    private void rollbackIngredients(Inventory inventory, Map<ResourceType, Integer> removedResources) {
        for (Map.Entry<ResourceType, Integer> entry : removedResources.entrySet()) {
            inventory.addResource(entry.getKey(), entry.getValue());
        }
    }

    public void addCraftingListener(CraftingListener listener) {
        if (!craftingListeners.contains(listener)) {
            craftingListeners.add(listener);
        }
    }

    private void notifyCraftingStarted(Recipe recipe) {
        for (CraftingListener listener : craftingListeners) {
            listener.onCraftingStarted(recipe);
        }
    }

    private void notifyCraftingCompleted(Recipe recipe) {
        for (CraftingListener listener : craftingListeners) {
            listener.onCraftingCompleted(recipe);
        }
    }

    private void notifyCraftingFailed(Recipe recipe, String reason) {
        for (CraftingListener listener : craftingListeners) {
            listener.onCraftingFailed(recipe, reason);
        }
    }

    public float getCraftingProgress(String craftingId) {
        CraftingProcess process = activeProcesses.get(craftingId);
        if (process == null) {
            System.out.println("Process not found for ID: " + craftingId); // Debug
            return 1.0f; // Return complete if process not found
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - process.getStartTime();
        float progress = (float) elapsed / process.getRecipe().getCraftingTime();
        
        
        return Math.min(1.0f, progress);
    }

    public void shutdown() {
        craftingExecutor.shutdown();
        try {
            craftingExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}