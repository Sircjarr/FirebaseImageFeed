package com.example.cliff.firebaseimagefeed;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

    private static final String TAG = "UploadActivity";

    private ProgressDialog mProgressDialog;

    Button btnUploadFromGallery;
    ImageView ivImage;
    Button btnPost;
    Button btnUpdateProfilePicture;

    byte[] byteArray;

    private String userID;

    public static final int GET_FROM_GALLERY = 3;
    private StorageReference mStorageRef;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        btnUploadFromGallery = (Button) view.findViewById(R.id.btnUploadFromGallery);
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        btnPost = (Button) view.findViewById(R.id.btnPost);
        btnUpdateProfilePicture = (Button) view.findViewById(R.id.btnUpdateProfilePicture);

        mProgressDialog = new ProgressDialog(getActivity());

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

                    userID = ((NavigationActivity)getActivity()).user.getUid();
                    Log.d(TAG, "onClick: userID " + userID);

                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    long name = System.currentTimeMillis();
                    StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/" + name + ".jpg");
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
            }
        });
        return view;
    }

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

    public void writeURLReferenceToDatabase(String url) {
        database = FirebaseDatabase.getInstance();

        // get the current user's username
        final String resultURL = url;
        final DatabaseReference usernameReference = database.getReference();
        usernameReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username = dataSnapshot.child("users").child(userID).child("username").getValue(String.class);

                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> userImages = dataSnapshot.child("user_images").child(username).getValue(t);

                if (userImages == null) {
                    userImages = new ArrayList<String>();
                }
                userImages.add(resultURL);
                usernameReference.child("user_images").child(username).setValue(userImages);
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
