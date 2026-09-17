package com.sikhosana.smartpantrymanager.logic;

import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.model.Recipe;
import com.sikhosana.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Applies the strict-matching rule across the whole recipe collection.
 *
 * THE RULE: a recipe is suggested only when EVERY ingredient it requires is
 * in the pantry, in at least the required quantity. A recipe needing five
 * ingredients where the user has four is excluded - there is no partial
 * credit in the suggestions list.
 *
 * Recipes missing exactly one ingredient are collected separately so they can
 * be shown in a clearly-labelled "Almost There" section, which the brief
 * allows as an optional extra provided it stays separate from the strict list.
 */
public final class RecipeMatcher {

    private RecipeMatcher() { }


     // Tests one recipe against the pantry.

     //Every ingredient is checked even after the first failure, because the
     //result records the complete shortfall so the UI can explain it.

    public static MatchResult match(Recipe recipe, List<PantryItem> pantry) {
        MatchResult result = new MatchResult(recipe);

        // A recipe with no ingredients cannot be verified, so it is never
        // suggested. This guards against malformed seed data.
        if (recipe.getIngredients().isEmpty()) {
            result.addMissing("no ingredients listed");
            return result;
        }

        for (RecipeIngredient required : recipe.getIngredients()) {
            IngredientMatcher.Result best = IngredientMatcher.Result.NAME_MISMATCH;

            // Scan the pantry for anything that matches this ingredient.
            // The best outcome found wins: a SATISFIED match anywhere beats an
            // INSUFFICIENT one, which matters if the user has two similar rows.
            for (PantryItem owned : pantry) {
                IngredientMatcher.Result outcome = IngredientMatcher.compare(owned, required);

                if (outcome == IngredientMatcher.Result.SATISFIED) {
                    best = outcome;
                    break;
                }
                if (outcome == IngredientMatcher.Result.INSUFFICIENT) {
                    best = outcome;
                }
            }

            if (best == IngredientMatcher.Result.NAME_MISMATCH) {
                result.addMissing(required.getName());
            } else if (best == IngredientMatcher.Result.INSUFFICIENT) {
                result.addInsufficient(required.getName());
            }
        }

        return result;
    }


     // Every recipe the user can cook right now.
     //Sorted by fewest ingredients first, so the quickest options appear at
     //the top of the suggestions list.

    public static List<MatchResult> findMakeable(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> makeable = new ArrayList<>();

        for (Recipe recipe : recipes) {
            MatchResult result = match(recipe, pantry);
            if (result.canMake()) {
                makeable.add(result);
            }
        }

        Collections.sort(makeable, (a, b) ->
                Integer.compare(a.getRecipe().getIngredientCount(),
                        b.getRecipe().getIngredientCount()));
        return makeable;
    }


     // Recipes short of exactly one ingredient.
     //Kept strictly separate from findMakeable so the suggestions list never
     //mixes "you can cook this" with "you nearly can".

    public static List<MatchResult> findAlmostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> almost = new ArrayList<>();

        for (Recipe recipe : recipes) {
            MatchResult result = match(recipe, pantry);
            if (result.isAlmostThere()) {
                almost.add(result);
            }
        }

        Collections.sort(almost, (a, b) ->
                Integer.compare(a.getRecipe().getIngredientCount(),
                        b.getRecipe().getIngredientCount()));
        return almost;
    }
}