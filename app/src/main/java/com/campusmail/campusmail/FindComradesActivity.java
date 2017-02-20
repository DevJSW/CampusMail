package com.campusmail.campusmail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.text.DateFormat;
import java.util.Date;

public class FindComradesActivity extends AppCompatActivity {

    private String community_id = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView searchBtn;
    private EditText searchInput;
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
        setContentView(R.layout.activity_find_comrades);
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

                Intent cardonClick = new Intent(FindComradesActivity.this, FindComradesResult.class);
                cardonClick.putExtra("heartraise_id", question );
                startActivity(cardonClick);




            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mLettersList = (RecyclerView) findViewById(R.id.letters_list);
        mLettersList.setLayoutManager(new LinearLayoutManager(this));
        mLettersList.setHasFixedSize(true);

        mDatabase.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabase.keepSynced(true);
        String question = searchInput.getText().toString().trim();
        mQueryLetters = mDatabaseUsers.orderByChild("name").startAt(question);

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

                    Intent intent = new Intent(FindComradesActivity.this, EditActivity.class);
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



        FirebaseRecyclerAdapter<Letters, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<Letters, LetterViewHolder>(

                Letters.class,
                R.layout.comrade_row,
                LetterViewHolder.class,
                mQueryLetters


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final Letters model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setFaculty(model.getFaculty());
                viewHolder.setCommunity(model.getCommunity());
                viewHolder.setName(model.getName());
                viewHolder.setImage(getApplicationContext(), model.getImage());


                mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_photo = (String) dataSnapshot.child("photo").getValue();

                        if (post_photo != null) {

                            //viewHolder.setPhoto(getApplicationContext(), model.getPhoto());

                        } else {

                            //viewHolder.mCardPhoto.setVisibility(View.GONE);
                            // viewHolder.mProgressBar.setVisibility(View.GONE);
                            //viewHolder.mInside.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(FindComradesActivity.this, ViewComradeProfileActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
                    }

                });

            }

        };

        mLettersList.setAdapter(firebaseRecyclerAdapter);


    }



    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageView mChatBtn, mInside;
        TextView uid;
        DatabaseReference mDatabase;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mDatabase = FirebaseDatabase.getInstance().getReference().child("Questions");
            mInside = (ImageView) mView.findViewById(R.id.inside_view2);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);

        }

        public void setFaculty(String faculty) {

            TextView post_faculty = (TextView) mView.findViewById(R.id.post_faculty);
            post_faculty.setText(faculty);
        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }



        public void setCommunity(String community) {

            TextView post_community = (TextView) mView.findViewById(R.id.post_community);
            post_community.setText(community);
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

        if (id == R.id.action_ask) {

            // custom dialog
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.ask_dialog);
            dialog.setTitle("Ask...");

            // set the custom dialog components - text, image and button

            final EditText question_input = (EditText) dialog.findViewById(R.id.quizInput);
            final EditText explanation_input = (EditText) dialog.findViewById(R.id.exInput);

            Button submitBtn = (Button) dialog.findViewById(R.id.submitBtn);

            // if button is clicked, close the custom dialog

            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

                    Date date = new Date();
                    final String stringDate = DateFormat.getDateInstance().format(date);

                    final String quiz_value = question_input.getText().toString().trim();
                    final String ex_value = explanation_input.getText().toString().trim();
                    final String user_id = mAuth.getCurrentUser().getUid();

                    if (!TextUtils.isEmpty(quiz_value) && !TextUtils.isEmpty(ex_value)) {


                        final DatabaseReference newPost = mDatabase.push();

                        mDatabaseUsers.child(user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                newPost.child("title").setValue(quiz_value);
                                newPost.child("story").setValue(ex_value);
                                newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                                newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                                newPost.child("uid").setValue(user_id);
                                newPost.child("time").setValue(stringDate);

                                dialog.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(FindComradesActivity.this);
                                builder.setTitle("Post Alert!");
                                builder.setMessage("Your Question has been posted SUCCESSFULLY!")
                                        .setCancelable(true)
                                        .setPositiveButton("OK", null);
                                //builder.setNegativeButton("no", null );
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }

                }
            });

            dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }
}
