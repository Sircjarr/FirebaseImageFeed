package com.example.cliff.firebaseimagefeed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cliff.firebaseimagefeed.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private EditText etUsername;
    private Button btnCreateUser;

    private FirebaseUser user;
    private String userID;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etUsername = (EditText) view.findViewById(R.id.etUsername);
        btnCreateUser = (Button) view.findViewById(R.id.btnCreateUser);

        userID = ((NavigationActivity)getActivity()).mAuth.getCurrentUser().getUid();

        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                if (username.trim().length() > 0) {
                    final User newUser = new User(user.getEmail(), username);

                    // Add user to the database
                    database = FirebaseDatabase.getInstance();
                    myRef = database.getReference("users");
                    myRef.child(userID).setValue(newUser);
                }
                else {
                    makeToast("Must enter a username");
                }
            }
        });

        return view;
    }

    private void makeToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
