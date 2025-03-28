package com.julien.go4lunch.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.julien.go4lunch.R;
import com.julien.go4lunch.view.tabviews.TabActivity;

import java.util.Arrays;
import java.util.List;

/**
 * Activity responsible for handling user authentication using Firebase Authentication.
 * This activity launches a Firebase Authentication sign-in intent and processes the result.
 */
public class AuthActivity extends AppCompatActivity {

    private final String TAG = "AuthActivity";
    private AuthActivity aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aa = this;

        List<AuthUI.IdpConfig> providers =
                Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(true)
                .setTheme(R.style.FirebaseAuthTheme)
                .build();
        signInLauncher.launch(signInIntent);
    }

    /**
     * Launcher for handling the result of the Firebase Authentication sign-in process.
     */
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    Log.i("Result", "" + result);
                    IdpResponse response = result.getIdpResponse();
                    if (result.getResultCode() == RESULT_OK) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Log.i(TAG, "User : " + user.getEmail());

                        showSnackBar(getString(R.string.auth_snack_bar_success));

                        Intent intent = new Intent(aa, TabActivity.class);
                        startActivity(intent);
                    } else {
                        if (response == null) {
                            showSnackBar(getString(R.string.auth_snack_bar_failed));
                        } else if (response.getError()!= null) {
                            if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK){
                                showSnackBar(getString(R.string.auth_snack_bar_internet_connection));
                            } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                                showSnackBar(getString(R.string.auth_snack_bar_unknow_error));
                            }
                        }
                    }
                }
            }
    );

    /**
     * Displays a Snackbar with the given message.
     */
    private void showSnackBar( String message){
        View view = findViewById(android.R.id.content);
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }
}





