package com.julien.go4lunch.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.julien.go4lunch.MainApplication;
import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Lunch;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.Workmate;
import com.julien.go4lunch.model.repository.LunchRepository;
import com.julien.go4lunch.model.repository.WorkmateRepository;
import com.julien.go4lunch.view.DetailsActivity;
import com.julien.go4lunch.view.tabviews.TabActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AlarmReceiver is a BroadcastReceiver that triggers notifications to remind the user to go to lunch.
 * It is invoked when the alarm goes off to notify the user of the restaurant they should go to, along with
 * the workmates who have chosen the same restaurant for lunch.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final boolean NOTIFICATION_DEBUG = TabActivity.NOTIFICATION_DEBUG;
    private static final String TAG = "ALARM";
    private final Context context = MainApplication.getApplication().getApplicationContext();
    private static final int NOTIFICATION_ID = 7;

    /**
     * Builds and sends a notification with the lunch details.
     *
     * @param context The application context.
     * @param restaurant The restaurant chosen for lunch.
     * @param userNames List of names of workmates joining the same restaurant.
     */
    private void sendMessage(Context context, Restaurant restaurant, List<String> userNames) {

        // Build String
        String userList = String.join(", ", userNames);
        String notificationMessage = context.getString(
                R.string.notification_message,
                restaurant.getName(),
                restaurant.getAddress(),
                userList
        );

        // Create intent
        Intent newIntent = new Intent(context, DetailsActivity.class);
        newIntent.putExtra("RESTAURANT", restaurant);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(context, TabActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(TabActivity.CHANNEL_NAME)
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // (it allows you to specify what should happen when the user interacts with the notification)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: ASK For permission
            return;
        }
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }


    /**
     * Called when the broadcast is received. This method is responsible for checking if the user has notifications
     * enabled, fetching the user's lunch and workmates, and sending the appropriate notification.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "ALARM TRIGGERED");

        /*if (NOTIFICATION_DEBUG) {
            Restaurant fakeRestaurant = new Restaurant();
            fakeRestaurant.setName("Fake Restaurant");
            fakeRestaurant.setAddress("Fake Address");
            fakeRestaurant.setId("Fake ID");
            List<String> userNames = Arrays.asList("Test1", "Test2", "Test3");
            sendMessage(context, fakeRestaurant, userNames);
            return;
        }*/

        // Get the Workmate repository
        WorkmateRepository workmateRepository = WorkmateRepository.getInstance();

        // Get the Lunch Repository
        LunchRepository lunchRepository = LunchRepository.getInstance();

        // Get the current user
        Workmate workmate = workmateRepository.getCurrentWorkmate();
        String workmateId = workmate.getUid();

        // Check if user enabled notification
        workmateRepository.getIsNotificationEnabled().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEnabled) {
                workmateRepository.getIsNotificationEnabled().removeObserver(this);

                if (isEnabled != null && isEnabled) {
                    lunchRepository.getTodayLunch(workmateId).observeForever(new Observer<Lunch>() {
                        @Override
                        public void onChanged(Lunch lunch) {
                            lunchRepository.getTodayLunch(workmateId).removeObserver(this);

                            if (lunch != null) {
                                Restaurant restaurant = lunch.getRestaurant();
                                lunchRepository.fetchTodayWorkmatesAtRestaurant(restaurant).observeForever(new Observer<List<Workmate>>() {
                                    @Override
                                    public void onChanged(List<Workmate> workmates) {
                                        lunchRepository.fetchTodayWorkmatesAtRestaurant(restaurant).removeObserver(this);

                                        if (workmates != null) {
                                            List<String> names = new ArrayList<>();
                                            for (Workmate w : workmates) {
                                                names.add(w.getName());
                                            }
                                            sendMessage(context, restaurant, names);
                                        } else {
                                            Log.i(TAG, "Unable to get the list of the other workmates that are participants of that lunch");
                                        }
                                    }
                                });
                            } else {
                                Log.i(TAG, "Current user does not have a lunch for today");
                            }
                        }
                    });
                } else {
                    Log.i(TAG, "ALARM NOTIFICATION NOT SENT : Notification is not active");
                }
            }
        });
    }
}

