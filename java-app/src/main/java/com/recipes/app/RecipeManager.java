package com.recipes.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeManager {
    private final ObservableList<Recipe> recipes;
    private final FilteredList<Recipe> filteredRecipes;

    public RecipeManager() {
        this.recipes = FXCollections.observableArrayList();
        this.filteredRecipes = new FilteredList<>(recipes);
    }

    public void addRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        if (recipe.getTitle() == null || recipe.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title cannot be empty");
        }
        recipes.add(recipe);
    }

    public void updateRecipe(Recipe recipe) {
        if (recipe == null || recipe.getId() == null) {
            throw new IllegalArgumentException("Invalid recipe");
        }

        Recipe existingRecipe = findRecipeById(recipe.getId());
        if (existingRecipe != null) {
            int index = recipes.indexOf(existingRecipe);
            recipes.set(index, recipe);
        }
    }

    public void deleteRecipe(String recipeId) {
        if (recipeId == null) {
            throw new IllegalArgumentException("Recipe ID cannot be null");
        }

        Recipe recipe = findRecipeById(recipeId);
        if (recipe != null) {
            recipes.remove(recipe);
        }
    }

    public ObservableList<Recipe> getAllRecipes() {
        return recipes;
    }

    public FilteredList<Recipe> getFilteredRecipes() {
        return filteredRecipes;
    }

    public void setFilter(RecipeFilter filter) {
        filteredRecipes.setPredicate(recipe -> {
            if (filter == null) {
                return true;
            }

            boolean matchesDietary = filter.getDietaryRestrictions().isEmpty() ||
                    recipe.getDietaryRestrictions().containsAll(filter.getDietaryRestrictions());

            boolean matchesCategory = filter.getCategory() == null ||
                    filter.getCategory().equals(recipe.getCategory());

            boolean matchesDifficulty = filter.getDifficulty() == null ||
                    filter.getDifficulty().equals(recipe.getDifficulty());

            boolean matchesSearch = filter.getSearchText() == null ||
                    filter.getSearchText().isEmpty() ||
                    recipe.getTitle().toLowerCase().contains(filter.getSearchText().toLowerCase()) ||
                    recipe.getIngredients().toLowerCase().contains(filter.getSearchText().toLowerCase());

            boolean matchesFavorites = !filter.isShowFavoritesOnly() ||
                    recipe.isFavorite();

            return matchesDietary && matchesCategory && matchesDifficulty && 
                   matchesSearch && matchesFavorites;
        });
    }

    public List<Recipe> filterRecipesByDietaryRestriction(String restriction) {
        if (restriction == null || restriction.trim().isEmpty()) {
            return recipes;
        }

        return recipes.stream()
                .filter(recipe -> recipe.getDietaryRestrictions().contains(restriction))
                .collect(Collectors.toList());
    }

    public void toggleFavorite(String recipeId) {
        Recipe recipe = findRecipeById(recipeId);
        if (recipe != null) {
            recipe.setFavorite(!recipe.isFavorite());
        }
    }

    private Recipe findRecipeById(String id) {
        return recipes.stream()
                .filter(recipe -> recipe.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Helper method to add sample recipes for testing
    public void addSampleRecipes() {
        Recipe recipe1 = new Recipe("Vegan Pasta", 
            "1 lb pasta\n2 cups cherry tomatoes\n1/4 cup fresh basil\n3 tbsp olive oil\n4 cloves garlic\nSalt and pepper to taste", 
            "1. Boil pasta according to package instructions\n2. Halve tomatoes and mince garlic\n3. Heat olive oil and sauté garlic\n4. Add tomatoes and cook until softened\n5. Toss with pasta and fresh basil\n6. Season with salt and pepper");
        recipe1.addDietaryRestriction("Vegan");
        recipe1.addDietaryRestriction("Vegetarian");
        recipe1.setCategory("Dinner");
        recipe1.setCookingTimeMinutes(30);
        recipe1.setDifficulty("Easy");
        recipe1.setServings(4);
        recipe1.setImageUrl("https://images.pexels.com/photos/1279330/pexels-photo-1279330.jpeg");

        Recipe recipe2 = new Recipe("Gluten-Free Pizza", 
            "1 medium cauliflower\n2 eggs\n1 cup mozzarella cheese\n1 cup tomato sauce\nToppings of choice\nItalian herbs", 
            "1. Rice cauliflower in food processor\n2. Steam and drain well\n3. Mix with eggs and cheese\n4. Form into crust on baking sheet\n5. Bake at 400°F for 20 minutes\n6. Add toppings and bake 10 more minutes");
        recipe2.addDietaryRestriction("Gluten-Free");
        recipe2.setCategory("Dinner");
        recipe2.setCookingTimeMinutes(45);
        recipe2.setDifficulty("Medium");
        recipe2.setServings(2);
        recipe2.setImageUrl("https://images.pexels.com/photos/825661/pexels-photo-825661.jpeg");

        Recipe recipe3 = new Recipe("Breakfast Smoothie Bowl", 
            "2 frozen bananas\n1 cup mixed frozen berries\n1/2 cup almond milk\n2 tbsp chia seeds\n1/4 cup granola\nFresh fruit for topping", 
            "1. Blend frozen fruits with almond milk until smooth\n2. Pour into bowl\n3. Top with chia seeds, granola, and fresh fruit\n4. Serve immediately");
        recipe3.addDietaryRestriction("Vegan");
        recipe3.addDietaryRestriction("Gluten-Free");
        recipe3.setCategory("Breakfast");
        recipe3.setCookingTimeMinutes(10);
        recipe3.setDifficulty("Easy");
        recipe3.setServings(1);
        recipe3.setImageUrl("https://images.pexels.com/photos/1099680/pexels-photo-1099680.jpeg");

        addRecipe(recipe1);
        addRecipe(recipe2);
        addRecipe(recipe3);
    }
}

// Helper class for filtering recipes
class RecipeFilter {
    private List<String> dietaryRestrictions;
    private String category;
    private String difficulty;
    private String searchText;
    private boolean showFavoritesOnly;

    public RecipeFilter() {
        this.dietaryRestrictions = FXCollections.observableArrayList();
        this.showFavoritesOnly = false;
    }

    // Getters and setters
    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public boolean isShowFavoritesOnly() {
        return showFavoritesOnly;
    }

    public void setShowFavoritesOnly(boolean showFavoritesOnly) {
        this.showFavoritesOnly = showFavoritesOnly;
    }
}
