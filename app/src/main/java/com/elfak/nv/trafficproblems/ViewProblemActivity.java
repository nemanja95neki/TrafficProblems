package com.elfak.nv.trafficproblems;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class ViewProblemActivity extends AppCompatActivity {


    private List<Comment> commentList = new ArrayList<>();
    private RecyclerView recyclerView;
    private CommentsAdapter mAdapter;
    FirebaseDatabase database;
    DatabaseReference ref;
    StorageReference mStorageRef;


    Problem loadedProblem;
    User logedUser;
    User usersProblem;

    TextView txtDescription,txtProblemName,txtUserName, txtComment,txtTime;
    ImageView imageProblem,imagePriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_problem);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtProblemName=findViewById(R.id.textView3);
        txtDescription= findViewById(R.id.textDesc2);
        txtUserName = findViewById(R.id.textView4);
        imageProblem=findViewById(R.id.imageView3);
        imagePriority=findViewById(R.id.imageView2);
        txtComment=findViewById(R.id.editText2);
        txtTime = findViewById(R.id.textView5);

        final UserLocalStore userLogged = new UserLocalStore(this);
        logedUser= userLogged.getLoggedInUser();

        setSupportActionBar(toolbar);
        ref = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();



        //prosledjen kljuc problema
        ref.child("problems").child("-LG_CExmSitIQyTCbByZ").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String loadedProblemKey =dataSnapshot.getKey();
                loadedProblem = dataSnapshot.getValue(Problem.class);
                loadedProblem.key=loadedProblemKey;
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

                String commentText=  txtComment.getText().toString();

                if(commentText==null && commentText.isEmpty())
                    return;
                String key= ref.push().getKey();
                Comment newComment = new Comment(commentText,logedUser.key);
                ref.child("comments").child(loadedProblem.key).child(key).setValue(newComment);
                newComment.key=key;
                //commentList.add(newComment);

                //DateUtils.getRelativeTimeSpanString(your_time_in_milliseconds, current_ time_in_millisecinds,DateUtils.MINUTE_IN_MILLIS);
            }
        });

        //prepareViewData();
    }

    private void showData() throws IOException {
        txtProblemName.setText(loadedProblem.problemName);
        txtDescription.setText(loadedProblem.problemDescription);
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
        final File localFile = File.createTempFile("images", "jpg");
        final StorageReference problemImage = mStorageRef.child("Problems").child(loadedProblem.key);
        problemImage.getFile(localFile)
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



}
