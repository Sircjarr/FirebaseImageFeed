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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    EditText etEmail, etPassword, etUsername;
    TextView tvSignInOrSignUp;
    Button btnSignInOrSignUp;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private boolean signInMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btnSignInOrSignUp = (Button) findViewById(R.id.btnSignInOrSignUp);
        tvSignInOrSignUp = (TextView) findViewById(R.id.tvSignUpOrSignIn);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    makeToast("Signed in");
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    makeToast("Signed out");
                }
                // ...
            }
        };

        tvSignInOrSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (signInMode) {
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

    public void btnSignInOrSignUp(View view) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        if (email.trim().length() > 0 && password.trim().length() > 0) {
            mAuth.signInWithEmailAndPassword(email, password);
            Intent intent = new Intent(this, NavigationActivity.class);
            startActivity(intent);
        }
        else {
            makeToast("E-mail or password cannot be blank");
        }
    }

    private void makeToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
