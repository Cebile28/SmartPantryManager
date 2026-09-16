package com.sikhosana.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sikhosana.smartpantrymanager.logic.MatchResult;
import com.sikhosana.smartpantrymanager.logic.RecipeMatcher;
import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.model.Recipe;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the strict-matching rule itself.
 *
 * The first test is the exact scenario the assignment brief describes: a recipe
 * needing five ingredients where the pantry holds four must not be suggested.
 */
public class RecipeMatcherTest {

    /** A five-ingredient recipe used across several tests. */
    private Recipe fiveIngredientRecipe() {
        Recipe recipe = new Recipe("Test Bake", "", "1. Mix it.", 20, 2, "Dessert");
        recipe.addIngredient("Flour", 100, "g");
        recipe.addIngredient("Eggs", 2, "pcs");
        recipe.addIngredient("Milk", 100, "ml");
        recipe.addIngredient("Butter", 20, "g");
        recipe.addIngredient("Sugar", 30, "g");
        return recipe;
    }

    private List<PantryItem> fourOfTheFive() {
        return new ArrayList<>(Arrays.asList(
                new PantryItem("Flour", 1, "kg"),
                new PantryItem("Eggs", 6, "pcs"),
                new PantryItem("Milk", 1, "l"),
                new PantryItem("Butter", 250, "g")));
    }

    // ------------------------------------------------------- the core rule

    /** THE RULE: 5 ingredients needed, 4 held, so the recipe is excluded. */
    @Test
    public void recipeMissingOneIngredientCannotBeMade() {
        MatchResult result = RecipeMatcher.match(fiveIngredientRecipe(), fourOfTheFive());

        assertFalse("a recipe missing an ingredient must not be suggested",
                result.canMake());
    }

    @Test
    public void missingIngredientIsNamedInTheResult() {
        MatchResult result = RecipeMatcher.match(fiveIngredientRecipe(), fourOfTheFive());

        assertEquals(1, result.getShortfallCount());
        assertTrue(result.getMissing().contains("Sugar"));
    }

    @Test
    public void addingTheLastIngredientMakesTheRecipeAvailable() {
        List<PantryItem> pantry = fourOfTheFive();
        pantry.add(new PantryItem("Sugar", 500, "g"));

        MatchResult result = RecipeMatcher.match(fiveIngredientRecipe(), pantry);

        assertTrue("with every ingredient present the recipe must be suggested",
                result.canMake());
        assertEquals(0, result.getShortfallCount());
    }

    /** Having the right ingredient but not enough of it also excludes a recipe. */
    @Test
    public void insufficientQuantityAlsoExcludesTheRecipe() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Flour", 50, "g"),          // recipe needs 100 g
                new PantryItem("Eggs", 6, "pcs"),
                new PantryItem("Milk", 1, "l"),
                new PantryItem("Butter", 250, "g"),
                new PantryItem("Sugar", 500, "g"));

        MatchResult result = RecipeMatcher.match(fiveIngredientRecipe(), pantry);

        assertFalse(result.canMake());
        assertTrue("shortfall should be reported as insufficient, not missing",
                result.getInsufficient().contains("Flour"));
        assertTrue(result.getMissing().isEmpty());
    }

    // ----------------------------------------------------------- edge cases

    @Test
    public void emptyPantryCanMakeNothing() {
        MatchResult result = RecipeMatcher.match(fiveIngredientRecipe(), new ArrayList<>());

        assertFalse(result.canMake());
        assertEquals(5, result.getShortfallCount());
    }

    /** Guards against malformed seed data being suggested as cookable. */
    @Test
    public void recipeWithNoIngredientsIsNeverSuggested() {
        Recipe empty = new Recipe("Broken Recipe", "", "", 5, 1, "Main");

        assertFalse(RecipeMatcher.match(empty, fourOfTheFive()).canMake());
    }

    @Test
    public void pluralDifferenceDoesNotHideARecipe() {
        Recipe recipe = new Recipe("Tomato Salad", "", "1. Slice.", 10, 2, "Side");
        recipe.addIngredient("Tomatoes", 3, "pcs");

        List<PantryItem> pantry = Arrays.asList(new PantryItem("Tomato", 5, "pcs"));

        assertTrue("\"Tomato\" in the pantry should satisfy \"Tomatoes\" in the recipe",
                RecipeMatcher.match(recipe, pantry).canMake());
    }

    // -------------------------------------------------------- list building

    @Test
    public void makeableListContainsOnlyCookableRecipes() {
        Recipe cookable = new Recipe("Boiled Rice", "", "1. Boil.", 20, 4, "Side");
        cookable.addIngredient("Rice", 250, "g");

        List<Recipe> recipes = Arrays.asList(cookable, fiveIngredientRecipe());
        List<PantryItem> pantry = Arrays.asList(new PantryItem("Rice", 1, "kg"));

        List<MatchResult> makeable = RecipeMatcher.findMakeable(recipes, pantry);

        assertEquals(1, makeable.size());
        assertEquals("Boiled Rice", makeable.get(0).getRecipe().getName());
    }

    @Test
    public void almostThereListHoldsOnlyRecipesShortOfExactlyOne() {
        List<Recipe> recipes = Arrays.asList(fiveIngredientRecipe());

        List<MatchResult> almost = RecipeMatcher.findAlmostThere(recipes, fourOfTheFive());
        assertEquals(1, almost.size());

        // With an empty pantry the same recipe is five short, so it must not appear.
        assertTrue(RecipeMatcher.findAlmostThere(recipes, new ArrayList<>()).isEmpty());
    }

    @Test
    public void makeableRecipesAreSortedBySimplicity() {
        Recipe simple = new Recipe("Two Things", "", "", 5, 1, "Side");
        simple.addIngredient("Rice", 100, "g");
        simple.addIngredient("Salt", 1, "tsp");

        Recipe complex = new Recipe("Three Things", "", "", 5, 1, "Side");
        complex.addIngredient("Rice", 100, "g");
        complex.addIngredient("Salt", 1, "tsp");
        complex.addIngredient("Oil", 10, "ml");

        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Rice", 1, "kg"),
                new PantryItem("Salt", 50, "tsp"),
                new PantryItem("Oil", 500, "ml"));

        List<MatchResult> makeable =
                RecipeMatcher.findMakeable(Arrays.asList(complex, simple), pantry);

        assertEquals("the simplest recipe should be listed first",
                "Two Things", makeable.get(0).getRecipe().getName());
    }
}