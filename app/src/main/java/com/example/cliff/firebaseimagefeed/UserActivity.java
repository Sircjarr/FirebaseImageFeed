package com.example.cliff.firebaseimagefeed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.cliff.firebaseimagefeed.Model.UserImage;
import com.example.cliff.firebaseimagefeed.Util.UserListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity{

    ListView lvUserImages;
    TextView tvNoImages;

    FirebaseDatabase uaDatabase;
    DatabaseReference uaDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        lvUserImages = (ListView) findViewById(R.id.lvUserImages);
        tvNoImages = (TextView) findViewById(R.id.tvNoImages);

        Intent intent = getIntent();
        final String username = intent.getStringExtra("username");

        uaDatabase = FirebaseDatabase.getInstance();
        uaDatabaseReference = uaDatabase.getReference("user_images");
        uaDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {

                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                List<String> userImagesURL = ds.child(username).getValue(t);

                if (userImagesURL == null) {
                    tvNoImages.setVisibility(View.VISIBLE);
                }
                else {
                    List<UserImage> userImages = new ArrayList<>();
                    for (int i = 0; i < userImagesURL.size(); i++) {
                        userImages.add(new UserImage(userImagesURL.get(i)));
                    }
                    UserListAdapter adapter = new UserListAdapter(UserActivity.this, R.layout.user_image_row, userImages);
                    lvUserImages.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        setTitle(username);
    }
}
