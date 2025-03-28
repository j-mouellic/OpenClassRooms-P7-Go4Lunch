package com.julien.go4lunch.view.tabviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.julien.go4lunch.BuildConfig;
import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.place.Location;
import com.julien.go4lunch.model.bo.place.Photo;

import java.util.ArrayList;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewholder> {

    // UTILS
    private List<RestaurantListFragment.RestaurantAndWorkmates> restaurantList;
    private final String NON_ASSIGNED = "N/A";
    private Context context;

    // CALLBACK
    private OnItemClickListener listener;

    // POSITION
    private LatLng currentUserPos;

    public RestaurantAdapter(OnItemClickListener listener) {
        this.listener = listener;
        restaurantList = new ArrayList<>();
    }

    public void updateRestaurantList(List<RestaurantListFragment.RestaurantAndWorkmates> restaurantList){
        this.restaurantList = restaurantList;
        notifyDataSetChanged();
    }

    public void updateCurrentUserPos(double latitude, double longitude){
        currentUserPos = new LatLng(latitude, longitude);
    }

    @NonNull
    @Override
    public RestaurantViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.restaurant_detail_item, parent, false);
        return new RestaurantViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewholder holder, int position) {
        RestaurantListFragment.RestaurantAndWorkmates restaurantAndWorkmates = restaurantList.get(position);

        Long workmateQuantity = restaurantAndWorkmates.getWorkmatesAtRestaurant();
        Restaurant restaurant = restaurantAndWorkmates.getRestaurant();

        String type;
        String address;

        if(restaurant.getTypes() != null){
            type = restaurant.getTypes().get(0);
        }else{
            type = NON_ASSIGNED;
        }

        if(restaurant.getAddress() != null){
            address = restaurant.getAddress();
        }else{
            address = NON_ASSIGNED;
        }

        String rTypeAddressText = type + " - " + address;

        holder.rName.setText(restaurant.getName());
        holder.rTypeAddress.setText(rTypeAddressText);
        holder.rWorkmateQtt.setText("(" + workmateQuantity + ")");

        if (restaurant.getLocation() != null){
            if (currentUserPos != null){
                Location restaurantLoc = restaurant.getLocation();
                LatLng restaurantPos = new LatLng(restaurantLoc.getLat(), restaurantLoc.getLng());
                double distance = SphericalUtil.computeDistanceBetween(currentUserPos, restaurantPos);
                int roundedDistance = (int) Math.round(distance);
                holder.rDistance.setText(roundedDistance + "m");
            }else{
                holder.rDistance.setText(NON_ASSIGNED);
            }
        }else{
            holder.rDistance.setText(NON_ASSIGNED);
        }

        if(restaurant.getOpened() != null){
            if (restaurant.getOpened()){
                holder.rOpened.setText(context.getString(R.string.restaurant_is_opened));
            }else{
                holder.rOpened.setText(context.getString(R.string.restaurant_is_closed));
            }
        }else{
            holder.rOpened.setText(NON_ASSIGNED);
        }

        if (restaurant.getRating() != null){
            holder.rRating.setRating(restaurant.getRating().floatValue());
        }else{
            holder.rRating.setVisibility(View.INVISIBLE);
        }

        setImageView(restaurant, holder.rImage);

        holder.itemView.setOnClickListener(view -> {
            listener.onItemClick(restaurant);
        });
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Restaurant restaurant);
    }


    static class RestaurantViewholder extends RecyclerView.ViewHolder{
        private final TextView rName, rTypeAddress, rDistance, rWorkmateQtt, rOpened;
        private final RatingBar rRating;
        private final ImageView rImage;
        public RestaurantViewholder(@NonNull View itemView) {
            super(itemView);

            // TextViews
            rName = itemView.findViewById(R.id.rName);
            rTypeAddress = itemView.findViewById(R.id.rTypeAddress);
            rDistance = itemView.findViewById(R.id.rDistance);
            rWorkmateQtt = itemView.findViewById(R.id.rWorkmateQtt);
            rOpened = itemView.findViewById(R.id.rOpened);

            // Rating
            rRating = itemView.findViewById(R.id.rRating);

            // ImageViews
            rImage = itemView.findViewById(R.id.rImage);
        }
    }

    /**
     * Sets the restaurant's image in the ImageView using the first photo reference.
     * The photo is loaded from the Google Places API using Glide.
     * If the image cannot be loaded, a default "image not found" placeholder is displayed.
     */
    private void setImageView(Restaurant restaurant, ImageView imageView) {
        String photoUrl = null;

        if (restaurant.getPhotos() != null) {
            Photo first_photo = restaurant.getPhotos().get(0);
            String photoReference = first_photo.getPhotoReference();

            String apiKey = BuildConfig.GOOGLE_MAPS_API_KEY;
            photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + apiKey;
        }

        Glide.with(context)
                .load(photoUrl != null ? photoUrl : R.drawable.restaurant_sample)
                .into(imageView);
    }
}


