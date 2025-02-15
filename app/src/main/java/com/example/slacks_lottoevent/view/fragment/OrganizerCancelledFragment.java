package com.example.slacks_lottoevent.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.slacks_lottoevent.Utility.DialogHelper;
import com.example.slacks_lottoevent.Utility.Notifications;
import com.example.slacks_lottoevent.model.Profile;
import com.example.slacks_lottoevent.R;
import com.example.slacks_lottoevent.viewmodel.adapter.ProfileListArrayAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * A fragment that displays a list of profiles for organizers of entrants who have been cancelled for an event.
 */
public class OrganizerCancelledFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventID";
    private ListView ListViewEntrantsCancelled;
    private String eventId;
    private FirebaseFirestore db;
    private ArrayList<Profile> profileList;

    /**
     * Default constructor
     */
    public OrganizerCancelledFragment() {
    }

    /**
     * Factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param eventId The current event's ID.
     * @return A new instance of OrganizerCancelledFragment.
     */
    public static OrganizerCancelledFragment newInstance(String eventId) {
        OrganizerCancelledFragment fragment = new OrganizerCancelledFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId); // Pass the event ID as a String
        fragment.setArguments(args);
        return fragment;
    }
    /**
     * Called when the fragment is created.
     * Initializes Firestore and retrieves the event ID from the fragment's arguments.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        profileList = new ArrayList<>(); // Initialize the profile list
        // Retrieve the event ID from the fragment's arguments
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
    }
    /**
     * Called to create the view hierarchy associated with the fragment.
     * Inflates the layout for the fragment and sets up the UI components.
     * @param inflater           The LayoutInflater object used to inflate views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_organizer_cancelled, container, false);
        ListViewEntrantsCancelled = view.findViewById(R.id.listViewEntrantsCancelled);

        ProfileListArrayAdapter adapter = new ProfileListArrayAdapter(getContext(), profileList,
                                                                      false, null, null,
                                                                      null, null);
        ListViewEntrantsCancelled.setAdapter(adapter);

        Button craftMessageButton = view.findViewById(R.id.craftMessage);
        Notifications notifications = new Notifications();

        craftMessageButton.setOnClickListener(
                v -> DialogHelper.showMessageDialog(getContext(), notifications, eventId,
                                                    "cancelledNotificationsList"));

        // Listen for real-time updates to the event document
        db.collection("events").document(eventId).addSnapshotListener((eventDoc, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening to event document updates", error);
                return;
            }

            if (eventDoc != null && eventDoc.exists()) {
                ArrayList<String> deviceIds = (ArrayList<String>) eventDoc.get("cancelled");

                if (deviceIds != null && !deviceIds.isEmpty()) {
                    profileList.clear(); // Clear the list before adding new data

                    // Listen for real-time updates to each profile document
                    for (String deviceId : deviceIds) {
                        db.collection("profiles").document(deviceId)
                          .addSnapshotListener((profileDoc, profileError) -> {
                              if (profileError != null) {
                                  Log.e("Firestore", "Error listening to profile document updates",
                                        profileError);
                                  return;
                              }

                              if (profileDoc != null && profileDoc.exists()) {
                                  Profile profile = profileDoc.toObject(Profile.class);
                                  profileList.add(
                                          profile); // Add the name if it’s not already in the list
                                  adapter.notifyDataSetChanged(); // Update the adapter
                              } else {
                                  Log.d("Firestore",
                                        "Profile document does not exist for device ID: " +
                                        deviceId);
                              }
                          });
                    }
                } else {
                    Log.d("Firestore", "No device IDs found in the waitlisted list.");
                    profileList.clear();
                    adapter.notifyDataSetChanged(); // Clear the ListView if no device IDs are found
                }
            } else {
                Log.d("Firestore", "Event document does not exist for ID: " + eventId);
                profileList.clear();
                adapter.notifyDataSetChanged(); // Clear the ListView if the event document doesn't exist
            }
        });

        return view;
    }

}
