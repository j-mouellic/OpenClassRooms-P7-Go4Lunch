package com.julien.go4lunch.view.tabviews;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.julien.go4lunch.BuildConfig;
import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.place.Location;
import com.julien.go4lunch.view.DetailsActivity;
import com.julien.go4lunch.viewmodel.MyViewModel;
import com.julien.go4lunch.viewmodel.ViewModelFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    // UTILS
    private final String TAG = "MapsFragment";
    private MyViewModel viewModel;
    private TabActivity parentActivity;
    private boolean isLookingForPlaces = false;

    // MAPS
    private Geocoder geocoder;
    private GoogleMap googleMap;
    private LatLng currentPosition;
    private final float mapZoom = 17;

    // VIEWS
    private AutocompleteSupportFragment acsf;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // Utils
        parentActivity = (TabActivity) getActivity();
        geocoder = new Geocoder(requireContext());

        // ViewModel
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MyViewModel.class);
        observeGpsStatusToGetRestaurant();

        // Views
        SupportMapFragment map = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        acsf = parentActivity.acsf;

        // View Configuration
        configureAutocompleteSupportFragment();

        return view;
    }

    private void observeGpsStatusToGetRestaurant() {
        viewModel.getGPSStatus().observe(getViewLifecycleOwner(), gpsStatus -> {
            if (gpsStatus.isQuerying() || !gpsStatus.isHasGPSPermission()) {
                Log.i(TAG, "GPS Status - waiting for current position");
            } else {
                double latitude = gpsStatus.getLatitude();
                double longitude = gpsStatus.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);

                Log.i(TAG, "Current Position, Lat : " + latitude + " - Long : " + longitude);

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoom));
            }
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

        acsf.setHint("Search restaurants");
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

                LatLng latLng = new LatLng(latitude, longitude);

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoom));
                parentActivity.resetTopBarViews();
            }

            @Override
            public void onError(Status status) {
                parentActivity.resetTopBarViews();
                isLookingForPlaces = false;
            }
        });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Updates the Google Map with markers representing nearby restaurants.
     * This method clears the map, fetches restaurants within a specified radius,
     * and displays their locations as markers. It also sets up a click listener
     * to handle marker interactions.
     **/
    private void updateMap(double latitude, double longitude) {
        googleMap.clear();
        HashMap<Marker, Restaurant> haspMap = new HashMap<>();

        currentPosition = new LatLng(latitude, longitude);
        String location = latitude + "," + longitude;

        viewModel.getAllRestaurants(location, 500, "restaurant").observe(getViewLifecycleOwner(), restaurants -> {
            Log.i(TAG, "Found " + restaurants.size() + " restaurants within 500 meters.");

            viewModel.fetchTodayLunchRestaurantNames().observe(getViewLifecycleOwner(), restaurantNameList -> {

                for (Restaurant restaurant : restaurants) {

                    Location restaurantLocation = restaurant.getLocation();
                    double lat = restaurantLocation.getLat();
                    double lng = restaurantLocation.getLng();

                    LatLng restaurantLatLng = new LatLng(lat, lng);
                    int lunchIconId = R.drawable.ic_no_lunch;

                    if (restaurantNameList != null) {
                        if (restaurantNameList.contains(restaurant.getName())) {
                            lunchIconId = R.drawable.ic_get_lunch;
                        }
                    }

                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(restaurantLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(lunchIconId))
                            .title(restaurant.getName())
                    );

                    haspMap.put(marker, restaurant);
                }
            });
        });

        googleMap.setOnMarkerClickListener(marker -> {
            Restaurant restaurant = haspMap.get(marker);

            if(restaurant != null){
                Log.i(TAG, "Marker is clicked - restaurant : " + restaurant.getName());

                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("RESTAURANT", restaurant);
                startActivity(intent);
            }else{
                Log.i(TAG, "Marker is clicked - restaurant : " + restaurant.getId());
            }
            return true;
        });

        isLookingForPlaces = false;
    }

    /**
     * Initializes the Google Map when it is ready to be displayed.
     * This method sets up interactions with the map, including a floating action button
     * to re-center the map on the current position and a listener for camera movements
     * to detect when the user has moved the map to a new location.
     **/
    @Override
    public void onMapReady(@NonNull GoogleMap gmap) {
        googleMap = gmap;

        Log.i(TAG, "OnMapReady is called");

        FloatingActionButton fab_center = getView().findViewById(R.id.fab_center);
        fab_center.setOnClickListener(view -> {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, mapZoom));
        });

        googleMap.setOnCameraIdleListener(() -> {
            Log.i(TAG, "Camera is moving");

            CameraPosition cameraPosition = googleMap.getCameraPosition();
            LatLng currentLatLng = cameraPosition.target;
            double latitude = currentLatLng.latitude;
            double longitude = currentLatLng.longitude;

            Log.i(TAG, "Update Map, New position, lat : " + latitude + " - long : " + longitude);

            Handler handler = new Handler();
            handler.postDelayed(() -> updateMap(latitude, longitude), 50);
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
                Log.i(TAG, "On Resume called - GPS refresh"); // à tester
                viewModel.refresh();
            }
        }
    }
}