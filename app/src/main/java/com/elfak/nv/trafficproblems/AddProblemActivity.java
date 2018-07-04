package com.elfak.nv.trafficproblems;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddProblemActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_GET_LOCATION=2;
    static final int GALLERY_INTENT = 3;
    String mCurrentPhotoPath;
    ImageView mImageView;
    DatabaseReference databaseReference;
    StorageReference mStorageRef;

    Uri uri;

    User user;

    String longitude;
    String latitude;
    String nameProblem;
    String descProblem;
    Bitmap problemImage;
    Integer priority;
    EditText nameTxt,descTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_problem);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UserLocalStore userLogged = new UserLocalStore(this);
        user= userLogged.getLoggedInUser();

        mImageView = findViewById(R.id.imageViewProblem);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        nameTxt=findViewById(R.id.textProblemName);
        descTxt=findViewById(R.id.textDesc);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
        findViewById(R.id.imageButtonGallery).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, GALLERY_INTENT);
                }
            }
        });
        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String key = databaseReference.push().getKey();
                nameProblem=nameTxt.getText().toString();
                descProblem=descTxt.getText().toString();
                if(checkInputs(nameProblem,descProblem,priority,longitude,latitude)) {
                    Problem newProblem = new Problem(nameProblem, descProblem, priority, longitude, latitude, user.key);
                    databaseReference.child("problems").child(key).setValue(newProblem);
                    //slika
                    final StorageReference filePath = mStorageRef.child("Problems").child(key);
                    filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(AddProblemActivity.this,"Upload done.",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                    return;
            }
        });

        findViewById(R.id.addLocationButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               Intent i = new Intent(AddProblemActivity.this,ChooseLocationActivity.class);
               startActivityForResult(i,REQUEST_GET_LOCATION);
            }
        });
        final ImageButton btnPrior1 = findViewById(R.id.priority1);
        final ImageButton btnPrior2 = findViewById(R.id.priority2);
        final ImageButton btnPrior3 = findViewById(R.id.priority3);
        final ImageButton btnPrior4 = findViewById(R.id.priority4);
        final ImageButton btnPrior5 = findViewById(R.id.priority5);
        btnPrior1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                priority=1;
                btnPrior1.setBackgroundColor(Color.GRAY);
                btnPrior2.setBackgroundColor(0);
                btnPrior3.setBackgroundColor(0);
                btnPrior4.setBackgroundColor(0);
                btnPrior5.setBackgroundColor(0);
            }
        });
        btnPrior2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                priority=2;
                btnPrior2.setBackgroundColor(Color.GRAY);
                btnPrior1.setBackgroundColor(0);
                btnPrior3.setBackgroundColor(0);
                btnPrior4.setBackgroundColor(0);
                btnPrior5.setBackgroundColor(0);
            }
        });
        btnPrior3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                priority=3;
                btnPrior3.setBackgroundColor(Color.GRAY);
                btnPrior1.setBackgroundColor(0);
                btnPrior2.setBackgroundColor(0);
                btnPrior4.setBackgroundColor(0);
                btnPrior5.setBackgroundColor(0);
            }
        });
        btnPrior4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                priority=4;
                btnPrior4.setBackgroundColor(Color.GRAY);
                btnPrior1.setBackgroundColor(0);
                btnPrior3.setBackgroundColor(0);
                btnPrior2.setBackgroundColor(0);
                btnPrior5.setBackgroundColor(0);
            }
        });
        btnPrior5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                priority=5;
                btnPrior5.setBackgroundColor(Color.GRAY);
                btnPrior1.setBackgroundColor(0);
                btnPrior3.setBackgroundColor(0);
                btnPrior4.setBackgroundColor(0);
                btnPrior2.setBackgroundColor(0);
            }
        });
    }


    private boolean checkInputs(String name, String desc,Integer prior, String longit, String lat)
    {
        if (name.isEmpty())
        {
            Toast.makeText(AddProblemActivity.this,"Please enter the name of problem!",Toast.LENGTH_LONG).show();
            nameTxt.requestFocus();
            return false;
        }
        else if(desc.isEmpty())
        {
            Toast.makeText(AddProblemActivity.this,"Please enter desctiption!",Toast.LENGTH_LONG).show();
            nameTxt.requestFocus();
            return false;
        }
        else if(prior==null)
        {
            Toast.makeText(AddProblemActivity.this,"Please choose priority!",Toast.LENGTH_LONG).show();
            nameTxt.requestFocus();
            return false;
        }
        else if(longit.isEmpty())
        {
            Toast.makeText(AddProblemActivity.this,"Please choose location!",Toast.LENGTH_LONG).show();
            nameTxt.requestFocus();
            return false;
        }
        else if(lat.isEmpty())
        {
            Toast.makeText(AddProblemActivity.this,"Please choose location!",Toast.LENGTH_LONG).show();
            nameTxt.requestFocus();
            return false;
        }
        else if(problemImage==null || uri==null)
        {
            Toast.makeText(AddProblemActivity.this,"Please choose image!",Toast.LENGTH_LONG).show();
            nameTxt.requestFocus();
            return false;
        }
        else
            return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            problemImage= (Bitmap) extras.get("data");
            uri = getImageUri(getApplicationContext(), problemImage);
            mImageView.setImageBitmap(problemImage);
        }
        else if(requestCode==REQUEST_GET_LOCATION && resultCode== Activity.RESULT_OK)
        {
            longitude =data.getExtras().getString("lon");
            latitude = data.getExtras().getString("lat");
            Toast.makeText(AddProblemActivity.this,"longitude, latitude "+longitude+" "+latitude ,Toast.LENGTH_LONG).show();
        }
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            uri = data.getData();
            problemImage = null;
            try {
                problemImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                mImageView.setImageBitmap(problemImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
