package com.sikhosana.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sikhosana.smartpantrymanager.logic.IngredientMatcher;
import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

/**
 * Unit tests for the name normalisation and unit conversion that the
 * strict-matching rule depends on.
 *
 * These are local JUnit tests rather than instrumented tests: IngredientMatcher
 * contains no Android imports, so it can be tested on the development machine
 * in a second or two without an emulator.
 */
public class IngredientMatcherTest {

    // ------------------------------------------------------ name handling

    @Test
    public void normalise_lowercasesAndTrims() {
        assertEquals("milk", IngredientMatcher.normaliseName("  MILK  "));
    }

    @Test
    public void normalise_stripsPunctuation() {
        assertEquals("milk", IngredientMatcher.normaliseName("Milk!"));
    }

    @Test
    public void normalise_handlesNullSafely() {
        assertEquals("", IngredientMatcher.normaliseName(null));
    }

    /** The case the brief specifically warns about. */
    @Test
    public void names_tomatoMatchesTomatoes() {
        assertTrue(IngredientMatcher.namesMatch("Tomatoes", "tomato"));
    }

    @Test
    public void names_regularPluralsMatchSingulars() {
        assertTrue(IngredientMatcher.namesMatch("EGGS", "egg"));
        assertTrue(IngredientMatcher.namesMatch("Potatoes", "potato"));
        assertTrue(IngredientMatcher.namesMatch("Berries", "berry"));
        assertTrue(IngredientMatcher.namesMatch("Dishes", "dish"));
    }

    @Test
    public void names_synonymsMatch() {
        assertTrue(IngredientMatcher.namesMatch("Soya Sauce", "soy sauce"));
        assertTrue(IngredientMatcher.namesMatch("Aubergine", "eggplant"));
    }

    /** Regression test: the synonym map must be applied after singularising too. */
    @Test
    public void names_pluralSynonymsMatch() {
        assertTrue(IngredientMatcher.namesMatch("Spring Onions", "green onion"));
    }

    @Test
    public void names_differentIngredientsDoNotMatch() {
        assertFalse(IngredientMatcher.namesMatch("Milk", "Butter"));
        assertFalse(IngredientMatcher.namesMatch("Chicken", "Chickpeas"));
        assertFalse(IngredientMatcher.namesMatch("Rice", "Ice"));
    }

    /** "glass" must not be reduced to "glas" by the plural rule. */
    @Test
    public void names_doubleSEndingIsNotTrimmed() {
        assertFalse(IngredientMatcher.namesMatch("Glass", "Glas"));
    }

    @Test
    public void names_emptyNameNeverMatches() {
        assertFalse(IngredientMatcher.namesMatch("", ""));
    }

    // --------------------------------------------------- unit conversion

    @Test
    public void units_kilogramsCoverGrams() {
        assertSatisfied(item("Flour", 1.5, "kg"), needs("Flour", 200, "g"));
    }

    @Test
    public void units_litresCoverMillilitres() {
        assertSatisfied(item("Milk", 1, "l"), needs("Milk", 300, "ml"));
    }

    @Test
    public void units_exactBoundaryIsSatisfied() {
        assertSatisfied(item("Flour", 0.5, "kg"), needs("Flour", 500, "g"));
    }

    @Test
    public void units_cupsConvertToMillilitres() {
        assertSatisfied(item("Milk", 2, "cup"), needs("Milk", 300, "ml"));
    }

    @Test
    public void units_tablespoonCoversThreeTeaspoons() {
        assertSatisfied(item("Oil", 1, "tbsp"), needs("Oil", 3, "tsp"));
    }

    @Test
    public void units_pluralUnitNamesAreUnderstood() {
        assertSatisfied(item("Flour", 2, "kilograms"), needs("Flour", 500, "g"));
    }

    @Test
    public void units_tooLittleIsInsufficient() {
        assertInsufficient(item("Flour", 100, "g"), needs("Flour", 200, "g"));
        assertInsufficient(item("Milk", 200, "ml"), needs("Milk", 1, "l"));
        assertInsufficient(item("Eggs", 1, "pcs"), needs("Eggs", 3, "pcs"));
    }

    /**
     * Grams cannot be converted to teaspoons without knowing the ingredient's
     * density. The deliberate choice is to treat the ingredient as present
     * rather than hide a recipe the user could obviously cook.
     */
    @Test
    public void units_incomparableDimensionsCountAsPresent() {
        assertSatisfied(item("Salt", 50, "g"), needs("Salt", 1, "tsp"));
    }

    @Test
    public void compare_nullsAreHandled() {
        assertEquals(IngredientMatcher.Result.NAME_MISMATCH,
                IngredientMatcher.compare(null, needs("Flour", 1, "g")));
        assertEquals(IngredientMatcher.Result.NAME_MISMATCH,
                IngredientMatcher.compare(item("Flour", 1, "g"), null));
    }

    // ------------------------------------------------------------ helpers

    private PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit);
    }

    private RecipeIngredient needs(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private void assertSatisfied(PantryItem have, RecipeIngredient need) {
        assertEquals("expected " + have.getFormattedQuantity() + " to satisfy "
                        + need.getDisplayText(),
                IngredientMatcher.Result.SATISFIED,
                IngredientMatcher.compare(have, need));
    }

    private void assertInsufficient(PantryItem have, RecipeIngredient need) {
        assertEquals("expected " + have.getFormattedQuantity() + " to be short of "
                        + need.getDisplayText(),
                IngredientMatcher.Result.INSUFFICIENT,
                IngredientMatcher.compare(have, need));
    }
}
