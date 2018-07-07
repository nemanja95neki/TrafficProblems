package com.elfak.nv.trafficproblems;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private TextView name, last_name, email, phone_number;
    private ImageButton editButton;
    private String userID;
    private TextView sideMenuEmail, sideMenuName;
    private UserLocalStore userLocalStore;
    private UserAvatarStore userAvatarStore;
    private User userInfo;
    private StorageReference mStorageRef;
    private ImageView imageView,imageSideMenu;
    private Bitmap avatar;
    String user_id;
    User openedUser;
    int profileCase = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        findViewById(R.id.includeMainView).setVisibility(View.INVISIBLE);
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

        name = (TextView)findViewById(R.id.name);
        last_name = (TextView)findViewById(R.id.last_name);
        email = (TextView)findViewById(R.id.email_address);
        phone_number = (TextView)findViewById(R.id.phone_number);
        editButton = (ImageButton)findViewById(R.id.edit);
        imageView = (ImageView) findViewById(R.id.imageView);

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

        try{
            Intent listIntent = getIntent();
            Bundle bundle = listIntent.getExtras();
            profileCase = bundle.getInt("case");
            if(profileCase == 2)
                user_id = bundle.getString("user_id");
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
        if(profileCase == 1) {
            name.setText("Name: " + userInfo.first_name);
            last_name.setText("Last name: " + userInfo.last_name);
            email.setText("Email: " + userInfo.email);
            phone_number.setText("Phone number: " + userInfo.phone_number);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editProfile = new Intent(Profile.this, EditProfile.class);
                    startActivityForResult(editProfile, 1);
                }
            });
        }
        else if(profileCase == 2 && !user_id.equals(""))
        {
            databaseReference = FirebaseDatabase.getInstance().getReference("users");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    openedUser = dataSnapshot.child(user_id).getValue(User.class);
                    openedUser.key = user_id;
                    name.setText("Name: " + openedUser.first_name);
                    last_name.setText("Last name: " + openedUser.last_name);
                    email.setText("Email: " + openedUser.email);
                    phone_number.setText("Phone number: " + openedUser.phone_number);
                    editButton.setVisibility(View.INVISIBLE);

                    try {
                        setProfilePicture(user_id);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //Toast.makeText(ProfileActivity.this,"Something went wrong. Please try again...",Toast.LENGTH_SHORT).show();
                }
            });
        }


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
                Intent intent = new Intent(Profile.this, ProblemsList.class);
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
                Intent login = new Intent(Profile.this,LoginActivity.class);
                startActivity(login);
                return true;
            }
        });

        editProfile.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent editProfile = new Intent(Profile.this, EditProfile.class);
                startActivityForResult(editProfile,1);
                return true;
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(avatar!=null) {
            avatar = userAvatarStore.getUserAvatar();
            if(profileCase == 1)
                imageView.setImageBitmap(avatar);
            imageSideMenu.setImageBitmap(avatar);
        }
        /*try {
            setProfilePicture();
            //setProfilePictureSideMenu();

        }catch (IOException e){
            e.printStackTrace();
        }*/
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String returned_name = data.getStringExtra("name");
                String returned_last_name = data.getStringExtra("last_name");
                String returned_email = data.getStringExtra("email");
                String returned_password = data.getStringExtra("password");
                String returned_phone_number = data.getStringExtra("phone_number");

                name.setText("Name: " + returned_name);
                last_name.setText("Last name: " + returned_last_name);
                email.setText("Email: " + returned_email);
                phone_number.setText("Phone number: " + returned_phone_number);

                sideMenuEmail.setText(returned_email);
                sideMenuName.setText(returned_name + " " + returned_last_name);
                /*avatar = userAvatarStore.getUserAvatar();

                if(avatar!=null) {
                    imageView.setImageBitmap(avatar);
                    imageSideMenu.setImageBitmap(avatar);
                }*/

               /* try {
                    setProfilePicture();
                    //setProfilePictureSideMenu();

                }catch (IOException e){
                    e.printStackTrace();
                }*/
            }
        }
    }

    private void setProfilePicture(String user_key) throws IOException {
        final File localFile = File.createTempFile("images", "jpg");
        StorageReference profileRef = mStorageRef.child("Avatars").child(user_key);
        profileRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        String imageLocation = localFile.getAbsolutePath();
                        rotateImage(setReducedImageSize(imageLocation), imageLocation);
                        imageSideMenu.setImageBitmap(BitmapFactory.decodeFile(imageLocation));
                        //imageView.setImageBitmap(bmp);
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
        int targetImageViewWidth = imageView.getWidth();
        int targetImageViewHeight = imageView.getHeight();

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
        imageView.setImageBitmap(rotatedBmp);
    }

    private void setProfilePictureSideMenu() throws IOException {
        final File localFile = File.createTempFile("imageSideMenu", "jpg");
        final StorageReference profileRef = mStorageRef.child("Avatars").child(userInfo.key);
        profileRef.getFile(localFile)
                .addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        //When the image has successfully uploaded, get its download URL
                        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri dlUri = uri;
                                Picasso.get().load(uri).fit().into(imageSideMenu);
                            }
                        });
                    }
                });
    }

}
