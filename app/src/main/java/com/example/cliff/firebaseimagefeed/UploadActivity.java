package com.example.cliff.firebaseimagefeed;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UploadActivity extends AppCompatActivity {

    private static final String TAG = "UploadActivity";

    private ProgressDialog mProgressDialog;

    Button btnUploadFromGallery;
    ImageView ivImage;
    Button btnPost;

    byte[] byteArray;

    static String username;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser user;

    public static final int GET_FROM_GALLERY = 3;
    private StorageReference mStorageRef;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        btnUploadFromGallery = (Button) findViewById(R.id.btnUploadFromGallery);
        ivImage = (ImageView) findViewById(R.id.ivImage);
        btnPost = (Button) findViewById(R.id.btnPost);

        mProgressDialog = new ProgressDialog(UploadActivity.this);

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    makeToast("Signed in 3");
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivity(new Intent(UploadActivity.this, MainActivity.class));
                    makeToast("Signed out 3");
                }
                // ...
            }
        };

        btnUploadFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI),
                        GET_FROM_GALLERY);
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivImage.getDrawable() != null) {

                    mProgressDialog.setMessage("Uploading Image...");
                    mProgressDialog.show();

                    user = auth.getCurrentUser();
                    String userID = user.getUid();

                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    long name = System.currentTimeMillis();
                    StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/" + name + ".jpg");
                    storageReference.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            storeURLReference(downloadUrl.toString());
                            makeToast("Upload Success");
                            mProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            makeToast("Upload Failed");
                            mProgressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                mProgressDialog.setMessage("Getting image...");
                mProgressDialog.show();
                Glide.with(this)
                        .load(bitmapToByte(bitmap))
                        .asBitmap()
                        .fitCenter() // scale to fit entire image within ImageView
                        .into(new BitmapImageViewTarget(ivImage) {
                            @Override
                            public void onResourceReady(Bitmap drawable, GlideAnimation anim) {
                                super.onResourceReady(drawable, anim);
                                mProgressDialog.hide();
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byteArray = stream.toByteArray();
        return byteArray;
    }

    public void storeURLReference(String url) {
        database = FirebaseDatabase.getInstance();
        final String resultURL = url;

        // Retrieve the username of the current user
        DatabaseReference userNameRef = database.getReference("users/" + user.getUid());
        userNameRef.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                  username = dataSnapshot.child("username").getValue(String.class);
                  myRef = database.getReference("user_images");
                  Log.d(TAG, "onDataChange: " + resultURL);
                  myRef.child(username).child(resultURL).setValue(resultURL);
              }

              // a different way to get the users name?
              @Override
              public void onCancelled(DatabaseError databaseError) {
                  Log.d(TAG, "onCancelled: " + databaseError.getMessage());
              }
          });

        /*
        while(username == null) {
            Log.d(TAG, "run: " + username);
        }
        Log.d(TAG, "storeURLReference: " + username);

        // Store a pointer to the image
        myRef = database.getReference("user_images");
        myRef.child(username).child(url).setValue(url);
        */
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
