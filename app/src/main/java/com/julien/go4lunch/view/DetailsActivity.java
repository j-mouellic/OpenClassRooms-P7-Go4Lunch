package com.julien.go4lunch.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.julien.go4lunch.BuildConfig;
import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.place.Photo;
import com.julien.go4lunch.viewmodel.MyViewModel;
import com.julien.go4lunch.viewmodel.ViewModelFactory;

public class DetailsActivity extends AppCompatActivity {

    // VIEWS
    private ImageView rImage, rIsChoosenByUser;
    private TextView rName, rType, rAddress, emptyMessage;
    private LinearLayout callBtn, likeBtn, websiteBtn;
    private RatingBar ratingBar;

    // RECYCLER & ADAPTER
    private RecyclerView recyclerView;
    private DetailsAdapter adapter;

    // UTILS
    private DetailsActivity da;
    private final String TAG = "DetailsActivity";
    private MyViewModel viewModel;
    private Restaurant restaurant;
    private boolean IS_LIKED, IS_CHOSEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // CONTEXT
        da = this;

        // VIEWMODEL
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MyViewModel.class);

        // VIEWS RESTAURANT DETAILS
        rImage = findViewById(R.id.rImage);
        rName = findViewById(R.id.rName);
        rType = findViewById(R.id.rType);
        rAddress = findViewById(R.id.rAddress);
        emptyMessage = findViewById(R.id.emptyMessage);
        ratingBar = findViewById(R.id.ratingBar);

        // VIEWS ACTIONS
        rIsChoosenByUser = findViewById(R.id.isChoosenByUser);
        callBtn = findViewById(R.id.callBtn);
        likeBtn = findViewById(R.id.likeBtn);
        websiteBtn = findViewById(R.id.websiteBtn);

        // RESTAURANT INTENT & CONFIGURATION METHODS
        Intent rIntent = getIntent();

        if (rIntent.getExtras() != null) {
            restaurant = (Restaurant) rIntent.getSerializableExtra("RESTAURANT");

            configureRestaurantUiWithDetail();
            configureIsChoosenBtn();
            configureRecyclerView();
            observeTodayWorkmatesAtRestaurant();
            configureLikedBtn();
        }
    }

    /**
     * Configures the like button to toggle the restaurant's like status.
     * Observes the current user's like status for the restaurant.
     */
    private void configureLikedBtn() {
        viewModel.checkIfCurrentWorkmateLikeThisRestaurant(restaurant).observe(da, b -> {
            IS_LIKED = b;
            likeBtn.setActivated(IS_LIKED);
        });

        likeBtn.setOnClickListener(view -> {
            viewModel.checkIfCurrentWorkmateLikeThisRestaurant(restaurant).observe(da, b -> {
                IS_LIKED = !b;
                viewModel.isRestaurantLikedByUser(restaurant, IS_LIKED);
                likeBtn.setActivated(IS_LIKED);
            });
        });
    }

    /**
     * Configures the button to indicate whether the current workmate
     * has chosen this restaurant.
     * Observes and updates the button's state accordingly.
     */
    private void configureIsChoosenBtn() {
        viewModel.hasWorkmateChosenThisRestaurant(restaurant).observe(da, b -> {
            if (b != null) {
                IS_CHOSEN = b;
                rIsChoosenByUser.setActivated(IS_CHOSEN);
            }
        });

        rIsChoosenByUser.setOnClickListener(view -> {
            IS_CHOSEN = !IS_CHOSEN;

            rIsChoosenByUser.setActivated(IS_CHOSEN);

            if (IS_CHOSEN){
                Log.i(TAG, "User has chosen a restaurant " + restaurant.getName());
                viewModel.createLunch(restaurant, viewModel.getCurrentWorkmate());

                observeTodayWorkmatesAtRestaurant();
            }else{
                Log.i(TAG, "User removed his lunch at " + restaurant.getName());

                viewModel.deleteLunch(restaurant).observe(da, isDeleted -> {
                    if (isDeleted != null){
                        if (isDeleted){
                            Log.i(TAG, "Get callback from delete action - Refresh recycler view");
                            observeTodayWorkmatesAtRestaurant();
                        }
                    }
                });
            }
        });
    }

    /**
     * Configures the RecyclerView to display a list of workmates who are
     * currently at the restaurant.
     * Observes the data for the workmates and updates the UI accordingly.
     * If no workmates are found, displays a message indicating no workmates.
     */
    private void configureRecyclerView() {
        recyclerView = findViewById(R.id.workmateRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DetailsAdapter();
        recyclerView.setAdapter(adapter);
    }

    /**
     * Refresh Recycler View with wormakte list.
     */
    private void observeTodayWorkmatesAtRestaurant() {
        viewModel.fetchTodayWorkmatesAtRestaurant(restaurant).observe(da, workmates -> {
            if (workmates == null) {
                recyclerView.setVisibility(View.INVISIBLE);
                emptyMessage.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyMessage.setVisibility(View.INVISIBLE);

                adapter.updateWorkmatesList(workmates);
            }
        });
    }

    /**
     * Configures VIEWS with restaurant detail and set OnClickListener for
     * CALL action and BROWSER action
     */
    private void configureRestaurantUiWithDetail() {
        viewModel.getRestaurantDetail(restaurant.getId()).observe(da, r -> {

            restaurant = r;

            if (r.getName() != null) {
                rName.setText(r.getName());
            } else {
                rName.setText("N/A");
            }

            if (r.getAddress() != null) {
                rAddress.setText(r.getAddress());
            } else {
                rAddress.setText("N/A");
            }

            if (r.getTypes() != null) {
                rType.setText(r.getTypes().get(0));
            } else {
                rType.setText("N/A");
            }

            if (r.getRating() != null) {
                double rating = r.getRating();
                ratingBar.setRating((float) rating);
            } else {
                ratingBar.setRating(0f);
            }

            setImageView(r);

            if (r.getFormattedPhoneNumber() != null) {
                callBtn.setActivated(true);
                callBtn.setOnClickListener(call -> {
                    String phoneNumber = r.getFormattedPhoneNumber();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    da.startActivity(callIntent);
                });
            } else {
                callBtn.setActivated(false);
            }

            if (r.getWebsite() != null) {
                websiteBtn.setActivated(true);
                websiteBtn.setOnClickListener(web -> {
                    String URL = r.getWebsite();
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(Uri.parse(URL));
                    da.startActivity(webIntent);
                });
            } else {
                websiteBtn.setActivated(false);
            }
        });
    }

    /**
     * Sets the restaurant's image in the ImageView using the first photo reference.
     * The photo is loaded from the Google Places API using Glide.
     * If the image cannot be loaded, a default "image not found" placeholder is displayed.
     */
    private void setImageView(Restaurant restaurant) {
        Photo first_photo = restaurant.getPhotos().get(0);
        String photoReference = first_photo.getPhotoReference();

        String apiKey = BuildConfig.GOOGLE_MAPS_API_KEY;
        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + apiKey;

        Glide.with(this)
                .load(photoUrl)
                .error(R.drawable.imagenotfound)
                .into(rImage);
    }
}