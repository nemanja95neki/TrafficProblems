package com.elfak.nv.trafficproblems;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.health.TimerStat;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> commentList;


    public CommentsAdapter(List<Comment> comments) {

        commentList = comments;
    }
    public String uriPicture="";

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_list_row, parent, false);

        return new CommentViewHolder(itemView);


    }

    @Override
    public void onBindViewHolder(@NonNull final CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference problemImage = mStorageRef.child("Avatars").child(comment.userId);
        problemImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //pPicasso.get().load(uri).into(picture);
                uriPicture = uri.toString();
                Picasso.get().load(uriPicture).into(holder.profileImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });


        ref.child("users").child(comment.userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                final User loadedUser = dataSnapshot.getValue(User.class);
                holder.userNameLastName.setText(loadedUser.first_name+" "+loadedUser.last_name);



            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)                                        {

            }
        });


        Long currentTime = System.currentTimeMillis();
        //DateUtils.getRelativeTimeSpanString(comment.time, currentTime,DateUtils.MINUTE_IN_MILLIS);
        holder.timeAgo.setText(DateUtils.getRelativeTimeSpanString(comment.time, currentTime,DateUtils.SECOND_IN_MILLIS).toString());

        holder.timeAgo.setText(DateUtils.getRelativeTimeSpanString(comment.time, currentTime,
                DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_NO_NOON));

        holder.commentText.setText(comment.text);
        //za sliku i za ostalo da se pribavi user
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }


    public class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView userNameLastName, commentText,timeAgo;
        public ImageView profileImage;

        public CommentViewHolder(View view) {
            super(view);
            userNameLastName = view.findViewById(R.id.nameUser);
            commentText =  view.findViewById(R.id.commentText);
            profileImage =  view.findViewById(R.id.profileImage);
            timeAgo = view.findViewById(R.id.textView10);
        }

    }




}
