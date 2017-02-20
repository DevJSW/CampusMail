package com.campusmail.campusmail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Date;

public class SendDMChat extends AppCompatActivity {

    private String mPostKey = null;

    private ImageView mPostImage;
    private Button mPostBtn;
    private EditText mPostStory, mPostTitle;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseUID;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseComments;
    private FirebaseAuth auth;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;
    private StorageReference mStorage;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_dmcomments);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Directmails");
        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("DM_Chat");
        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        mPostStory = (EditText) findViewById(R.id.post_story);
        mPostTitle = (EditText) findViewById(R.id.post_title);

        mProgress = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();

        mPostImage = (ImageView) findViewById(R.id.post_userimg);
        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mPostBtn = (Button) findViewById(R.id.postBtn);
        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

    }

    private void startPosting() {

        mProgress.setMessage("Posting...");

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        final String story_val = mPostStory.getText().toString().trim();
        final String title_val = mPostTitle.getText().toString().trim();

        final String user_id = auth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(story_val) && !TextUtils.isEmpty(title_val)) {

            mProgress.show();

            if ( mImageUri != null) {

                StorageReference filepath = mStorage.child("Direct mail_images").child(mImageUri.getLastPathSegment());

                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        final DatabaseReference newPost = mDatabase.push();


                        mDatabaseComments.child(mPostKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //newPost.child("title").setValue(title_val);
                                newPost.child("story").setValue(story_val);
                                newPost.child("title").setValue(title_val);
                                newPost.child("photo").setValue(downloadUrl.toString());
                                newPost.child("sender_uid").setValue(user_id);
                                newPost.child("reciever_uid").setValue(dataSnapshot.child("uid").getValue());
                                newPost.child("time").setValue(stringDate);

                                AlertDialog.Builder builder = new AlertDialog.Builder(SendDMChat.this);
                                builder.setTitle("Direct mail Alert!");
                                builder.setMessage("Your Letter has been sent SUCCESSFULLY!")
                                        .setCancelable(true)
                                        .setPositiveButton("OK", null);
                                AlertDialog dialog = builder.create();
                                dialog.show();


                                mProgress.dismiss();

                                //startActivity(new Intent(PostActivity.this, MainActivity.class));


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });
            } else {
                final DatabaseReference newPost = mDatabase.push();

                mDatabaseComments.child(mPostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //newPost.child("title").setValue(title_val);
                        newPost.child("story").setValue(story_val);
                        newPost.child("title").setValue(title_val);
                        newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                        newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                        newPost.child("community").setValue(dataSnapshot.child("community").getValue());
                        newPost.child("faculty").setValue(dataSnapshot.child("faculty").getValue());
                        newPost.child("location").setValue(dataSnapshot.child("location").getValue());
                        newPost.child("campus").setValue(dataSnapshot.child("campus").getValue());
                        newPost.child("sender_uid").setValue(user_id);
                        newPost.child("reciever_uid").setValue(dataSnapshot.child("uid").getValue());

                        AlertDialog.Builder builder = new AlertDialog.Builder(SendDMChat.this);
                        builder.setTitle("Post Alert!");
                        builder.setMessage("Your Letter has been posted SUCCESSFULLY!")
                                .setCancelable(true)
                                .setPositiveButton("OK", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();


                        mProgress.dismiss();

                        //startActivity(new Intent(PostActivity.this, MainActivity.class));


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_menu, menu);
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
                if (id == R.id.action_settings) {

                    mPostImage.setVisibility(View.VISIBLE);
                }

                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();
            mPostImage.setImageURI(mImageUri);

        }
    }

}
