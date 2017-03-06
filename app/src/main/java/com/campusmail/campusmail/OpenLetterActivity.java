package com.campusmail.campusmail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class OpenLetterActivity extends AppCompatActivity {

    private ImageView  mPostImage, mChatBtn, mLikeBtn, mCall, mShareBtn;
    private TextView  mPostDate, mPostTitle, mPostStory, mLikeCount, mCommentCount;
    String mPostKey = null;
    private String mUidKey = null;
    private DatabaseReference mDatabase;
    private boolean mProcessLike = false;
    private FirebaseAuth auth;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseComment;
    private Query mQueryComments;
    private Query mQueryLetters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_letter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();

        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mPostImage = (ImageView) findViewById(R.id.post_image);
        mChatBtn = (ImageView) findViewById(R.id.chatBtn);
        mChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cardonClick = new Intent(OpenLetterActivity.this, CommentsActivity.class);
                        cardonClick.putExtra("heartraise_id", mPostKey );
                        startActivity(cardonClick);
                    }
                });

            }
        });

        mCall = (ImageView) findViewById(R.id.buttonCall);
        mShareBtn = (ImageView) findViewById(R.id.shareBtn);

        mPostDate = (TextView) findViewById(R.id.post_date);
        mPostStory = (TextView) findViewById(R.id.post_story);
        mPostTitle = (TextView) findViewById(R.id.post_title);
        mLikeCount = (TextView) findViewById(R.id.likeCount);
        mCommentCount = (TextView) findViewById(R.id.commentCount);


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Letters");
        mDatabase.keepSynced(true);

        mPostKey = getIntent().getExtras().getString("heartraise_id");
        mDatabaseLike.keepSynced(true);
        mDatabase.keepSynced(true);


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

        mDatabaseLike.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.equals(mPostKey)) {
                    mLikeCount.setText(dataSnapshot.getChildrenCount() + "");

                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mQueryComments = mDatabaseComment.orderByChild("post_key").equalTo(mPostKey);
        mQueryComments.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCommentCount.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseLike.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cardonClick = new Intent(OpenLetterActivity.this, ViewLetterProfileActivity.class);
                cardonClick.putExtra("heartraise_id", mPostKey );
                startActivity(cardonClick);
            }
        });


        mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProcessLike = true;

                mDatabaseLike.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(mProcessLike) {

                            if (dataSnapshot.child(mPostKey).hasChild(auth.getCurrentUser().getUid())) {

                                mDatabaseLike.child(mPostKey).child(auth.getCurrentUser().getUid()).removeValue();
                                mProcessLike = false;
                            }else {

                                mDatabaseLike.child(mPostKey).child(auth.getCurrentUser().getUid()).setValue("");
                                // mDatabaseLike.child(post_key).child("post_key").setValue(mAuth.getCurrentUser().getUid());

                                mProcessLike = false;

                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String post_story = (String) dataSnapshot.child("story").getValue();
                final String post_title = (String) dataSnapshot.child("title").getValue();

                mShareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent myIntent = new Intent(Intent.ACTION_SEND);
                        myIntent.setType("text/plain");
                        String shareBody = post_story + " ... read further info & comments on CampusMail";
                        String shareSub = "Dear "+ post_title ;
                        myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
                        myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                        startActivity(Intent.createChooser(myIntent,"Share mail through..."));

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setLikeBtn(final String post_key) {

        mDatabaseLike.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(post_key).hasChild(auth.getCurrentUser().getUid())) {

                    mCall.setImageResource(R.drawable.like_btn_red);
                } else {
                    mCall.setImageResource(R.drawable.like_btn_black);
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

                return super.onOptionsItemSelected(item);
        }
    }
}
