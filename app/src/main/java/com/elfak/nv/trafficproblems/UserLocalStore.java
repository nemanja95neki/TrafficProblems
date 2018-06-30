package com.elfak.nv.trafficproblems;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalStore {
    public static final String SP_NAME = "userDetails";

    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(SP_NAME, 0);
    }

    public void storeUserData(User user) {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putString("key", user.key);
        userLocalDatabaseEditor.putString("name", user.first_name);
        userLocalDatabaseEditor.putString("last_name", user.last_name);
        userLocalDatabaseEditor.putString("email", user.email);
        userLocalDatabaseEditor.putString("phone_number", user.phone_number);
        userLocalDatabaseEditor.putString("password", user.password);
        userLocalDatabaseEditor.commit();
    }

    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putBoolean("loggedIn", loggedIn);
        userLocalDatabaseEditor.commit();
    }

    public void clearUserData() {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.clear();
        userLocalDatabaseEditor.commit();
    }

    public User getLoggedInUser() {
        if (userLocalDatabase.getBoolean("loggedIn", false) == false) {
            return null;
        }
        String key = userLocalDatabase.getString("key","");
        String name = userLocalDatabase.getString("name", "");
        String last_name = userLocalDatabase.getString("last_name", "");
        String email = userLocalDatabase.getString("email", "");
        String phone_number = userLocalDatabase.getString("phone_number", "");
        String password = userLocalDatabase.getString("password", "");

        User user = new User(key, email, password, name, last_name,phone_number);
        return user;
    }
}
