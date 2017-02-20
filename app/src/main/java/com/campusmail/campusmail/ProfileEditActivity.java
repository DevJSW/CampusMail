package com.campusmail.campusmail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private ImageView mPostUserimg, mPostImage;
    private TextView  mPostCommunity, mPostGender,mPostYear, mPostCampus, mPostLocation, mPostFaculty;
    private EditText mPostName, mPostSkills, mPostWeb, mPostBio, mPostPhone;
    private Button mSaveBtn;
    String country_id = null;

    private DatabaseReference mDatabaseUsers;
    private String mDatabaseUid;
    private StorageReference mStorage;
    private Uri mImageUri = null;
    private FirebaseAuth auth;
    private String mCurrentUser;
    private ProgressDialog mprogress;
    private Uri mPlaceholderUri = null;
    private static final int GALLERY_REQUEST =1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //   final String country_id = getIntent().getExtras().getString("country_id");

        mPostCommunity = (TextView) findViewById(R.id.post_community);
        mPostYear = (TextView) findViewById(R.id.post_year);
        mPostGender = (TextView) findViewById(R.id.post_gender);
        mPostCampus = (TextView) findViewById(R.id.post_campus);
        mPostLocation = (TextView) findViewById(R.id.post_location);
        mPostYear = (TextView) findViewById(R.id.post_year);
        mPostGender = (TextView) findViewById(R.id.post_gender);
        mPostFaculty = (TextView) findViewById(R.id.post_gender);

        mprogress = new ProgressDialog(this);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        auth = FirebaseAuth.getInstance();
        // mCurrentUser = auth.getCurrentUser().getUid();


        mPostUserimg = (ImageView) findViewById(R.id.post_userimg);
        mPostUserimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mPostName = (EditText) findViewById(R.id.post_name);
        mPostSkills = (EditText) findViewById(R.id.post_skills);
        mPostBio = (EditText) findViewById(R.id.post_bio);
        mPostPhone = (EditText) findViewById(R.id.post_phone);
        mPostWeb = (EditText) findViewById(R.id.post_web);
        mPostCampus = (EditText) findViewById(R.id.post_campus);

        //mPostImage = (ImageView) findViewById(R.id.post_image);

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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
                mPostBio.setText(post_bio);
                mPostSkills.setText(post_skills);
                mPostWeb.setText(post_web);
                mPostPhone.setText(post_phone);
                mPostCommunity.setText(post_community);
                mPostCampus.setText(post_campus);
                mPostGender.setText(post_gender);
                mPostLocation.setText(post_location);
                mPostYear.setText(post_year);
                mPostFaculty.setText(post_faculty);

//                Picasso.with(EditActivity.this).load(post_image).into(mPostImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void startPosting() {

        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
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


                final String name = mPostName.getText().toString().trim();
                final String phone = mPostPhone.getText().toString().trim();
                final String web = mPostWeb.getText().toString().trim();
                final String skills = mPostSkills.getText().toString().trim();
                final String bio = mPostBio.getText().toString().trim();

                final String user_id = auth.getCurrentUser().getUid();
                final String uid = user_id.substring(0, Math.min(user_id.length(), 5));

                if (mImageUri == null) {
                    Toast.makeText(getApplicationContext(), "Add profile image!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mImageUri != null) {

                    mprogress.setMessage("Editing ...");
                    mprogress.show();

                    StorageReference filepath = mStorage.child("Profile_images").child(mImageUri.getLastPathSegment());


                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            //final DatabaseReference newPost = mDatabaseUsers.push();


                            mDatabaseUsers.child(user_id).child("name").setValue(name);
                            mDatabaseUsers.child(user_id).child("web").setValue(web);
                            mDatabaseUsers.child(user_id).child("phone").setValue(phone);
                            mDatabaseUsers.child(user_id).child("skills").setValue(skills);
                            mDatabaseUsers.child(user_id).child("bio").setValue(bio);

                            mDatabaseUsers.child("community").setValue(dataSnapshot.child("community").getValue());
                            mDatabaseUsers.child("faculty").setValue(dataSnapshot.child("faculty").getValue());
                            mDatabaseUsers.child("location").setValue(dataSnapshot.child("location").getValue());
                            mDatabaseUsers.child("campus").setValue(dataSnapshot.child("campus").getValue());
                            mDatabaseUsers.child("year").setValue(dataSnapshot.child("year").getValue());

                            //mDatabaseUsers.child(user_id).child("location").setValue(country);
                            mDatabaseUsers.child(user_id).child("image").setValue(downloadUrl.toString());
                            mDatabaseUsers.child(user_id).child("uid").setValue(user_id);
                            mDatabaseUsers.child(user_id).child("campusmail_uid").setValue(uid);



                            mprogress.dismiss();

                            Intent cardonClick = new Intent(ProfileEditActivity.this, Profile_Activity.class);
                            cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(cardonClick);

                        }
                    });


                }



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
        if (id == R.id.action_save) {
            startPosting();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();

            mPostUserimg.setImageURI(mImageUri);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }
}
