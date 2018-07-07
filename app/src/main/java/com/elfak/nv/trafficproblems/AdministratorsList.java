package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdministratorsList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView mRecyclerView;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef;
    MyAdapter adapter;
    List<User> listData;
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

        findViewById(R.id.includeActivityProfile).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeActivityEditProfile).setVisibility(View.INVISIBLE);
        findViewById(R.id.includeMainView).setVisibility(View.INVISIBLE);
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Administrators");
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

        Menu menuNav = navigationView.getMenu();
        MenuItem editProfile = menuNav.findItem(R.id.nav_edit_profile);
        MenuItem logoutUser = menuNav.findItem(R.id.logout);
        MenuItem myProblems = menuNav.findItem(R.id.nav_problems);

        myProblems.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case",3);
                idBundle.putString("user_id",userInfo.key);
                Intent intent = new Intent(AdministratorsList.this, ProblemsList.class);
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
                Intent login = new Intent(AdministratorsList.this,LoginActivity.class);
                startActivity(login);
                return true;
            }
        });

        editProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent editProfile = new Intent(AdministratorsList.this, EditProfile.class);
                startActivityForResult(editProfile,1);
                return true;
            }
        });

        profileImageOnSideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case", 1);
                Intent profile = new Intent(AdministratorsList.this,Profile.class);
                profile.putExtras(idBundle);
                startActivity(profile);
            }
        });

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        listData = new ArrayList<>();
        adapter = new MyAdapter(listData);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        GetDataFirebase();


    }

    void GetDataFirebase()
    {
        mRef = mFirebaseDatabase.getReference("users");
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final User data = dataSnapshot.getValue(User.class);
                if(data.role.equals("admin")) {
                    String key = dataSnapshot.getKey();
                    data.key = key;
                    uriPicture = "";
                    StorageReference profileRef = mStorageRef.child("Avatars").child(key);
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

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
    {
        List<User> listArray;
        public MyAdapter(List<User> List)
        {
            this.listArray = List;
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.administartors_row,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
            User data = listArray.get(position);
            holder.admin_name.setText("Name: " + data.getFirst_name());
            holder.admin_last_name.setText("Last name: " + data.getLast_name());
            holder.saving_user_id.setText(data.key);
            //holder.admin_picture.setImageBitmap(data.get_Picture());
            Picasso.get().load(data.imageUri).into(holder.admin_picture);
        }
        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            TextView admin_name,admin_last_name,saving_user_id;
            ImageView admin_picture;
            ImageButton open_profile;
            public MyViewHolder(View itemView) {
                super(itemView);
                admin_name = itemView.findViewById(R.id.admin_name);
                admin_last_name = itemView.findViewById(R.id.admin_last_name);
                admin_picture = itemView.findViewById(R.id.admin_image_view);
                open_profile = itemView.findViewById(R.id.open_profile);
                saving_user_id = itemView.findViewById(R.id.saiving_user_id);

                open_profile.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                if (v.getId() == open_profile.getId()){
                    String user_id = saving_user_id.getText().toString();
                    Bundle idBundle = new Bundle();
                    idBundle.putInt("case",2);
                    idBundle.putString("user_id", user_id);
                    Intent intent = new Intent(AdministratorsList.this, Profile.class);
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
        /*FirebaseRecyclerAdapter<ModelForAdmins,ViewHolderAdminsCard> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ModelForAdmins, ViewHolderAdminsCard>(
                        ModelForAdmins.class,
                        R.layout.administartors_row,
                        ViewHolderAdminsCard.class,
                        mRef
                ) {
                    @Override
                    protected void populateViewHolder(ViewHolderAdminsCard viewHolder, ModelForAdmins model, int position) {
                        String key = mFirebaseDatabase.getReference("users").getKey();
                        viewHolder.setDetails(model.getFirst_name(),model.getLast_name());
                    }
                };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);*/
    }
    public static  byte[] getByteArrayFromImageView(ImageView imageView)
    {
        BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
        Bitmap bitmap;
        if(bitmapDrawable==null){
            imageView.buildDrawingCache();
            bitmap = imageView.getDrawingCache();
            imageView.buildDrawingCache(false);
        }else
        {
            bitmap = bitmapDrawable .getBitmap();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
