package com.example.slacks_lottoevent.view;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.slacks_lottoevent.model.Profile;
import com.example.slacks_lottoevent.R;
import com.example.slacks_lottoevent.databinding.SignUpActivityBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/*
 *
 *
 * Relevant Documentation
 * https://developer.android.com/reference/android/util/Patterns#EMAIL_ADDRESS
 * https://developer.android.com/reference/android/util/Patterns#PHONE
 * https://developer.android.com/reference/java/util/regex/Pattern
 * https://stackoverflow.com/questions/48705124/how-can-i-create-a-unique-key-and-use-it-to-send-data-in-firebase
 * https://github.com/google/gson
 * */

/**
 * SignUpActivity is the Activity that allows the user to sign up for the.
 * It takes in the user's name, email, and phone number and validates the inputs.
 * If the inputs are valid, it saves the user's information to the device and to the Firebase Firestore database.
 */
public class SignUpActivity extends BaseActivity {

    CollectionReference usersRef;
    EditText nameInput;
    EditText emailInput;
    EditText phoneInput;
    private FirebaseFirestore db;
    private SignUpActivityBinding binding;
    private String name;
    private String email;
    private String phoneNumber;

    /**
     * onCreate is called when the activity is starting.
     * It sets the content view to the activity_sign_up layout.
     * It initializes the Firebase Firestore database and the usersRef CollectionReference.
     * It initializes the nameInput, emailInput, and phoneInput EditTexts.
     * It sets an onClickListener on the signUpButton.
     * When the signUpButton is clicked, it validates the user's inputs.
     * If the inputs are valid, it saves the user's information to the device and to the Firebase Firestore database.
     * It then displays a toast message saying "Sign-Up Successful" and finishes the activity.
     *
     * @param savedInstanceState a Bundle object containing the activity's previously saved state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate SignUpActivity layout into BaseActivity's content_frame
        binding = SignUpActivityBinding.inflate(getLayoutInflater());
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.addView(binding.getRoot()); // Add SignUpActivity's root layout

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Set up the app bar for back navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
            getSupportActionBar().setTitle("Sign Up"); // Set a custom title if needed
        }

        usersRef = db.collection("profiles");

        nameInput = binding.nameInput;
        emailInput = binding.emailInput;
        phoneInput = binding.phoneInput;

        Button signUpBtn = binding.signUpButton;
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    Toast.makeText(SignUpActivity.this, "Sign-Up Successful", Toast.LENGTH_SHORT)
                         .show();

                    // Inserting the info Device and DB
                    saveUserInfoToDevice();
                    saveUserInfoToFirebase();
                    finish(); // Closing the SignUpActivity to prevent any possible other Activity navigating back to it.
                    // Will just return to whatever the previous activity was
                }
            }
        });
    }

    /**
     * Validates the user's input for name, email, and phone number.
     * If the input is invalid, it sets an error message on the input field.
     *
     * @return true if the input is valid, false otherwise.
     */
    private boolean validateInputs() {
        name = binding.nameInput.getText().toString().trim();
        email = binding.emailInput.getText().toString().trim();
        phoneNumber = binding.phoneInput.getText().toString().trim();
        System.out.println(phoneNumber);
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return false;
        }

        // Validating the email to ensure it is not empty and matches email pattern
        // Patterns.EMAIL_ADDRESS is a regex pattern and we are using matcher() to apply the regex to the field.
        // matches() checks if the pattern is fully matched.
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return false;
        }

        // Validating phone number similarly to email
        if (!TextUtils.isEmpty(phoneNumber)) {
            if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
                phoneInput.setError("Phone number should only contain numbers");
                phoneInput.requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * Saves the user's information to the device's SharedPreferences.
     */
    private void saveUserInfoToDevice() {
        SharedPreferences sharedPreferences = getSharedPreferences("SlacksLottoEventUserInfo",
                                                                   MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("userName", nameInput.getText().toString().trim());
        editor.putString("userEmail", emailInput.getText().toString().trim());
        editor.putString("userPhone", phoneInput.getText().toString().trim());
        editor.putBoolean("isSignedUp",
                          true); // Mark the user as signed up so MainActivity can check this.
        editor.apply(); // Saving changes to sharedPreferences
    }

    /**
     * Saves the user's information to the Firebase Firestore database.
     */
    void saveUserInfoToFirebase() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);
        Profile userInfo = new Profile(name, phone, email, deviceId, getApplicationContext());
        System.out.println(usersRef);
        usersRef.document(deviceId).set(userInfo)
                .addOnSuccessListener(nothing -> {
                    System.out.println("Added to DB");
                })
                .addOnFailureListener(nothing -> {
                    System.out.println("failed");
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button click
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
