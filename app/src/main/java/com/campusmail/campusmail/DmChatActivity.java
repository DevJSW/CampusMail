package com.campusmail.campusmail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.text.DateFormat;
import java.util.Date;

public class DmChatActivity extends AppCompatActivity {

    private String mPostKey = null;
    private TextView mNoPostTxt;
    private ProgressDialog mProgress;
    private RecyclerView mCommentList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseComment;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabasePostComments;
    private Query mQueryPostComment;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private ImageButton mCommentBtn;
    private EditText mCommentField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dm_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Letters");
        mProgress = new ProgressDialog(this);
        mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);

        mAuth = FirebaseAuth.getInstance();
        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mDatabasePostComments = FirebaseDatabase.getInstance().getReference().child("DM_Chat");
        mQueryPostComment = mDatabasePostComments.orderByChild("post_key").equalTo(mPostKey);

        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mCommentList = (RecyclerView) findViewById(R.id.comment_list);
        mCommentList.setHasFixedSize(true);
        mCommentList.setLayoutManager(new LinearLayoutManager(this));
        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("DM_Chat");
        mDatabaseComment.keepSynced(true);

        // mDatabaseCurrentPost = FirebaseDatabase.getInstance().getReference().child("Comments").child(mPostKey);

        mCommentField = (EditText) findViewById(R.id.commentField);
        mCommentBtn = (ImageButton) findViewById(R.id.commentBtn);
        mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

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

        mDatabaseComment.keepSynced(true);
        mDatabase.keepSynced(true);


    }

    private void startPosting() {
        mProgress.setMessage("Posting...");

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        final String comment_val = mCommentField.getText().toString().trim();
        if (!TextUtils.isEmpty(comment_val)) {

            mProgress.show();


            final DatabaseReference newPost = mDatabaseComment.push();

            mDatabaseUser.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    newPost.child("comment").setValue(comment_val);
                    newPost.child("uid").setValue(mCurrentUser.getUid());
                    newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                    newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                    newPost.child("time").setValue(stringDate);
                    newPost.child("post_key").setValue(mPostKey);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            AlertDialog.Builder builder = new AlertDialog.Builder(DmChatActivity.this);
            builder.setTitle("Post Alert!");
            builder.setMessage("Your message has been posted SUCCESSFULLY!")
                    .setCancelable(true)
                    .setPositiveButton("OK",null);
            AlertDialog dialog = builder.create();
            dialog.show();

            mProgress.dismiss();

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comment, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(

                Comment.class,
                R.layout.comments_row,
                CommentViewHolder.class,
                mQueryPostComment


        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, Comment model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setComment(model.getComment());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getName());
                viewHolder.setTime(model.getTime());


                viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(DmChatActivity.this, ViewProfileDMChatActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
                    }
                });

            }
        };

        mCommentList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView mCardPhoto, mImage;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mCardPhoto = (ImageView) mView.findViewById(R.id.post_photo);
            mImage = (ImageView) mView.findViewById(R.id.post_image);

        }

        public void setComment(String comment) {

            TextView post_comment = (TextView) mView.findViewById(R.id.post_comment);
            post_comment.setText(comment);

        }

        public void setUsername(String username) {

            TextView post_username = (TextView) mView.findViewById(R.id.post_username);
            post_username.setText(username);
        }

        public void setTime(String time) {

            RelativeTimeTextView v = (RelativeTimeTextView)mView.findViewById(R.id.timestamp);
            v.setText(time);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx)
                    .load(image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(post_image, new Callback() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comment_menu, menu);
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

                    Intent cardonClick = new Intent(DmChatActivity.this, SendPhotoActivity.class);
                    cardonClick.putExtra("heartraise_id", mPostKey );
                    startActivity(cardonClick);
                }

                return super.onOptionsItemSelected(item);
        }
    }


}

