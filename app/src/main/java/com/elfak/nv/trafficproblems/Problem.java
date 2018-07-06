package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.widget.ImageButton;

import com.google.firebase.database.Exclude;

public class Problem {
    public String problemName;
    public String problemDescription;
    public String longitude;
    public String latitude;
    public Integer priority;
    public String userId;
    @Exclude
    public String key;

    public Problem(){}


    public Problem(String problemName, String problemDescription, Integer priority,String longitude, String latitude, String userId)
    {
        this.problemName=problemName;
        this.problemDescription=problemDescription;
        this.longitude=longitude;
        this.latitude=latitude;
        this.userId=userId;
        this.priority=priority;
    }
}