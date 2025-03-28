package com.julien.go4lunch.model.service;

import com.julien.go4lunch.model.bo.place.ListRestaurant;
import com.julien.go4lunch.model.bo.place.ResultDetails;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("nearbysearch/json")
    Call<ListRestaurant> getAllRestaurants(
            @Query("location") String location,
            @Query("radius") Integer radius,
            @Query("type") String type,
            @Query("key") String key
    );

    @GET("details/json")
    Call<ResultDetails> getRestaurantDetails(
            @Query("key") String key,
            @Query("place_id") String placeId,
            @Query("fields") String fields
    );
}
