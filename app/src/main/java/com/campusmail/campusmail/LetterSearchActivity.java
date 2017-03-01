package com.campusmail.campusmail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.curioustechizen.ago.RelativeTimeTextView;
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

public class LetterSearchActivity extends AppCompatActivity {

    private String community_id = null;
    private String post_name = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView searchBtn;
    private EditText searchInput;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseComment;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mCurrentDatabaseUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;
    private RecyclerView mLettersList;
    private FirebaseUser mCurrentUser;
    private Boolean mProcessLike = false;

    private Query mQueryLetters;
    private Query mQueryComments;
    private Query mQueryLikes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        searchInput = (EditText) findViewById(R.id.searchInput);
        searchBtn = (ImageView) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String question = searchInput.getText().toString().trim();

                Intent cardonClick = new Intent(LetterSearchActivity.this, LetterSearchResult.class);
                cardonClick.putExtra("heartraise_id", question );
                startActivity(cardonClick);

            }

        });

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Letters");
        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mLettersList = (RecyclerView) findViewById(R.id.letters_list);
        mLettersList.setLayoutManager(new LinearLayoutManager(this));
        mLettersList.setHasFixedSize(true);

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        String question = searchInput.getText().toString().trim();
        mQueryLetters = mDatabase.orderByChild("title").startAt(question);
        community_id =  getIntent().getExtras().getString("community_id");
        mQueryLetters = mDatabase.orderByChild("community").equalTo(community_id);

        checkUserExists();

    }



    void refreshItems() {
        // Load items
        // ...

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void checkUserExists() {

        mProgressBar.setVisibility(View.VISIBLE);
        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (!dataSnapshot.hasChild(user_id)) {

                    mProgressBar.setVisibility(View.GONE);

                    Intent intent = new Intent(LetterSearchActivity.this, EditActivity.class);
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


    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Letters, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Letters, LetterViewHolder>(

                Letters.class,
                R.layout.letter_mail_row,
                LetterViewHolder.class,
                mQueryLetters


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final Letters model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setStory(model.getStory());
                viewHolder.setName(model.getName());
                viewHolder.setTime(model.getTime());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeBtn(post_key);


                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_photo = (String) dataSnapshot.child("photo").getValue();
                        final String post_story = (String) dataSnapshot.child("story").getValue();

                        if (post_photo != null) {

                            viewHolder.mCardPhoto.setVisibility(View.VISIBLE);
                            viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                            viewHolder.mInside.setVisibility(View.VISIBLE);

                            viewHolder.setPhoto(getApplicationContext(), model.getPhoto());

                        } else {

                            viewHolder.mCardPhoto.setVisibility(View.GONE);
                            viewHolder.mProgressBar.setVisibility(View.GONE);
                            viewHolder.mInside.setVisibility(View.GONE);

                            //viewHolder.mAttchBtn.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_photo = (String) dataSnapshot.child("photo").getValue();
                        final String post_story = (String) dataSnapshot.child("story").getValue();

                        if (post_photo != null) {

                            viewHolder.mCardPhoto.setVisibility(View.VISIBLE);
                            viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                            viewHolder.mInside.setVisibility(View.VISIBLE);

                            viewHolder.setPhoto(getApplicationContext(), model.getPhoto());

                        } else {

                            viewHolder.mCardPhoto.setVisibility(View.GONE);
                            viewHolder.mProgressBar.setVisibility(View.GONE);
                            viewHolder.mInside.setVisibility(View.GONE);

                            //viewHolder.mAttchBtn.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_story = (String) dataSnapshot.child("story").getValue();
                        final String post_title = (String) dataSnapshot.child("title").getValue();
                        post_name = (String) dataSnapshot.child("name").getValue();

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



                viewHolder.mChatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cardonClick = new Intent(LetterSearchActivity.this, CommentsActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
                    }
                });


                viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(LetterSearchActivity.this, ViewLetterProfileActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);

                    }
                });

                mQueryComments = mDatabaseComment.orderByChild("post_key").equalTo(post_key);
                mQueryComments.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.mCommentCount.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                //mQueryLikes = mDatabaseLike.orderByChild("post_key").equalTo(post_key);
                mDatabaseLike.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.mCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(mProcessLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mDatabaseLike.child(post_key).child("post_key").removeValue();
                                        mProcessLike = false;
                                    }else {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(post_name);
                                        mDatabaseLike.child(post_key).child("post_key").setValue(post_key);

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


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                    }

                });

            }

        };

        mLettersList.setAdapter(firebaseRecyclerAdapter);


    }



    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView mChatBtn, mInside, mImage, mAttchBtn ,mCardPhoto, mCall, mShareBtn;
        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;
        TextView mCommentCount, mLikeCount;
        DatabaseReference mDatabase;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mAuth = FirebaseAuth.getInstance();
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mDatabaseLike.keepSynced(true);
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            mChatBtn = (ImageView)mView.findViewById(R.id.chatBtn);
            mInside = (ImageView) mView.findViewById(R.id.inside_view2);
            mShareBtn = (ImageView) mView.findViewById(R.id.shareBtn);
            mCardPhoto = (ImageView) mView.findViewById(R.id.post_photo);
            mImage = (ImageView) mView.findViewById(R.id.post_image);
            mCall = (ImageView)mView.findViewById(R.id.buttonCall);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
            mCommentCount = (TextView) mView.findViewById(R.id.commentCount);
            mLikeCount = (TextView) mView.findViewById(R.id.likeCount);


        }

        public void setLikeBtn(final String post_key) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

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

            RelativeTimeTextView v = (RelativeTimeTextView)mView.findViewById(R.id.timestamp);
            v.setText(time);
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
        final Context context = this;

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:


        }

               return super.onOptionsItemSelected(item);
    }
}
