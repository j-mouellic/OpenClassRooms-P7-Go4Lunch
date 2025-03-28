package com.julien.go4lunch.model.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.julien.go4lunch.BuildConfig;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.place.ListRestaurant;
import com.julien.go4lunch.model.bo.place.Result;
import com.julien.go4lunch.model.bo.place.ResultDetails;
import com.julien.go4lunch.model.service.RetrofitService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestaurantRepository {
    // TAG for logs
    private final String TAG = "RestaurantRepository";

    // RETROFIT Utils
    private final String BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    private final String API_KEY = BuildConfig.GOOGLE_MAPS_API_KEY;
    private final String RESTAURANT_DETAILS_FIELD = "place_id,name,rating,opening_hours,photo,vicinity,type,website,formatted_phone_number";

    // SINGLETON
    private static RestaurantRepository instance = null;
    private static Retrofit retrofit = null;


    // Private constructor to prevent direct instantiation
    private RestaurantRepository() {
        // Keep empty
    }

    /**
     * Returns the unique instance of the RestaurantRepository.
     * Implements the Singleton pattern to ensure only one instance of the repository exists.
     */
    public static RestaurantRepository getInstance() {
        if (instance == null) {
            instance = new RestaurantRepository();
        }
        return instance;
    }

    private RetrofitService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(RetrofitService.class);
    }

    public Call<ListRestaurant> getCallAllRestaurants(String location, Integer radius, String type){
        RetrofitService retrofitService = getService();

        return retrofitService.getAllRestaurants(location, radius, type, API_KEY);
    }

    public LiveData<List<Restaurant>> getAllRestaurants(String location, Integer radius, String type) {
        MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();

        getCallAllRestaurants(location, radius, type).enqueue(new Callback<ListRestaurant>() {
            @Override
            public void onResponse(Call<ListRestaurant> call, Response<ListRestaurant> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<Result> resultList = response.body().getResults();
                        List<Restaurant> restaurantsList = new ArrayList<>();

                        Log.i(TAG, "Successful API call : ListRestaurant, restaurants founded : " + resultList.size());

                        for (Result r : resultList) {
                            Restaurant restaurant = new Restaurant();

                            restaurant.setId(r.getPlaceId());

                            restaurant.setName(r.getName());
                            restaurant.setLocation(r.getGeometry().getLocation());

                            if (r.getTypes() != null) {
                                restaurant.setTypes(r.getTypes());
                            }

                            if (r.getOpeningHours() != null) {
                                if (r.getOpeningHours().getOpenNow() != null){
                                    restaurant.setOpened(r.getOpeningHours().getOpenNow());
                                }
                            }

                            if (r.getUserRatingsTotal() != null) {
                                restaurant.setNumberOfReviews(r.getUserRatingsTotal());
                            }

                            if (r.getRating() != null) {
                                restaurant.setRating(r.getRating());
                            }

                            if (r.getPhotos() != null) {
                                restaurant.setPhotos(r.getPhotos());
                            }

                            if (r.getVicinity() != null) {
                                restaurant.setAddress(r.getVicinity());
                            }

                            restaurantsList.add(restaurant);
                        }

                        restaurantsLiveData.setValue(restaurantsList);
                    } else {
                        Log.e(TAG, "ListRestaurant : API response body is null. Unable to retrieve restaurant data.");
                    }
                } else {
                    Log.e(TAG, "ListRestaurant : API call was not successful. Response code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ListRestaurant> call, Throwable t) {
                Log.i(TAG, "Failure API call : ListRestaurant, exception : " + t.getMessage());
            }
        });

        return restaurantsLiveData;
    }

    public LiveData<Restaurant> getRestaurantDetail(String placeId) {
        MutableLiveData<Restaurant> restaurant = new MutableLiveData<>();

        RetrofitService retrofitService = getService();

        Call<ResultDetails> call = retrofitService.getRestaurantDetails(API_KEY, placeId, RESTAURANT_DETAILS_FIELD);

        call.enqueue(new Callback<ResultDetails>() {
            @Override
            public void onResponse(Call<ResultDetails> call, Response<ResultDetails> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body() != null) {
                        Result result = response.body().getResult();

                        Log.i("DEBUG", "Restaurant detail : " + result);

                        Restaurant newRestaurant = new Restaurant();

                        if (result.getPlaceId() != null) {
                            newRestaurant.setId(result.getPlaceId());
                        }

                        if (result.getWebsite() != null) {
                            newRestaurant.setWebsite(result.getWebsite());
                        }

                        if (result.getFormattedPhoneNumber() != null) {
                            newRestaurant.setformattedPhoneNumber(result.getFormattedPhoneNumber());
                        }

                        if (result.getName() != null) {
                            newRestaurant.setName(result.getName());
                        }

                        if (result.getVicinity() != null) {
                            newRestaurant.setAddress(result.getVicinity());
                        }

                        if (result.getOpeningHours() != null && result.getOpeningHours().getOpenNow() != null) {
                            newRestaurant.setOpened(result.getOpeningHours().getOpenNow());
                        }

                        if (result.getGeometry() != null && result.getGeometry().getLocation() != null) {
                            newRestaurant.setLocation(result.getGeometry().getLocation());
                        }

                        if (result.getTypes() != null) {
                            newRestaurant.setTypes(result.getTypes());
                        }

                        if (result.getUserRatingsTotal() != null) {
                            newRestaurant.setNumberOfReviews(result.getUserRatingsTotal());
                        }

                        if (result.getRating() != null) {
                            newRestaurant.setRating(result.getRating());
                        }

                        if (result.getPhotos() != null) {
                            newRestaurant.setPhotos(result.getPhotos());
                        }

                        restaurant.setValue(newRestaurant);

                       // Log.i(TAG, "Successful API call : ResultDetails, restaurant name : " + newRestaurant.toString());
                    } else {
                        Log.e(TAG, "Restaurant detail : API response body is null. Unable to retrieve restaurant data.");
                    }
                } else {
                    Log.e(TAG, "Restaurant detail : API call was not successful. Response code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResultDetails> call, Throwable t) {
                Log.i(TAG, "Failure API call : ResultDetails, exception : " + t.getMessage());
            }
        });

        return restaurant;
    }
}
