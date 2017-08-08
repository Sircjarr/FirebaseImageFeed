package com.example.cliff.firebaseimagefeed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cliff.firebaseimagefeed.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// This class handles both registering and signing in with Firebase Authentication

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    EditText etEmail, etPassword, etUsername;
    TextView tvSignInOrSignUp;
    Button btnSignInOrSignUp;

    // Firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // Firebase Database
    public FirebaseDatabase mDatabase;
    public DatabaseReference mDatabaseReference;

    public String username;
    public String email;

    private boolean signInMode = true;
    private boolean newUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btnSignInOrSignUp = (Button) findViewById(R.id.btnSignInOrSignUp);
        tvSignInOrSignUp = (TextView) findViewById(R.id.tvSignUpOrSignIn);

        // Begin listening for registered users
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // FirebaseUser contains information about the authenticated user
                FirebaseUser mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    // User is signed in
                    if (!newUser) {
                        startActivity(new Intent(MainActivity.this, NavigationActivity.class));
                    }
                    else {
                        addUserToDatabase();
                        startActivity(new Intent(MainActivity.this, NavigationActivity.class));
                    }
                    makeToast("Signed in");
                    finish(); // Finish the current activity so that it will not stack.
                } else {
                    // User is signed out
                    makeToast("Signed out");
                }
            }
        };

        // Clickable TextView to handle logging in or signing up
        tvSignInOrSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (signInMode) {
                    newUser = false;
                    signInMode = false;
                    btnSignInOrSignUp.setText("Sign up");
                    tvSignInOrSignUp.setText("or, Sign in");
                    etUsername.setVisibility(View.VISIBLE);
                }
                else {
                    signInMode = true;
                    btnSignInOrSignUp.setText("Sign in");
                    tvSignInOrSignUp.setText("or, Sign up");
                    etUsername.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void btnSignInOrSignUp(View view) {
        if (signInMode) {
            // Sign user in
            email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.trim().length() > 0 && password.trim().length() > 0) {
                mAuth.signInWithEmailAndPassword(email, password);
            }
            else {
                makeToast("E-mail or password cannot be blank");
            }
        }
        else {
            // Register new user
            username = etUsername.getText().toString();
            email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.trim().length() > 0 && password.trim().length() > 0 && username.trim().length() > 0) {
                newUser = true;
                mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            makeToast("Auth failed");
                        }
                    }
                    });

            }
            else {
                makeToast("Fields must not be blank");
            }
        }
    }

    // After user is registered with Firebase Authentication,
    // Details about the user will be written to the FirebaseDatabase
    public void addUserToDatabase() {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference("users");

        FirebaseUser fbUser = mAuth.getCurrentUser();
        String userID = fbUser.getUid();
        User user = new User(userID, email, username);

        // Database > users > userID > User.Class
        mDatabaseReference.child(userID).setValue(user);
    }

    // Needed for every class with FirebaseAuth.AuthStateListener
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
