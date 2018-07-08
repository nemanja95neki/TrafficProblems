package com.elfak.nv.trafficproblems;

import android.Manifest;
import android.app.LauncherActivity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class NavDrawerMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private DatabaseReference databaseReference;
    private boolean setFocusToCurrentLocation = false;
    private FirebaseAuth firebaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FirebaseUser user;
    private User userInfo;
    private String userID;
    private UserLocalStore userLocalStore;
    private UserAvatarStore userAvatarStore;
    private TextView sideMenuEmail, sideMenuName;
    private GoogleMap mMap;
    static final int PREMISSION_ACESS_FINE_LOCATION = 1;
    private ImageView imageSideMenu;
    private StorageReference mStorageRef,mStorageRefProblems;
    private ImageButton searching_button;
    private boolean firstLoad = false;
    private Bitmap avatar;
    DatabaseReference mFirebaseDatabase;
    DatabaseReference dbFirebase;
    String uriPicture="";
    private Marker myLocation;
    private ArrayList<Problem> myProblems;
    File file;
    private ArrayList<Friend> friendsList;
    private ArrayList<User> usersList;
    private DatabaseReference friendsReference,dbRefUsersLocation;
    private ArrayList<Marker> friendsMarkers = new ArrayList<Marker>();
    private ArrayList<Marker> usersMarkers = new ArrayList<Marker>();
    private ArrayList<Marker> problemMarkers = new ArrayList<Marker>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myProblems = new ArrayList<Problem>();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRefProblems = FirebaseStorage.getInstance().getReference("Problems");
        friendsReference = FirebaseDatabase.getInstance().getReference("friends");
        dbRefUsersLocation = FirebaseDatabase.getInstance().getReference("Current Location");
        dbFirebase = FirebaseDatabase.getInstance().getReference("users");
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("problems");
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

        searching_button = findViewById(R.id.searching_button);
        searching_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NavDrawerMain.this,SearchActivity.class);
                startActivity(i);
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

        friendsList = new ArrayList<Friend>();
        usersList = new ArrayList<User>();


        Menu menuNav = navigationView.getMenu();
        MenuItem editProfile = menuNav.findItem(R.id.nav_edit_profile);
        MenuItem logoutUser = menuNav.findItem(R.id.logout);
        MenuItem myProblems = menuNav.findItem(R.id.nav_problems);
        MenuItem friends = menuNav.findItem(R.id.nav_friends);

        friends.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(NavDrawerMain.this, Friends.class);
                startActivity(intent);
                return true;
            }
        });

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
        GetDataFirebaseProblems();
        GetAllFriends();
        GetAllUsers();
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

    private void GetAllFriends(){
        DatabaseReference ref = friendsReference.child(userID);
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    Friend f = child.getValue(Friend.class);
                    if(f != null) {
                        try {
                            f.uri = GetImageForFriends(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        friendsList.add(f);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private Uri GetImageForFriends(final Friend f) throws IOException {
            final File localFile = File.createTempFile("images", "jpg");
            final StorageReference profileRef = mStorageRef.child("Avatars").child(f.id);
            profileRef.getFile(localFile)
                    .addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //When the image has successfully uploaded, get its download URL
                            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    f.uri = uri;
                                }
                            });
                        }
                    });
            return  f.uri;
    }
    private Uri GetImageForUsers(final User u) throws IOException {
        final File localFile = File.createTempFile("images", "jpg");
        final StorageReference profileRef = mStorageRef.child("Avatars").child(u.key);
        profileRef.getFile(localFile)
                .addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        //When the image has successfully uploaded, get its download URL
                        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                u.imagePicture = uri;
                            }
                        });
                    }
                });
        return  u.imagePicture;
    }
    private Uri GetImageForProblems(final Problem p) throws IOException {

        return p.imagePicture;
    }
    private void GetAllUsers(){
        DatabaseReference ref = dbFirebase;
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    User u = child.getValue(User.class);
                    u.key = child.getKey();
                    if(u != null) {
                        try {
                            u.imagePicture = GetImageForUsers(u);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        usersList.add(u);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        } else {
            myLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).visible(false));
            /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(marker.getId().equals(myLocation.getId()) || marker.getId().equals( newParking.getId())){
                        return true;
                    }
                    return false;
                }
            });*/
            mMap.setMyLocationEnabled(true);
            //addProblemMarkers();
        }
        SetFriendsMarkers();


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(final Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info, null);

                ImageView image = (ImageView) v.findViewById(R.id.imageView1);
                TextView tvTitle = (TextView) v.findViewById(R.id.tv_title);

                MarkerInformation markerInfo = (MarkerInformation) marker.getTag();
                tvTitle.setText(markerInfo.title);
                Picasso.get().load(markerInfo.imageUri).into(image);
                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                MarkerInformation markerInfo = (MarkerInformation) marker.getTag();
                if(markerInfo.isUser == true) {
                    String user_id = marker.getTitle();
                    Bundle idBundle = new Bundle();
                    idBundle.putInt("case", 2);
                    idBundle.putString("user_id", user_id);
                    Intent intent = new Intent(NavDrawerMain.this, Profile.class);
                    intent.putExtras(idBundle);
                    startActivity(intent);

                }
                if(markerInfo.isUser == false) {
                    String problem_id = marker.getTitle();
                    Bundle idBundle = new Bundle();
                    idBundle.putString("problem_id", problem_id);
                    Intent intent = new Intent(NavDrawerMain.this, ViewProblemActivity.class);
                    intent.putExtras(idBundle);
                    startActivity(intent);

                }
            }

        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        setFocusToCurrentLocation = false;
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
                        //addProblemMarkers();
                        return;
                    }

                }
                return;
            }
        }
    }
    private void GetDataFirebaseProblems(){
        DatabaseReference mRef = mFirebaseDatabase;
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    final Problem p = child.getValue(Problem.class);
                    p.key = child.getKey();
                    if(p != null) {
                        GetProblemImage gpi = new GetProblemImage();
                        p.imagePicture = gpi.getImage(p);
                        myProblems.add(p);
                        LatLng latlng = new LatLng(Double.parseDouble(p.latitude),Double.parseDouble(p.longitude));
                        addMarkerProblem(latlng,p.key);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private HashMap<Marker,Integer> markerProblemIdMap;
    /*private void addProblemMarkers()
    {
        ArrayList<Problem> problems = myProblems;
        markerProblemIdMap = new HashMap<Marker, Integer>((int)((double)problems.size()*1.2));
        for(int i=0;i<problems.size();i++)
        {
            Problem problem = problems.get(i);
            String lat = problem.latitude;
            String lon = problem.longitude;
            LatLng loc = new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(loc);
            if(problem.bmp== null)
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.warning));
            else
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(problem.bmp));
            markerOptions.title(problem.problemName);
            Marker marker = mMap.addMarker(markerOptions);
            markerProblemIdMap.put(marker,i);
        }
    }*/
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

    private void SetFriendsMarkers() {

        dbRefUsersLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RemoveUserAndFriendsMarkers();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    UserLocationInfo location = child.getValue(UserLocationInfo.class);
                    LatLng latlng = new LatLng(location.latitude,location.longitude);
                    boolean isFriend = false;
                    for (Friend f : friendsList) {
                        if(f.id.equals(location.id))
                            isFriend = true;
                    }
                    if (!location.id.equals(user.getUid()))
                        addMarker(latlng,location.id ,isFriend);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void addMarkerProblem(final LatLng latlng, final String problemID) {
            MarkerOptions options = new MarkerOptions()
                    .draggable(false)
                    .title(problemID)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.warning))
                    .position(latlng);
            final Marker m = mMap.addMarker(options);

            MarkerInformation mi = new MarkerInformation();
            for (Problem p : myProblems) {
                if(p.key.equals(problemID)) {
                    mi.title = p.problemName;
                    mi.isUser = false;
                    mi.imageUri = p.imagePicture;
                }
            }
            m.setTag(mi);
            problemMarkers.add(m);
    }
    private void addMarker(final LatLng latlng, final String userIDs, boolean isFriend) {
        if (isFriend){

            MarkerOptions options = new MarkerOptions()
                    .draggable(false)
                    .title(userIDs)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_friend_marker))
                    .position(latlng);
            final Marker m = mMap.addMarker(options);

            MarkerInformation mi = new MarkerInformation();
            for (Friend f : friendsList) {
                if(f.id.equals(userIDs)) {
                    mi.title = f.name;
                    mi.isUser = true;
                    mi.imageUri = f.uri;
                }
            }
            m.setTag(mi);
            friendsMarkers.add(m);
        }else{

            MarkerOptions options = new MarkerOptions()
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_users_marker))
                    .position(latlng);
            Marker m = mMap.addMarker(options);
            MarkerInformation mi = new MarkerInformation();
            for (User u : usersList) {
                if(u.key == userIDs) {
                    mi.title = u.first_name + u.last_name;
                    mi.isUser = true;
                    mi.imageUri = u.imagePicture;
                }
            }
            m.setTag(mi);
            usersMarkers.add(m);
        }
    }
    private void RemoveUserAndFriendsMarkers(){
        for (Marker m : usersMarkers) {
            m.remove();
        }
        for (Marker m : friendsMarkers) {
            m.remove();
        }
        usersMarkers.clear();
        friendsMarkers.clear();
    }
    @Override
    public void onLocationChanged(Location location) {

        if (location == null) {
            Toast.makeText(this, "Cant get current location", Toast.LENGTH_LONG).show();
        } else {

            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            myLocation.setPosition(ll);
            //currentLocation = location;
            if(!setFocusToCurrentLocation) {
                setMarkerOnCurrentLocation(location.getLatitude(), location.getLongitude());
                setFocusToCurrentLocation = true;
            }

            UserLocationInfo lInfo = new UserLocationInfo(location.getLatitude(),location.getLongitude(),user.getUid());
            dbRefUsersLocation.child(userID).setValue(lInfo);
        }

    }
    private void setMarkerOnCurrentLocation(double currentLatitude, double currentLongitude){
        if (currentLatitude != 0 && currentLongitude != 0){

            LatLng ll = new LatLng(currentLatitude, currentLongitude);

            myLocation.setPosition(ll);
            myLocation.setTitle("Your position");
            myLocation.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_position));
            myLocation.setVisible(true);

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 16);
            mMap.animateCamera(update);
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
