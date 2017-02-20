package com.campusmail.campusmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ViewProfileDMChatActivity extends AppCompatActivity {

    private TextView mPostName, mPostLocation, mPostCommunity, mPostGender, mPostSkill, mPostBio, mPostWeb, mPostPhone, mPostYear, mPostCampus;
    private ImageView mPostImage, mSendDM;

    private String mPostKey = null;
    private DatabaseReference mDatabaseComments, mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostCommunity = (TextView) findViewById(R.id.post_community);
        mPostLocation = (TextView) findViewById(R.id.post_location);
        mPostName = (TextView) findViewById(R.id.post_name);
        mPostSkill = (TextView) findViewById(R.id.post_skills);
        mPostBio = (TextView) findViewById(R.id.post_bio);
        mPostWeb = (TextView) findViewById(R.id.post_web);
        mPostPhone = (TextView) findViewById(R.id.post_phone);
        mPostYear = (TextView) findViewById(R.id.post_year);
        mPostGender = (TextView) findViewById(R.id.post_gender);
        mPostCampus = (TextView) findViewById(R.id.post_campus);

        mPostImage = (ImageView) findViewById(R.id.post_image);
        mSendDM = (ImageView) findViewById(R.id.sendDMBtn);
        mSendDM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cardonClick = new Intent(ViewProfileDMChatActivity.this, SendDMChat.class);
                cardonClick.putExtra("heartraise_id", mPostKey );
                startActivity(cardonClick);
            }
        });




        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("DM_Chat");
        mDatabaseComments.keepSynced(true);
        mDatabaseComments.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String user_id = (String) dataSnapshot.child("uid").getValue();

                mDatabaseUsers.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String post_name = (String) dataSnapshot.child("name").getValue();
                        String post_image = (String) dataSnapshot.child("image").getValue();
                        String post_community = (String) dataSnapshot.child("community").getValue();
                        String post_location = (String) dataSnapshot.child("location").getValue();
                        String post_bio = (String) dataSnapshot.child("bio").getValue();
                        String post_skills = (String) dataSnapshot.child("skills").getValue();
                        String post_web = (String) dataSnapshot.child("web").getValue();
                        String post_phone = (String) dataSnapshot.child("phone").getValue();
                        String post_campus = (String) dataSnapshot.child("campus").getValue();
                        String post_gender = (String) dataSnapshot.child("gender").getValue();
                        String post_year = (String) dataSnapshot.child("year").getValue();

                        mPostName.setText(post_name);
                        mPostCommunity.setText(post_community);
                        mPostLocation.setText(post_location);
                        mPostBio.setText(post_bio);
                        mPostSkill.setText(post_skills);
                        mPostWeb.setText(post_web);
                        mPostPhone.setText(post_phone);
                        mPostCampus.setText(post_campus);
                        mPostGender.setText(post_gender);
                        mPostYear.setText(post_year);

                        Picasso.with(ViewProfileDMChatActivity.this).load(post_image).into(mPostImage);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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


        }

        return super.onOptionsItemSelected(item);
    }

}
