package com.julien.go4lunch.model.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.julien.go4lunch.model.bo.Lunch;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.Workmate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LunchRepository {
    // TAG for logs
    private final String TAG = "LUNCH_REPOSITORY";

    // COLLECTION NAME
    public final String LUNCH_COLLECTION = "lunches";
    public final String LUNCH_WORKMATE_ID_FIELD = "workmate.uid";
    public final String LUNCH_DATE_FIELD = "date";
    public final String RESTAURANT_CHOSEN_NAME = "restaurant.name";

    // SINGLETON
    private static LunchRepository instance;

    // Private constructor to prevent direct instantiation
    private LunchRepository() {
        // Keep Empty
    }

    /**
     * Returns the unique instance of the LunchRepository.
     * Implements the Singleton pattern to ensure only one instance of the repository exists.
     */
    public static LunchRepository getInstance() {
        if (instance == null) {
            instance = new LunchRepository();
        }
        return instance;
    }

    /**
     * Returns the reference to the "Lunch" collection in Firestore.
     */
    private CollectionReference getLunchCollection() {
        return FirebaseFirestore.getInstance().collection(LUNCH_COLLECTION);
    }

    /**
     * Returns the current date truncated to the day (without time).
     */
    private String toDay() {
        return Instant.now().truncatedTo(ChronoUnit.DAYS).toString();
    }

    /**
     * Fetches a list of restaurant names where lunches are scheduled for today
     */
    public LiveData<List<String>> fetchTodayLunchRestaurantNames() {
        MutableLiveData<List<String>> restaurantNameLiveData = new MutableLiveData<>();

        Task<QuerySnapshot> task = getLunchCollection()
                .whereEqualTo(LUNCH_DATE_FIELD, toDay())
                .get();

        task.addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                List<Lunch> lunchList = result.getResult().toObjects(Lunch.class);

                if (!lunchList.isEmpty()) {
                    Log.i(TAG, "Successfully retrieved today's lunches: " + lunchList.size() + " lunch(es) scheduled.");

                    List<String> restaurantNameList = new ArrayList<>();
                    for (Lunch lunch : lunchList) {
                        String restaurantName = lunch.getRestaurant().getName();
                        restaurantNameList.add(restaurantName);
                    }

                    restaurantNameLiveData.setValue(restaurantNameList);
                } else {
                    Log.i(TAG, "No lunches scheduled for today.");
                    restaurantNameLiveData.setValue(null);
                }

            } else {
                Log.e(TAG, "Error retrieving lunches: ", task.getException());
                restaurantNameLiveData.setValue(null);
            }
        });

        task.addOnFailureListener(result -> {
            Log.e(TAG, "Task failed while retrieving today's lunches.");
            restaurantNameLiveData.setValue(null);
        });

        return restaurantNameLiveData;
    }

    /**
     * Get all lunches for today
     **/
    public LiveData<List<Lunch>> fetchTodayLunches() {
        MutableLiveData<List<Lunch>> lunchLiveData = new MutableLiveData<>();

        Task<QuerySnapshot> task = getLunchCollection()
                .whereEqualTo(LUNCH_DATE_FIELD, toDay())
                .get();

        task.addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                List<Lunch> lunchList = result.getResult().toObjects(Lunch.class);

                if (!lunchList.isEmpty()) {
                    Log.i(TAG, "Successfully retrieved today's lunches: " + lunchList.size() + " lunch(es) scheduled.");
                    lunchLiveData.setValue(lunchList);
                } else {
                    Log.i(TAG, "No lunches scheduled for today.");
                    lunchLiveData.setValue(new ArrayList<>());
                }
            } else {
                Log.e(TAG, "Error retrieving lunches: ", task.getException());
                lunchLiveData.setValue(null);
            }
        });

        task.addOnFailureListener(result -> {
            Log.e(TAG, "Task failed while retrieving today's lunches.");
            lunchLiveData.setValue(null);
        });

        return lunchLiveData;
    }


    /**
     * Create a Lunch
     **/
    public void createLunch(Restaurant restaurantChosen, Workmate workmate) {
        Lunch lunch = new Lunch(workmate, restaurantChosen, toDay());

        getLunchCollection().add(lunch)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Lunch successfully created for workmate: " + workmate.getName() + " at restaurant: " + restaurantChosen.getName());
                    } else {
                        Log.e(TAG, "Error creating lunch for workmate: " + workmate.getName() + " at restaurant: " + restaurantChosen.getName(), task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failure in creating lunch for workmate: " + workmate.getName() + " at restaurant: " + restaurantChosen.getName() + ". Exception: " + e.getMessage(), e);
                });
    }

    /**
     * Get the today lunch for a given workmate, if it exists. As LiveData
     */
    public LiveData<Lunch> getTodayLunch(String uid) {
        MutableLiveData<Lunch> todayLunch = new MutableLiveData<>();

        Task<QuerySnapshot> task = getLunchCollection()
                .whereEqualTo(LUNCH_WORKMATE_ID_FIELD, uid)
                .whereEqualTo(LUNCH_DATE_FIELD, toDay())
                .get();

        task.addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                QuerySnapshot querySnapshot = result.getResult();
                if (querySnapshot.size() != 0) {
                    todayLunch.setValue(querySnapshot.toObjects(Lunch.class).get(0));
                    Log.i(TAG, "SUCCESS GetTodayLunch result : " + todayLunch.getValue());
                } else {
                    todayLunch.setValue(null);
                    Log.i(TAG, "NULL GetTodayLunch result : " + todayLunch.getValue());
                }
            } else {
                Log.d(TAG, "Error in GetTodayLunch onComplete: " + result.getException());
            }
        });

        task.addOnFailureListener(e -> {
            Log.e(TAG, "FAILURE in GetTodayLunch: " + e.getMessage(), e);
        });

        return todayLunch;
    }

    /**
     * Get ALL the today lunch for a given restaurant, if it exists
     */
    public LiveData<List<Workmate>> fetchTodayWorkmatesAtRestaurant(Restaurant restaurant) {
        MutableLiveData<List<Workmate>> workmates = new MutableLiveData<>();

        Task<QuerySnapshot> task = getLunchCollection()
                .whereEqualTo(RESTAURANT_CHOSEN_NAME, restaurant.getName())
                .whereEqualTo(LUNCH_DATE_FIELD, toDay())
                .get();

        task.addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                if (!result.getResult().isEmpty()) {
                    QuerySnapshot querySnapshot = result.getResult();
                    List<Workmate> workmateList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        Lunch lunch = document.toObject(Lunch.class);

                        Workmate workmateFound = lunch.getWorkmate();
                        workmateList.add(workmateFound);
                    }

                    Log.i(TAG, "Results found for the chosen restaurant: " + restaurant.getName() + ". Workmates count: " + workmateList.size());
                    workmates.setValue(workmateList);
                } else {
                    Log.i(TAG, "No workmates found for the chosen restaurant: " + restaurant.getName() + " today.");
                    workmates.setValue(null);
                }

            } else {
                Log.e(TAG, "Error getting workmates for chosen restaurant : ", result.getException());
                workmates.setValue(null);
            }
        });

        task.addOnFailureListener(e -> {
            Log.e(TAG, "Failure in retrieving workmates for chosen restaurant: " + restaurant.getName() + ". Exception: " + e.getMessage(), e);
            workmates.setValue(null);
        });

        return workmates;
    }

    ;

    /**
     * Check if the current workmates has chosen a particular Restaurant for today
     */
    public LiveData<Boolean> hasWorkmateChosenThisRestaurant(Restaurant restaurant, String user_uid) {
        MutableLiveData<Boolean> hasChosen = new MutableLiveData<>();

        Task<QuerySnapshot> task = getLunchCollection()
                .whereEqualTo(RESTAURANT_CHOSEN_NAME, restaurant.getName())
                .whereEqualTo(LUNCH_WORKMATE_ID_FIELD, user_uid)
                .whereEqualTo(LUNCH_DATE_FIELD, toDay())
                .get();

        task.addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                if (!result.getResult().isEmpty()) {
                    QuerySnapshot querySnapshot = result.getResult();

                    boolean found = false;

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        Lunch lunch = document.toObject(Lunch.class);

                        Workmate workmateFound = lunch.getWorkmate();

                        if (Objects.equals(workmateFound.getUid(), user_uid)) {
                            found = true;
                            break;
                        }
                    }
                    hasChosen.setValue(found);
                    Log.i(TAG, "Check completed: Current workmate " + (found ? "has" : "has not") + " chosen the restaurant: " + restaurant.getName());

                } else {
                    Log.i(TAG, "No matching results found for user_id: " + user_uid + " and restaurant: " + restaurant.getName());
                    hasChosen.setValue(null);
                }
            } else {
                Log.e(TAG, "Error getting result : ", result.getException());
                hasChosen.setValue(null);
            }
        });

        task.addOnFailureListener(e -> {
            Log.e(TAG, "Failure in task to check if current workmate chose the restaurant: " + restaurant.getName() + ". Exception: " + e.getMessage(), e);
            hasChosen.setValue(null);
        });

        return hasChosen;
    }


    /**
     * Delete a lunch, by specifying the restaurant and the user_id
     */
    public LiveData<Boolean> deleteLunch(Restaurant restaurant, String user_uid) {

        MutableLiveData<Boolean> isDeleted = new MutableLiveData<>();

        Task<QuerySnapshot> task = getLunchCollection()
                .whereEqualTo(RESTAURANT_CHOSEN_NAME, restaurant.getName())
                .whereEqualTo(LUNCH_WORKMATE_ID_FIELD, user_uid)
                .whereEqualTo(LUNCH_DATE_FIELD, toDay())
                .get();

        task.addOnCompleteListener(result -> {
            if (result.isSuccessful()) {
                if (!result.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot document : result.getResult()) {


                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.i(TAG, "Successfully deleted lunch for user_id: " + user_uid + " and restaurant: " + restaurant.getName());
                                    isDeleted.setValue(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting document for user_id: " + user_uid + " and restaurant: " + restaurant.getName(), e);
                                    isDeleted.setValue(false);
                                });

                    }
                } else {
                    isDeleted.setValue(null);
                    Log.i(TAG, "No lunch found to delete for user_id: " + user_uid + " and restaurant: " + restaurant.getName());
                }
            } else {
                isDeleted.setValue(null);
                Log.e(TAG, "No lunch found for the given user_id and restaurant.");
            }
        });

        task.addOnFailureListener(e -> {
            isDeleted.setValue(null);
            Log.e(TAG, "Failure in task to retrieve lunch for deletion. User_id: " + user_uid + ", Restaurant: " + restaurant.getName() + ". Exception: " + e.getMessage(), e);
        });

        return isDeleted;
    }
}
