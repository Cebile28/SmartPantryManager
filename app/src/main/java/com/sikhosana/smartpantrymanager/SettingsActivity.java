package com.sikhosana.smartpantrymanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.sikhosana.smartpantrymanager.data.PantryDao;
import com.sikhosana.smartpantrymanager.data.RecipeDao;
import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.util.Prefs;

/**
 * User preferences and a couple of pantry housekeeping tools.
 *
 * The settings here are not decorative: switching expiry alerts off removes
 * the warning banner from the pantry list, the warning window changes how far
 * ahead that banner looks, and the preferred unit decides which unit is
 * selected when the Add Item form opens.
 */
public class SettingsActivity extends AppCompatActivity {

    /** Matches the units offered on the Add/Edit form. */
    private static final String[] UNITS =
            {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};

    private static final int[] DAY_OPTIONS = {3, 7, 14, 30};

    private PantryDao pantryDao;
    private RecipeDao recipeDao;
    private TextView textStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Settings");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        pantryDao = new PantryDao(this);
        recipeDao = new RecipeDao(this);
        textStats = findViewById(R.id.textStats);

        setUpExpirySwitch();
        setUpWarningDaysSpinner();
        setUpUnitSpinner();
        setUpButtons();
        updateStats();
    }

    // ------------------------------------------------------------ settings

    private void setUpExpirySwitch() {
        SwitchCompat switchAlerts = findViewById(R.id.switchExpiryAlerts);
        switchAlerts.setChecked(Prefs.areExpiryAlertsEnabled(this));

        switchAlerts.setOnCheckedChangeListener((button, isChecked) -> {
            Prefs.setExpiryAlertsEnabled(this, isChecked);
            Toast.makeText(this,
                    isChecked ? "Expiry alerts on" : "Expiry alerts off",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setUpWarningDaysSpinner() {
        String[] labels = new String[DAY_OPTIONS.length];
        for (int i = 0; i < DAY_OPTIONS.length; i++) {
            labels[i] = DAY_OPTIONS[i] + " days";
        }

        Spinner spinner = findViewById(R.id.spinnerWarningDays);
        spinner.setAdapter(simpleAdapter(labels));

        int current = Prefs.getWarningDays(this);
        for (int i = 0; i < DAY_OPTIONS.length; i++) {
            if (DAY_OPTIONS[i] == current) {
                spinner.setSelection(i);
                break;
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.setWarningDays(SettingsActivity.this, DAY_OPTIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setUpUnitSpinner() {
        Spinner spinner = findViewById(R.id.spinnerPreferredUnit);
        spinner.setAdapter(simpleAdapter(UNITS));

        String current = Prefs.getPreferredUnit(this);
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(current)) {
                spinner.setSelection(i);
                break;
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Prefs.setPreferredUnit(SettingsActivity.this, UNITS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private ArrayAdapter<String> simpleAdapter(String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    // ------------------------------------------------------- pantry tools

    private void setUpButtons() {
        Button buttonLoadSample = findViewById(R.id.buttonLoadSample);
        Button buttonClear = findViewById(R.id.buttonClearPantry);

        buttonLoadSample.setOnClickListener(v -> loadSamplePantry());
        buttonClear.setOnClickListener(v -> confirmClearPantry());
    }

    /**
     * Adds a handful of common staples so the recipe suggestions can be tried
     * out immediately without typing every ingredient by hand.
     *
     * Anything already in the pantry is skipped rather than duplicated, which
     * matters because two rows of the same ingredient would split the quantity
     * and confuse the matching.
     */
    private void loadSamplePantry() {
        String[][] samples = {
                {"Eggs", "6", "pcs"},
                {"Milk", "500", "ml"},
                {"Butter", "250", "g"},
                {"Salt", "50", "tsp"},
                {"Bread", "8", "pcs"},
                {"Cheese", "200", "g"},
                {"Flour", "1", "kg"},
                {"Sugar", "500", "g"}
        };

        int added = 0;
        for (String[] sample : samples) {
            if (pantryDao.nameExists(sample[0], PantryItem.NO_ID)) {
                continue;
            }

            PantryItem item = new PantryItem(
                    sample[0], Double.parseDouble(sample[1]), sample[2]);

            if (pantryDao.insert(item) != -1) {
                added++;
            }
        }

        Toast.makeText(this,
                added == 0 ? "You already have all the sample items"
                        : added + " item(s) added to your pantry",
                Toast.LENGTH_SHORT).show();
        updateStats();
    }

    private void confirmClearPantry() {
        if (pantryDao.count() == 0) {
            Toast.makeText(this, "Your pantry is already empty", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear pantry")
                .setMessage("Remove every item from your pantry? This cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    int removed = pantryDao.deleteAll();
                    Toast.makeText(this, removed + " item(s) removed", Toast.LENGTH_SHORT).show();
                    updateStats();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateStats() {
        textStats.setText(pantryDao.count() + " items in your pantry, "
                + recipeDao.count() + " recipes available");
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