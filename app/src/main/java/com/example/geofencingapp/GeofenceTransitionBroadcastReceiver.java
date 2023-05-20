package com.example.geofencingapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            // Handle the error if there is any
            int errorCode = geofencingEvent.getErrorCode();
            Log.e("Geofence", "Geofence transition error: " + errorCode);
            return;
        }

        // Get the transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Check the transition type
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Handle geofence enter transition
            Log.d("Geofence", "Geofence enter transition");
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Handle geofence exit transition
            Log.d("Geofence", "Geofence exit transition");
        }
    }
}