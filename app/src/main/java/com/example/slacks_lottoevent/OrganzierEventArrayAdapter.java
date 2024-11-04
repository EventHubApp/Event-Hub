package com.example.slacks_lottoevent;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Custom ArrayAdapter for displaying events in the OrganizerEventsActivity
 */
public class OrganzierEventArrayAdapter extends ArrayAdapter<Event> implements Serializable {
    private Context context;
    private ArrayList<Event> events;

    /**
     * Constructor for the OrganzierEventArrayAdapter
     *
     * @param context The context of the activity
     * @param events  The list of events to display
     */
    public OrganzierEventArrayAdapter(@NonNull Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Event event = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_organizer_events, parent, false);
        }

        ImageView qrCode = convertView.findViewById(R.id.qr_code_image);
        String qrData = event.getQRData();


        if (qrData != null && !qrData.isEmpty()) {
            try {
                BitMatrix bitMatrix = deserializeBitMatrix(qrData); // Convert back to BitMatrix
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(bitMatrix); // Create Bitmap from BitMatrix
                qrCode.setImageBitmap(bitmap); // Set the QR code image
            } catch (WriterException e) {
                Log.e("QRCodeError", "Error converting QR code string to BitMatrix");
            }
        } else {
            qrCode.setImageBitmap(null); // Clear the image if QR data is null or empty
        }


        TextView eventName = convertView.findViewById(R.id.event_name);
        TextView eventDate = convertView.findViewById(R.id.event_date);
        TextView eventTime = convertView.findViewById(R.id.event_time);
//        TODO: Fix this
        TextView eventAddress = convertView.findViewById(R.id.event_address);
        TextView eventDescription = convertView.findViewById(R.id.event_description);
        eventName.setText(event.getName());
        eventDate.setText(event.getDate());
        eventTime.setText(event.getTime());
//        TODO: get the facility from the user we are at and display it there
//        if (event.getFacility() != null) {
//            eventAddress.setText(
//                    event.getFacility().getStreetAddress1() + " " +
//                            event.getFacility().getStreetAddress2() + ", " +
//                            event.getFacility().getCity() + ", " +
//                            event.getFacility().getProvince() + ", " +
//                            event.getFacility().getCountry() + " " +
//                            event.getFacility().getPostalCode()
//            );

//        } else {
//            eventAddress.setText("No Address Available");
//        }
        eventAddress.setText("Need to grab facility");
        eventDescription.setText(event.getDescription());
        return convertView;
    }

    /**
     * Deserializes a BitMatrix from a string
     *
     * @param data The string to deserialize
     * @return The deserialized BitMatrix
     * @throws WriterException If the string cannot be deserialized
     */
    private BitMatrix deserializeBitMatrix(String data) throws WriterException {
        String[] lines = data.split("\n");
        int width = lines[0].length();
        int height = lines.length;
        BitMatrix bitMatrix = new BitMatrix(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Set pixel based on whether the character is '1' (black) or '0' (white)
                if (lines[y].charAt(x) == '1') {
                    bitMatrix.set(x, y); // Set pixel to black
                }
            }
        }
        return bitMatrix;
    }
}