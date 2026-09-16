# Smart Pantry Manager

An Android application that helps reduce food waste by tracking the ingredients
you already have at home and suggesting only the recipes you can cook with them
right now — no shopping trip required.

Built in Java with Android Studio for **Mobile App Development 700**.

---

## The problem it solves

Most recipe apps work the wrong way round: you pick a recipe, then go shopping.
That is no help when the question is *"what can I actually make with what is in
the cupboard?"* Ingredients get bought for one dish, partly used, and forgotten
until they are thrown away.

Smart Pantry Manager starts from your pantry instead. You record what you have,
and the app shows only the recipes you can cook immediately.

---

## The strict-matching rule

This is the core logic of the app. A recipe is suggested **only when every
single ingredient it requires is present in the pantry, in at least the required
quantity.**

- A recipe needing 5 ingredients where you have 4 is **not** suggested.
- Having the right ingredient but not enough of it also excludes the recipe.
- Recipes short of exactly one ingredient appear in a clearly separated
  **"Almost There"** section, never mixed into the main suggestions.

The matching is designed to survive ordinary real-world messiness:

| Situation                                      | Handled by                                                     |
|------------------------------------------------|----------------------------------------------------------------|
| `"Tomatoes"` vs `"tomato"`                     | Names are lowercased, stripped of punctuation and singularised |
| `"Spring Onions"` vs `"green onion"`           | A synonym map applied before and after singularising           |
| 1.5 **kg** of flour vs a 200 **g** requirement | Units converted to a base unit (g / ml / pcs) before comparing |
| 2 **cups** of milk vs 300 **ml**               | Same conversion — 1 cup = 250 ml                               |

Where two units measure different things — grams of salt against a teaspoon of
salt — no correct conversion exists without knowing the ingredient's density.
In that case the ingredient counts as present, erring towards suggesting a
recipe rather than silently hiding one the user could obviously cook.

The logic lives in `logic/IngredientMatcher.java` and `logic/RecipeMatcher.java`,
deliberately written as plain Java with no Android imports so it can be reasoned
about and tested independently of the interface.

---

## Features

- **Pantry management** — full create, read, update and delete on ingredients,
  each with a quantity, a unit and an optional expiry date
- **Expiry tracking** — items are colour-coded by how soon they expire, with a
  warning banner on the pantry list
- **18 seeded recipes** — loaded into the database automatically on first run
- **Suggested Recipes** — the strict-matching rule applied to your pantry
- **Recipe Detail** — full method, with each ingredient ticked or crossed
  depending on whether your pantry covers it
- **Settings** — toggle expiry alerts, choose the warning window, set a default
  unit, load a sample pantry, or clear the pantry

---

## Database: SQLite (and why)

This app uses **SQLite via
`SQLiteOpenHelper`**, for three reasons:

**1. The data is private and single-user.** A pantry belongs to one person and is
only ever edited by that person. There is nothing to sync and nobody to share
with, so the account management and network dependency Firebase would introduce
would add complexity without adding value.

**2. The app must work offline.** People check what they can cook while standing
in their kitchen. Requiring a network connection to answer that question would
be a significant step backwards. SQLite is on-device, so the app works
regardless of connectivity.

**3. The data is relational.** Recipes and their ingredients form a genuine
one-to-many relationship that benefits from a foreign key and cascading deletes.
A relational engine models this naturally, whereas a document store would push
the same structure into nested objects.

### Schema

Three tables in `smart_pantry.db`, created by `DatabaseHelper.onCreate()`:

**`pantry_items`** — what the user has at home; the only table the user writes to
`_id` (PK), `name`, `quantity` (REAL), `unit`, `expiry_date` (epoch ms, 0 = none), `date_added`

**`recipes`** — the seeded collection
`_id` (PK), `name`, `description`, `instructions`, `prep_time_minutes`, `servings`, `category`

**`recipe_ingredients`** — one row per ingredient a recipe needs
`_id` (PK), `recipe_id` (FK → `recipes._id`, ON DELETE CASCADE), `name`, `quantity` (REAL), `unit`

Two design notes worth highlighting:

- Ingredients are a **separate table**, not a comma-separated field on the
  recipe. The strict rule compares the *required quantity* of each ingredient, and
  a text field such as `"flour, eggs, milk"` could not answer *"is there at least
  200 g of flour?"*
- There is **no foreign key between `pantry_items` and `recipe_ingredients`**.
  Matching happens in Java rather than as a SQL join, because names and units
  have to be normalised before they can be compared.

User settings are stored in **SharedPreferences** rather than the database —
they are a handful of single values with nothing to query, sort or relate.

---

## Project structure

```
app/src/main/java/com/sikhosana/smartpantrymanager/
├── data/           All database access
│   ├── DatabaseHelper.java      schema, seeding, lifecycle
│   ├── PantryDao.java           CRUD for pantry items
│   ├── RecipeDao.java           reads recipes + ingredients
│   └── RecipeSeedData.java      the 18 starter recipes
├── logic/          The matching rule (pure Java, no Android imports)
│   ├── IngredientMatcher.java   name normalisation + unit conversion
│   ├── RecipeMatcher.java       applies the strict rule
│   └── MatchResult.java         result, including what is missing
├── model/          Plain data objects
│   ├── PantryItem.java
│   ├── Recipe.java
│   └── RecipeIngredient.java
├── ui/             RecyclerView adapters
│   ├── PantryAdapter.java
│   └── RecipeAdapter.java       two view types: headers + recipes
├── util/
│   └── Prefs.java               SharedPreferences access
├── MainActivity.java            pantry list (launcher)
├── AddEditItemActivity.java     add / edit, one screen for both
├── SuggestedRecipesActivity.java
├── RecipeDetailActivity.java
└── SettingsActivity.java
```

No SQL appears inside an Activity: screens work only with objects returned by
the DAO classes.

---

## Setup and running

### Requirements

- Android Studio (Narwhal 2025.1.1 or newer)
- JDK 17 or newer (bundled with Android Studio)
- An emulator or physical device running **Android 7.0 (API 24)** or above

### Steps

1. Clone the repository:
   ```
   git clone https://github.com/Cebile28/SmartPantryManager.git
   ```
2. Open Android Studio and choose **Open**, then select the cloned folder.
3. Wait for Gradle to finish syncing (a few minutes on first open).
4. Create an emulator via **Device Manager** — a Pixel 8 with an API 35 image is
   what this was developed against — or connect a physical device with USB
   debugging enabled.
5. Press **Run**.

No API keys, accounts or network connection are needed. The database is created
and the 18 recipes are seeded automatically the first time the app launches.

### Trying it out quickly

The pantry starts empty. To see the recipe matching immediately:

1. Open the overflow menu (⋮) → **Settings**
2. Tap **Load sample pantry** — this adds eight common staples
3. Go back, then tap the search icon to open **Suggested Recipes**

To see the strict rule in action, delete **Sugar** from the pantry: *French
Toast* moves from the suggestions into "Almost There". Add it back and it
returns.

---

## Notes and known limitations

- Database queries run on the main thread. With 18 recipes this is
  imperceptible, but a larger collection would need a background thread or Room
  with `LiveData`.
- Adapters call `notifyDataSetChanged()` rather than using `DiffUtil`, so the
  whole list redraws on any change.
- Recipes are read-only — the user cannot add their own.
- Ingredient singularisation uses rules covering regular plurals; irregular
  forms such as "leaves" are not handled.
- The app deliberately contains no mapping, location or GPS
  features.

---

## Author

Cebile Skhosana
Student Number - 402419528
Mobile App Development 700 — Practical Assignment