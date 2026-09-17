package com.sikhosana.smartpantrymanager.logic;

import com.sikhosana.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;


 //The outcome of testing one recipe against the pantry.
 //As well as the yes/no answer, this carries WHY a recipe failed, which lets
 //the "Almost There" list tell the user exactly what they are short of.

public class MatchResult {

    private final Recipe recipe;
    private final List<String> missing = new ArrayList<>();      // not in the pantry at all
    private final List<String> insufficient = new ArrayList<>(); // present, not enough

    public MatchResult(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getRecipe() { return recipe; }

    public List<String> getMissing() { return missing; }
    public List<String> getInsufficient() { return insufficient; }

    void addMissing(String ingredientName) { missing.add(ingredientName); }
    void addInsufficient(String ingredientName) { insufficient.add(ingredientName); }


     //The strict-matching rule in one line: a recipe can be made only when
     //nothing is missing AND nothing is short.

    public boolean canMake() {
        return missing.isEmpty() && insufficient.isEmpty();
    }

    //How many ingredients stand between the user and this recipe.
    public int getShortfallCount() {
        return missing.size() + insufficient.size();
    }

    // True when exactly one ingredient is missing - the "almost there" case.
    public boolean isAlmostThere() {
        return getShortfallCount() == 1;
    }

    // Human-readable summary for the Almost There list.
    public String getShortfallText() {
        List<String> all = new ArrayList<>(missing);
        all.addAll(insufficient);

        if (all.isEmpty()) {
            return "Ready to cook";
        }
        if (all.size() == 1) {
            return "Missing " + all.get(0);
        }
        return "Missing " + all.size() + ": " + String.join(", ", all);
    }

    @Override
    public String toString() {
        return recipe.getName() + " -> " + (canMake() ? "CAN MAKE" : getShortfallText());
    }
}