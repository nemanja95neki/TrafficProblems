package com.elfak.nv.trafficproblems;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    public String email;
    public String password;
    public String first_name;
    public String last_name;
    public String phone_number;
    @Exclude
    public String key;

    public User() {}

    public User(String email,String password,String first_name,String last_name,String phone_number)
    {
        this.email = email;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
    }

}
