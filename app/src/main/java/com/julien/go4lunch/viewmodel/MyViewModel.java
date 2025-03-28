package com.julien.go4lunch.viewmodel;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.julien.go4lunch.MainApplication;
import com.julien.go4lunch.model.bo.GPSStatus;
import com.julien.go4lunch.model.bo.Lunch;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.Workmate;
import com.julien.go4lunch.model.repository.LocationRepository;
import com.julien.go4lunch.model.repository.LunchRepository;
import com.julien.go4lunch.model.repository.RestaurantRepository;
import com.julien.go4lunch.model.repository.WorkmateRepository;

import java.util.List;

/**
 * ViewModel that includes GPS LiveData
 */
public class MyViewModel extends ViewModel {

    /**
     * LocationRepository
     */
    @NonNull
    private final LocationRepository locationRepository;

    /**
     * LunchRepository
     */
     private final LunchRepository lunchRepository;

    /**
     * RestaurantRepository
     */
    private final RestaurantRepository restaurantRepository;

    /**
     * WorkmateRepository
     */
    private final WorkmateRepository workmateRepository;

    /**
     * LiveData that indicates if the app has GPS permission
     * MutableLiveData is a subclass of LiveData thats exposes the setValue and postValue methods
     * (the second one is thread safe), so you can dispatch a value to any active observers.
     */
    private final MutableLiveData<Boolean> hasGpsPermissionLiveData = new MutableLiveData<>();

    /**
     * Mediator LiveData that combines GPS location and permission
     * Mediator LiveData can observe multiples LiveData objects (sources) and react to their
     * onChange events, this will give you control on when you want to propagate the event,
     * or do something in particular.
     */
    private final MediatorLiveData<GPSStatus> gpsMessageLiveData = new MediatorLiveData<>();

    /**
     * Constructor
     * @param locationRepository LocationRepository instance to get GPS location
     */
    public MyViewModel(@NonNull LocationRepository locationRepository, LunchRepository lunchRepository, RestaurantRepository restaurantRepository, WorkmateRepository workmateRepository) {
        this.locationRepository = locationRepository;
        this.lunchRepository = lunchRepository;
        this.restaurantRepository = restaurantRepository;
        this.workmateRepository = workmateRepository;

        // get the Location LiveData from the LocationRepository
        LiveData<Location> locationLiveData = locationRepository.getLocationLiveData();

        // add the locationLiveData and hasGpsPermissionLiveData to the MediatorLiveData
        gpsMessageLiveData.addSource(locationLiveData, location ->
                combine(location, hasGpsPermissionLiveData.getValue())
        );
        gpsMessageLiveData.addSource(hasGpsPermissionLiveData, hasGpsPermission ->
                combine(locationLiveData.getValue(), hasGpsPermission)
        );
    }

    /**
     * Refresh the GPS location
     * This method is called when the user wants to refresh the GPS location
     * or when the app has GPS permission
     */
    @SuppressLint("MissingPermission")
    public void refresh() {

        // check if the app has GPS permission
        boolean hasGpsPermission = ContextCompat.checkSelfPermission(
                MainApplication.getApplication(), ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED;

        hasGpsPermissionLiveData.setValue(hasGpsPermission);

        // if the app has GPS permission, start the location request
        if (hasGpsPermission) {
            locationRepository.startLocationRequest();
        } else {
            // or else.. stop the location request
            locationRepository.stopLocationRequest();
        }
    }

    /**
     * Get the workmate using app
     * @return Workmate current workmate
     */
    public Workmate getCurrentWorkmate(){
        return workmateRepository.getCurrentWorkmate();
    }

    /**
     * Get the GPS status LiveData
     * @return LiveData<String> GPS message
     */
    public LiveData<GPSStatus> getGPSStatus() {
        return gpsMessageLiveData;
    }

    /**
     * Combine the GPS location and permission
     * @param location GPS location
     * @param hasGpsPermission GPS permission
     */
    private void combine(@Nullable Location location, @Nullable Boolean hasGpsPermission) {
        if (location == null) {
            if (hasGpsPermission == null || !hasGpsPermission) {
                Log.i("GPSCombine", "Location is null and GPS permission is not granted.");
                gpsMessageLiveData.setValue(new GPSStatus(false, false));
            } else {
                Log.i("GPSCombine", "Location is null but GPS permission is granted.");
                gpsMessageLiveData.setValue(new GPSStatus(false, true));
            }
        } else {
            Log.i("GPSCombine", "Location is available: Latitude = " + location.getLatitude()
                    + ", Longitude = " + location.getLongitude());
            gpsMessageLiveData.setValue(new GPSStatus(location.getLatitude(), location.getLongitude()));
        }
    }

    //region LUNCH REPOSITORY METHODS

    /**
     * Fetches today's lunches and returns a list of the associated restaurant names.
     * **/
    public LiveData<List<String>> fetchTodayLunchRestaurantNames(){
        return lunchRepository.fetchTodayLunchRestaurantNames();
    }

    /**
     * Fetches today's lunches and returns a list of lunch.
     * **/
    public LiveData<List<Lunch>> fetchTodayLunches(){
        return lunchRepository.fetchTodayLunches();
    }


    /**
     * Creates a lunch for a given workmate at the specified restaurant.
     */
    public void createLunch(Restaurant restaurant, Workmate workmate) {
        lunchRepository.createLunch(restaurant, workmate);
    }

    /**
     * Deletes the lunch of a given workmate at the specified restaurant.
     */
    public LiveData<Boolean> deleteLunch(Restaurant restaurant) {
        String uid = workmateRepository.getCurrentWorkmate().getUid();

        return lunchRepository.deleteLunch(restaurant, uid);
    }

    /**
     * Checks if a specific workmate has chosen a particular restaurant for lunch today.
     */
    public LiveData<Boolean> hasWorkmateChosenThisRestaurant(Restaurant restaurant) {
        String uid = workmateRepository.getCurrentWorkmate().getUid();

        return lunchRepository.hasWorkmateChosenThisRestaurant(restaurant, uid);
    }

    /**
     * Get today lunch
     */
    public LiveData<Lunch> getTodayLunch(String uid) {
        return lunchRepository.getTodayLunch(uid);
    }

    /**
     * Retrieves a list of workmates who have chosen the specified restaurant for lunch today.
     */
    public LiveData<List<Workmate>> fetchTodayWorkmatesAtRestaurant(Restaurant restaurant) {
        return lunchRepository.fetchTodayWorkmatesAtRestaurant(restaurant);
    }
    //endregion

    //region RESTAURANT REPOSITORY METHODS
    /**
     * Fetches a list of restaurants based on location, radius, and type.
     */
    public LiveData<List<Restaurant>> getAllRestaurants(String location, Integer radius, String type) {
        return restaurantRepository.getAllRestaurants(location, radius, type);
    }

    /**
     * Fetches the details of a restaurant by its placeId..
     */
    public LiveData<Restaurant> getRestaurantDetail(String placeId) {
        return restaurantRepository.getRestaurantDetail(placeId);
    }
    //endregion

    //region WORKMATE REPOSITORY METHODS
    /**
     * LiveData to retrieve all workmates
     */
    public LiveData<List<Workmate>> getAllWorkmates() {
        return workmateRepository.getAllWorkmates();
    }


    /**
     * Check if notifications are enabled for a specific workmate
     */
    public LiveData<Boolean> getIsNotificationEnabled() {
        return workmateRepository.getIsNotificationEnabled();
    }

    /**
     * Create or update a workmate based on notification status
     */
    public void createOrUpdateWorkmate(Boolean isNotificationActive) {
        workmateRepository.createOrUpdateWorkmate(isNotificationActive);
    }

    /**
     * Add or Delete a restaurant to a workmate's liked restaurants
     */
    public void isRestaurantLikedByUser(Restaurant restaurant, boolean liked){
        if (liked){
            workmateRepository.addLikeRestaurant(restaurant);
        }else{
            workmateRepository.deleteLikeRestaurant(restaurant);
        }
    }

    /**
     * Check if the current workmate has liked a specific restaurant
     */
    public LiveData<Boolean> checkIfCurrentWorkmateLikeThisRestaurant(Restaurant restaurant) {
        return workmateRepository.checkIfCurrentWorkmateLikeThisRestaurant(restaurant);
    }
    //endregion

}
