package com.elfak.nv.trafficproblems;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceTransitionsIntentService  extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * //@param name Used to name the worker thread, important only for debugging.
     */


    public GeofenceTransitionsIntentService() {
        super("Display");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails =  GeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);

        } else {
            // Log the error.
        }
    }

    private void sendNotification(String geofenceTransitionDetails) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.map_icon)
                .setContentTitle("Nova notifikacija")
                .setContentText(geofenceTransitionDetails)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define

       mBuilder.notify();
    }

    private String GeofenceTransitionDetails(GeofenceTransitionsIntentService geofenceTransitionsIntentService, int geofenceTransition, List<Geofence> triggeringGeofences) {
        String message="";
        if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER)
            message+="Blizu je problem";
        else if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT)
                message+="Udaljavate se od problema";
        return message;
    }

}