package com.campusmail.campusmail;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ProfileUserLettersActivity extends AppCompatActivity {

    private String mPostKey = null;
    private TextView mPostName, mPostLocation, mPostCommunity;
    private ImageView mPostImage;
    private TextView mNoPostTxt;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private Query mQueryPostComment;
    private ProgressBar mProgressBar;
    private RecyclerView mLettersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostCommunity = (TextView) findViewById(R.id.post_community);
        mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);
        mPostName = (TextView) findViewById(R.id.post_name);

        mPostImage = (ImageView) findViewById(R.id.post_image);
        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Letters");

        mQueryPostComment = mDatabase.orderByChild("uid").equalTo(mPostKey);
       // mQueryPostComment = mDatabase.orderByChild("uid").equalTo(user_id);

        mLettersList = (RecyclerView) findViewById(R.id.letters_list);
        mLettersList.setLayoutManager(new LinearLayoutManager(this));
        mLettersList.setHasFixedSize(true);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);

        checkForNetwork();
        checkUserExists();

        mQueryPostComment.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null){

                    mNoPostTxt.setVisibility(View.VISIBLE);
                } else {
                    mNoPostTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mDatabaseUsers.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_name = (String) dataSnapshot.child("name").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();

                mPostName.setText(post_name);


                Picasso.with(ProfileUserLettersActivity.this).load(post_image).into(mPostImage);
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

                mProgressBar.setVisibility(View.GONE);

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

            Toast.makeText(ProfileUserLettersActivity.this, "You are not connected to Internet... Please Enable Internet!", Toast.LENGTH_LONG)
                    .show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<Letter, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Letter, LetterViewHolder>(

                Letter.class,
                R.layout.letter_mail_row,
                LetterViewHolder.class,
                mQueryPostComment


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final Letter model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setStory(model.getStory());
                viewHolder.setName(model.getName());
                viewHolder.setTime(model.getTime());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setPhoto(getApplicationContext(), model.getPhoto());


                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_photo = (String) dataSnapshot.child("photo").getValue();

                        if (post_photo != null) {

                            viewHolder.mCardPhoto.setVisibility(View.VISIBLE);
                            viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                            viewHolder.mInside.setVisibility(View.VISIBLE);

                            viewHolder.setPhoto(getApplicationContext(), model.getPhoto());

                        } else {

                            viewHolder.mCardPhoto.setVisibility(View.GONE);
                            viewHolder.mProgressBar.setVisibility(View.GONE);
                            viewHolder.mInside.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.mPostImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cardonClick = new Intent(ProfileUserLettersActivity.this, ViewLetterProfileActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key);
                        startActivity(cardonClick);
                    }
                });




                viewHolder.mCommentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cardonClick = new Intent(ProfileUserLettersActivity.this, CommentsActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key);
                        startActivity(cardonClick);
                    }
                });


                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_story = (String) dataSnapshot.child("story").getValue();
                        final String post_title = (String) dataSnapshot.child("title").getValue();

                        viewHolder.mShareBtn.setOnClickListener(new View.OnClickListener() {
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


                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String phone = (String) dataSnapshot.child("phone").getValue();

                        viewHolder.mCallBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + phone));
                                if (ActivityCompat.checkSelfPermission(ProfileUserLettersActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                startActivity(callIntent);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

        };

        mLettersList.setAdapter(firebaseRecyclerAdapter);


    }



    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseViewsCount;
        TextView mCountField;
        FirebaseAuth auth;
        DatabaseReference mDatabase, mDatabaseLetter;
        ImageView mCardPhoto, mPostImg, mCallBtn,mCommentBtn, mShareBtn, mInside, mDelete;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mCardPhoto = (ImageView) mView.findViewById(R.id.post_photo);
            mPostImg = (ImageView) mView.findViewById(R.id.post_image);
            mCallBtn = (ImageView) mView.findViewById(R.id.buttonCall);
            mCommentBtn = (ImageView) mView.findViewById(R.id.chatBtn);
            mShareBtn = (ImageView) mView.findViewById(R.id.shareBtn);
            mInside = (ImageView) mView.findViewById(R.id.inside_view2);
            mDelete = (ImageView) mView.findViewById(R.id.delete);

            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
            mDatabaseViewsCount = FirebaseDatabase.getInstance().getReference().child("Views_Count");
            auth = FirebaseAuth.getInstance();

            mCountField = (TextView)mView.findViewById(R.id.post_count);

            mDatabaseLetter = FirebaseDatabase.getInstance().getReference().child("Letters");
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        }



        public void setTitle(String title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setStory(String story) {

            TextView post_story = (TextView) mView.findViewById(R.id.post_story);
            post_story.setText(story);
        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setTime(String time) {

            TextView post_time = (TextView) mView.findViewById(R.id.timestamp);
            post_time.setText(time);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {


                    Picasso.with(ctx).load(image).into(post_image);
                }
            });
        }

        public void setPhoto(final Context ctx, final String photo) {
            final ImageView post_photo = (ImageView) mView.findViewById(R.id.post_photo);

            Picasso.with(ctx).load(photo).networkPolicy(NetworkPolicy.OFFLINE).into(post_photo, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {


                    Picasso.with(ctx).load(photo).into(post_photo);
                }
            });
        }
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
