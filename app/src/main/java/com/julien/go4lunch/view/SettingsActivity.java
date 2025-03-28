package com.julien.go4lunch.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.julien.go4lunch.R;
import com.julien.go4lunch.viewmodel.MyViewModel;
import com.julien.go4lunch.viewmodel.ViewModelFactory;

public class SettingsActivity extends AppCompatActivity {

    // UTILS
    private SettingsActivity sa;
    private final String TAG = "SettingsActivity";
    private boolean notificationCurrentState;

    // VIEWMODEL
    private MyViewModel viewModel;

    // VIEWS
    private SwitchMaterial notificationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize utility references
        sa = this;
        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MyViewModel.class);

        // Views
        notificationSwitch = findViewById(R.id.notificationSwitch);

        // Configure switch
        configureSwitchStateAndListener();
    }

    /**
     * Configures the notification switch state and its listener in the settings activity.
     */
    private void configureSwitchStateAndListener() {
        viewModel.getIsNotificationEnabled().observe(sa, isNotificationEnabled -> {
            if (isNotificationEnabled != null) {
                Log.i(TAG, "Is notification enabled ? " + isNotificationEnabled);
                notificationCurrentState = isNotificationEnabled;
                notificationSwitch.setChecked(isNotificationEnabled);
            } else {
                notificationSwitch.setChecked(false);
                Log.i(TAG, "Failed to get isNotificationEnabled - null");
            }

            notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isActive) {
                    if (notificationCurrentState != isActive) {
                        notificationCurrentState = isActive;
                        viewModel.createOrUpdateWorkmate(isActive);
                    }
                }
            });
        });
    }
}