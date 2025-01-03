package com.example.slacks_lottoevent.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.slacks_lottoevent.view.fragment.AdminEventsFragment;
import com.example.slacks_lottoevent.view.fragment.AdminFacilitiesFragment;
import com.example.slacks_lottoevent.view.fragment.AdminImagesFragment;
import com.example.slacks_lottoevent.view.fragment.AdminProfilesFragment;
import com.example.slacks_lottoevent.R;
import com.google.android.material.tabs.TabLayout;
/**
 * This class is responsible for creating and manage the AdminActivity which in turn is responsible
 * for managing and creating fragments within this Activity that allow Admin users to perform various
 * admin functionalities. Uses a tab system so admin users can switch between different fragments.
 *
 * */
public class AdminActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private TabLayout tabLayout;

    /**
     * Creates a Customized Dialog box
     */
    public static void showAdminAlertDialog(Context context, Runnable onConfirmAction, String Title,
                                            String Message, String Terms,
                                            String CancelText, String ConfirmText,
                                            String PictureURL) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_profile_details, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView DialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView DialogMessage = dialogView.findViewById(R.id.dialog_message);
        TextView DialogTerms = dialogView.findViewById(R.id.dialog_terms);
        Button cancelBtn = dialogView.findViewById(R.id.cancel_button);
        Button confirmBtn = dialogView.findViewById(R.id.confirm_button);
        ImageView Picture = dialogView.findViewById(R.id.Picture);

        if (Title == null || Title.isEmpty()) {
            DialogTitle.setVisibility(View.GONE);
        } else {
            DialogTitle.setText(Title); // Optionally set the text if not null or empty
        }

        if (Message == null || Message.isEmpty()) {
            DialogMessage.setVisibility(View.GONE);
        } else {
            DialogMessage.setText(Message); // Optionally set the text if not null or empty
        }

        if (Terms == null || Terms.isEmpty()) {
            DialogTerms.setVisibility(View.GONE);
        } else {
            DialogTerms.setText(Terms); // Optionally set the text if not null or empty
        }

        if (CancelText == null || CancelText.isEmpty()) {
            cancelBtn.setVisibility(View.GONE);
        } else {
            cancelBtn.setText(CancelText); // Optionally set the text if not null or empty
            cancelBtn.setOnClickListener(view -> dialog.dismiss());
        }

        if (ConfirmText == null || ConfirmText.isEmpty()) {
            confirmBtn.setVisibility(View.GONE);
        } else {
            if (ConfirmText != null) {
                confirmBtn.setText(ConfirmText); // Optionally set the text if not null or empty
            }
            confirmBtn.setOnClickListener(view -> {
                // Execute the confirm action (e.g., request geolocation permission)
                if (onConfirmAction != null) {
                    onConfirmAction.run();
                }
                dialog.dismiss();
            });
        }

        if (PictureURL != null && !PictureURL.isEmpty()) {
            Glide.with(context)
                 .load(PictureURL)
                 .placeholder(R.drawable.placeholder_image)
                 .error(R.drawable.error_image)
                 .into(Picture);
        } else {
            Picture.setVisibility(View.GONE);
        }

        dialog.show();
    }

    /**
     * This method is called when the activity is first created.
     * It sets up the tab layout for the organizer to view the waitlist, invited, cancelled, and enrolled users.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

//        onBackPressed();

        frameLayout = findViewById(R.id.FrameLayoutAdmin);
        tabLayout = findViewById(R.id.tab_Layout_Admin);

        getSupportFragmentManager().beginTransaction().replace(R.id.FrameLayoutAdmin,
                                                               AdminEventsFragment.newInstance()) //AdminEventsFragment()
                                   .addToBackStack(null)
                                   .commit();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selected_fragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        selected_fragment = AdminEventsFragment.newInstance();
                        break;
                    case 1:
                        selected_fragment = AdminImagesFragment.newInstance();
                        break;
                    case 2:
                        selected_fragment = AdminFacilitiesFragment.newInstance();
                        break;
                    case 3:
                        selected_fragment = AdminProfilesFragment.newInstance();
                        break;
                }
                if (selected_fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                                               .replace(R.id.FrameLayoutAdmin, selected_fragment)
                                               .setTransition(
                                                       FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                               .commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        ImageView back = findViewById(R.id.back_button2);
        back.setOnClickListener(v -> {
            onBackPressed();
            onBackPressed();
        });

    }

}
