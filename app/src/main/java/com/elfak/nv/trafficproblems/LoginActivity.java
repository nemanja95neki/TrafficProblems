package com.elfak.nv.trafficproblems;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import android.support.annotation.NonNull;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField;
    private EditText passwordField;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        emailField = (EditText)findViewById(R.id.email);
        passwordField = (EditText)findViewById(R.id.password);
        loginButton = (Button)findViewById(R.id.login);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    //Intent loggedIn = new Intent(LoginActivity.class, Main.class);
                    //startActivity(loggedIn);
                    Toast.makeText(LoginActivity.this,"COOL!",Toast.LENGTH_LONG).show();

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
            Toast.makeText(LoginActivity.this,"Fields are empty!",Toast.LENGTH_LONG).show();

        }
        else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,"Successfuly logged!",Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(LoginActivity.this,"Please check your email and password and try again!",Toast.LENGTH_LONG).show();

                            }

                        }
                    });
        }
    }

}
