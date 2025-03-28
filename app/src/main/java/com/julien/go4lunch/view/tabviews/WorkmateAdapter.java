package com.julien.go4lunch.view.tabviews;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Workmate;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for managing and displaying a list of workmates and their selected lunch information
 * in a RecyclerView.
 */
public class WorkmateAdapter extends RecyclerView.Adapter<WorkmateAdapter.WorkmateViewHolder> {

    /**
     * List of pairs containing workmate information and their associated lunch (if any).
     */
    private List<WorkmatesFragment.WorkmateLunchPair> workmateLunchPair;

    /**
     * Constructor for the WorkmateAdapter.
     * Initializes the internal list of workmate-lunch pairs as an empty list.
     */
    public WorkmateAdapter() {
        workmateLunchPair = new ArrayList<>();
    }

    /**
     * Updates the adapter's data set with a new list of workmate-lunch pairs and refreshes the RecyclerView.
     */
    void updateWorkmatesList(List<WorkmatesFragment.WorkmateLunchPair> workmateLunchPair) {
        this.workmateLunchPair = workmateLunchPair;
        notifyDataSetChanged();
    }


    /**
     * Creates and returns a ViewHolder for a workmate item.
     */
    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workmate_fragment_list_item, parent, false);
        return new WorkmateViewHolder(view);
    }


    /**
     * Binds the data for a workmate and their lunch to the specified ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {
        WorkmatesFragment.WorkmateLunchPair pair = workmateLunchPair.get(position);

        // Get workmate name
        String workmateName = pair.getWorkmate().getName();

        // Set text based on whether the workmate has chosen a lunch or not
        if (pair.getLunch() == null) {
            String text = holder.itemView.getContext().getString(R.string.workmate_undecided_message, workmateName);
            holder.wName.setText(text);

            // Set style to gray and italic
            holder.wName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gray, null));
            holder.wName.setTypeface(null, Typeface.ITALIC);
        } else {
            String restaurantName = pair.getLunch().getRestaurant().getName();
            String restaurantType = pair.getLunch().getRestaurant().getTypes().get(0);

            String text = holder.itemView.getContext().getString(
                    R.string.workmate_eating_message,
                    workmateName,
                    restaurantType,
                    restaurantName
            );
            holder.wName.setText(text);
        }

        String firstLetter = workmateName.substring(0, 1);
        holder.wAvatar.setText(firstLetter);
        String hexColor = Workmate.getRandomHexColorFromPalette();
        holder.wAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hexColor)));
    }


    /**
     * Returns the total number of items managed by the adapter.
     */
    @Override
    public int getItemCount() {
        return workmateLunchPair.size();
    }

    /**
     * ViewHolder class for displaying workmate and lunch information in the RecyclerView.
     */
    static class WorkmateViewHolder extends RecyclerView.ViewHolder {
        // Workmate Avatar
        private final TextView wAvatar;

        // Workmate Name
        private final TextView wName;

        /**
         * Constructor for WorkmateViewHolder.
         */
        public WorkmateViewHolder(@NonNull View itemView) {
            super(itemView);

            wAvatar = itemView.findViewById(R.id.wAvatar);
            wName = itemView.findViewById(R.id.wName);
        }
    }
}

