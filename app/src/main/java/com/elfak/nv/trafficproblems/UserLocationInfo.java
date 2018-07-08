package com.elfak.nv.trafficproblems;

public class UserLocationInfo {
    public double latitude;
    public double longitude;
    public String id;

    public UserLocationInfo(double latitude, double longitude, String id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }

    public UserLocationInfo (){
    }
}