package com.example.cliff.firebaseimagefeed;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.cliff.firebaseimagefeed.Model.CurrentUser;
import com.example.cliff.firebaseimagefeed.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadFragment extends Fragment {

    private static final String TAG = "UploadFragment";

    private ProgressDialog mProgressDialog;

    private Button btnUploadFromGallery;
    private ImageView ivImage;
    private Button btnPost;
    private Button btnUpdateProfilePicture;

    // For transferring bitmaps
    private byte[] byteArray;

    // Request code
    public static final int GET_FROM_GALLERY = 3;

    private StorageReference storageRef;
    private FirebaseDatabase database;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        btnUploadFromGallery = (Button) view.findViewById(R.id.btnUploadFromGallery);
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        btnPost = (Button) view.findViewById(R.id.btnPost);
        btnUpdateProfilePicture = (Button) view.findViewById(R.id.btnUpdateProfilePicture);

        // Create a progress dialog for uploading images
        mProgressDialog = new ProgressDialog(getActivity());

        // Code to retrieve an image from the phone's gallery
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

                    mProgressDialog.setMessage("Uploading image...");
                    mProgressDialog.show();

                    // Using FirebaseStorage is similar to using FirebaseDatabase
                    long name = System.currentTimeMillis();
                    storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference storageReference = storageRef.child("images/users/" + CurrentUser.ID + "/" + name + ".jpg");

                    storageReference.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Get a URL to the uploaded content
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            writeURLReferenceToDatabase(downloadUrl.toString());

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
                else {
                    makeToast("Upload an image");
                }
            }
        });

        btnUpdateProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (ivImage.getDrawable() != null) {

                    mProgressDialog.setMessage("Uploading image...");
                    mProgressDialog.show();

                    storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference storageReference = storageRef.child("profile_images/users/" + CurrentUser.ID + "/profile.jpg");

                    storageReference.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Get a URL to the uploaded content
                            @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            writeProfileURLReferenceToDatabase(downloadUrl.toString());

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
               else {
                   makeToast("Upload an image");
               }
            }
        });

        return view;
    }

    // Run when a user selects a photo in their phone's gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

                mProgressDialog.setMessage("Getting image...");
                mProgressDialog.show();

                // Load in a Bitmap with Glide
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

    // Convert a Bitmap into a byte[] so Glide can load it in the view
    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byteArray = stream.toByteArray();
        return byteArray;
    }

    // ArrayLists with FirebaseDatabase
    public void writeURLReferenceToDatabase(String url) {

        final String resultURL = url;

        database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference("user_images");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get an ArrayList from the database with GenericTypeIndicator<>
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> userImages = dataSnapshot.child(CurrentUser.USERNAME).getValue(t);

                if (userImages == null) {
                    // User's first uploaded image
                    userImages = new ArrayList<String>();
                }

                userImages.add(resultURL);

                // Overwrite ArrayList with the new one
                databaseReference.child(CurrentUser.USERNAME).setValue(userImages);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void writeProfileURLReferenceToDatabase(String url) {
        final String resultURL = url;

        database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference("users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {

                // Read in the current user's class
                User user = ds.child(CurrentUser.ID).getValue(User.class);

                // Update the profileURL
                user.setProfileURL(resultURL);

                // Overwrite the class with the updated one
                databaseReference.child(CurrentUser.ID).setValue(user);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void makeToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
