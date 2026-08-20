package com.sikhosana.smartpantrymanager.model;

/**
 * One ingredient required by a recipe.
 *
 * This is a separate class - and later a separate database table - rather than
 * a comma-separated string on the Recipe, because the strict-matching rule has
 * to compare quantities per ingredient ("at least the required quantity").
 * A single text field could not answer "does the user have 200g of flour?".
 */
public class RecipeIngredient {

    public static final long NO_ID = -1;

    private long id;
    private long recipeId;        // foreign key back to the owning recipe
    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient() {
        this.id = NO_ID;
        this.recipeId = NO_ID;
    }

    public RecipeIngredient(String name, double quantity, String unit) {
        this();
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public RecipeIngredient(long id, long recipeId, String name,
                            double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    /** Formatted for the recipe detail screen, e.g. "200 g flour". */
    public String getDisplayText() {
        String amount = (quantity == Math.floor(quantity))
                ? String.valueOf((long) quantity)
                : String.valueOf(quantity);
        if (unit == null || unit.isEmpty()) {
            return amount + " " + name;
        }
        return amount + " " + unit + " " + name;
    }

    @Override
    public String toString() {
        return getDisplayText();
    }
}