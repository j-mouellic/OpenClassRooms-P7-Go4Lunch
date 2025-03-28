package com.julien.go4lunch.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
 * DetailsAdapter is a RecyclerView adapter that binds a list of workmates to a RecyclerView,
 * where each item displays the workmate's avatar (the first letter of their name) and their name,
 * along with a message indicating that they are joining a specific lunch.
 */
 public class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder> {
    private List<Workmate> workmates;

    public DetailsAdapter() {
        workmates = new ArrayList<>();
    }

    @NonNull
    @Override
    public DetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workmate_detail_item, parent, false);
        return new DetailsViewHolder(view);
    }

    void updateWorkmatesList(List<Workmate> workmates) {
        this.workmates = workmates;
        notifyDataSetChanged();
    }


    /**
     * Binds the data to the views within each ViewHolder.
     * This method sets the name of the workmate and assigns the avatar's first letter and color.
     */
    @Override
    public void onBindViewHolder(@NonNull DetailsViewHolder holder, int position) {
        Workmate workmate = workmates.get(position);

        String text = holder.itemView.getContext().getString(R.string.workmate_joining_message, workmate.getName());
        holder.wName.setText(text);

        String firstLetter = workmate.getName().substring(0, 1);
        holder.wAvatar.setText(firstLetter);
        String hexColor = Workmate.getRandomHexColorFromPalette();
        holder.wAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hexColor)));
    }

    @Override
    public int getItemCount() {
        return workmates.size();
    }

    static class DetailsViewHolder extends RecyclerView.ViewHolder {
        private final TextView wAvatar;
        private final TextView wName;

        public DetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            wAvatar = itemView.findViewById(R.id.wAvatar);
            wName = itemView.findViewById(R.id.wName);
        }
    }
}
