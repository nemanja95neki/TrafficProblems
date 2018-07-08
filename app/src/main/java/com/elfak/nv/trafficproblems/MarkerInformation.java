package com.elfak.nv.trafficproblems;

import android.net.Uri;

public class MarkerInformation {
    public double latitude,longitude;
    public String title;
    public Uri imageUri;
    public boolean isUser;

    public MarkerInformation(){

    }

    public MarkerInformation(double latitude, double longitude, String title) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }

}