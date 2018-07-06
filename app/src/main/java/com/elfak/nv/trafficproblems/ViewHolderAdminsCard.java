package com.elfak.nv.trafficproblems;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ViewHolderAdminsCard extends RecyclerView.ViewHolder{

    View mView;
    public ViewHolderAdminsCard(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setDetails(String name, String last_name)
    {
       // ImageView admin_picture = mView.findViewById(R.id.admin_image_view);
        TextView admin_name = mView.findViewById(R.id.admin_name);
        TextView admin_last_name = mView.findViewById(R.id.admin_last_name);
       // ImageButton button_open_profile = mView.findViewById(R.id.open_profile);

        admin_name.setText("Name: " + name);
        admin_last_name.setText("Last name: " + last_name);
       // Picasso.get().load(image).into(admin_picture);

    }
}
