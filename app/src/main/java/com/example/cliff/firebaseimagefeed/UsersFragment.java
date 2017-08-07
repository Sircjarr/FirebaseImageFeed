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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cliff.firebaseimagefeed.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private static final String TAG = "UsersFragment";

    private ListView listView;

    // Database variables
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_users, container, false);

        listView = (ListView) view.findViewById(R.id.listView);

        // Get the instance of the database
        database = FirebaseDatabase.getInstance();
        // Reference object to access the database
        myRef = database.getReference("users");

        myRef.addValueEventListener(new ValueEventListener() {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                generateList(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        return view;
    }

    public void generateList(DataSnapshot dataSnapshot) {
        final List<String> list = new ArrayList<String>();

        // Gets the snapshot of all userId data, which are the children of 'users' reference
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            // Where username is a child of the userId ds
            String username = ds.child("username").getValue(String.class);
            list.add(username);
        }

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), UserActivity.class);
                intent.putExtra("username", list.get(position));
                startActivity(intent);
            }
        });
    }
}