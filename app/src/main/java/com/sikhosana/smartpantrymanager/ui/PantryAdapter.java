package com.sikhosana.smartpantrymanager.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sikhosana.smartpantrymanager.R;
import com.sikhosana.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds the list of PantryItem objects to the rows of the RecyclerView.
 *
 * A RecyclerView only keeps enough row Views alive to fill the screen and
 * recycles them as the user scrolls. That is why the work is split in two:
 * onCreateViewHolder() inflates a row layout (called rarely), and
 * onBindViewHolder() fills an existing row with new data (called constantly).
 * Because rows are reused, every field must be set on every bind - including
 * hiding the expiry line - or recycled data from a previous row shows through.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Lets the Activity react to taps without the adapter knowing about it. */
    public interface OnItemActionListener {
        void onItemClicked(PantryItem item);
        void onDeleteClicked(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemActionListener listener;

    public PantryAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    /** Replaces the whole list and redraws. */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ------------------------------------------------------------ ViewHolder

    /**
     * Holds the findViewById results for one row so they are looked up once
     * when the row is created rather than every time it scrolls into view.
     */
    class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textExpiry;
        private final ImageButton buttonDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textItemQuantity);
            textExpiry = itemView.findViewById(R.id.textItemExpiry);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        void bind(final PantryItem item) {
            textName.setText(item.getName());
            textQuantity.setText(item.getFormattedQuantity());
            bindExpiry(item);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClicked(item);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClicked(item);
                }
            });
        }

        /**
         * Shows a colour-coded expiry line, or hides it when the item has no
         * expiry date. The else-branch that hides the view is essential:
         * without it a recycled row would keep the previous item's warning.
         */
        private void bindExpiry(PantryItem item) {
            if (!item.hasExpiryDate()) {
                textExpiry.setVisibility(View.GONE);
                return;
            }

            long days = item.daysUntilExpiry();
            String message;
            int colour;

            if (days < 0) {
                message = "Expired";
                colour = Color.parseColor("#C62828");        // red
            } else if (days == 0) {
                message = "Expires today";
                colour = Color.parseColor("#C62828");
            } else if (days <= 3) {
                message = "Expires in " + days + (days == 1 ? " day" : " days");
                colour = Color.parseColor("#EF6C00");        // orange
            } else {
                message = "Expires in " + days + " days";
                colour = Color.parseColor("#2E7D32");        // green
            }

            textExpiry.setText(message);
            textExpiry.setTextColor(colour);
            textExpiry.setVisibility(View.VISIBLE);
        }
    }
}