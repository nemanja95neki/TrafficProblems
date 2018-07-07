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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        setContentView(R.layout.activity_problems_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Problems");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        userLocalStore = new UserLocalStore(this);
        userInfo = userLocalStore.getLoggedInUser();
        userAvatarStore = new UserAvatarStore(this);
        avatar = userAvatarStore.getUserAvatar();

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        listData = new ArrayList<>();
        adapter =  new MyAdapter(listData);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        GetDataFirebase();
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
                            //data.imageUri = uriPicture;
                            //listData.add(data);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    public class MyAdapter extends RecyclerView.Adapter<ProblemsList.MyAdapter.MyViewHolder>
    {
        List<User> listArray;
        public MyAdapter(List<User> List)
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
                    Intent intent = new Intent(ProblemsList.this, Profile.class);
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

}
