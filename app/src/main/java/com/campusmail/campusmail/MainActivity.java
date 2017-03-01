package com.campusmail.campusmail;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String community_id = null;
    private String user_community = null;
    private String post_name = null;
    private TextView mNoPostTxt;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView mCallBtn, mImg;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseComment;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;
    private RecyclerView mLettersList;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorage;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;
    private Boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike;

    private Query mQueryLetters;
    private Query mQueryComments;
    private Query mQueryLikes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mImg = (ImageView) findViewById(R.id.post_userimg);
        mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cardonClick = new Intent(MainActivity.this, PostActivity.class);
                startActivity(cardonClick);

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        //final String user_id = mAuth.getCurrentUser().getUid();
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
        mDatabaseLike.keepSynced(true);
        mDatabase.keepSynced(true);
        community_id =  getIntent().getExtras().getString("community_id");
        mQueryLetters = mDatabase.orderByChild("community").equalTo(community_id);



        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_image = (String) dataSnapshot.child("image").getValue();
                post_name = (String) dataSnapshot.child("name").getValue();
                user_community = (String) dataSnapshot.child("community").getValue();

                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                View hView =  navigationView.getHeaderView(0);

                ImageView nav_user = (ImageView)hView.findViewById(R.id.nav_photo);
                TextView nav_username = (TextView)hView.findViewById(R.id.nav_username);

                nav_username.setText(post_name);

                Picasso.with(MainActivity.this).load(post_image).into(nav_user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mQueryLetters.addValueEventListener(new ValueEventListener() {
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

        checkUserExists();
        checkForNetwork();

    }


    private void checkForNetwork() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ninfo = cm.getActiveNetworkInfo();

        if (ninfo != null && ninfo.isConnected()) {


        } else {


            Toast.makeText(MainActivity.this, "You are not connected to Internet... Please Enable Internet!", Toast.LENGTH_LONG)
                    .show();

        }
    }

    private void checkUserExists() {

        mProgressBar.setVisibility(View.VISIBLE);
        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (!dataSnapshot.hasChild(user_id)) {

                    mProgressBar.setVisibility(View.GONE);

                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }else {

                    if ( getIntent().getExtras().getString("community_id") == null) {

                        Intent intent = new Intent(MainActivity.this, Profile_Activity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    }
                    mProgressBar.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                mProgressBar.setVisibility(View.GONE);
            }
        });
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



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Letters, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Letters, LetterViewHolder>(

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

               mDatabaseLike.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.equals(post_key)) {
                            viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");

                        } else {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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



                viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cardonClick = new Intent(MainActivity.this, ViewLetterProfileActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
                    }
                });

                viewHolder.mChatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cardonClick = new Intent(MainActivity.this, CommentsActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
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
                                            //mDatabaseLike.child(post_key).child("post_key").removeValue();
                                            mProcessLike = false;
                                        }else {

                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(post_name);
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

        ImageView mChatBtn, mCall,mCardPhoto, mInside, mImage, mShareBtn;
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
            mChatBtn = (ImageView)mView.findViewById(R.id.chatBtn);
            mCall = (ImageView)mView.findViewById(R.id.buttonCall);
            mInside = (ImageView) mView.findViewById(R.id.inside_view2);
            mCardPhoto = (ImageView) mView.findViewById(R.id.post_photo);
            mShareBtn = (ImageView) mView.findViewById(R.id.shareBtn);
            mImage = (ImageView) mView.findViewById(R.id.post_image);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                if (id == R.id.action_search) {

                    Intent cardonClick = new Intent(MainActivity.this, LetterSearchActivity.class);
                    cardonClick.putExtra("community_id", community_id );
                    startActivity(cardonClick);
                }

                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {

            Intent intent = new Intent(MainActivity.this, Profile_Activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_postmail) {

            Intent intent = new Intent(MainActivity.this, FindComradesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_cm_search) {

            Intent intent = new Intent(MainActivity.this, CMSearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_manage) {

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String shareBody ="https://play.google.com/store/apps/details?id=com.campusmail.campusmail";
            String shareSub = "Dear ";
            myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
            myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
            startActivity(Intent.createChooser(myIntent,"Share mail through..."));

        } else if (id == R.id.nav_directmail) {

            Intent intent = new Intent(MainActivity.this, DirectMail.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_settings) {

        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();
            mImg.setImageURI(mImageUri);

        }
    }
}
