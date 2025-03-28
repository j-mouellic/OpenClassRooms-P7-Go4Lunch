package com.julien.go4lunch.view.tabviews;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.julien.go4lunch.BuildConfig;
import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Lunch;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.view.DetailsActivity;
import com.julien.go4lunch.viewmodel.MyViewModel;
import com.julien.go4lunch.viewmodel.ViewModelFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestaurantListFragment extends Fragment implements RestaurantAdapter.OnItemClickListener {

    // UTILS
    private final String TAG = "RestaurantListFragment";
    private MyViewModel viewModel;
    private Geocoder geocoder;
    private TabActivity parentActivity;
    private boolean isLookingForPlaces = false;

    // VIEWS
    private AutocompleteSupportFragment acsf;
    private RestaurantAdapter adapter;

    public RestaurantListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        // Utils
        parentActivity = (TabActivity) getActivity();
        geocoder = new Geocoder(requireContext());

        // Get ViewModel
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MyViewModel.class);

        // Initialize Recycler Views
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new RestaurantAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Views
        acsf = parentActivity.acsf;

        // Observe GPS Status & Configure places search bar
        observeGpsStatus();
        configureAutocompleteSupportFragment();

        return view;
    }

    /**
     * Observes the GPS status and updates the UI accordingly.
     *
     * If the GPS status is valid, it updates the current user's position and fetches
     * the relevant restaurant and workmate data for the current location.
     */
    private void observeGpsStatus() {
        viewModel.getGPSStatus().observe(getViewLifecycleOwner(), gpsStatus -> {
            if (gpsStatus.isQuerying() || !gpsStatus.isHasGPSPermission()) {
                Log.i(TAG, "GPS Status - waiting for current position");
            } else {
                double latitude = gpsStatus.getLatitude();
                double longitude = gpsStatus.getLongitude();

                adapter.updateCurrentUserPos(latitude, longitude);

                fetchRestaurantAndWorkmatesAtRestaurant(latitude, longitude);
            }
        });
    }

    /**
     * Fetches restaurants and workmates at a specific location (latitude, longitude).
     * This method retrieves a list of restaurants within a 500-meter radius,
     * and then associates each restaurant with the number of workmates who have selected it for lunch today.
     * The result is updated in the adapter.
     */
    private void fetchRestaurantAndWorkmatesAtRestaurant(double latitude, double longitude) {
        String location = latitude + "," + longitude;

        viewModel.getAllRestaurants(location, 500, "restaurant").observe(getViewLifecycleOwner(), restaurants -> {

            viewModel.fetchTodayLunches().observe(getViewLifecycleOwner(), lunches -> {

                List<RestaurantAndWorkmates> workmatesAtRestaurant = new ArrayList<>();
                List<String> restaurantNameList = new ArrayList<>();

                for (Lunch lunch : lunches){
                    String restaurantName = lunch.getRestaurant().getName().trim().toLowerCase();
                    restaurantNameList.add(restaurantName);
                }

                Map<String, Long> rest = restaurantNameList.stream()
                        .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

                // Set workmates quantity for each restaurant
                for (Restaurant restaurant : restaurants) {
                    String restaurantName = restaurant.getName().toLowerCase().trim();
                    if (!rest.containsKey(restaurantName)) {
                        RestaurantAndWorkmates restaurantWithoutWorkmates = new RestaurantAndWorkmates(0L, restaurant);
                        workmatesAtRestaurant.add(restaurantWithoutWorkmates);
                    } else {
                        Long workmatesQuantity = rest.get(restaurantName);
                        Log.i(TAG, "Workmate quantity at " + restaurantName + " -> " + workmatesQuantity);
                        RestaurantAndWorkmates restaurantWithWorkmates = new RestaurantAndWorkmates(workmatesQuantity, restaurant);
                        workmatesAtRestaurant.add(restaurantWithWorkmates);
                    }
                }

                adapter.updateRestaurantList(workmatesAtRestaurant);
                isLookingForPlaces = false;
            });
        });
    }

    /**
     * Configures the AutocompleteSupportFragment to allow users to search for restaurants.
     * This method initializes the Google Places API, sets up the AutocompleteSupportFragment's appearance,
     * and defines the behavior when a place is selected or an error occurs.
     **/
    private void configureAutocompleteSupportFragment() {

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), BuildConfig.GOOGLE_MAPS_API_KEY);
        }

        PlacesClient placeClient = Places.createClient(getContext());

        acsf.setHint(getString(R.string.hint_input_restaurant));
        acsf.getView().setBackgroundColor(Color.WHITE);
        acsf.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
        acsf.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String placeAddress = place.getAddress();
                Log.i(TAG, "Found place address for " + place.getName() + " -> " + placeAddress);
                List<Address> addresses;

                try {
                    addresses = geocoder.getFromLocationName(placeAddress, 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                isLookingForPlaces = true;

                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();

                Log.i(TAG, "Place Position, Lat : " + latitude + " - Long : " + longitude);

                adapter.updateCurrentUserPos(latitude, longitude);
                fetchRestaurantAndWorkmatesAtRestaurant(latitude, longitude);
                parentActivity.resetTopBarViews();
            }

            @Override
            public void onError(Status status) {
                parentActivity.resetTopBarViews();
                isLookingForPlaces = false;
            }
        });
    }

    /**
     * Lifecycle : VERY IMPORTANT FOR GPS TRACKING !
     **/
    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            if (!isLookingForPlaces){
                Log.i(TAG, "On Resume called - GPS refresh");
                viewModel.refresh();
            }
        }
    }

    @Override
    public void onItemClick(Restaurant restaurant) {
        Log.i(TAG, "Restaurant in list has been clicked - restaurant : " + restaurant.getName());

        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("RESTAURANT", restaurant);
        startActivity(intent);
    }

    /**
     * A model that links the number of workmates present at a restaurant with the restaurant itself.
     */
    public static class RestaurantAndWorkmates {
        private final Long workmatesAtRestaurant;
        private final Restaurant restaurant;

        public RestaurantAndWorkmates(Long workmatesAtRestaurant, Restaurant restaurant) {
            this.workmatesAtRestaurant = workmatesAtRestaurant;
            this.restaurant = restaurant;
        }

        /**
         * Gets the number of workmates present at the restaurant.
         */
        public Long getWorkmatesAtRestaurant() {
            return workmatesAtRestaurant;
        }

        /**
         * Gets the restaurant instance.
         */

        public Restaurant getRestaurant() {
            return restaurant;
        }
    }
}