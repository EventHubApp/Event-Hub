package com.example.slacks_lottoevent.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.slacks_lottoevent.database.EntrantDB;
import com.example.slacks_lottoevent.Entrant;
import com.example.slacks_lottoevent.model.User;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;

public class EntrantViewModel extends ViewModel {
    private final MutableLiveData<HashMap<String, Entrant>> entrantsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Entrant> currentEntrantLiveData = new MutableLiveData<>();
    private final EntrantDB entrantDB;
    private final User user = User.getInstance();

    public EntrantViewModel() {
        entrantDB = EntrantDB.getInstance();
        entrantDB.startListening();
    }

    private void updateEntrants(HashMap<String, Entrant> updatedEntrants) {
        entrantsLiveData.setValue(updatedEntrants);
        updateCurrentEntrant();
    }

    public LiveData<HashMap<String, Entrant>> getEntrantsLiveData() {
        return entrantsLiveData;
    }

    public LiveData<Entrant> getCurrentEntrantLiveData() {
        return currentEntrantLiveData;
    }

    private void updateCurrentEntrant() {
        HashMap<String, Entrant> entrants = entrantsLiveData.getValue();
        if (entrants != null) {
            Entrant currentEntrant = entrants.get(user.getDeviceId());
            currentEntrantLiveData.setValue(currentEntrant);
        }
    }
}