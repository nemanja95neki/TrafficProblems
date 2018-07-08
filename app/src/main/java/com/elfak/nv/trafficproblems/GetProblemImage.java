package com.elfak.nv.trafficproblems;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class GetProblemImage {
    public Uri uriii;
    public  GetProblemImage()
    {

    }
    public Uri getImage(Problem p)
    {
        final StorageReference problemImage = FirebaseStorage.getInstance().getReference("Problems").child(p.key);
        problemImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //pPicasso.get().load(uri).into(picture);
                uriii = uri;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
        return uriii;
    }




}
