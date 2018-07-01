package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private UserLocalStore userLocalStore;
    private DatabaseReference databaseReference;
    private User user;
    EditText editName,editLastName,editEmail,editPassword,editPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.includeMainView).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityProfile).setVisibility(View.INVISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userLocalStore = new UserLocalStore(this);
        user = userLocalStore.getLoggedInUser();

        editName = (EditText)findViewById(R.id.editName);
        editLastName = (EditText)findViewById(R.id.editLastName);
        editEmail = (EditText)findViewById(R.id.editEmail);
        editPassword = (EditText)findViewById(R.id.editPassword);
        editPhoneNumber = (EditText)findViewById(R.id.editPhoneNumber);
        ImageButton saveButton = (ImageButton)findViewById(R.id.save);

        editName.setText(user.first_name);
        editLastName.setText(user.last_name);
        editEmail.setText(user.email);
        editPassword.setText(user.password);
        editPhoneNumber.setText(user.phone_number);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String last_name = editLastName.getText().toString();
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                String phone_number = editPhoneNumber.getText().toString();
                User changedUser = new User(user.key,email,password,name,last_name,phone_number);
                databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("users").child(changedUser.key).setValue(changedUser);
                userLocalStore.clearUserData();
                userLocalStore.storeUserData(changedUser);
                userLocalStore.setUserLoggedIn(true);
                Intent data = new Intent();
                data.putExtra("name",changedUser.first_name);
                data.putExtra("last_name",changedUser.last_name);
                data.putExtra("email",changedUser.email);
                data.putExtra("password",changedUser.password);
                data.putExtra("phone_number",changedUser.phone_number);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
