package com.sikhosana.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sikhosana.smartpantrymanager.data.DatabaseHelper.PantryTable;
import com.sikhosana.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * All database access for pantry items lives here.
 *
 * Keeping the SQL in one place means the Activities deal only with PantryItem
 * objects and never touch a Cursor or a query string. If the storage layer ever
 * changed, only this class would need rewriting.
 *
 * Every method uses parameterised queries (the ? placeholders) rather than
 * building SQL by joining strings together, which keeps user input from being
 * interpreted as SQL.
 */
public class PantryDao {

    private static final String TAG = "PantryDao";

    private final DatabaseHelper helper;

    public PantryDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    // ------------------------------------------------------------ CREATE

    /**
     * Saves a new pantry item.
     *
     * @return the generated row id, or -1 if the insert failed
     */
    public long insert(PantryItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = toContentValues(item);

        long id = db.insert(PantryTable.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(TAG, "Failed to insert pantry item: " + item.getName());
        } else {
            item.setId(id);
        }
        return id;
    }

    // -------------------------------------------------------------- READ

    /** Every pantry item, sorted alphabetically and ignoring case. */
    public List<PantryItem> getAll() {
        String sql = "SELECT * FROM " + PantryTable.TABLE_NAME +
                " ORDER BY " + PantryTable.COL_NAME + " COLLATE NOCASE ASC";
        return runQuery(sql, null);
    }

    /** One item by id, or null when nothing matches. */
    public PantryItem getById(long id) {
        String sql = "SELECT * FROM " + PantryTable.TABLE_NAME +
                " WHERE " + PantryTable.COL_ID + " = ?";
        List<PantryItem> results = runQuery(sql, new String[]{String.valueOf(id)});
        return results.isEmpty() ? null : results.get(0);
    }

    /** Items whose name contains the keyword, for the search box. */
    public List<PantryItem> searchByName(String keyword) {
        String sql = "SELECT * FROM " + PantryTable.TABLE_NAME +
                " WHERE " + PantryTable.COL_NAME + " LIKE ?" +
                " ORDER BY " + PantryTable.COL_NAME + " COLLATE NOCASE ASC";
        return runQuery(sql, new String[]{"%" + keyword + "%"});
    }

    /**
     * Items expiring within the given number of days, soonest first.
     * Items with no expiry date are excluded, and already-expired items are
     * included so the user is told about them.
     */
    public List<PantryItem> getExpiringWithin(int days) {
        long cutoff = System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000);

        String sql = "SELECT * FROM " + PantryTable.TABLE_NAME +
                " WHERE " + PantryTable.COL_EXPIRY + " > 0" +
                " AND " + PantryTable.COL_EXPIRY + " <= ?" +
                " ORDER BY " + PantryTable.COL_EXPIRY + " ASC";
        return runQuery(sql, new String[]{String.valueOf(cutoff)});
    }

    /** How many items are in the pantry, used for the empty-state message. */
    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + PantryTable.TABLE_NAME;

        try (Cursor cursor = db.rawQuery(sql, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /**
     * True when an item of this name already exists, ignoring case.
     * Used by the add form so the user is warned instead of quietly creating
     * two separate "Milk" rows that would confuse the recipe matching.
     *
     * @param excludeId pass the id being edited, or -1 when adding
     */
    public boolean nameExists(String name, long excludeId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + PantryTable.TABLE_NAME +
                " WHERE " + PantryTable.COL_NAME + " = ? COLLATE NOCASE" +
                " AND " + PantryTable.COL_ID + " != ?";

        try (Cursor cursor = db.rawQuery(sql,
                new String[]{name.trim(), String.valueOf(excludeId)})) {
            return cursor.moveToFirst() && cursor.getInt(0) > 0;
        }
    }

    // ------------------------------------------------------------ UPDATE

    /** Saves changes to an existing item. Returns true when a row was changed. */
    public boolean update(PantryItem item) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = toContentValues(item);

        int rows = db.update(PantryTable.TABLE_NAME, values,
                PantryTable.COL_ID + " = ?",
                new String[]{String.valueOf(item.getId())});

        return rows > 0;
    }

    // ------------------------------------------------------------ DELETE

    /** Removes one item. Returns true when a row was actually deleted. */
    public boolean delete(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();

        int rows = db.delete(PantryTable.TABLE_NAME,
                PantryTable.COL_ID + " = ?",
                new String[]{String.valueOf(id)});

        return rows > 0;
    }

    /** Empties the pantry. Offered from the settings screen. */
    public int deleteAll() {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(PantryTable.TABLE_NAME, null, null);
    }

    // ------------------------------------------------------------ helpers

    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(PantryTable.COL_NAME, item.getName().trim());
        values.put(PantryTable.COL_QUANTITY, item.getQuantity());
        values.put(PantryTable.COL_UNIT, item.getUnit());
        values.put(PantryTable.COL_EXPIRY, item.getExpiryDate());
        values.put(PantryTable.COL_ADDED, item.getDateAdded());
        return values;
    }

    /** Runs a query and turns every row into a PantryItem. */
    private List<PantryItem> runQuery(String sql, String[] args) {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        // try-with-resources closes the Cursor even if something throws.
        // A leaked Cursor is one of the most common sources of Android memory
        // warnings, so it is worth being strict about.
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        } catch (Exception e) {
            Log.e(TAG, "Query failed: " + sql, e);
        }
        return items;
    }

    /** Reads the current row of a Cursor into a PantryItem. */
    private PantryItem fromCursor(Cursor cursor) {
        PantryItem item = new PantryItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PantryTable.COL_ID)));
        item.setName(cursor.getString(cursor.getColumnIndexOrThrow(PantryTable.COL_NAME)));
        item.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(PantryTable.COL_QUANTITY)));
        item.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(PantryTable.COL_UNIT)));
        item.setExpiryDate(cursor.getLong(cursor.getColumnIndexOrThrow(PantryTable.COL_EXPIRY)));
        item.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(PantryTable.COL_ADDED)));
        return item;
    }
}