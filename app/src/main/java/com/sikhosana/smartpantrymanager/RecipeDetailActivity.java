package com.sikhosana.smartpantrymanager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sikhosana.smartpantrymanager.data.PantryDao;
import com.sikhosana.smartpantrymanager.data.RecipeDao;
import com.sikhosana.smartpantrymanager.logic.IngredientMatcher;
import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.model.Recipe;
import com.sikhosana.smartpantrymanager.model.RecipeIngredient;

import java.util.List;

/**
 * The full method and ingredient list for one recipe.
 *
 * Reached from the suggestions list by an Intent carrying the recipe's id.
 * Only the id travels in the Intent, not the whole Recipe object: passing an
 * id and re-reading the record here keeps the Intent small and guarantees the
 * screen shows current data rather than a stale copy.
 *
 * Each ingredient is marked with a tick or a cross by re-running the same
 * IngredientMatcher used by the suggestions screen, so the user can see at a
 * glance exactly which items they are short of.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.sikhosana.smartpantrymanager.RECIPE_ID";

    private LinearLayout layoutIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Recipe");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        layoutIngredients = findViewById(R.id.layoutIngredients);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, Recipe.NO_ID);
        if (recipeId == Recipe.NO_ID) {
            Toast.makeText(this, "No recipe was selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Recipe recipe = new RecipeDao(this).getById(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "That recipe could not be found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showRecipe(recipe);
    }

    private void showRecipe(Recipe recipe) {
        setTitle(recipe.getName());

        TextView textName = findViewById(R.id.textRecipeName);
        TextView textMeta = findViewById(R.id.textRecipeMeta);
        TextView textDescription = findViewById(R.id.textRecipeDescription);
        TextView textInstructions = findViewById(R.id.textInstructions);

        textName.setText(recipe.getName());
        textMeta.setText(recipe.getCategory() + "  \u2022  "
                + recipe.getPrepTimeMinutes() + " min  \u2022  serves " + recipe.getServings());

        if (recipe.getDescription() == null || recipe.getDescription().isEmpty()) {
            textDescription.setVisibility(View.GONE);
        } else {
            textDescription.setText(recipe.getDescription());
        }

        textInstructions.setText(recipe.getInstructions());

        showIngredients(recipe);
    }

    /**
     * Builds one row per ingredient, ticking the ones the pantry can cover.
     *
     * The rows are inflated in code rather than declared in the layout because
     * the number of ingredients differs from recipe to recipe and is only
     * known once the record has been read.
     */
    private void showIngredients(Recipe recipe) {
        List<PantryItem> pantry = new PantryDao(this).getAll();
        LayoutInflater inflater = LayoutInflater.from(this);

        int haveCount = 0;

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            boolean satisfied = pantryCanCover(pantry, ingredient);
            if (satisfied) {
                haveCount++;
            }

            View row = inflater.inflate(R.layout.item_ingredient_row, layoutIngredients, false);

            TextView tick = row.findViewById(R.id.textTick);
            TextView text = row.findViewById(R.id.textIngredient);

            tick.setText(satisfied ? "\u2713" : "\u2717");
            tick.setTextColor(satisfied ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#C62828"));
            text.setText(ingredient.getDisplayText());

            layoutIngredients.addView(row);
        }

        TextView hint = findViewById(R.id.textIngredientsHint);
        int total = recipe.getIngredientCount();

        if (haveCount == total) {
            hint.setText("You have everything you need.");
            hint.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            hint.setText("You have " + haveCount + " of " + total + " ingredients.");
            hint.setTextColor(Color.parseColor("#EF6C00"));
        }
    }

    /** True when anything in the pantry satisfies this requirement. */
    private boolean pantryCanCover(List<PantryItem> pantry, RecipeIngredient ingredient) {
        for (PantryItem item : pantry) {
            if (IngredientMatcher.compare(item, ingredient)
                    == IngredientMatcher.Result.SATISFIED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}