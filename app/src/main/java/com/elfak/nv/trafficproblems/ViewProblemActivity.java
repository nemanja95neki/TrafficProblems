package com.elfak.nv.trafficproblems;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ViewProblemActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private List<Comment> commentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CommentsAdapter mAdapter;
    FirebaseDatabase database;
    DatabaseReference ref;
    StorageReference mStorageRef;
    private String problem_key="";
    String uriPicture="";

    Problem loadedProblem;
    User logedUser;
    User usersProblem;

    TextView txtDescription,txtProblemName,txtUserName, txtComment,txtTime, txtSolved;
    ImageView imageProblem,imagePriority;

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
        findViewById(R.id.includeActivityProblemsList).setVisibility(View.INVISIBLE);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Problem details");

        txtProblemName=findViewById(R.id.textView3);
        txtDescription= findViewById(R.id.textDesc2);
        txtUserName = findViewById(R.id.textView4);
        imageProblem=findViewById(R.id.imageView3);
        imagePriority=findViewById(R.id.imageView2);
        txtComment=findViewById(R.id.editText2);
        txtTime = findViewById(R.id.textView5);
        txtSolved = findViewById(R.id.textView6);

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

        final UserLocalStore userLogged = new UserLocalStore(this);
        logedUser= userLogged.getLoggedInUser();

        setSupportActionBar(toolbar);
        ref = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        try{
            Intent listIntent = getIntent();
            Bundle bundle = listIntent.getExtras();
            problem_key = bundle.getString("problem_id");
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
        if(problem_key!="") {
            //prosledjen kljuc problema
            ref.child("problems").child(problem_key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String loadedProblemKey = dataSnapshot.getKey();
                    loadedProblem = dataSnapshot.getValue(Problem.class);
                    loadedProblem.key = loadedProblemKey;
                    try {
                        showData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            recyclerView = findViewById(R.id.recycler_view);

            mAdapter = new CommentsAdapter(commentList);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);


            findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String commentText = txtComment.getText().toString();

                    if (commentText == null || commentText.isEmpty())
                        return;
                    else {
                        String key = ref.push().getKey();
                        Comment newComment = new Comment(commentText, logedUser.key);
                        ref.child("comments").child(loadedProblem.key).child(key).setValue(newComment);
                            newComment.key = key;
                            Toast.makeText(ViewProblemActivity.this, "Comment added!", Toast.LENGTH_SHORT).show();
                            txtComment.setText("");
                            commentList.add(newComment);
                            mAdapter.notifyDataSetChanged();


                    }


                    //DateUtils.getRelativeTimeSpanString(your_time_in_milliseconds, current_ time_in_millisecinds,DateUtils.MINUTE_IN_MILLIS);
                }
            });
        }

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
                Intent login = new Intent(ViewProblemActivity.this,LoginActivity.class);
                startActivity(login);
                return true;
            }
        });

        editProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent editProfile = new Intent(ViewProblemActivity.this, EditProfile.class);
                startActivityForResult(editProfile,1);
                return true;
            }
        });
        profileImageOnSideMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle idBundle = new Bundle();
                idBundle.putInt("case", 1);
                Intent profile = new Intent(ViewProblemActivity.this,Profile.class);
                profile.putExtras(idBundle);
                startActivity(profile);
            }
        });

        //prepareViewData();
    }

    private void showData() throws IOException {
        txtProblemName.setText(loadedProblem.problemName);
        txtDescription.setText(loadedProblem.problemDescription);
        if(loadedProblem.solved==1)
            txtSolved.setText("Solved!");
        else
            txtSolved.setText("Not Solved!");
        switch (loadedProblem.priority){
            case 1:
                imagePriority.setImageResource(R.drawable.ic_priority_5);
            case 2:
                imagePriority.setImageResource(R.drawable.ic_priority_4);
            case 3:
                imagePriority.setImageResource(R.drawable.ic_priority_3);
            case 4:
                imagePriority.setImageResource(R.drawable.ic_priority_2);
            case 5:
                imagePriority.setImageResource(R.drawable.ic_priority_1);
        }

        setImage();
        Long currentTime = System.currentTimeMillis();
        txtTime.setText(DateUtils.getRelativeTimeSpanString(loadedProblem.time, currentTime,
                DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_NO_NOON));

        ref.child("users").child(loadedProblem.userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String loadedUserKey =dataSnapshot.getKey();
                usersProblem = dataSnapshot.getValue(User.class);
                usersProblem.key=loadedUserKey;
                txtUserName.setText(usersProblem.first_name+" "+usersProblem.last_name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        ref.child("comments").child(loadedProblem.key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                commentList.add(comment);
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



    private void prepareViewData()
    {
        Comment c1=new Comment("wewe","ewew" );
        commentList.add(c1);

    }

    private void setImage() throws IOException {

        final StorageReference problemImage = mStorageRef.child("Problems").child(loadedProblem.key);
        /*problemImage.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        String imageLocation = localFile.getAbsolutePath();
                        //rotateImage(setReducedImageSize(imageLocation), imageLocation);
                        imageProblem.setImageBitmap(setReducedImageSize(imageLocation));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // Toast.makeText(getApplicationContext(),"Something went wrong, couldn't download profile picture",Toast.LENGTH_LONG).show();
            }
        });*/

        problemImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //pPicasso.get().load(uri).into(picture);
                uriPicture = uri.toString();
                Picasso.get().load(uriPicture).into(imageProblem);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
    private Bitmap setReducedImageSize(String fileLocation){
        int targetImageViewWidth = imageProblem.getWidth();
        int targetImageViewHeight = imageProblem.getHeight();

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
    protected void onStart() {
        super.onStart();
        if(avatar!=null) {
            avatar = userAvatarStore.getUserAvatar();
            imageSideMenu.setImageBitmap(avatar);
        }
    }
}
