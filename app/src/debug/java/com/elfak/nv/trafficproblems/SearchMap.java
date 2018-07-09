package com.elfak.nv.trafficproblems;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SearchMap extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    static final int PREMISSION_ACESS_FINE_LOCATION = 1;
    private boolean setFocusToCurrentLocation = false;
    DatabaseReference mFirebaseDatabase;
    private ArrayList<Problem> myProblems;
    private int searchCase = -1;
    private int radius = 0;
    private int priority = 0;
    private String name;
    private Long date;
    private ArrayList<Marker> problemMarkers = new ArrayList<Marker>();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker myLocation;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private User userInfo;
    private String userID;
    private UserLocalStore userLocalStore;
    private UserAvatarStore userAvatarStore;
    private TextView sideMenuEmail, sideMenuName;
    private ImageView imageSideMenu;
    private Bitmap avatar;
    private DatabaseReference friendsReference, dbRefUsersLocation;
    double Latitude;
    double Longitude;
    private String problem_id;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("problems");
        dbRefUsersLocation = FirebaseDatabase.getInstance().getReference("Current Location");
        userLocalStore = new UserLocalStore(this);
        userAvatarStore = new UserAvatarStore(this);
        myProblems = new ArrayList<Problem>();
        Intent listIntent = getIntent();
        Bundle bundle = listIntent.getExtras();
        searchCase = bundle.getInt("case");
        if (searchCase == 1)
            radius = Integer.parseInt(bundle.getString("radius"));
        else if (searchCase == 2)
        {
            String prior = bundle.getString("priority");
            if(prior!=null)
                priority = Integer.parseInt(prior);
            String nam =  bundle.getString("name");
            if(nam.equals(""))
                name = bundle.getString("name");
            String dat = bundle.getString("date");
            if(dat.equals(""))
                date = Long.parseLong(dat);
        }
        else if(searchCase==3)
        {
            problem_id=bundle.getString("problem_id");
            if(problem_id!=null)
            {

            }

        }


    }

    private void GetDataFirebaseProblems(final int radius) {
        DatabaseReference mRef = mFirebaseDatabase;
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    final Problem p = child.getValue(Problem.class);
                    p.key = child.getKey();
                    if (p != null) {
                        GetProblemImage gpi = new GetProblemImage();
                        p.imagePicture = gpi.getImage(p);
                        myProblems.add(p);
                        LatLng latlng = new LatLng(Double.parseDouble(p.latitude), Double.parseDouble(p.longitude));
                        double dist = distance(Latitude, latlng.latitude, Longitude, latlng.longitude, 0.0, 0.0);
                        if (dist <= radius)
                            addMarkerProblem(latlng, p.key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void GetDataFirebaseProblems(final int priority, final String name, final Long date) {
        DatabaseReference mRef = mFirebaseDatabase;
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    final Problem p = child.getValue(Problem.class);
                    p.key = child.getKey();
                    if (p != null) {
                        GetProblemImage gpi = new GetProblemImage();
                        p.imagePicture = gpi.getImage(p);
                        myProblems.add(p);
                        LatLng latlng = new LatLng(Double.parseDouble(p.latitude), Double.parseDouble(p.longitude));
                        boolean okay = true;
                        if(priority!=0 && p.priority!=priority && okay == true)
                        {
                            okay = false;
                        }
                        if(name!=null && !name.equals(p.problemName) && okay == true)
                        {
                            okay = false;
                        }
                        /*SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        String dateString = formatter.format(new Date(date*1000));*/
                        if(date!=null) {
                            Long longDate = Long.valueOf(p.time);

                            Calendar cal = Calendar.getInstance();
                            int offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());
                            Date da = new Date();
                            da = new Date(longDate - (long) offset);
                            cal.setTime(da);
                            String time = cal.getTime().toLocaleString();
                            time = DateFormat.getDateInstance(DateFormat.MEDIUM).format(da);
                            if(!time.equals(date))
                                okay = false;
                        }
                        if(okay == true)
                            addMarkerProblem(latlng, p.key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void GetDataFirebaseProblem(String problemId) {
        DatabaseReference mRef = mFirebaseDatabase;
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {

                    final Problem p = child.getValue(Problem.class);
                    p.key = child.getKey();
                    if (p != null) {
                        GetProblemImage gpi = new GetProblemImage();
                        p.imagePicture = gpi.getImage(p);
                        myProblems.add(p);
                        LatLng latlng = new LatLng(Double.parseDouble(p.latitude), Double.parseDouble(p.longitude));
                        boolean okay = true;
                        if(priority!=0 && p.priority!=priority && okay == true)
                        {
                            okay = false;
                        }
                        if(name!=null && !name.equals(p.problemName) && okay == true)
                        {
                            okay = false;
                        }
                        /*SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        String dateString = formatter.format(new Date(date*1000));*/
                        if(date!=null) {
                            Long longDate = Long.valueOf(p.time);

                            Calendar cal = Calendar.getInstance();
                            int offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());
                            Date da = new Date();
                            da = new Date(longDate - (long) offset);
                            cal.setTime(da);
                            String time = cal.getTime().toLocaleString();
                            time = DateFormat.getDateInstance(DateFormat.MEDIUM).format(da);
                            if(!time.equals(date))
                                okay = false;
                        }
                        if(okay == true)
                            addMarkerProblem(latlng, p.key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static String toYYYYMMDDHHMMSS(long time) {
        SimpleDateFormat format = new SimpleDateFormat("M??d??H?m??s??");
        return format.format(new Date(time));
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
            if (p.key.equals(problemID)) {
                mi.title = p.problemName;
                mi.isUser = false;
                mi.imageUri = p.imagePicture;
            }
        }
        m.setTag(mi);
        problemMarkers.add(m);
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
                //sideMenuName.setText(userInfo.first_name + " " + userInfo.last_name);
                //sideMenuEmail.setText(userInfo.email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(ProfileActivity.this,"Something went wrong. Please try again...",Toast.LENGTH_SHORT).show();
            }
        });
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                Latitude = latti;
                Longitude = longi;
            } else  if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                Latitude = latti;
                Longitude = longi;
            } else  if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                Latitude = latti;
                Longitude = longi;
            }else{

                Toast.makeText(this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();

            }


        avatar = userAvatarStore.getUserAvatar();

        if (avatar != null) {
           //imageSideMenu.setImageBitmap(avatar);
        }

    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PREMISSION_ACESS_FINE_LOCATION);
        } else {
            if(searchCase==1)
                GetDataFirebaseProblems(radius);
            if(searchCase==2)
                GetDataFirebaseProblems(priority, name, date);
            myLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).visible(false));
            mMap.setMyLocationEnabled(true);
            //addProblemMarkers();
        }
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
                if(markerInfo.isUser == false) {
                    String problem_id = marker.getTitle();
                    Bundle idBundle = new Bundle();
                    idBundle.putString("problem_id", problem_id);
                    Intent intent = new Intent(SearchMap.this, ViewProblemActivity.class);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
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
