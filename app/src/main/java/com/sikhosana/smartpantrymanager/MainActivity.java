package com.sikhosana.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sikhosana.smartpantrymanager.data.PantryDao;
import com.sikhosana.smartpantrymanager.model.PantryItem;
import com.sikhosana.smartpantrymanager.ui.PantryAdapter;

import java.util.List;

/**
 * The pantry list - the app's home screen.
 *
 * Shows everything the user currently has at home, and offers delete straight
 * from the list. Adding and editing happen on a separate screen reached with
 * an Intent that optionally carries the id of the item being edited.
 */
public class MainActivity extends AppCompatActivity
        implements PantryAdapter.OnItemActionListener {

    private PantryDao pantryDao;
    private PantryAdapter adapter;

    private RecyclerView recyclerView;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("My Pantry");

        pantryDao = new PantryDao(this);

        recyclerView = findViewById(R.id.recyclerPantry);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        adapter = new PantryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddItem);
        fab.setOnClickListener(v -> openAddScreen());
    }

    /**
     * Reloading here rather than in onCreate matters: onCreate runs once, but
     * onResume runs every time this screen comes back to the front - including
     * after returning from the Add/Edit screen - so the list always shows the
     * current contents of the database.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        List<PantryItem> items = pantryDao.getAll();
        adapter.setItems(items);
        showEmptyState(items.isEmpty());
    }

    /** Exactly one of the list and the empty message is visible at a time. */
    private void showEmptyState(boolean isEmpty) {
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // ------------------------------------------------------- toolbar menu

    /** Inflates the toolbar menu, which is this app's navigation element. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_suggestions) {
            startActivity(new Intent(this, SuggestedRecipesActivity.class));
            return true;
        }

        if (id == R.id.action_settings) {
            Toast.makeText(this, "Settings screen comes next", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ------------------------------------------------------------- navigation

    /** Opens the form with no extras, so it starts blank in "add" mode. */
    private void openAddScreen() {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        startActivity(intent);
    }

    /**
     * Opens the same form carrying the item's id. AddEditItemActivity reads
     * that extra, loads the record and pre-fills the fields, which is how one
     * screen serves both adding and editing.
     */
    private void openEditScreen(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    // ------------------------------------------- PantryAdapter callbacks

    @Override
    public void onItemClicked(PantryItem item) {
        openEditScreen(item);
    }

    @Override
    public void onDeleteClicked(final PantryItem item) {
        // Deleting is destructive and the button is small, so confirm first.
        new AlertDialog.Builder(this)
                .setTitle("Remove item")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Remove", (dialog, which) -> deleteItem(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(PantryItem item) {
        if (pantryDao.delete(item.getId())) {
            Toast.makeText(this, item.getName() + " removed", Toast.LENGTH_SHORT).show();
            loadPantryItems();
        } else {
            Toast.makeText(this, "Could not remove item", Toast.LENGTH_SHORT).show();
        }
    }
}