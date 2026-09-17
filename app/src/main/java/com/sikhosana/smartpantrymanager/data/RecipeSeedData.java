package com.sikhosana.smartpantrymanager.data;

import com.sikhosana.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;


 //The starter recipe collection loaded into the database on first run.

 //The recipes deliberately share a small set of common staples (eggs, milk,
 // flour, butter, rice, pasta, onion, garlic and so on). That overlap is what
 //makes the Suggested Recipes screen interesting: adding or removing a single
 //pantry item causes recipes to appear or disappear from the list.

 //Ingredient counts range from 2 to 6 so that a small pantry unlocks a few
 //simple recipes while more demanding ones stay correctly excluded.

public final class RecipeSeedData {

    private RecipeSeedData() { }

    public static List<Recipe> getSeedRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        Recipe r;

        // Breakfast
        r = new Recipe("Scrambled Eggs",
                "Soft, creamy scrambled eggs in under five minutes.",
                "1. Beat the eggs with the milk and salt.\n" +
                        "2. Melt the butter in a pan over low heat.\n" +
                        "3. Pour in the eggs and stir gently until just set.\n" +
                        "4. Serve immediately.",
                5, 2, "Breakfast");
        r.addIngredient("Eggs", 3, "pcs");
        r.addIngredient("Milk", 30, "ml");
        r.addIngredient("Butter", 15, "g");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Pancakes",
                "Fluffy stack of classic pancakes.",
                "1. Whisk the flour, sugar and eggs together.\n" +
                        "2. Add the milk slowly until the batter is smooth.\n" +
                        "3. Melt a little butter in a hot pan.\n" +
                        "4. Cook each pancake for about two minutes per side.",
                20, 4, "Breakfast");
        r.addIngredient("Flour", 200, "g");
        r.addIngredient("Eggs", 2, "pcs");
        r.addIngredient("Milk", 300, "ml");
        r.addIngredient("Sugar", 30, "g");
        r.addIngredient("Butter", 20, "g");
        recipes.add(r);

        r = new Recipe("Cheese Omelette",
                "A folded omelette with a melting cheese centre.",
                "1. Beat the eggs with the salt.\n" +
                        "2. Melt the butter in a non-stick pan.\n" +
                        "3. Pour in the eggs and cook until almost set.\n" +
                        "4. Add the cheese, fold over and serve.",
                10, 1, "Breakfast");
        r.addIngredient("Eggs", 3, "pcs");
        r.addIngredient("Cheese", 50, "g");
        r.addIngredient("Butter", 10, "g");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("French Toast",
                "A good way to use up bread that is going stale.",
                "1. Whisk the eggs, milk and sugar in a shallow dish.\n" +
                        "2. Soak each slice of bread for a few seconds per side.\n" +
                        "3. Fry in a buttered pan until golden.\n" +
                        "4. Serve warm.",
                15, 2, "Breakfast");
        r.addIngredient("Bread", 4, "pcs");
        r.addIngredient("Eggs", 2, "pcs");
        r.addIngredient("Milk", 100, "ml");
        r.addIngredient("Sugar", 20, "g");
        recipes.add(r);

        // Main
        r = new Recipe("Garlic Butter Pasta",
                "Four ingredients, fifteen minutes, no shopping trip.",
                "1. Boil the pasta in salted water until al dente.\n" +
                        "2. Melt the butter and gently fry the sliced garlic.\n" +
                        "3. Drain the pasta and toss it through the garlic butter.\n" +
                        "4. Season and serve.",
                15, 2, "Main");
        r.addIngredient("Pasta", 250, "g");
        r.addIngredient("Butter", 40, "g");
        r.addIngredient("Garlic", 3, "pcs");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Tomato Pasta",
                "A simple fresh tomato sauce over pasta.",
                "1. Boil the pasta in salted water.\n" +
                        "2. Fry the chopped onion and garlic in the oil until soft.\n" +
                        "3. Add the chopped tomatoes and simmer for ten minutes.\n" +
                        "4. Stir the drained pasta through the sauce.",
                25, 3, "Main");
        r.addIngredient("Pasta", 250, "g");
        r.addIngredient("Tomatoes", 4, "pcs");
        r.addIngredient("Onion", 1, "pcs");
        r.addIngredient("Garlic", 2, "pcs");
        r.addIngredient("Oil", 30, "ml");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Egg Fried Rice",
                "Best made with rice left over from the day before.",
                "1. Heat the oil in a wok or large pan.\n" +
                        "2. Fry the chopped onion until translucent.\n" +
                        "3. Push aside, scramble the eggs in the same pan.\n" +
                        "4. Add the rice and salt, stir-frying until hot through.",
                15, 2, "Main");
        r.addIngredient("Rice", 300, "g");
        r.addIngredient("Eggs", 2, "pcs");
        r.addIngredient("Onion", 1, "pcs");
        r.addIngredient("Oil", 30, "ml");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Chicken Stir Fry",
                "Quick weeknight chicken with a soy glaze.",
                "1. Slice the chicken into strips.\n" +
                        "2. Heat the oil and fry the onion and garlic.\n" +
                        "3. Add the chicken and cook until browned through.\n" +
                        "4. Pour over the soy sauce and toss for a minute.",
                20, 3, "Main");
        r.addIngredient("Chicken", 400, "g");
        r.addIngredient("Onion", 1, "pcs");
        r.addIngredient("Garlic", 2, "pcs");
        r.addIngredient("Oil", 30, "ml");
        r.addIngredient("Soy Sauce", 30, "ml");
        recipes.add(r);

        r = new Recipe("Chicken Fried Rice",
                "Fried rice turned into a full meal.",
                "1. Fry the diced chicken in the oil until cooked.\n" +
                        "2. Add the chopped onion and soften.\n" +
                        "3. Scramble in the eggs.\n" +
                        "4. Stir through the rice and soy sauce until piping hot.",
                25, 3, "Main");
        r.addIngredient("Rice", 300, "g");
        r.addIngredient("Chicken", 250, "g");
        r.addIngredient("Eggs", 2, "pcs");
        r.addIngredient("Onion", 1, "pcs");
        r.addIngredient("Oil", 30, "ml");
        r.addIngredient("Soy Sauce", 20, "ml");
        recipes.add(r);

        r = new Recipe("Roast Chicken and Potatoes",
                "A one-tray Sunday roast.",
                "1. Heat the oven to 200 degrees Celsius.\n" +
                        "2. Toss the potatoes and crushed garlic in the oil and salt.\n" +
                        "3. Sit the chicken on top in a roasting tray.\n" +
                        "4. Roast for about an hour until cooked through.",
                75, 4, "Main");
        r.addIngredient("Chicken", 800, "g");
        r.addIngredient("Potatoes", 600, "g");
        r.addIngredient("Garlic", 4, "pcs");
        r.addIngredient("Oil", 40, "ml");
        r.addIngredient("Salt", 2, "tsp");
        recipes.add(r);

        r = new Recipe("Boiled Rice",
                "The simplest side dish there is.",
                "1. Rinse the rice until the water runs clear.\n" +
                        "2. Add twice its volume of salted water.\n" +
                        "3. Simmer covered for twelve minutes.\n" +
                        "4. Rest off the heat for five minutes, then fluff.",
                20, 4, "Side");
        r.addIngredient("Rice", 250, "g");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Cheese Toastie",
                "Three ingredients and a hot pan.",
                "1. Butter the outside of both slices of bread.\n" +
                        "2. Fill with the grated cheese.\n" +
                        "3. Fry over medium heat until golden on both sides.",
                8, 1, "Snack");
        r.addIngredient("Bread", 2, "pcs");
        r.addIngredient("Cheese", 60, "g");
        r.addIngredient("Butter", 20, "g");
        recipes.add(r);

        // ------------------------------------------------------------ Sides
        r = new Recipe("Mashed Potatoes",
                "Smooth, buttery mash.",
                "1. Peel and boil the potatoes until tender.\n" +
                        "2. Drain well and return to the pan.\n" +
                        "3. Mash with the butter, milk and salt until smooth.",
                30, 4, "Side");
        r.addIngredient("Potatoes", 500, "g");
        r.addIngredient("Butter", 50, "g");
        r.addIngredient("Milk", 100, "ml");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Cheesy Baked Potatoes",
                "Crisp outside, molten cheese inside.",
                "1. Heat the oven to 200 degrees Celsius.\n" +
                        "2. Halve the potatoes and dot with butter and salt.\n" +
                        "3. Bake for forty minutes.\n" +
                        "4. Scatter over the cheese and bake five minutes more.",
                50, 3, "Side");
        r.addIngredient("Potatoes", 600, "g");
        r.addIngredient("Cheese", 100, "g");
        r.addIngredient("Butter", 30, "g");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Simple Tomato Salad",
                "No cooking required.",
                "1. Slice the tomatoes and onion thinly.\n" +
                        "2. Arrange on a plate.\n" +
                        "3. Dress with the oil and a pinch of salt.",
                10, 2, "Side");
        r.addIngredient("Tomatoes", 2, "pcs");
        r.addIngredient("Onion", 1, "pcs");
        r.addIngredient("Oil", 20, "ml");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        r = new Recipe("Tomato Soup",
                "A warming soup from storecupboard basics.",
                "1. Soften the chopped onion and garlic in the oil.\n" +
                        "2. Add the chopped tomatoes and a cup of water.\n" +
                        "3. Simmer for twenty minutes.\n" +
                        "4. Blend until smooth and season with the salt.",
                35, 3, "Main");
        r.addIngredient("Tomatoes", 6, "pcs");
        r.addIngredient("Onion", 1, "pcs");
        r.addIngredient("Garlic", 2, "pcs");
        r.addIngredient("Oil", 20, "ml");
        r.addIngredient("Salt", 1, "tsp");
        recipes.add(r);

        //  Dessert
        r = new Recipe("Sugar Cookies",
                "Crisp-edged, chewy-centred biscuits.",
                "1. Cream the butter and sugar together.\n" +
                        "2. Beat in the egg.\n" +
                        "3. Fold in the flour to form a dough.\n" +
                        "4. Bake spoonfuls at 180 degrees for twelve minutes.",
                40, 6, "Dessert");
        r.addIngredient("Flour", 250, "g");
        r.addIngredient("Butter", 150, "g");
        r.addIngredient("Sugar", 120, "g");
        r.addIngredient("Eggs", 1, "pcs");
        recipes.add(r);

        r = new Recipe("Vanilla Sponge Cake",
                "The classic equal-weights sponge.",
                "1. Cream the butter and sugar until pale.\n" +
                        "2. Beat in the eggs one at a time.\n" +
                        "3. Fold in the flour, then loosen with the milk.\n" +
                        "4. Bake at 180 degrees for twenty-five minutes.",
                45, 8, "Dessert");
        r.addIngredient("Flour", 200, "g");
        r.addIngredient("Sugar", 200, "g");
        r.addIngredient("Butter", 200, "g");
        r.addIngredient("Eggs", 4, "pcs");
        r.addIngredient("Milk", 60, "ml");
        recipes.add(r);

        return recipes;
    }
}