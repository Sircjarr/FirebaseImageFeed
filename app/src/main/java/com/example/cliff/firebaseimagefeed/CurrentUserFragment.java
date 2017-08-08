package com.example.cliff.firebaseimagefeed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cliff.firebaseimagefeed.Model.CurrentUser;
import com.example.cliff.firebaseimagefeed.Model.UserImage;
import com.example.cliff.firebaseimagefeed.Util.UserListAdapter;
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

import java.util.ArrayList;
import java.util.List;

// Display the current user's images in a list
// Clicking on these images will delete them from Firebase Storage and Database

public class CurrentUserFragment extends Fragment {

    private static final String TAG = "CurrentUserFragment";

    private ListView lvUserImages;
    private TextView tvNoImages;

    private FirebaseDatabase cufDatabase;
    private DatabaseReference cufDatabaseReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_user, container, false);

        lvUserImages = (ListView) view.findViewById(R.id.lvUserImages);
        tvNoImages = (TextView) view.findViewById(R.id.tvNoImages);

        cufDatabase = FirebaseDatabase.getInstance();
        cufDatabaseReference = cufDatabase.getReference("user_images");

        cufDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {

                // Read in ArrayList from the Database
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                final List<String> userImagesURL = ds.child(CurrentUser.USERNAME).getValue(t);

                if (userImagesURL == null) {
                    // user has not uploaded images
                    tvNoImages.setVisibility(View.VISIBLE);
                }
                else {
                    // Create the List<UserImage
                    final List<UserImage> userImages = new ArrayList<>();
                    for (int i = 0; i < userImagesURL.size(); i++) {
                        userImages.add(new UserImage(userImagesURL.get(i)));
                    }

                    // Set the custom adapter
                    final UserListAdapter adapter = new UserListAdapter(getActivity(), R.layout.user_image_row, userImages);
                    lvUserImages.setAdapter(adapter);

                    lvUserImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            // Update Storage and the Database
                            deleteFromStorage(userImagesURL.get(position));
                            deleteFromDatabase(position);

                            // Update the ArrayLists
                            userImagesURL.remove(position);
                            userImages.remove(position);
                            adapter.notifyDataSetChanged();

                            makeToast("Image deleted");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    public void deleteFromStorage(String URLToDelete) {

        // Get FirebaseStorage instance and Reference
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference imageRef = firebaseStorage.getReferenceFromUrl(URLToDelete);

        // Delete the item defined in imageRef
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.d(TAG, "onSuccess: deleted file");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.d(TAG, "onFailure: did not delete file");
            }
        });
    }

    public void deleteFromDatabase(int position) {
        final int resultPosition = position;

        cufDatabase = FirebaseDatabase.getInstance();
        cufDatabaseReference = cufDatabase.getReference();

        cufDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get the ArrayList of images
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                };
                List<String> userImages = dataSnapshot.child("user_images").child(CurrentUser.USERNAME).getValue(t);

                // update the ArrayList
                userImages.remove(resultPosition);

                // Overwrite the ArrayList with the updated one
                cufDatabaseReference.child("user_images").child(CurrentUser.USERNAME).setValue(userImages);
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
