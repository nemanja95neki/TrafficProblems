package com.elfak.nv.trafficproblems;

import com.google.firebase.database.Exclude;

import java.security.Timestamp;
import java.sql.Time;

public class Comment {
    public String text;
    public String userId;
    //public String time;
    Long time;

    @Exclude
    public String key;
    public Comment(){}

    public Comment(String commentText, String userId)
    {
        text=commentText;
        this.userId=userId;
        time= System.currentTimeMillis();
    }


}
