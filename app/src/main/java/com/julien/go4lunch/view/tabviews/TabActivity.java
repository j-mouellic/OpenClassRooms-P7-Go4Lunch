package com.julien.go4lunch.view.tabviews;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.auth.AuthUI;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.julien.go4lunch.R;
import com.julien.go4lunch.model.bo.Restaurant;
import com.julien.go4lunch.model.bo.Workmate;
import com.julien.go4lunch.model.seed.Seeder;
import com.julien.go4lunch.utils.AlarmReceiver;
import com.julien.go4lunch.view.AuthActivity;
import com.julien.go4lunch.view.DetailsActivity;
import com.julien.go4lunch.view.SettingsActivity;
import com.julien.go4lunch.viewmodel.MyViewModel;
import com.julien.go4lunch.viewmodel.ViewModelFactory;

import java.util.Calendar;

/**
 * This activity serves as the central hub of the Go4Lunch application, allowing users to search for
 * restaurants to dine at with their colleagues. It features a BottomNavigationView for navigation
 * between three main fragments:
 *
 * 1. **WorkmatesFragment**: Displays a list of colleagues and their chosen restaurants.
 * 2. **MapFragment**: Shows a map with nearby restaurants and highlights the ones chosen by colleagues.
 * 3. **RestaurantListFragment**: Provides a list view of restaurants with detailed information.
 */
public class TabActivity extends AppCompatActivity {

    // NOTIFICATION VARIABLES
    public static final CharSequence CHANNEL_NAME = "Lunch Notification";
    public static final String CHANNEL_ID = "1";
    public static boolean NOTIFICATION_DEBUG = true;

    // UTILS
    private TabActivity ta;
    private final String TAG = "TabActivity";

    // FRAGMENTS
    private WorkmatesFragment workmatesFragment;
    private MapsFragment mapsFragment;
    private RestaurantListFragment restaurantListFragment;
    private Fragment currentFragment;

    // VIEWS
    private DrawerLayout drawerLayout;
    public Toolbar toolbar;
    public AutocompleteSupportFragment acsf;
    public TextInputLayout inputLayout;
    public TextInputEditText workmatesSearchInput;

    // VIEWMODEL
    private MyViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Seeder.initDbSeeding(this);

        // Initialize utility references and fragments
        ta = this;
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MyViewModel.class);
        workmatesFragment = new WorkmatesFragment(viewModel);
        mapsFragment = new MapsFragment();
        restaurantListFragment = new RestaurantListFragment();

        // Views
        acsf = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        acsf.getView().setVisibility(View.INVISIBLE);
        toolbar = findViewById(R.id.toolbar);
        inputLayout = findViewById(R.id.inputLayout);
        workmatesSearchInput = findViewById(R.id.workmatesSearchInput);

        // Configure UI components
        configureToolBar();
        configureDrawerLayout();
        configureBottomNavigationBar();

        // GPS Location permissions
        askForPermission();

        //
        configureAlarm();
    }

    /**
     * Ask access for fine location & coarse location
     */
    private void askForPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS},
                0
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.POST_NOTIFICATIONS.equals(permissions[i])) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        viewModel.createOrUpdateWorkmate(true);
                    }
                }
            }
        }
    }

    /**
     * Inflate menu ressource and manage click action on search icon
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_nav_bar, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);

        searchItem.setOnMenuItemClickListener(view -> {
            toolbar.setVisibility(View.INVISIBLE);
            if (currentFragment instanceof WorkmatesFragment) {
                acsf.getView().setFocusable(false);
                acsf.getView().setClickable(false);
                inputLayout.setVisibility(View.VISIBLE);
            } else {
                acsf.getView().setVisibility(View.VISIBLE);
            }
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Configures the toolbar to act as the app bar for this activity.
     */
    private void configureToolBar() {
        toolbar.setTitle(getString(R.string.nav_bar_title_restaurant));
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
    }

    /**
     * Configures the DrawerLayout for additional navigation or options.
     * Includes a menu icon to open the drawer programmatically.
     */
    private void configureDrawerLayout() {
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(ta, drawerLayout, R.string.nav_open, R.string.nav_close);

        // Get drawerLayout views
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.user_name);
        TextView userEmail = headerView.findViewById(R.id.user_email);
        TextView userAvatar = headerView.findViewById(R.id.user_avatar);

        // Get current workmate
        Workmate workmate = viewModel.getCurrentWorkmate();

        // Set User Avatar
        String workmateName = workmate.getName();
        String firstLetter = workmateName.substring(0, 1);
        userAvatar.setText(firstLetter);
        String hexColor = Workmate.getRandomHexColorFromPalette();
        userAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hexColor)));

        // Set UserName && UserEmail
        userName.setText(workmate.getName());
        userEmail.setText(workmate.getEmail());

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Enable click action on toolbar's menu burger icon
        toolbar.setNavigationOnClickListener(view -> {
            drawerLayout.open();
        });

        // Set item click listener for drawer menu
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_lunch) {
                viewModel.getTodayLunch(viewModel.getCurrentWorkmate().getUid()).observe(ta, lunch -> {
                    if (lunch != null) {
                        Restaurant chosenRestaurant = lunch.getRestaurant();

                        Intent intent = new Intent(ta, DetailsActivity.class);
                        intent.putExtra("RESTAURANT", chosenRestaurant);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ta, "NO LUNCH PLANNED", Toast.LENGTH_LONG).show();
                    }
                });
            } else if (item.getItemId() == R.id.nav_settings) {
                Intent intent = new Intent(ta, SettingsActivity.class);
                startActivity(intent);
            } else {
                signOutUser();
            }
            return true;
        });
    }

    /**
     * Signs out the currently logged-in user using Firebase AuthUI.
     * If the sign-out is successful, redirects the user to the AuthActivity.
     * If the sign-out fails, displays an error message using a Snackbar.
     */
    private void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(ta, AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        View view = findViewById(android.R.id.content);
                        Snackbar.make(view, getString(R.string.logout_snack_bar), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Configures the BottomNavigationView to navigate between fragments.
     * Allows users to switch between:
     * - Map view
     * - Restaurant list
     * - Workmates view
     */
    private void configureBottomNavigationBar() {
        BottomNavigationView navBar = findViewById(R.id.bottomNavBar);
        navBar.setSelectedItemId(R.id.map); // Default to the map view
        currentFragment = mapsFragment;
        replaceFragment(mapsFragment);

        navBar.setOnItemSelectedListener(item -> {

            resetTopBarViews();

            if (item.getItemId() == R.id.map) {
                replaceFragment(mapsFragment);
                currentFragment = mapsFragment;
                acsf.setHint(getString(R.string.hint_input_restaurant));
                toolbar.setTitle(getString(R.string.nav_bar_title_restaurant));
                return true;
            } else if (item.getItemId() == R.id.list) {
                replaceFragment(restaurantListFragment);
                currentFragment = restaurantListFragment;
                acsf.setHint(getString(R.string.hint_input_restaurant));
                toolbar.setTitle(getString(R.string.nav_bar_title_restaurant));
                return true;
            } else {
                replaceFragment(workmatesFragment);
                currentFragment = workmatesFragment;
                acsf.setHint(getString(R.string.hint_input_workmates));
                toolbar.setTitle(getString(R.string.nav_bar_title_workmates));
                return true;
            }
        });
    }

    /**
     * Reset toolbar visibility when fragment view change
     */
    public void resetTopBarViews() {
        acsf.getView().setVisibility(View.INVISIBLE);
        inputLayout.setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.VISIBLE);
    }

    /**
     * Replaces the current fragment in the FrameLayout container.
     */
    private void replaceFragment(Fragment fragment) {
        runOnUiThread(() -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit();
        });
    }


    /**
     * Configures a daily alarm to trigger notifications using the AlarmManager.
     *
     * This method sets up a notification channel (required for Android O and above)
     * and schedules an alarm that sends a broadcast to the `AlarmReceiver` at a specified time
     * every day. The time is set to 12:00 PM by default but can be adjusted for debugging purposes.
     *
     * Debug mode (`NOTIFICATION_DEBUG`) can be used to trigger the alarm 10 seconds from the current time.
     */
    private void configureAlarm() {

        // Create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Log.d(TAG, "configureAlarm: Creating notification channel");

            // Create the NotificationChannel
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, // id
                    CHANNEL_NAME, // name
                    NotificationManager.IMPORTANCE_HIGH // importance
            );

            // Register the channel with the system
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        // Get calendar instance to day
        Calendar calendar = Calendar.getInstance();

        if (!NOTIFICATION_DEBUG) {
            // Set the alarm to start at 12:00
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        // Create an Intent to broadcast to the AlarmReceiver
        Intent intent = new Intent(this, AlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, // context
                0, // no need to request code
                intent, // intent to be triggered
                PendingIntent.FLAG_IMMUTABLE // PendingIntent flag
        );

        // Get the AlarmManager service
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to repeat every day
        // Warning : the alarm is not exact, it can be delayed by the system up to few minutes
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, // alarm type
                NOTIFICATION_DEBUG ? System.currentTimeMillis() + 10000 : calendar.getTimeInMillis(), // time to start
                AlarmManager.INTERVAL_DAY, // interval
                pendingIntent // pending intent
        );

    }
}