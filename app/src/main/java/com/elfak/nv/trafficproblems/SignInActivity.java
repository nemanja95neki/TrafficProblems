package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText txtName, txtLastName, txtEmail, txtPassword, txtPhone;
    private Button signUpBtn;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtName = (EditText)findViewById(R.id.txtName);
        txtLastName = (EditText)findViewById(R.id.txtLastName);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPassword = (EditText)findViewById(R.id.txtPassword);
        txtPhone = (EditText)findViewById(R.id.txtPhone);
        signUpBtn = (Button)findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.btnSignUp).setOnClickListener(this);
    }
    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            //handle the already login user
        }
    }

    private void registerUser(){
        final String name = txtName.getText().toString();
        final String lastName = txtLastName.getText().toString();
        final String email = txtEmail.getText().toString();
        final String password = txtPassword.getText().toString();
        final String phone = txtPhone.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(SignInActivity.this,"Please enter your name!",Toast.LENGTH_LONG).show();
            txtName.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            Toast.makeText(SignInActivity.this,"Please enter your last name!",Toast.LENGTH_LONG).show();
            txtLastName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            Toast.makeText(SignInActivity.this,"Please enter your email!",Toast.LENGTH_LONG).show();
            txtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignInActivity.this,"Please enter your email!",Toast.LENGTH_LONG).show();
            txtEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(SignInActivity.this, "Please enter your password!", Toast.LENGTH_LONG).show();
            txtPassword.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(SignInActivity.this,"Please enter your phone number!",Toast.LENGTH_LONG).show();
            txtPhone.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            User user = new User(
                                   email,
                                    password,
                                    name,
                                    lastName,
                                    phone
                            );

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignInActivity.this, "Sucesfully signed!", Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(SignInActivity.this, LoginActivity.class);
                                        startActivity(i);
                                    } else {
                                        //display a failure message
                                        Toast.makeText(SignInActivity.this, "Error. Please try again!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignUp:
                registerUser();
                break;
        }
    }
}
