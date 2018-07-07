package com.elfak.nv.trafficproblems;

import android.Manifest;
import android.app.LauncherActivity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

public class NavDrawerMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private User userInfo;
    private String userID;
    private UserLocalStore userLocalStore;
    private UserAvatarStore userAvatarStore;
    private TextView sideMenuEmail, sideMenuName;
    private GoogleMap mMap;
    static final int PREMISSION_ACESS_FINE_LOCATION = 1;
    private ImageView imageSideMenu;
    private StorageReference mStorageRef;
    private boolean firstLoad = false;
    private Bitmap avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        userLocalStore = new UserLocalStore(this);
        userAvatarStore = new UserAvatarStore(this);
        firstLoad = true;
        findViewById(R.id.includeActivityProfile).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityEditProfile).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityAdministratorsList).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityAddProblem).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityProblemsList).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityViewProblem).setVisibility(View.INVISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button btnNewProblem = findViewById(R.id.button2);
        btnNewProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewProblemActivity();
            }
        });

        Button btnAdministrators = findViewById(R.id.buttonAdministrators);
        btnAdministrators.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NavDrawerMain.this,AdministratorsList.class);
                startActivity(i);
            }
        });
        Button btnProblems = findViewById(R.id.buttonProblems);
        btnProblems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case",1);
                Intent intent = new Intent(NavDrawerMain.this, ProblemsList.class);
                intent.putExtras(idBundle);
                startActivity(intent);
            }
        });
        Button btnSolvedProblems = findViewById(R.id.buttonSolvedProblems);
        btnSolvedProblems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case",2);
                Intent intent = new Intent(NavDrawerMain.this, ProblemsList.class);
                intent.putExtras(idBundle);
                startActivity(intent);
            }
        });

        View header = navigationView.getHeaderView(0);
        LinearLayout profileImageOnSideMenu = (LinearLayout)header.findViewById(R.id.viewProfile);
        sideMenuEmail = profileImageOnSideMenu.findViewById(R.id.textEmail);
        sideMenuName = profileImageOnSideMenu.findViewById(R.id.textUserName);
        imageSideMenu = (ImageView)profileImageOnSideMenu.findViewById(R.id.imageProfileImage);

        Menu menuNav = navigationView.getMenu();
        MenuItem editProfile = menuNav.findItem(R.id.nav_edit_profile);
        MenuItem logoutUser = menuNav.findItem(R.id.logout);
        MenuItem myProblems = menuNav.findItem(R.id.nav_problems);

        myProblems.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case",3);
                idBundle.putString("user_id",userID);
                Intent intent = new Intent(NavDrawerMain.this, ProblemsList.class);
                intent.putExtras(idBundle);
                startActivity(intent);
                return true;
            }
        });

        logoutUser.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                userLocalStore.setUserLoggedIn(false);
                userLocalStore.clearUserData();
                userAvatarStore.clearUserData();
                FirebaseAuth.getInstance().signOut();
                Intent login = new Intent(NavDrawerMain.this,LoginActivity.class);
                startActivity(login);
                return true;
            }
        });

        editProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent editProfile = new Intent(NavDrawerMain.this, EditProfile.class);
                startActivityForResult(editProfile,1);
                return true;
            }
        });

        profileImageOnSideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case", 1);
                Intent profile = new Intent(NavDrawerMain.this,Profile.class);
                profile.putExtras(idBundle);
                startActivity(profile);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();
        if (user == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        userID = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userInfo = dataSnapshot.child(userID).getValue(User.class);
                userInfo.key = userID;
                userLocalStore.storeUserData(userInfo);
                userLocalStore.setUserLoggedIn(true);
                sideMenuName.setText(userInfo.first_name + " " + userInfo.last_name);
                sideMenuEmail.setText(userInfo.email);
                try {
                    if(firstLoad == true){
                        setProfilePicture();
                    }
                    firstLoad = false;
                    //setProfilePictureSideMenu();

                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(ProfileActivity.this,"Something went wrong. Please try again...",Toast.LENGTH_SHORT).show();
            }
        });
        if(firstLoad == false) {
            avatar = userAvatarStore.getUserAvatar();

            if (avatar != null) {
                imageSideMenu.setImageBitmap(avatar);
            }
        }
    }

    private void startNewProblemActivity()
    {
        Intent i = new Intent(NavDrawerMain.this,AddProblemActivity.class);
        startActivity(i);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_edit_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_friends) {

        } else if (id == R.id.nav_problems) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PREMISSION_ACESS_FINE_LOCATION);
        } else
            mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PREMISSION_ACESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        mMap.setMyLocationEnabled(true);
                        return;
                    }

                }
                return;
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String returned_name = data.getStringExtra("name");
                String returned_last_name = data.getStringExtra("last_name");
                String returned_email = data.getStringExtra("email");
                String returned_password = data.getStringExtra("password");
                String returned_phone_number = data.getStringExtra("phone_number");

                sideMenuEmail.setText(returned_email);
                sideMenuName.setText(returned_name + " " + returned_last_name);
            }
        }
    }

    private void setProfilePicture() throws IOException {
        final File localFile = File.createTempFile("images", "jpg");
        StorageReference profileRef = mStorageRef.child("Avatars").child(userInfo.key);
        profileRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        String imageLocation = localFile.getAbsolutePath();
                        rotateImage(setReducedImageSize(imageLocation), imageLocation);
                        userAvatarStore.storeUserAvatar(BitmapFactory.decodeFile(imageLocation));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // Toast.makeText(getApplicationContext(),"Something went wrong, couldn't download profile picture",Toast.LENGTH_LONG).show();
            }
        });
    }
    private Bitmap setReducedImageSize(String fileLocation){
        int targetImageViewWidth = imageSideMenu.getWidth();
        int targetImageViewHeight = imageSideMenu.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileLocation,bmOptions);
        int cameraImageWidth = bmOptions.outWidth;
        int cameraImageHeight = bmOptions.outHeight;

        int scaleFactor = Math.min(cameraImageHeight/targetImageViewHeight,cameraImageWidth/targetImageViewWidth);

        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(fileLocation, bmOptions);
    }

    private void rotateImage(Bitmap bitmap,String fileLocation){

        ExifInterface exifInterface = null;
        try{
            exifInterface = new ExifInterface(fileLocation);
        }catch (IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
        }
        Bitmap rotatedBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        imageSideMenu.setImageBitmap(rotatedBmp);
    }
}
