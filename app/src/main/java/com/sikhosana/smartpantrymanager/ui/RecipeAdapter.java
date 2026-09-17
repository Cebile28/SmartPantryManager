package com.sikhosana.smartpantrymanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sikhosana.smartpantrymanager.R;
import com.sikhosana.smartpantrymanager.logic.MatchResult;
import com.sikhosana.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;


 // Shows the suggested recipes, split into labelled sections.
 // The list holds two different kinds of row - section headers and recipe
 // cards - so the adapter overrides getItemViewType(). The RecyclerView asks
 // that method what sort of row sits at each position, then hands the answer
 // back through onCreateViewHolder so the right layout gets inflated. Headers
 // are stored as plain Strings in the list and recipes as MatchResult objects,
 // which is why the binding code checks which one it is dealing with.

public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_RECIPE = 1;

    public interface OnRecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }

    // Mixed list: String entries are headers, MatchResult entries are recipes.
    private final List<Object> rows = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }


     // Rebuilds the list from the two result sets.
     // A header is only added when its section actually has something in it,
     // so the user never sees an "Almost There" title with nothing under it.

    public void setResults(List<MatchResult> makeable, List<MatchResult> almostThere) {
        rows.clear();

        if (makeable != null && !makeable.isEmpty()) {
            rows.add("You can cook these now");
            rows.addAll(makeable);
        }

        if (almostThere != null && !almostThere.isEmpty()) {
            rows.add("Almost there - one ingredient short");
            rows.addAll(almostThere);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (rows.get(position) instanceof String) ? TYPE_HEADER : TYPE_RECIPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        }

        View view = inflater.inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) row);
        } else {
            ((RecipeViewHolder) holder).bind((MatchResult) row);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    //  viewholders

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textHeader = itemView.findViewById(R.id.textSectionHeader);
        }

        void bind(String title) {
            textHeader.setText(title);
        }
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textSummary;
        private final TextView textShortfall;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textSummary = itemView.findViewById(R.id.textRecipeSummary);
            textShortfall = itemView.findViewById(R.id.textShortfall);
        }

        void bind(final MatchResult result) {
            final Recipe recipe = result.getRecipe();

            textName.setText(recipe.getName());
            textSummary.setText(recipe.getIngredientCount() + " ingredients  \u2022  "
                    + recipe.getPrepTimeMinutes() + " min  \u2022  " + recipe.getCategory());

            // Rows are recycled, so the shortfall line must be explicitly
            // hidden for makeable recipes or a previous row's text shows through.
            if (result.canMake()) {
                textShortfall.setVisibility(View.GONE);
            } else {
                textShortfall.setText(result.getShortfallText());
                textShortfall.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClicked(recipe);
                }
            });
        }
    }
}