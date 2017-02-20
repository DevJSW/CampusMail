package com.campusmail.campusmail;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class OpenLetterActivity extends AppCompatActivity {

    private ImageView  mPostImage;
    private TextView  mPostDate, mPostTitle, mPostStory;

    String mPostKey = null;
    private String mUidKey = null;
    private DatabaseReference mDatabase;
    private boolean mProcessLike = false;
    private FirebaseAuth auth;
    private DatabaseReference mDatabaseLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_letter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();

        mPostImage = (ImageView) findViewById(R.id.post_image);
        mPostDate = (TextView) findViewById(R.id.post_date);
        mPostStory = (TextView) findViewById(R.id.post_story);
        mPostTitle = (TextView) findViewById(R.id.post_title);


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Letters");
        mDatabase.keepSynced(true);

        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_date = (String) dataSnapshot.child("time").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();
                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();
                String post_story = (String) dataSnapshot.child("story").getValue();

                mPostStory.setText(post_story);
                mPostTitle.setText(post_title);
                mPostDate.setText(post_date);
                Picasso.with(OpenLetterActivity.this).load(post_image).into(mPostImage);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }
}
