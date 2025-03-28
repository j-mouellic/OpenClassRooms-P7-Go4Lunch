package com.julien.go4lunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.place.ListRestaurant;
import com.julien.go4lunch.model.bo.place.Location;
import com.julien.go4lunch.model.bo.place.Result;
import com.julien.go4lunch.model.bo.place.ResultDetails;
import com.julien.go4lunch.model.repository.RestaurantRepository;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RetrofitUnitTest {
    private final String MAPS_RESTAURANT_TYPE = "restaurant";
    private String placeId = null;
    private String restaurantName = null;
    private String restaurantAddress = null;
    private Location location = null;

    @Test
    public void checkRestaurantNearByAsynchronously() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger restaurantCount = new AtomicInteger(0);

        RestaurantRepository restaurantRepository = RestaurantRepository.getInstance();

        Call<ListRestaurant> listRestaurantCall = restaurantRepository.getCallAllRestaurants("48.1159843,-1.7296427", 1000, MAPS_RESTAURANT_TYPE);

        listRestaurantCall.enqueue(new Callback<ListRestaurant>() {
            @Override
            public void onResponse(Call<ListRestaurant> call, Response<ListRestaurant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Result> result = response.body().getResults();
                    restaurantCount.set(result.size());

                    placeId = result.get(0).getPlaceId();
                }
                latch.countDown();
            }

            @Override
            public void onFailure(Call<ListRestaurant> call, Throwable t) {
                latch.countDown();
            }
        });

        latch.await();

        assertTrue(restaurantCount.get() > 0);
    }

    @Test
    public void checkRestaurantDetails() throws InterruptedException {


        RestaurantRepository restaurantRepository = RestaurantRepository.getInstance();

        Call<ResultDetails> restaurantDetails = restaurantRepository.getCallRestaurantDetails(placeId);

        restaurantDetails.enqueue(new Callback<ResultDetails>() {
            @Override
            public void onResponse(Call<ResultDetails> call, Response<ResultDetails> response) {

                Result result = response.body().getResult();

                restaurantName = result.getName();
                restaurantAddress = result.getVicinity();
                location = result.getGeometry().getLocation();

                assertNotNull("Restaurant name should not be null", restaurantName);
                assertNotNull("Restaurant address should not be null", restaurantAddress);
                assertNotNull("Restaurant location should not be null", location);
            }

            @Override
            public void onFailure(Call<ResultDetails> call, Throwable t) {
                assertNotNull("Restaurant name should not be null", restaurantName);
                assertNotNull("Restaurant address should not be null", restaurantAddress);
                assertNotNull("Restaurant location should not be null", location);
            }
        });
    }
}
