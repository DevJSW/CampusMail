package com.campusmail.campusmail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class OpenLetterWithPhotoActivity extends AppCompatActivity {

    private ImageView mPostPhoto;
    private TextView mPostDate;

    String mPostKey = null;
    private String mUidKey = null;
    private DatabaseReference mDatabase;
    private boolean mProcessLike = false;
    private FirebaseAuth auth;
    private DatabaseReference mDatabaseLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_letter_with_photo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();

        mPostPhoto = (ImageView) findViewById(R.id.post_photo);
        mPostDate = (TextView) findViewById(R.id.post_date);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabase.keepSynced(true);

        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_date = (String) dataSnapshot.child("time").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();
                String post_photo = (String) dataSnapshot.child("photo").getValue();



                mPostDate.setText(post_date);
                Picasso.with(OpenLetterWithPhotoActivity.this).load(post_photo).into(mPostPhoto);

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
