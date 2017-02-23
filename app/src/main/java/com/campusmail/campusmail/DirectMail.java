package com.campusmail.campusmail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

public class DirectMail extends AppCompatActivity {

    private String community_id = null;
    private TextView mNoPostTxt;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView mCallBtn;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mCurrentDatabaseUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;
    private RecyclerView mLettersList;
    private FirebaseUser mCurrentUser;

    private Query mQueryLetters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_mail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Directmails");
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mLettersList = (RecyclerView) findViewById(R.id.letters_list);
        mLettersList.setLayoutManager(new LinearLayoutManager(this));
        mLettersList.setHasFixedSize(true);

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);
        mQueryLetters = mDatabase.orderByChild("reciever_uid").equalTo(mAuth.getCurrentUser().getUid());

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

    }

    private void checkUserExists() {

        mProgressBar.setVisibility(View.VISIBLE);
        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (!dataSnapshot.hasChild(user_id)) {

                    mProgressBar.setVisibility(View.GONE);

                    Intent intent = new Intent(DirectMail.this, EditActivity.class);
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
                R.layout.letter_mail_row_delete_dm,
                LetterViewHolder.class,
                mQueryLetters


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final Letters model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setStory(model.getStory());
                viewHolder.setTime(model.getTime());
                //viewHolder.setImage(getApplicationContext(), model.getImage());


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


                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String sender_uid = (String) dataSnapshot.child("sender_uid").getValue();
                        String sender_name = (String) dataSnapshot.child("name").getValue();

                        mDatabaseUsers.child(sender_uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String post_image = (String) dataSnapshot.child("image").getValue();
                                String post_name = (String) dataSnapshot.child("name").getValue();

                                viewHolder.mName.setText(post_name);
                                Picasso.with(DirectMail.this).load(post_image).into(viewHolder.mImage);


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

                viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(DirectMail.this, ViewDirectmailActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
                    }
                });

                viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog diaBox = AskOption();
                        diaBox.show();



                    }

                    private AlertDialog AskOption() {

                        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(DirectMail.this)
                                //set message, title, and icon
                                .setTitle("Delete")
                                .setMessage("Do you want to Delete this post?")
                                .setIcon(R.drawable.ic_delete_forever_2)

                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //your deleting code

                                        mDatabase.child(post_key).removeValue();

                                        dialog.dismiss();
                                    }

                                })



                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();

                                    }
                                })
                                .create();
                        return myQuittingDialogBox;


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

        ImageView mCardPhoto, mInside, mImage, mDelete;
        TextView mName;
        DatabaseReference mDatabase;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Directmails");
            mInside = (ImageView) mView.findViewById(R.id.inside_view2);
            mDelete = (ImageView) mView.findViewById(R.id.delete);
            mName = (TextView) mView.findViewById(R.id.post_name);
            mCardPhoto = (ImageView) mView.findViewById(R.id.post_photo);
            mImage = (ImageView) mView.findViewById(R.id.post_image);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);




        }


        public void setTitle(String title) {

            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setStory(String story) {

          TextView post_story = (TextView) mView.findViewById(R.id.post_story);
        post_story.setText(story);
        }

        public void setTime(String time) {

            RelativeTimeTextView v = (RelativeTimeTextView)mView.findViewById(R.id.timestamp);
            v.setText(time);
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

                if (id == R.id.action_sent) {

                    Intent cardonClick = new Intent(DirectMail.this, SentDMActivity.class);
                    cardonClick.putExtra("community_id", community_id );
                    startActivity(cardonClick);
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.direct_menu, menu);
        return true;
    }


}
