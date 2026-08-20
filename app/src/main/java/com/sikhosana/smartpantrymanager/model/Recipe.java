package com.sikhosana.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe the user might cook.
 *
 * The ingredient list is held as a List<RecipeIngredient> in memory but is
 * stored across two tables in the database (recipes and recipe_ingredients),
 * which keeps the data normalised and lets the matching logic compare
 * quantities ingredient by ingredient.
 */
public class Recipe {

    public static final long NO_ID = -1;

    private long id;
    private String name;
    private String description;
    private String instructions;      // preparation steps, newline separated
    private int prepTimeMinutes;
    private int servings;
    private String category;          // e.g. "Breakfast", "Main", "Dessert"

    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {
        this.id = NO_ID;
    }

    public Recipe(String name, String description, String instructions,
                  int prepTimeMinutes, int servings, String category) {
        this();
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.prepTimeMinutes = prepTimeMinutes;
        this.servings = servings;
        this.category = category;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = (ingredients == null) ? new ArrayList<>() : ingredients;
    }

    /** Convenience for building recipes in the seed data. */
    public void addIngredient(RecipeIngredient ingredient) {
        if (ingredient != null) {
            ingredient.setRecipeId(this.id);
            this.ingredients.add(ingredient);
        }
    }

    public void addIngredient(String name, double quantity, String unit) {
        addIngredient(new RecipeIngredient(name, quantity, unit));
    }

    public int getIngredientCount() {
        return ingredients.size();
    }

    /** Shown on the recipe card, e.g. "5 ingredients - 25 min". */
    public String getSummaryText() {
        return getIngredientCount() + " ingredients \u2022 " + prepTimeMinutes + " min";
    }

    @Override
    public String toString() {
        return name;
    }
}