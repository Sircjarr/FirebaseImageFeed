package com.example.cliff.firebaseimagefeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.cliff.firebaseimagefeed.Model.CurrentUser;
import com.example.cliff.firebaseimagefeed.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
 *This Activity contains three fragments
 *All fragments must be part of an Activity
 *These fragments will have access to all of the data within this Activity
 *Fragments are useful for things like navigation bars, which remain on-screen when swapping them
 *These fragments will have access to the AuthListener coded here
 */

public class NavigationActivity extends AppCompatActivity {

    private static final String TAG = "NavigationActivity";

    public FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    public FirebaseUser fbUser;

    FirebaseDatabase navDatabase;
    DatabaseReference navReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fbUser = firebaseAuth.getCurrentUser();
                if (fbUser != null) {
                    // User is signed in
                } else {
                    // User is signed out
                    startActivity(new Intent(NavigationActivity.this, MainActivity.class));
                    makeToast("Signed out");
                    finish();
                }
            }
        };

        // Get a reference to the current user's data to be accessed elsewhere
        initCurrentUserInfo();

        // Load initial fragment into view
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, new UsersFragment()).commit();

        // Setup BottomNavigationView
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Setup Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitle("Users");
    }

    private void initCurrentUserInfo() {

        // Get the current user's userID
        fbUser = auth.getCurrentUser();
        final String userID = fbUser.getUid();

        // Read in an object from the database
        navDatabase = FirebaseDatabase.getInstance();
        navReference = navDatabase.getReference("users");

        navReference.addValueEventListener(new ValueEventListener() {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.
            @Override
            public void onDataChange(DataSnapshot ds) {
                User currentUserInfo = ds.child(userID).getValue(User.class);

                CurrentUser.ID = currentUserInfo.getUserID();
                CurrentUser.USERNAME = currentUserInfo.getUsername();
                CurrentUser.EMAIL = currentUserInfo.getEmail();
                CurrentUser.PROFILE_URL = currentUserInfo.getProfileURL();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.users:
                    fragment = new UsersFragment();
                    setTitle("Users");
                    break;
                case R.id.upload:
                    fragment = new UploadFragment();
                    setTitle("Upload");
                    break;
                case R.id.current_user:
                    fragment = new CurrentUserFragment();
                    setTitle(CurrentUser.USERNAME);
                    break;
            }

            // Swap fragment out of the FrameLayout located in activity_navigation.xml
            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content, fragment).commit();
                return true;
            }
            return false;
        }

    };

    // Logout menu in appActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.logout:
                auth.signOut();
                startActivity(new Intent(this, MainActivity.class));
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
