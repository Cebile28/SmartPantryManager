package com.sikhosana.smartpantrymanager.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sikhosana.smartpantrymanager.data.DatabaseHelper.IngredientTable;
import com.sikhosana.smartpantrymanager.data.DatabaseHelper.RecipeTable;
import com.sikhosana.smartpantrymanager.model.Recipe;
import com.sikhosana.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


 //Reads recipes and their ingredients out of the database.
 //Recipes are seeded once and never edited by the user, so this class only
 //needs to read - there are no insert or update methods here.

public class RecipeDao {

    private static final String TAG = "RecipeDao";

    private final DatabaseHelper helper;

    public RecipeDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

     //Every recipe, each with its ingredient list already filled in.
     // This deliberately runs two queries - all recipes, then all ingredients -
     //and stitches them together in memory. The obvious alternative is to loop
     //the recipes and query the ingredients of each one, but that is the
     //classic "N+1 queries" problem: 18 recipes would mean 19 trips to the
     //database instead of 2, and it gets worse as the collection grows.

    public List<Recipe> getAllWithIngredients() {
        List<Recipe> recipes = loadRecipes(null, null);
        if (recipes.isEmpty()) {
            return recipes;
        }

        Map<Long, List<RecipeIngredient>> ingredientsByRecipe = loadAllIngredients();

        for (Recipe recipe : recipes) {
            List<RecipeIngredient> ingredients = ingredientsByRecipe.get(recipe.getId());
            if (ingredients != null) {
                recipe.setIngredients(ingredients);
            }
        }
        return recipes;
    }

    // One recipe with its ingredients, or null when the id is unknown.
    public Recipe getById(long recipeId) {
        List<Recipe> found = loadRecipes(
                RecipeTable.COL_ID + " = ?",
                new String[]{String.valueOf(recipeId)});

        if (found.isEmpty()) {
            return null;
        }

        Recipe recipe = found.get(0);
        recipe.setIngredients(loadIngredientsFor(recipeId));
        return recipe;
    }

    // Used by the settings screen to report how many recipes are loaded.
    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + RecipeTable.TABLE_NAME, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    // helpers

    private List<Recipe> loadRecipes(String whereClause, String[] args) {
        List<Recipe> recipes = new ArrayList<>();

        String sql = "SELECT * FROM " + RecipeTable.TABLE_NAME;
        if (whereClause != null) {
            sql += " WHERE " + whereClause;
        }
        sql += " ORDER BY " + RecipeTable.COL_NAME + " COLLATE NOCASE ASC";

        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                recipes.add(recipeFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load recipes", e);
        }
        return recipes;
    }

    // Every ingredient in the database, grouped by the recipe that owns it.
    private Map<Long, List<RecipeIngredient>> loadAllIngredients() {
        Map<Long, List<RecipeIngredient>> grouped = new HashMap<>();

        String sql = "SELECT * FROM " + IngredientTable.TABLE_NAME +
                " ORDER BY " + IngredientTable.COL_ID + " ASC";

        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                RecipeIngredient ingredient = ingredientFromCursor(cursor);

                List<RecipeIngredient> list = grouped.get(ingredient.getRecipeId());
                if (list == null) {
                    list = new ArrayList<>();
                    grouped.put(ingredient.getRecipeId(), list);
                }
                list.add(ingredient);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load ingredients", e);
        }
        return grouped;
    }

    private List<RecipeIngredient> loadIngredientsFor(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        String sql = "SELECT * FROM " + IngredientTable.TABLE_NAME +
                " WHERE " + IngredientTable.COL_RECIPE + " = ?" +
                " ORDER BY " + IngredientTable.COL_ID + " ASC";

        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(recipeId)})) {
            while (cursor.moveToNext()) {
                ingredients.add(ingredientFromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load ingredients for recipe " + recipeId, e);
        }
        return ingredients;
    }

    private Recipe recipeFromCursor(Cursor c) {
        Recipe recipe = new Recipe();
        recipe.setId(c.getLong(c.getColumnIndexOrThrow(RecipeTable.COL_ID)));
        recipe.setName(c.getString(c.getColumnIndexOrThrow(RecipeTable.COL_NAME)));
        recipe.setDescription(c.getString(c.getColumnIndexOrThrow(RecipeTable.COL_DESC)));
        recipe.setInstructions(c.getString(c.getColumnIndexOrThrow(RecipeTable.COL_STEPS)));
        recipe.setPrepTimeMinutes(c.getInt(c.getColumnIndexOrThrow(RecipeTable.COL_PREP_TIME)));
        recipe.setServings(c.getInt(c.getColumnIndexOrThrow(RecipeTable.COL_SERVINGS)));
        recipe.setCategory(c.getString(c.getColumnIndexOrThrow(RecipeTable.COL_CATEGORY)));
        return recipe;
    }

    private RecipeIngredient ingredientFromCursor(Cursor c) {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setId(c.getLong(c.getColumnIndexOrThrow(IngredientTable.COL_ID)));
        ingredient.setRecipeId(c.getLong(c.getColumnIndexOrThrow(IngredientTable.COL_RECIPE)));
        ingredient.setName(c.getString(c.getColumnIndexOrThrow(IngredientTable.COL_NAME)));
        ingredient.setQuantity(c.getDouble(c.getColumnIndexOrThrow(IngredientTable.COL_QUANTITY)));
        ingredient.setUnit(c.getString(c.getColumnIndexOrThrow(IngredientTable.COL_UNIT)));
        return ingredient;
    }
}