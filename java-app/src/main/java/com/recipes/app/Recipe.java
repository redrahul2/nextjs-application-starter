package com.recipes.app;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recipe {
    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private List<String> dietaryRestrictions;
    private String category;
    private int cookingTimeMinutes;
    private String difficulty;
    private int servings;
    private boolean favorite;
    private String imageUrl;

    public Recipe() {
        this.id = UUID.randomUUID().toString();
        this.dietaryRestrictions = new ArrayList<>();
        this.favorite = false;
    }

    public Recipe(String title, String ingredients, String instructions) {
        this();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    // Existing getters and setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    // New getters and setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCookingTimeMinutes() {
        return cookingTimeMinutes;
    }

    public void setCookingTimeMinutes(int cookingTimeMinutes) {
        this.cookingTimeMinutes = cookingTimeMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void addDietaryRestriction(String restriction) {
        if (!dietaryRestrictions.contains(restriction)) {
            dietaryRestrictions.add(restriction);
        }
    }

    public void removeDietaryRestriction(String restriction) {
        dietaryRestrictions.remove(restriction);
    }

    // Formatted time string for display
    public String getFormattedCookingTime() {
        if (cookingTimeMinutes < 60) {
            return cookingTimeMinutes + " mins";
        }
        int hours = cookingTimeMinutes / 60;
        int minutes = cookingTimeMinutes % 60;
        return hours + "h " + (minutes > 0 ? minutes + "m" : "");
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", cookingTime='" + getFormattedCookingTime() + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", servings=" + servings +
                ", dietaryRestrictions=" + dietaryRestrictions +
                '}';
    }
}
