package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import android.support.annotation.NonNull;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private String userID;
    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private User userInfo;
    private UserLocalStore userLocalStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        emailField = (EditText) findViewById(R.id.email);
        passwordField = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login);
        userLocalStore = new UserLocalStore(this);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent i = new Intent(LoginActivity.this, NavDrawerMain.class);
                    startActivity(i);
                }
            }


        };
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void startSignIn()
    {

        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            if(TextUtils.isEmpty(email)){
                //email is empty
                Toast.makeText(this,"Please enter email",Toast.LENGTH_SHORT).show();
                //stoping the function from execution further
                return;
            }
            if(TextUtils.isEmpty(password)){
                //password is empty
                Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
                //stoping the function from execution further
                return;
            }

        }
        else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent profile = new Intent(LoginActivity.this,NavDrawerMain.class);
                                startActivity(profile);
                                //Toast.makeText(LoginActivity.this,"Successfuly logged!",Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(LoginActivity.this,"Please check your email and password and try again!",Toast.LENGTH_LONG).show();

                            }

                        }
                    });
        }
    }

}
