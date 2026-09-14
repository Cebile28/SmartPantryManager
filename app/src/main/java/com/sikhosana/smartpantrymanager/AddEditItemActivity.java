package com.sikhosana.smartpantrymanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sikhosana.smartpantrymanager.data.PantryDao;
import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.util.Prefs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Adds a new pantry item, or edits an existing one.
 *
 * One screen handles both jobs. Which mode it is in depends on whether the
 * calling Activity put an item id into the Intent:
 *
 *   Add  - startActivity(new Intent(this, AddEditItemActivity.class))
 *   Edit - the same, plus intent.putExtra(EXTRA_ITEM_ID, item.getId())
 *
 * Sharing the screen avoids duplicating the form and its validation twice.
 */
public class AddEditItemActivity extends AppCompatActivity {

    /** Key for the Intent extra carrying the id of the item being edited. */
    public static final String EXTRA_ITEM_ID = "com.sikhosana.smartpantrymanager.ITEM_ID";

    /** Saved across rotation so a chosen date is not lost. */
    private static final String STATE_EXPIRY = "state_expiry";

    private static final String[] UNITS =
            {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup"};

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

    private PantryDao pantryDao;

    private EditText editName;
    private EditText editQuantity;
    private Spinner spinnerUnit;
    private TextView textExpiryValue;

    private long itemId = PantryItem.NO_ID;      // NO_ID means "adding"
    private long expiryDate = PantryItem.NO_EXPIRY;
    private long dateAdded = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        pantryDao = new PantryDao(this);

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        textExpiryValue = findViewById(R.id.textExpiryValue);

        setUpToolbar();
        setUpUnitSpinner();
        setUpButtons();

        // Read the Intent to decide whether we are adding or editing.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, PantryItem.NO_ID);
        if (isEditing()) {
            setTitle("Edit Item");
            loadExistingItem();
        } else {
            setTitle("Add Item");

            // Start on whichever unit the user chose in Settings.
            int preferred = indexOfUnit(Prefs.getPreferredUnit(this));
            if (preferred >= 0) {
                spinnerUnit.setSelection(preferred);
            }
        }

        // Restore a date the user picked before the screen rotated.
        if (savedInstanceState != null) {
            expiryDate = savedInstanceState.getLong(STATE_EXPIRY, expiryDate);
        }
        updateExpiryLabel();
    }

    private boolean isEditing() {
        return itemId != PantryItem.NO_ID;
    }

    /**
     * onCreate runs again from scratch when the device rotates, so anything not
     * held in a View has to be saved here or it is lost. The EditText contents
     * are restored automatically because they have ids; expiryDate is a plain
     * field, so we save it ourselves.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_EXPIRY, expiryDate);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();       // back arrow behaves like Cancel
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpUnitSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, UNITS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);
    }

    private void setUpButtons() {
        Button buttonPickDate = findViewById(R.id.buttonPickDate);
        Button buttonClearDate = findViewById(R.id.buttonClearDate);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        buttonPickDate.setOnClickListener(v -> showDatePicker());
        buttonClearDate.setOnClickListener(v -> {
            expiryDate = PantryItem.NO_EXPIRY;
            updateExpiryLabel();
        });
        buttonSave.setOnClickListener(v -> saveItem());
        buttonCancel.setOnClickListener(v -> finish());
    }

    // ----------------------------------------------------------- edit mode

    private void loadExistingItem() {
        PantryItem item = pantryDao.getById(itemId);
        if (item == null) {
            Toast.makeText(this, "That item no longer exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName.setText(item.getName());
        editQuantity.setText(trimTrailingZero(item.getQuantity()));
        expiryDate = item.getExpiryDate();
        dateAdded = item.getDateAdded();       // keep the original date added

        int unitPosition = indexOfUnit(item.getUnit());
        if (unitPosition >= 0) {
            spinnerUnit.setSelection(unitPosition);
        }
    }

    private int indexOfUnit(String unit) {
        if (unit == null) {
            return -1;
        }
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return -1;
    }

    /** Shows 2 rather than 2.0, but keeps 1.5 intact. */
    private String trimTrailingZero(double value) {
        return (value == Math.floor(value))
                ? String.valueOf((long) value)
                : String.valueOf(value);
    }

    // --------------------------------------------------------- date picker

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (expiryDate != PantryItem.NO_EXPIRY) {
            calendar.setTimeInMillis(expiryDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar chosen = Calendar.getInstance();
                    chosen.set(year, month, dayOfMonth, 23, 59, 59);
                    chosen.set(Calendar.MILLISECOND, 0);
                    expiryDate = chosen.getTimeInMillis();
                    updateExpiryLabel();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void updateExpiryLabel() {
        if (expiryDate == PantryItem.NO_EXPIRY) {
            textExpiryValue.setText("Not set");
        } else {
            textExpiryValue.setText(dateFormat.format(expiryDate));
        }
    }

    // ---------------------------------------------------- validation + save

    /**
     * Checks the form and saves.
     *
     * Errors are set on the EditText itself with setError() rather than shown
     * as a Toast, so the message appears next to the field that caused it and
     * the keyboard focus moves there.
     */
    private void saveItem() {
        String name = editName.getText().toString().trim();
        String quantityText = editQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Enter an ingredient name");
            editName.requestFocus();
            return;
        }

        if (name.length() < 2) {
            editName.setError("Name is too short");
            editName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            editQuantity.setError("Enter a quantity");
            editQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            // Reachable if the user pastes text into the field.
            editQuantity.setError("Quantity must be a number");
            editQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            editQuantity.setError("Quantity must be greater than zero");
            editQuantity.requestFocus();
            return;
        }

        // Two rows called "Milk" would split the quantity in half and break
        // recipe matching, so block the duplicate before it is created.
        if (pantryDao.nameExists(name, itemId)) {
            editName.setError("You already have " + name + " in your pantry");
            editName.requestFocus();
            return;
        }

        PantryItem item = new PantryItem();
        item.setId(itemId);
        item.setName(name);
        item.setQuantity(quantity);
        item.setUnit((String) spinnerUnit.getSelectedItem());
        item.setExpiryDate(expiryDate);
        item.setDateAdded(dateAdded);

        boolean success = isEditing()
                ? pantryDao.update(item)
                : pantryDao.insert(item) != -1;

        if (success) {
            Toast.makeText(this,
                    isEditing() ? name + " updated" : name + " added to pantry",
                    Toast.LENGTH_SHORT).show();

            // RESULT_OK tells the pantry list that something changed.
            setResult(RESULT_OK, new Intent());
            finish();
        } else {
            Toast.makeText(this, "Could not save the item", Toast.LENGTH_SHORT).show();
        }
    }
}