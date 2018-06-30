package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

public class Profile extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private TextView name, last_name, email, phone_number;
    private ImageButton editButton;
    private String userID;
    private UserLocalStore userLocalStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();
        if (user == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        userID = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        name = (TextView)findViewById(R.id.name);
        last_name = (TextView)findViewById(R.id.last_name);
        email = (TextView)findViewById(R.id.email_address);
        phone_number = (TextView)findViewById(R.id.phone_number);
        editButton = (ImageButton)findViewById(R.id.edit);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent editProfile = new Intent(Profile.this,)
            }
        });
        userLocalStore = new UserLocalStore(this);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userInfo = dataSnapshot.child(userID).getValue(User.class);
                if (userInfo != null){
                    name.setText("Name: " + userInfo.first_name);
                    last_name.setText("Last name: " + userInfo.last_name);
                    email.setText("Email: " + userInfo.email);
                    phone_number.setText("Phone number: " + userInfo.phone_number);
                }

                /*userInfo = dataSnapshot.getValue(User.class);
                userInfo.key = dataSnapshot.getKey();
                userLocalStore.storeUserData(userInfo);
                userLocalStore.setUserLoggedIn(true);*/

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(ProfileActivity.this,"Something went wrong. Please try again...",Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

    }

}
