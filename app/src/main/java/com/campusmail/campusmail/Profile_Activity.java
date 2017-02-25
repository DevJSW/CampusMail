package com.campusmail.campusmail;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Profile_Activity extends AppCompatActivity {

    private TextView mPostName, mPostLocation, mPostCommunity, mPostGender, mPostFaculty, mPostSkill, mPostBio, mPostWeb, mPostPhone, mPostYear, mPostCampus, mHomeBtn;
    private ImageView mPostImage, mEditBtn;
    private String community_id = null;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private Query mQueryPostComment;
    private ProgressBar mProgressBar;
    String post_community = null;
    ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Context context = this;


        mEditBtn = (ImageView) findViewById(R.id.editBtn);
        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile_Activity.this, ProfileEditActivity.class));
            }
        });

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
        mPostFaculty = (TextView) findViewById(R.id.post_faculty);
        mPostImage = (ImageView) findViewById(R.id.post_image);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Letters");


        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);

        checkForNetwork();
        checkUserExists();

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                community_id = (String) dataSnapshot.child("community").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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
                String post_faculty = (String) dataSnapshot.child("faculty").getValue();

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
                mPostFaculty.setText(post_faculty);

                Picasso.with(Profile_Activity.this).load(post_image).into(mPostImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkUserExists() {

        mProgressBar.setVisibility(View.VISIBLE);
        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (!dataSnapshot.hasChild(user_id)) {

                    mProgressBar.setVisibility(View.GONE);

                    Intent intent = new Intent(Profile_Activity.this, EditActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }else {

                    mProgressBar.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void checkForNetwork() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();

        if (ninfo != null && ninfo.isConnected()) {


        } else {


            Toast.makeText(Profile_Activity.this, "You are not connected to Internet... Please Enable Internet!", Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
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
        if (id == R.id.action_sentmails) {

            Intent cardonClick = new Intent(Profile_Activity.this, ProfileActivity.class);
            cardonClick.putExtra("community_id", community_id );
            startActivity(cardonClick);

            return true;
        }
        if (id == R.id.action_home) {

            Intent cardonClick = new Intent(Profile_Activity.this, MainActivity.class);
            cardonClick.putExtra("community_id", community_id );
            startActivity(cardonClick);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}

