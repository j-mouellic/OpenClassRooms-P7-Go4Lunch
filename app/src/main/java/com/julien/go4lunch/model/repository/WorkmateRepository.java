package com.julien.go4lunch.model.repository;

import static com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.Workmate;

import java.util.List;

public class WorkmateRepository {
    // TAG for logs
    private final String TAG = "WORKMATE_REPOSITORY";

    // COLLECTION NAME
    public final String COLLECTION_NAME = "workmates";
    public final String USER_ID_FIELD = "uid";
    public final String LIKED_SUB_COLLECTION = "likedRestaurant";
    public final String LIKED_RESTAURANT_NAME = "name";
    public final String IS_NOTIFICATION_ENABLED_FIELD = "notificationEnabled";

    // CURRENT USER ID
    private String currentWorkmateDocumentId = null;
    private String fireBaseUserUid = null;

    // SINGLETON
    private static WorkmateRepository instance;

    // Private constructor to prevent direct instantiation
    private WorkmateRepository() {
        getOrCreateWorkmate();
    }

    /**
     * Returns the unique instance of the WorkmateRepository.
     * Implements the Singleton pattern to ensure only one instance of the repository exists.
     */
    public static WorkmateRepository getInstance() {
        if (instance == null) {
            instance = new WorkmateRepository();
        }
        return instance;
    }

    /**
     * Retrieves the Firestore collection of workmates.
     */
    private CollectionReference getWorkmateCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    /**
     * Retrieves the current authenticated workmate as a FirebaseUser.
     */
    private FirebaseUser getFirebaseUserAsWorkmate() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void getOrCreateWorkmate(){
        FirebaseUser user = getFirebaseUserAsWorkmate();
        fireBaseUserUid = user.getUid();

        getWorkmateCollection()
                .whereEqualTo(USER_ID_FIELD, fireBaseUserUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        currentWorkmateDocumentId = task.getResult()
                                .getDocuments()
                                .get(0).getId();
                        Log.i(TAG, "Current workmate document id found : " + currentWorkmateDocumentId);
                    }else{
                        Log.i(TAG, "No document found for user: " + fireBaseUserUid);
                        addWorkmate(user);
                    }
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
                        if (firestoreException.getCode() == NOT_FOUND) {
                            Log.i(TAG, "Document not found - add user as workmate to FireStore : ");
                            addWorkmate(user);
                        } else {
                            Log.e(TAG, "🔥 Error getOrCreateWorkmate : " + firestoreException);
                        }
                    }
                });
    }

    /**
     * Converts the current FirebaseUser to a Workmate object.
     */
    public Workmate getCurrentWorkmate() {
        FirebaseUser user = getFirebaseUserAsWorkmate();

        Workmate workmate = null;
        if (user != null) {
            String urlPicture = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
            String name = user.getDisplayName();
            String userId = user.getUid();
            String email = user.getEmail();

            workmate = new Workmate(userId, name, email, urlPicture, false);
        } else {
            Log.e(TAG, "getFirebaseUserAsWorkmate: workmate is null");
        }
        Log.i(TAG, "Current user : " + workmate);
        return workmate;
    }

    public String getCurrentWorkmateDocumentId(){
        return currentWorkmateDocumentId;
    }


    /**
     * Adds a new workmate to the Firestore database.
     */
    private void addWorkmate(FirebaseUser user) {
        String urlPicture = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
        String name = user.getDisplayName();
        String uid = user.getUid();
        String email = user.getEmail();

        Workmate workmate = new Workmate(uid, name, email, urlPicture, false);

        getWorkmateCollection().add(workmate)
                .addOnSuccessListener(result -> {
                    currentWorkmateDocumentId = result.getId();
                    Log.i(TAG, "Workmate successfully added to FireStore, current workmate id : " + currentWorkmateDocumentId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add to workmate, error : " + e);
                });
    }


    /**
     * Creates or updates the workmate in the Firestore database.
     * If the workmate exists, it will be updated; otherwise, a new workmate will be created.
     */
    public void createOrUpdateWorkmate(Boolean isNotificationActive) {
        if (currentWorkmateDocumentId != null) {
            Log.i(TAG, "Update Workmate, is notification active : " + isNotificationActive);
            getWorkmateCollection().document(currentWorkmateDocumentId)
                    .update(IS_NOTIFICATION_ENABLED_FIELD, isNotificationActive)
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "Workmate updated successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating workmate: " + e));
        } else {
            Log.i(TAG, "Failed to get current FireBaseUser");
        }
    }


    /**
     * Retrieves the notification activation status for the current workmate.
     */
    public LiveData<Boolean> getIsNotificationEnabled() {
        MutableLiveData<Boolean> isActive = new MutableLiveData<>();

        getWorkmateCollection()
                .whereEqualTo(USER_ID_FIELD, fireBaseUserUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {

                        Boolean isNotificationEnabled = task.getResult()
                                .getDocuments()
                                .get(0)
                                .getBoolean(IS_NOTIFICATION_ENABLED_FIELD);

                        isActive.setValue(isNotificationEnabled);

                        Log.i(TAG, "Workmate : " + currentWorkmateDocumentId + " Is Notification Enabled " + isNotificationEnabled);
                    } else {
                        Log.e(TAG, "Error getting notificationEnabled : ", task.getException());
                        isActive.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failure in retrieving notificationEnabled for Workmate: " + currentWorkmateDocumentId + ". Exception: " + e.getMessage(), e);
                    isActive.setValue(null);
                });

        return isActive;
    }


    /**
     * Retrieves all workmates.
     */
    public LiveData<List<Workmate>> getAllWorkmates() {
        MutableLiveData<List<Workmate>> workmates = new MutableLiveData<>();

        getWorkmateCollection()
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Workmate> results = task.getResult().toObjects(Workmate.class);
                        workmates.setValue(results);
                    } else {
                        Log.e(TAG, "Error getting workmates: ", task.getException());
                        workmates.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failure in retrieving workmates. Exception: " + e.getMessage(), e);
                    workmates.setValue(null);
                });

        return workmates;
    }

    /**
     * Adds a like for a specific restaurant by the current workmate.
     */
    public void addLikeRestaurant(Restaurant restaurant) {

        getWorkmateCollection()
                .document(currentWorkmateDocumentId)
                .collection(LIKED_SUB_COLLECTION)
                .add(restaurant)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Successfully added restaurant: " + restaurant.getName() + " to likes for user: " + currentWorkmateDocumentId);
                    } else {
                        Log.e(TAG, "Failed to add restaurant: " + restaurant.getName() + " to likes for user: " + currentWorkmateDocumentId, task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding restaurant: " + restaurant.getName() + " to likes for user: " + currentWorkmateDocumentId, e);
                });
    }


    /**
     * Deletes the like for a specific restaurant by the current workmate.
     */
    public void deleteLikeRestaurant(Restaurant restaurant) {
        getWorkmateCollection()
                .document(currentWorkmateDocumentId)
                .collection(LIKED_SUB_COLLECTION)
                .whereEqualTo(LIKED_RESTAURANT_NAME, restaurant.getName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.i(TAG, "Successfully deleted restaurant: " + restaurant.getName() + " for user: " + currentWorkmateDocumentId))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Error deleting restaurant: " + restaurant.getName() + " for user: " + currentWorkmateDocumentId, e));
                            }
                        } else {
                            Log.i(TAG, "No likes found for restaurant: " + restaurant.getName() + " from user: " + currentWorkmateDocumentId);
                        }
                    } else {
                        Log.e(TAG, "Error retrieving liked restaurants for user: " + currentWorkmateDocumentId, task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error initiating the query to delete like for restaurant: " + restaurant.getName() + " for user: " + currentWorkmateDocumentId, e);
                });
    }

    /**
     * Checks if the current workmate has liked the given restaurant.
     */
    public LiveData<Boolean> checkIfCurrentWorkmateLikeThisRestaurant(Restaurant restaurant) {
        MutableLiveData<Boolean> isLiked = new MutableLiveData<>();

        getWorkmateCollection()
                .document(currentWorkmateDocumentId)
                .collection(LIKED_SUB_COLLECTION)
                .whereEqualTo(LIKED_RESTAURANT_NAME, restaurant.getName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isLikedResult = task.getResult().size() > 0;
                        isLiked.postValue(isLikedResult);

                        if (isLikedResult) {
                            Log.i(TAG, "Workmate " + currentWorkmateDocumentId + " likes the restaurant: " + restaurant.getName());
                        } else {
                            Log.i(TAG, "Workmate " + currentWorkmateDocumentId + " has not liked the restaurant: " + restaurant.getName());
                        }
                    } else {
                        isLiked.postValue(null);
                        Log.e(TAG, "Error checking if workmate " + currentWorkmateDocumentId + " likes the restaurant: " + restaurant.getName(), task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    isLiked.postValue(null);
                    Log.e(TAG, "Failure checking if workmate " + currentWorkmateDocumentId + " likes the restaurant: " + restaurant.getName(), e);
                });

        return isLiked;
    }

    /**
     * ONLY FOR SEEDING
     */
    public void addWorkmateForSeeding(List<Workmate> newWorkmates) {
        for (int i = 0; i < newWorkmates.size(); i++) {
            getWorkmateCollection().add(newWorkmates.get(i))
                    .addOnCompleteListener(task -> {
                        Log.i(TAG, "Success");
                    })
                    .addOnFailureListener(e -> {
                        Log.i(TAG, "Failure");
                    });
        }
    }
}
