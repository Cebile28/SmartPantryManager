package com.sikhosana.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sikhosana.smartpantrymanager.data.PantryDao;
import com.sikhosana.smartpantrymanager.data.RecipeDao;
import com.sikhosana.smartpantrymanager.logic.MatchResult;
import com.sikhosana.smartpantrymanager.logic.RecipeMatcher;
import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.model.Recipe;
import com.sikhosana.smartpantrymanager.ui.RecipeAdapter;

import java.util.List;

/**
 * Shows only the recipes the user can cook right now.
 *
 * This screen is where the strict-matching rule becomes visible: it loads the
 * pantry and the recipe collection, hands both to RecipeMatcher, and displays
 * the result. Recipes short of a single ingredient appear underneath in a
 * clearly separated section rather than mixed into the suggestions.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private PantryDao pantryDao;
    private RecipeDao recipeDao;
    private RecipeAdapter adapter;

    private RecyclerView recyclerView;
    private View layoutEmpty;
    private TextView textEmptyTitle;
    private TextView textEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Suggested Recipes");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pantryDao = new PantryDao(this);
        recipeDao = new RecipeDao(this);

        recyclerView = findViewById(R.id.recyclerSuggestions);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        textEmptyTitle = findViewById(R.id.textEmptyTitle);
        textEmptyMessage = findViewById(R.id.textEmptyMessage);

        adapter = new RecipeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Recalculating in onResume rather than onCreate is what makes the
     * demonstration work: change the pantry, come back to this screen, and the
     * suggestions have already been recalculated against the new contents.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshSuggestions();
    }

    private void refreshSuggestions() {
        List<PantryItem> pantry = pantryDao.getAll();
        List<Recipe> recipes = recipeDao.getAllWithIngredients();

        List<MatchResult> makeable = RecipeMatcher.findMakeable(recipes, pantry);
        List<MatchResult> almostThere = RecipeMatcher.findAlmostThere(recipes, pantry);

        adapter.setResults(makeable, almostThere);

        boolean nothingToShow = makeable.isEmpty() && almostThere.isEmpty();
        showEmptyState(nothingToShow, pantry.isEmpty());
    }

    /** The empty message explains WHY the list is empty, which differs by cause. */
    private void showEmptyState(boolean nothingToShow, boolean pantryIsEmpty) {
        layoutEmpty.setVisibility(nothingToShow ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(nothingToShow ? View.GONE : View.VISIBLE);

        if (!nothingToShow) {
            return;
        }

        if (pantryIsEmpty) {
            textEmptyTitle.setText("Your pantry is empty");
            textEmptyMessage.setText(
                    "Add the ingredients you have at home and we will show you what you can cook.");
        } else {
            textEmptyTitle.setText("Nothing you can cook yet");
            textEmptyMessage.setText(
                    "None of the recipes match everything in your pantry. Add a few more staples and check back.");
        }
    }

    // ------------------------------------------ RecipeAdapter callback

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Toast.makeText(this, "Recipe detail screen comes next", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();       // back arrow returns to the pantry list
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}