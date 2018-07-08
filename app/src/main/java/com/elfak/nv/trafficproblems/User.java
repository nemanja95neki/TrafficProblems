package com.elfak.nv.trafficproblems;

import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.net.PortUnreachableException;

@IgnoreExtraProperties
public class User {
    public String email;
    public String password;
    public String first_name;
    public String last_name;
    public String phone_number;
    public String role;
    @Exclude
    public String key;
    @Exclude
    public String imageUri;
    @Exclude
    public Uri imagePicture;

    public User() {}

    public User(String email,String password,String first_name,String last_name,String phone_number)
    {
        this.email = email;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.role = "user";
    }

    public User(String key, String email,String password,String first_name,String last_name,String phone_number)
    {
        this.key = key;
        this.email = email;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.role = "user";
    }


    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getPhone_number() {
        return phone_number;
    }
    public String get_Picture(){return imageUri;}

}
