package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProblemsList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    RecyclerView mRecyclerView;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef;
    ProblemsList.MyAdapter adapter;
    List<Problem> listData;
    private StorageReference mStorageRef;
    String uriPicture="";
    private UserLocalStore userLocalStore;
    private UserAvatarStore userAvatarStore;
    private Bitmap avatar;
    private User userInfo;
    private TextView sideMenuEmail, sideMenuName;
    private ImageView imageSideMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.includeMainView).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityEditProfile).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityAdministratorsList).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityAddProblem).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityProfile).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityViewProblem).setVisibility(View.INVISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Problems");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        userLocalStore = new UserLocalStore(this);
        userInfo = userLocalStore.getLoggedInUser();
        userAvatarStore = new UserAvatarStore(this);
        avatar = userAvatarStore.getUserAvatar();

        View header = navigationView.getHeaderView(0);
        LinearLayout profileImageOnSideMenu = (LinearLayout)header.findViewById(R.id.viewProfile);
        sideMenuEmail = profileImageOnSideMenu.findViewById(R.id.textEmail);
        sideMenuName = profileImageOnSideMenu.findViewById(R.id.textUserName);
        sideMenuEmail.setText(userInfo.email);
        sideMenuName.setText(userInfo.first_name + " " + userInfo.last_name);
        imageSideMenu = (ImageView)profileImageOnSideMenu.findViewById(R.id.imageProfileImage);

        mRecyclerView = findViewById(R.id.recyclerViewProblems);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        listData = new ArrayList<>();
        adapter =  new MyAdapter(listData);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        GetDataFirebase();

        Menu menuNav = navigationView.getMenu();
        MenuItem editProfile = menuNav.findItem(R.id.nav_edit_profile);
        MenuItem logoutUser = menuNav.findItem(R.id.logout);

        logoutUser.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                userLocalStore.setUserLoggedIn(false);
                userLocalStore.clearUserData();
                userAvatarStore.clearUserData();
                FirebaseAuth.getInstance().signOut();
                Intent login = new Intent(ProblemsList.this,LoginActivity.class);
                startActivity(login);
                return true;
            }
        });

        editProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent editProfile = new Intent(ProblemsList.this, EditProfile.class);
                startActivityForResult(editProfile,1);
                return true;
            }
        });
        profileImageOnSideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case", 1);
                Intent profile = new Intent(ProblemsList.this,Profile.class);
                profile.putExtras(idBundle);
                startActivity(profile);
            }
        });
    }

    void GetDataFirebase()
    {
        mRef = mFirebaseDatabase.getReference("problems");
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final Problem data = dataSnapshot.getValue(Problem.class);
                    String key = dataSnapshot.getKey();
                    data.key = key;
                    uriPicture = "";
                    StorageReference profileRef = mStorageRef.child("Problems").child(key);
                    profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //pPicasso.get().load(uri).into(picture);
                            uriPicture = uri.toString();
                            data.imageUri = uriPicture;
                            listData.add(data);
                            mRecyclerView.setAdapter(adapter);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    public class MyAdapter extends RecyclerView.Adapter<ProblemsList.MyAdapter.MyViewHolder>
    {
        List<Problem> listArray;
        public MyAdapter(List<Problem> List)
        {
            this.listArray = List;
        }

        @NonNull
        @Override
        public ProblemsList.MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.problems_row,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProblemsList.MyAdapter.MyViewHolder holder, int position) {
            Problem data = listArray.get(position);
            holder.problem_name.setText("Problem: " + data.problemName);
            holder.saving_problem_id.setText(data.key);
            //holder.admin_picture.setImageBitmap(data.get_Picture());
            Picasso.get().load(data.imageUri).into(holder.problem_picture);
            Long currentTime = System.currentTimeMillis();
            holder.time_ago.setText(DateUtils.getRelativeTimeSpanString(data.time, currentTime,
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_NO_NOON));
        }
        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView problem_name,saving_problem_id,time_ago;
            ImageView problem_picture;
            ImageButton problem_show_on_map,problem_details;
            public MyViewHolder(View itemView) {
                super(itemView);
                problem_name = itemView.findViewById(R.id.title_problems_list);
                problem_picture = itemView.findViewById(R.id.problem_image);
                problem_show_on_map = itemView.findViewById(R.id.problem_show_on_map);
                problem_details = itemView.findViewById(R.id.problem_details);
                saving_problem_id = itemView.findViewById(R.id.saving_problem_id);
                time_ago = itemView.findViewById(R.id.problem_time_ago);

                problem_show_on_map.setOnClickListener(this);
                problem_details.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == problem_show_on_map.getId()){
                    String problem_id = saving_problem_id.getText().toString();
                    Bundle idBundle = new Bundle();
                    idBundle.putString("problem_id", problem_id);
                    Intent intent = new Intent(ProblemsList.this, Profile.class);
                    intent.putExtras(idBundle);
                    startActivity(intent);

                }
                if (v.getId() == problem_details.getId()){
                    String problem_id = saving_problem_id.getText().toString();
                    Bundle idBundle = new Bundle();
                    idBundle.putString("problem_id", problem_id);
                    Intent intent = new Intent(ProblemsList.this, ViewProblemActivity.class);
                    intent.putExtras(idBundle);
                    startActivity(intent);
                }
            }
        }

        @Override
        public int getItemCount() {
            return listArray.size();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(avatar!=null) {
            avatar = userAvatarStore.getUserAvatar();
            imageSideMenu.setImageBitmap(avatar);
        }
    }
}
