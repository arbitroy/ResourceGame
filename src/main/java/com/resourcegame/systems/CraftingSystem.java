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

    // Static nested class to hold crafting process information
    private static class CraftingProcess {
        private final Recipe recipe;
        private final Inventory inventory;
        private final String id;
        private final long startTime;
        private final Map<ResourceType, Integer> removedResources;

        public CraftingProcess(Recipe recipe, Inventory inventory, String id,
                Map<ResourceType, Integer> removedResources) {
            this.recipe = recipe;
            this.inventory = inventory;
            this.id = id;
            this.startTime = System.currentTimeMillis();
            this.removedResources = new HashMap<>(removedResources);
        }

        public Recipe getRecipe() {
            return recipe;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public String getId() {
            return id;
        }

        public long getStartTime() {
            return startTime;
        }

        public Map<ResourceType, Integer> getRemovedResources() {
            return removedResources;
        }
    }

    public void addCraftingListener(CraftingListener listener) {
        if (!craftingListeners.contains(listener)) {
            craftingListeners.add(listener);
        }
    }

    private void initializeRecipes() {
        // Basic processing recipes
        Recipe plankRecipe = new Recipe("Wooden Planks", 2000);
        plankRecipe.addIngredient(ResourceType.WOOD, 1);
        plankRecipe.addResult(ResourceType.WOOD, 2);
        plankRecipe.setDescription("Process raw wood into more efficient planks");
        recipes.add(plankRecipe);

        Recipe stoneToolRecipe = new Recipe("Stone Tools", 3000);
        stoneToolRecipe.addIngredient(ResourceType.WOOD, 2);
        stoneToolRecipe.addIngredient(ResourceType.STONE, 3);
        stoneToolRecipe.addResult(ResourceType.IRON, 1);
        stoneToolRecipe.setDescription("Craft basic tools to help mine iron");
        recipes.add(stoneToolRecipe);

        Recipe metalAlloyRecipe = new Recipe("Metal Alloy", 5000);
        metalAlloyRecipe.addIngredient(ResourceType.IRON, 2);
        metalAlloyRecipe.addIngredient(ResourceType.STONE, 1);
        metalAlloyRecipe.addResult(ResourceType.GOLD, 1);
        metalAlloyRecipe.setDescription("Combine iron and stone to create valuable gold");
        recipes.add(metalAlloyRecipe);

        Recipe preservedFoodRecipe = new Recipe("Preserved Food", 4000);
        preservedFoodRecipe.addIngredient(ResourceType.FOOD, 3);
        preservedFoodRecipe.addIngredient(ResourceType.WOOD, 1);
        preservedFoodRecipe.addResult(ResourceType.FOOD, 5);
        preservedFoodRecipe.setDescription("Preserve food to increase its quantity");
        recipes.add(preservedFoodRecipe);

        Recipe constructionMaterialRecipe = new Recipe("Construction Materials", 6000);
        constructionMaterialRecipe.addIngredient(ResourceType.STONE, 2);
        constructionMaterialRecipe.addIngredient(ResourceType.WOOD, 2);
        constructionMaterialRecipe.addResult(ResourceType.STONE, 3);
        constructionMaterialRecipe.addResult(ResourceType.IRON, 1);
        constructionMaterialRecipe.setDescription("Create improved building materials");
        recipes.add(constructionMaterialRecipe);

        Recipe luxuryItemRecipe = new Recipe("Luxury Items", 8000);
        luxuryItemRecipe.addIngredient(ResourceType.GOLD, 1);
        luxuryItemRecipe.addIngredient(ResourceType.IRON, 2);
        luxuryItemRecipe.addResult(ResourceType.GOLD, 2);
        luxuryItemRecipe.setDescription("Create valuable luxury items from gold and iron");
        recipes.add(luxuryItemRecipe);
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }

    public boolean canCraft(Recipe recipe, Inventory inventory) {
        // Check for null recipe or inventory
        if (recipe == null || inventory == null)
            return false;

        // Check if inventory has enough space for results
        int totalResultItems = recipe.getResults().values().stream()
                .mapToInt(Integer::intValue).sum();
        if (!inventory.hasSpace(totalResultItems)) {
            return false;
        }

        // Check if we have all required ingredients
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

        try {
            // First verify we can remove all ingredients
            Map<ResourceType, Integer> removedResources = new HashMap<>();

            // Try to remove all ingredients
            for (Map.Entry<ResourceType, Integer> ingredient : recipe.getIngredients().entrySet()) {
                ResourceType type = ingredient.getKey();
                int amount = ingredient.getValue();

                if (!inventory.hasResource(type, amount)) {
                    // Rollback any resources we've already removed
                    rollbackIngredients(inventory, removedResources);
                    notifyCraftingFailed(recipe, "Missing required resources");
                    return false;
                }

                if (!inventory.removeResource(type, amount)) {
                    // Rollback any resources we've already removed
                    rollbackIngredients(inventory, removedResources);
                    notifyCraftingFailed(recipe, "Failed to remove resources");
                    return false;
                }

                removedResources.put(type, amount);
            }

            // Verify we have space for the results
            int totalResults = recipe.getResults().values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            if (!inventory.hasSpace(totalResults)) {
                // Rollback the removed ingredients
                rollbackIngredients(inventory, removedResources);
                notifyCraftingFailed(recipe, "Not enough inventory space for results");
                return false;
            }

            // Start crafting process
            CraftingProcess process = new CraftingProcess(recipe, inventory, craftingId, removedResources);
            activeProcesses.put(craftingId, process);

            // Schedule completion
            craftingExecutor.schedule(() -> {
                completeCrafting(craftingId);
            }, recipe.getCraftingTime(), TimeUnit.MILLISECONDS);

            notifyCraftingStarted(recipe);
            return true;

        } catch (Exception e) {
            notifyCraftingFailed(recipe, "Unexpected error: " + e.getMessage());
            return false;
        }
    }

    private void rollbackIngredients(Inventory inventory, Map<ResourceType, Integer> removedResources) {
        for (Map.Entry<ResourceType, Integer> entry : removedResources.entrySet()) {
            inventory.addResource(entry.getKey(), entry.getValue());
        }
    }

    private void completeCrafting(String craftingId) {
        CraftingProcess process = activeProcesses.remove(craftingId);
        if (process != null) {
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
                notifyCraftingFailed(recipe, "Failed to add results to inventory");
                // Return the original ingredients
                rollbackIngredients(inventory, process.getRemovedResources());
            }
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
            return 0f;
        }

        long elapsedTime = System.currentTimeMillis() - process.getStartTime();
        return Math.min(1.0f, (float) elapsedTime / process.getRecipe().getCraftingTime());
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