package com.sikhosana.smartpantrymanager.logic;

import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.model.RecipeIngredient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Decides whether one pantry item can satisfy one recipe ingredient.
 *
 * Two kinds of real-world messiness have to be handled before any comparison
 * is possible:
 *
 *  1. NAMES.  A user typing "Tomatoes" should match a recipe asking for
 *     "tomato". Names are normalised - lowercased, punctuation stripped,
 *     plurals reduced to singular, and a few common synonyms mapped - before
 *     they are compared.
 *
 *  2. UNITS.  A pantry holding 1.5 kg of flour must satisfy a recipe wanting
 *     200 g. Units are converted to a base unit (grams for mass, millilitres
 *     for volume, pieces for counts) so the numbers can be compared directly.
 *
 * Everything here is plain Java with no Android imports, which keeps the rule
 * testable on its own and separate from the screens that display it.
 */
public final class IngredientMatcher {

    private IngredientMatcher() { }

    //The outcome of comparing one pantry item against one requirement.
    public enum Result {
        // Enough of the right ingredient.
        SATISFIED,
        // Right ingredient, but not enough of it.
        INSUFFICIENT,
        //Different ingredient entirely.
        NAME_MISMATCH
    }

    //  names

    // Words that mean the same thing, mapped onto one spelling.
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    static {
        SYNONYMS.put("aubergine", "eggplant");
        SYNONYMS.put("courgette", "zucchini");
        SYNONYMS.put("capsicum", "pepper");
        SYNONYMS.put("coriander", "cilantro");
        SYNONYMS.put("soya sauce", "soy sauce");
        SYNONYMS.put("maize meal", "cornmeal");
        SYNONYMS.put("mince", "ground beef");
        SYNONYMS.put("prawn", "shrimp");
        SYNONYMS.put("spring onion", "green onion");
        SYNONYMS.put("chile", "chilli");
        SYNONYMS.put("chili", "chilli");
    }


     // Reduces a name to a comparable form.
     // "  Tomatoes! " and "tomato" both become "tomato", which is what stops a
     //trivial spelling difference from hiding a recipe the user can cook.

    public static String normaliseName(String raw) {
        if (raw == null) {
            return "";
        }

        String name = raw.toLowerCase(Locale.ROOT).trim();

        // Drop anything that is not a letter, digit or space.
        name = name.replaceAll("[^a-z0-9 ]", " ");

        // Collapse runs of whitespace down to a single space.
        name = name.replaceAll("\\s+", " ").trim();

        if (name.isEmpty()) {
            return "";
        }

        // Map the whole phrase first, so "spring onion" is caught before
        // "onion" is looked at on its own.
        String synonym = SYNONYMS.get(name);
        if (synonym != null) {
            name = synonym;
        }

        name = singularise(name);

        // Check again after singularising, so a plural synonym such as
        // "spring onions" resolves once it has become "spring onion".
        synonym = SYNONYMS.get(name);
        if (synonym != null) {
            name = synonym;
        }

        return name;
    }


     //Turns a plural into its singular form.

     //These are deliberately simple rules rather than a full language library:
     //they cover the food words this app deals with, which is all the brief
     // requires. Irregular plurals such as "leaves" are left alone, which is
     // safe because both sides of the comparison are normalised the same way.

    private static String singularise(String word) {
        // Only the last word of a phrase is pluralised: "tins of tomatoes".
        int lastSpace = word.lastIndexOf(' ');
        String prefix = lastSpace == -1 ? "" : word.substring(0, lastSpace + 1);
        String last = lastSpace == -1 ? word : word.substring(lastSpace + 1);

        if (last.length() <= 3) {
            return word;                       // "peas" is left as is, too short to trim safely
        }

        if (last.endsWith("ies")) {
            // berries -> berry
            last = last.substring(0, last.length() - 3) + "y";
        } else if (last.endsWith("oes")) {
            // tomatoes -> tomato, potatoes -> potato
            last = last.substring(0, last.length() - 2);
        } else if (last.endsWith("ses") || last.endsWith("shes")
                || last.endsWith("ches") || last.endsWith("xes")) {
            // dishes -> dish, boxes -> box
            last = last.substring(0, last.length() - 2);
        } else if (last.endsWith("s") && !last.endsWith("ss")) {
            // eggs -> egg, but glass stays glass
            last = last.substring(0, last.length() - 1);
        }

        return prefix + last;
    }

    //True when two ingredient names refer to the same thing.
    public static boolean namesMatch(String a, String b) {
        String normalisedA = normaliseName(a);
        String normalisedB = normaliseName(b);
        return !normalisedA.isEmpty() && normalisedA.equals(normalisedB);
    }

    // ------------------------------------------------------------- units

    // What kind of measurement a unit expresses.
    private enum Dimension { MASS, VOLUME, COUNT, UNKNOWN }

    //A unit and how many base units one of it is worth.
    private static final class Unit {
        final Dimension dimension;
        final double toBase;

        Unit(Dimension dimension, double toBase) {
            this.dimension = dimension;
            this.toBase = toBase;
        }
    }

    private static final Map<String, Unit> UNITS = new HashMap<>();
    static {
        // Mass, base unit gram
        put(new Unit(Dimension.MASS, 0.001), "mg", "milligram", "milligrams");
        put(new Unit(Dimension.MASS, 1), "g", "gram", "grams", "gr");
        put(new Unit(Dimension.MASS, 1000), "kg", "kilo", "kilos", "kilogram", "kilograms");

        // Volume, base unit millilitre
        put(new Unit(Dimension.VOLUME, 1), "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        put(new Unit(Dimension.VOLUME, 1000), "l", "litre", "litres", "liter", "liters");
        put(new Unit(Dimension.VOLUME, 5), "tsp", "teaspoon", "teaspoons");
        put(new Unit(Dimension.VOLUME, 15), "tbsp", "tablespoon", "tablespoons");
        put(new Unit(Dimension.VOLUME, 250), "cup", "cups");

        // Countable things, base unit one piece
        put(new Unit(Dimension.COUNT, 1), "pcs", "pc", "piece", "pieces", "unit", "units", "each", "");
    }

    private static void put(Unit unit, String... names) {
        for (String name : names) {
            UNITS.put(name, unit);
        }
    }

    private static Unit lookupUnit(String raw) {
        if (raw == null) {
            return UNITS.get("");
        }
        Unit unit = UNITS.get(raw.toLowerCase(Locale.ROOT).trim());
        return unit == null ? new Unit(Dimension.UNKNOWN, 1) : unit;
    }

    // the decision

     //Compares one pantry item against one recipe requirement.
     //When the two units measure different things - grams of salt against a
     //teaspoon of salt, say - there is no correct conversion without knowing
     //the ingredient's density. Rather than wrongly hiding a recipe the user
     //can almost certainly cook, the ingredient counts as satisfied on the
     // strength of being present. This is a deliberate trade-off: it errs
     //towards suggesting a recipe rather than silently excluding it.

    public static Result compare(PantryItem have, RecipeIngredient need) {
        if (have == null || need == null) {
            return Result.NAME_MISMATCH;
        }

        if (!namesMatch(have.getName(), need.getName())) {
            return Result.NAME_MISMATCH;
        }

        Unit haveUnit = lookupUnit(have.getUnit());
        Unit needUnit = lookupUnit(need.getUnit());

        boolean comparable = haveUnit.dimension == needUnit.dimension
                && haveUnit.dimension != Dimension.UNKNOWN;

        if (!comparable) {
            // Present, but the amounts cannot be compared meaningfully.
            return Result.SATISFIED;
        }

        double haveAmount = have.getQuantity() * haveUnit.toBase;
        double needAmount = need.getQuantity() * needUnit.toBase;

        // A tiny tolerance absorbs floating point rounding, so that 0.5 kg
        // does not fail to cover a 500 g requirement by a fraction of a gram.
        return (haveAmount + 0.0001 >= needAmount)
                ? Result.SATISFIED
                : Result.INSUFFICIENT;
    }
}