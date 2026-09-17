package com.sikhosana.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sikhosana.smartpantrymanager.model.Recipe;
import com.sikhosana.smartpantrymanager.model.RecipeIngredient;

import java.util.List;


 //Creates and manages the app's local SQLite database.

 //SQLiteOpenHelper handles the file lifecycle for us: onCreate() runs exactly
 //once, the first time the database is opened on a device, and onUpgrade() runs
 //only when DATABASE_VERSION is increased. Every later launch simply opens the
 //existing file, which is what makes the data genuinely persistent.

 // Three tables are used:
 // pantry_items       - what the user currently has at home
 //  recipes            - the seeded recipe collection
 // recipe_ingredients - one row per ingredient a recipe needs

 //Recipes and their ingredients are kept in two tables rather than storing a
 //comma-separated list, because the strict-matching rule has to compare the
 // required quantity of each individual ingredient against the pantry.

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "smart_pantry.db";
    public static final int DATABASE_VERSION = 1;

    //pantry
    public static final class PantryTable {
        public static final String TABLE_NAME   = "pantry_items";
        public static final String COL_ID       = "_id";
        public static final String COL_NAME     = "name";
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_UNIT     = "unit";
        public static final String COL_EXPIRY   = "expiry_date";   // epoch millis, 0 = none
        public static final String COL_ADDED    = "date_added";    // epoch millis

        private PantryTable() { }
    }

    // recipes
    public static final class RecipeTable {
        public static final String TABLE_NAME    = "recipes";
        public static final String COL_ID        = "_id";
        public static final String COL_NAME      = "name";
        public static final String COL_DESC      = "description";
        public static final String COL_STEPS     = "instructions";
        public static final String COL_PREP_TIME = "prep_time_minutes";
        public static final String COL_SERVINGS  = "servings";
        public static final String COL_CATEGORY  = "category";

        private RecipeTable() { }
    }

    // recipe ingredients
    public static final class IngredientTable {
        public static final String TABLE_NAME   = "recipe_ingredients";
        public static final String COL_ID       = "_id";
        public static final String COL_RECIPE   = "recipe_id";     // FK -> recipes(_id)
        public static final String COL_NAME     = "name";
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_UNIT     = "unit";

        private IngredientTable() { }
    }

    // statements

    private static final String SQL_CREATE_PANTRY =
            "CREATE TABLE " + PantryTable.TABLE_NAME + " (" +
                    PantryTable.COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PantryTable.COL_NAME     + " TEXT NOT NULL, " +
                    PantryTable.COL_QUANTITY + " REAL NOT NULL DEFAULT 0, " +
                    PantryTable.COL_UNIT     + " TEXT, " +
                    PantryTable.COL_EXPIRY   + " INTEGER NOT NULL DEFAULT 0, " +
                    PantryTable.COL_ADDED    + " INTEGER NOT NULL DEFAULT 0)";

    private static final String SQL_CREATE_RECIPES =
            "CREATE TABLE " + RecipeTable.TABLE_NAME + " (" +
                    RecipeTable.COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RecipeTable.COL_NAME      + " TEXT NOT NULL, " +
                    RecipeTable.COL_DESC      + " TEXT, " +
                    RecipeTable.COL_STEPS     + " TEXT, " +
                    RecipeTable.COL_PREP_TIME + " INTEGER NOT NULL DEFAULT 0, " +
                    RecipeTable.COL_SERVINGS  + " INTEGER NOT NULL DEFAULT 1, " +
                    RecipeTable.COL_CATEGORY  + " TEXT)";

    private static final String SQL_CREATE_INGREDIENTS =
            "CREATE TABLE " + IngredientTable.TABLE_NAME + " (" +
                    IngredientTable.COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    IngredientTable.COL_RECIPE   + " INTEGER NOT NULL, " +
                    IngredientTable.COL_NAME     + " TEXT NOT NULL, " +
                    IngredientTable.COL_QUANTITY + " REAL NOT NULL DEFAULT 0, " +
                    IngredientTable.COL_UNIT     + " TEXT, " +
                    "FOREIGN KEY (" + IngredientTable.COL_RECIPE + ") REFERENCES " +
                    RecipeTable.TABLE_NAME + "(" + RecipeTable.COL_ID + ") ON DELETE CASCADE)";

    //Speeds up looking up every ingredient belonging to one recipe.
    private static final String SQL_INDEX_INGREDIENTS =
            "CREATE INDEX idx_ingredient_recipe ON " + IngredientTable.TABLE_NAME +
                    "(" + IngredientTable.COL_RECIPE + ")";

    //singleton

    private static DatabaseHelper instance;

    /**
     * One shared helper for the whole app. The application context is used
     * rather than an Activity so that a rotated or destroyed screen cannot
     * leak, and so every screen talks to the same database connection.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // lifecycle

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite ignores foreign keys unless they are switched on per connection.
        // Without this, deleting a recipe would leave orphaned ingredient rows.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables for the first time");
        db.execSQL(SQL_CREATE_PANTRY);
        db.execSQL(SQL_CREATE_RECIPES);
        db.execSQL(SQL_CREATE_INGREDIENTS);
        db.execSQL(SQL_INDEX_INGREDIENTS);

        // onCreate runs once per device, so this is the correct place to load
        // the starter recipes. The pantry is left empty for the user to fill.
        seedRecipes(db);
    }


     //Inserts the starter recipe collection.

     //This takes the SQLiteDatabase that onCreate was handed, rather than
     //calling getWritableDatabase(). Asking the helper for a database while it
     //is still inside onCreate would recurse and throw.

     //Everything happens inside one transaction: with 18 recipes and roughly 80
     //ingredient rows, committing individually would be noticeably slow on
     //first launch, and a failure halfway through would leave recipes with
     //missing ingredients.

    private void seedRecipes(SQLiteDatabase db) {
        List<Recipe> recipes = RecipeSeedData.getSeedRecipes();
        int ingredientCount = 0;

        db.beginTransaction();
        try {
            for (Recipe recipe : recipes) {
                ContentValues rv = new ContentValues();
                rv.put(RecipeTable.COL_NAME, recipe.getName());
                rv.put(RecipeTable.COL_DESC, recipe.getDescription());
                rv.put(RecipeTable.COL_STEPS, recipe.getInstructions());
                rv.put(RecipeTable.COL_PREP_TIME, recipe.getPrepTimeMinutes());
                rv.put(RecipeTable.COL_SERVINGS, recipe.getServings());
                rv.put(RecipeTable.COL_CATEGORY, recipe.getCategory());

                long recipeId = db.insert(RecipeTable.TABLE_NAME, null, rv);
                if (recipeId == -1) {
                    Log.e(TAG, "Failed to insert recipe: " + recipe.getName());
                    continue;
                }

                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    ContentValues iv = new ContentValues();
                    iv.put(IngredientTable.COL_RECIPE, recipeId);
                    iv.put(IngredientTable.COL_NAME, ingredient.getName());
                    iv.put(IngredientTable.COL_QUANTITY, ingredient.getQuantity());
                    iv.put(IngredientTable.COL_UNIT, ingredient.getUnit());

                    db.insert(IngredientTable.TABLE_NAME, null, iv);
                    ingredientCount++;
                }
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Seeded " + recipes.size() + " recipes and "
                    + ingredientCount + " ingredients");
        } finally {
            // Without setTransactionSuccessful() above, this rolls everything back.
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // The recipe data is seeded, and the pantry is small and easily re-entered,
        // so a simple drop-and-recreate is acceptable for this project.
        db.execSQL("DROP TABLE IF EXISTS " + IngredientTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecipeTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PantryTable.TABLE_NAME);
        onCreate(db);
    }
}