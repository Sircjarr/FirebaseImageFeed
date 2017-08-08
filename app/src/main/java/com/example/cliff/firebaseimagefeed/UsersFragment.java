package com.example.cliff.firebaseimagefeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.cliff.firebaseimagefeed.Model.UserPreview;
import com.example.cliff.firebaseimagefeed.Util.PreviewListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// This class shows a list of all users with their profile pictures
// Clicking on them will take you to that user's uploaded images

public class UsersFragment extends Fragment {

    private static final String TAG = "UsersFragment";

    private ListView listView;
    private ProgressBar progressBar;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_users, container, false);

        listView = (ListView) view.findViewById(R.id.listView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        createListFromAllUserData();

        return view;
    }

    public void createListFromAllUserData() {

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Ensure that the listener code does not run if there is no Activity
                if (getActivity() != null) {
                    generateList(dataSnapshot);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void generateList(DataSnapshot dataSnapshot) {

        final List<UserPreview> list = new ArrayList<>();

        // Gets the snapshot of all userId data, which are the children of 'users' reference
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            // Where username is a child of the userId ds
            String username = ds.child("username").getValue(String.class);
            String profileImageURL = ds.child("profileURL").getValue(String.class);

            list.add(new UserPreview(username, profileImageURL));
        }

        // Set custom adapter
        PreviewListAdapter adapter = new PreviewListAdapter(getActivity(), R.layout.profile_preview_row, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), UserActivity.class);
                intent.putExtra("username", list.get(position).getUsername());
                startActivity(intent);
            }
        });
    }
}