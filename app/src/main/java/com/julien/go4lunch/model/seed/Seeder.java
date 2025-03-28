package com.julien.go4lunch.model.seed;

import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

import com.github.javafaker.Faker;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.Workmate;
import com.julien.go4lunch.model.repository.LunchRepository;
import com.julien.go4lunch.model.repository.RestaurantRepository;
import com.julien.go4lunch.model.repository.WorkmateRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Seeder {

    // UTILS
    private static final String TAG = "Seeder";
    private static Faker faker = new Faker();

    // REPOSITORIES
    private static LunchRepository lunchRepository;
    private static RestaurantRepository restaurantRepository;
    private static WorkmateRepository workmateRepository;

    // RESTAURANT REPO UTILS
    private static String location = "48.8572,2.3473";
    private static Integer radius = 500;
    private static String type = "restaurant";

    // SEEDER VARIABLES
    private static List<Workmate> workmateList;
    private boolean enableDbSeeding = false;


    public static void initDbSeeding(LifecycleOwner lifecycleOwner) {
        lunchRepository = LunchRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
        workmateRepository = WorkmateRepository.getInstance();

        workmateRepository.getAllWorkmates().observe(lifecycleOwner, workmates -> {
            if (workmates.size() < 40) {
                Log.i(TAG, "Create 20 workmates");
                workmateList = generateRandomWorkmates();
                workmateRepository.addWorkmateForSeeding(workmateList);
            } else {
                Log.i(TAG, "All workmates retreived : " + workmates.size());
                workmateList = workmates;
            }

            // Chaining methods
            restaurantRepository.getAllRestaurants(location, radius, type).observe(lifecycleOwner, restaurants -> {

                // Restaurant 1 - 5 lunches
                for (int i = 0; i < 5; i++) {
                    Restaurant restaurant = restaurants.get(0);
                    Workmate workmate = workmateList.get(i);
                    Log.i(TAG, "Create a new lunch for : " + workmate.getName() + " at restaurant : " + restaurant.getName());
                    lunchRepository.createLunch(restaurant, workmate);
                }

                // Restaurant 3 - 4 lunches
                for (int i = 6; i < 9; i++) {
                    Restaurant restaurant = restaurants.get(2);
                    Workmate workmate = workmateList.get(i);
                    Log.i(TAG, "Create a new lunch for : " + workmate.getName() + " at restaurant : " + restaurant.getName());
                    lunchRepository.createLunch(restaurant, workmate);
                }

                // Restaurant 5 - 6 lunches
                for (int i = 10; i < 15; i++) {
                    Restaurant restaurant = restaurants.get(4);
                    Workmate workmate = workmateList.get(i);
                    Log.i(TAG, "Create a new lunch for : " + workmate.getName() + " at restaurant : " + restaurant.getName());
                    lunchRepository.createLunch(restaurant, workmate);
                }

                // Restaurant 6 - 6 lunches
                for (int i = 16; i < 21; i++) {
                    Restaurant restaurant = restaurants.get(5);
                    Workmate workmate = workmateList.get(i);
                    Log.i(TAG, "Create a new lunch for : " + workmate.getName() + " at restaurant : " + restaurant.getName());
                    lunchRepository.createLunch(restaurant, workmate);
                }
            });
        });
    }

    /**
     * Generates a list of random Workmate objects.
     *
     * @return a list of Workmate objects
     */
    private static List<Workmate> generateRandomWorkmates() {
        List<Workmate> workmates = new ArrayList<>();

        for (int i = 0; i < 40; i++) {
            String userUid = String.valueOf(UUID.randomUUID());

            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();

            String name = firstName + " " + lastName;
            String email = firstName + "." + lastName + "@mail.com";

            String avatar = faker.internet().image();

            boolean isNotificationEnabled = false;

            workmates.add(new Workmate(userUid, name, email, avatar, isNotificationEnabled));
        }

        return workmates;
    }
}
