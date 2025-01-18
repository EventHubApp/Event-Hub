package com.example.slacks_lottoevent.view;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.slacks_lottoevent.Utility.NotificationHelper;
import com.example.slacks_lottoevent.Utility.Notifications;
import com.example.slacks_lottoevent.model.Profile;
import com.example.slacks_lottoevent.R;
import com.example.slacks_lottoevent.model.User;
import com.example.slacks_lottoevent.viewmodel.EntrantViewModel;
import com.example.slacks_lottoevent.viewmodel.EventViewModel;
import com.example.slacks_lottoevent.viewmodel.OrganizerViewModel;
import com.example.slacks_lottoevent.viewmodel.ProfileViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * MainActivity serves as the main/primary activity of the application.
 * It extends BaseActivity and provides additional functionality for:
 * - Managing user profiles and event interactions
 * - Handling navigation through a BottomNavigationView and FloatingActionButton
 * - Displaying notifications and managing notification permissions
 * - Observing and updating event-related data using ViewModels
 * - Integrating with Firebase Firestore for data management
 */
public class MainActivity extends BaseActivity {
    private EntrantViewModel entrantViewModel;
    private EventViewModel eventViewModel;
    private ProfileViewModel profileViewModel;
    private OrganizerViewModel organizerViewModel;
    private User user;
    private String deviceId;
    private FirebaseFirestore db;
    private CollectionReference usersRef;

    private NotificationHelper notificationHelper;

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Sets up the main application layout, initializes ViewModels, handles navigation and notifications.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("profiles");

        super.onCreate(savedInstanceState);

        // Inflate activity_main layout into content_frame of activity_base
        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.content_frame), true);

//      Notifications for the entrant
        createNotificationChannel();
        notificationHelper = new NotificationHelper(this);
        checkAndRequestNotificationPermission();

        User.initialize(this);
        user = User.getInstance();
        deviceId = user.getDeviceId();

        // Initialize ViewModels
        entrantViewModel = new ViewModelProvider(this).get(EntrantViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        organizerViewModel = new ViewModelProvider(this).get(OrganizerViewModel.class);

        // Observe the events the entrant is involved in
        entrantViewModel.getCurrentEntrantLiveData().observe(this, entrant -> {
            if (entrant != null) {
                List<String> waitlistedIds = entrant.getWaitlistedEvents();
                List<String> unselectedIds = entrant.getUninvitedEvents();
                List<String> invitedIds = entrant.getInvitedEvents();
                List<String> attendingIds = entrant.getFinalistEvents();
                eventViewModel.updateEventLists(waitlistedIds, unselectedIds, invitedIds,
                                                attendingIds);
            } else {
                // Clear all event lists
                eventViewModel.updateEventLists(null, null, null, null);
            }
        });

        // Observe the events the organizer is involved in
        organizerViewModel.getCurrentOrganizerLiveData().observe(this, organizer -> {
            if (organizer != null) {
                List<String> eventIds = organizer.getEvents();
                eventViewModel.updateOrganizerEvents(eventIds);
            } else {
                // Clear all event lists
                eventViewModel.updateOrganizerEvents(null);
            }
        });

        // Set up the Toolbar
        setSupportActionBar(findViewById(R.id.top_app_bar));

        // Set up the BottomNavigationView and FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fabQR);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up the NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_main);
        NavController navController = navHostFragment.getNavController();

        // Set up NavigationUI for the BottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Set a click listener for BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navController.navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.nav_manage) {
                navController.navigate(R.id.manageFragment);
                return true;
            } else if (itemId == R.id.nav_inbox) {
                navController.navigate(R.id.invitesFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                navController.navigate(R.id.profileFragment);
                return true;
            }
            return false;
        });

        // Set a click listener for the FAB
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Observes the current profile and updates the menu items accordingly.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    /**
     * Prepares the menu items based on the current profile.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Iterate through all menu items and make them visible
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            // if the item is not the admin item, make it visible
            if (item.getItemId() != R.id.action_admin)
                item.setVisible(true); // Set visibility to tru
            profileViewModel.getCurrentProfileLiveData().observe(this, profile -> {
                if (profile != null) {
                    if (profile.getAdmin()) {
                        item.setVisible(true);
                    }
                    if (item.getItemId() != R.id.action_admin) {
                        if (profile.getAdminNotifications()) {
                            item.setIcon(R.drawable.baseline_notifications_active_24);
                        } else {
                            item.setIcon(R.drawable.baseline_notifications_off_24);
                        }
                    }
                }
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handles the selection of menu items.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("MainActivity", "Menu item selected: " + item.getItemId());
        int itemId = item.getItemId();
        if (itemId == R.id.action_notifications) {
            Profile profile = profileViewModel.getCurrentProfileLiveData().getValue();
            if (profile != null) {
                profile.setAdminNotifications(!profile.getAdminNotifications());
                profileViewModel.updateProfile(profile);
                if (profile.getAdminNotifications()) {
                    item.setIcon(R.drawable.baseline_notifications_active_24);
                } else {
                    item.setIcon(R.drawable.baseline_notifications_off_24);
                }
            }
            return true;
        } else if (itemId == R.id.action_admin) {
            // Handle Admin click
            Log.d("MainActivity", "Admin clicked");
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles the navigation up action.
     * For the back button
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = ((NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_main))
                .getNavController();
        return NavigationUI.navigateUp(navController, (AppBarConfiguration) null) ||
               super.onSupportNavigateUp();
    }

    /**
     * Creates a notification channel for sending notifications.
     * This is only applicable for Android O (API 26) and above.
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use the appropriate context method
            String channelId = getString(R.string.channel_id); // ensure channel ID is properly defined in strings.xml
            CharSequence name = getString(R.string.channel_name); // name of the channel
            String description = getString(R.string.channel_description); // description of the channel
            int importance = NotificationManager.IMPORTANCE_DEFAULT; // adjust as necessary

            // Create the NotificationChannel
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Fetches notifications for the current user from Firestore based on device id and sends them.
     *
     * @param deviceId The unique device ID associated with the user.
     * */
    private void grabbingNotifications(String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notifications").whereEqualTo("userId", deviceId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String title = document.getString("title");
                            String messageContent = document.getString("message");
                            notificationHelper.sendNotifications(deviceId, title, messageContent);
                            Log.d("Notis", "Sent");
                        }
                    }
                    new Notifications().removeNotifications(deviceId);
                });
    }

    /**
     * Checks and requests notification permissions.
     * If the permission is already granted, starts fetching notifications.
     */
    private void checkAndRequestNotificationPermission() {
        SharedPreferences sharedPreferences = getSharedPreferences("SlacksLottoEventUserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Boolean hasAsked = sharedPreferences.getBoolean("hasAskedNotifcations", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED && (!hasAsked)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                editor.putBoolean("hasAskedNotifcations", true);
                editor.apply();
            }
            else {
                startFetchingNotifications();
            }
        }
        else {
            startFetchingNotifications();
        }
    }

    /**
     * Handles the result of the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SharedPreferences sharedPreferences = getSharedPreferences("SlacksLottoEventUserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startFetchingNotifications();
                editor.putBoolean("notificationsEnabled", true);
                editor.apply();

            } else {
                editor.putBoolean("notificationsEnabled", false);
                editor.apply();

            }
        }
    }

    /**
     * Starts fetching notifications for the current user.
     */
    private void startFetchingNotifications() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        grabbingNotifications(deviceId);

        Notifications notification = new Notifications();
        notification.removeNotifications(deviceId);
    }
}
