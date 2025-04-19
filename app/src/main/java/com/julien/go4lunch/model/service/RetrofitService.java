package com.julien.go4lunch.model.service;

import com.julien.go4lunch.model.bo.place.ListRestaurant;
import com.julien.go4lunch.model.bo.place.ResultDetails;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface for Retrofit service to interact with the Google Places API.
 * Provides methods to retrieve nearby restaurants and detailed information about a specific restaurant.
 */
public interface RetrofitService {

    /**
     * Retrieves a list of restaurants based on the specified location, radius, type, and API key.
     */
    @GET("nearbysearch/json")
    Call<ListRestaurant> getAllRestaurants(
            @Query("location") String location,
            @Query("radius") Integer radius,
            @Query("type") String type,
            @Query("key") String key
    );

    /**
     * Retrieves detailed information about a specific restaurant based on the place ID.
     */
    @GET("details/json")
    Call<ResultDetails> getRestaurantDetails(
            @Query("key") String key,
            @Query("place_id") String placeId,
            @Query("fields") String fields
    );
}
