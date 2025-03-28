package com.julien.go4lunch.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.LocationServices;
import com.julien.go4lunch.MainApplication;
import com.julien.go4lunch.model.repository.LocationRepository;
import com.julien.go4lunch.model.repository.LunchRepository;
import com.julien.go4lunch.model.repository.RestaurantRepository;
import com.julien.go4lunch.model.repository.WorkmateRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    // Singleton instance of the ViewModelFactory
    private static volatile ViewModelFactory instance;

    // Repositories used by the ViewModel
    private final LocationRepository locationRepository;
    private final LunchRepository lunchRepository;
    private final RestaurantRepository restaurantRepository;
    private final WorkmateRepository workmateRepository;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ViewModelFactory(LocationRepository locationRepository, LunchRepository lunchRepository,
                             RestaurantRepository restaurantRepository, WorkmateRepository workmateRepository) {
        this.locationRepository = locationRepository;
        this.lunchRepository = lunchRepository;
        this.restaurantRepository = restaurantRepository;
        this.workmateRepository = workmateRepository;
    }

    /**
     * Provides the singleton instance of the ViewModelFactory.
     * This ensures that the same factory is used throughout the application.
     */
    public static synchronized ViewModelFactory getInstance() {
        if (instance == null) {
            // Initialize repositories using their respective getInstance() methods
            LocationRepository locationRepository = new LocationRepository(
                    LocationServices.getFusedLocationProviderClient(MainApplication.getApplication())
            );
            LunchRepository lunchRepository = LunchRepository.getInstance();
            RestaurantRepository restaurantRepository = RestaurantRepository.getInstance();
            WorkmateRepository workmateRepository = WorkmateRepository.getInstance();

            // Create the singleton instance of ViewModelFactory
            instance = new ViewModelFactory(locationRepository, lunchRepository, restaurantRepository, workmateRepository);
        }
        return instance;
    }

    /**
     * Creates and provides ViewModel instances.
     * **/
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
        if (aClass.isAssignableFrom(MyViewModel.class)) {
            return (T) new MyViewModel(locationRepository, lunchRepository, restaurantRepository, workmateRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
