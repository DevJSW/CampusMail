package com.campusmail.campusmail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class EditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageView mPostUserimg, mPostImage, mFlag;
    private EditText mPostName, mPostSkills, mPostWeb, mPostBio, mPostPhone, mPostCampus;
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

    Spinner spinner_community, spinner_gender, spinner_course, spinner_country, spinner_year;
    ArrayAdapter community_adapter, gender_adapter, course_adapter, country_adapter,  year_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

     //   final String country_id = getIntent().getExtras().getString("country_id");

        mprogress = new ProgressDialog(this);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        auth = FirebaseAuth.getInstance();
        // mCurrentUser = auth.getCurrentUser().getUid();


        year_adapter = ArrayAdapter.createFromResource(this, R.array.choose_year, android.R.layout.simple_dropdown_item_1line);
        spinner_year = (Spinner) findViewById(R.id.spinner_year);
        spinner_year.setAdapter(year_adapter);
        spinner_year.setOnItemSelectedListener( EditActivity.this);

        country_adapter = ArrayAdapter.createFromResource(this, R.array.choose_country, android.R.layout.simple_dropdown_item_1line);
        spinner_country = (Spinner) findViewById(R.id.spinner_country);
        spinner_country.setAdapter(country_adapter);
        spinner_country.setOnItemSelectedListener( EditActivity.this);

        gender_adapter = ArrayAdapter.createFromResource(this, R.array.choose_gender, android.R.layout.simple_dropdown_item_1line);
        spinner_gender = (Spinner) findViewById(R.id.spinner_gender);
        spinner_gender.setAdapter(gender_adapter);
        spinner_gender.setOnItemSelectedListener( EditActivity.this);

        course_adapter = ArrayAdapter.createFromResource(this, R.array.choose_course, android.R.layout.simple_dropdown_item_1line);
        spinner_course = (Spinner) findViewById(R.id.spinner_course);
        spinner_course.setAdapter(course_adapter);
        spinner_course.setOnItemSelectedListener( EditActivity.this);


        spinner_community = (Spinner) findViewById(R.id.spinner_community);

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

        mPostCampus = (EditText) findViewById(R.id.post_campus);
        mPostName = (EditText) findViewById(R.id.post_name);
        mPostSkills = (EditText) findViewById(R.id.post_skills);
        mPostBio = (EditText) findViewById(R.id.post_bio);
        mPostPhone = (EditText) findViewById(R.id.post_phone);
        mPostWeb = (EditText) findViewById(R.id.post_web);

        //mPostImage = (ImageView) findViewById(R.id.post_image);
        mFlag = (ImageView) findViewById(R.id.flag);

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

                mPostName.setText(post_name);
                mPostBio.setText(post_bio);
                mPostSkills.setText(post_skills);
                mPostWeb.setText(post_web);
                mPostPhone.setText(post_phone);

//                Picasso.with(EditActivity.this).load(post_image).into(mPostImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void startPosting() {

        final String community = spinner_community.getSelectedItem().toString().trim();
        final String gender = spinner_gender.getSelectedItem().toString().trim();
        final String faculty = spinner_course.getSelectedItem().toString().trim();
        final String year = spinner_year.getSelectedItem().toString().trim();
        final String country = spinner_country.getSelectedItem().toString().trim();

        final String post_key = spinner_community.getSelectedItem().toString().trim();

        final String name = mPostName.getText().toString().trim();
        final String phone = mPostPhone.getText().toString().trim();
        final String web = mPostWeb.getText().toString().trim();
        final String skills = mPostSkills.getText().toString().trim();
        final String bio = mPostBio.getText().toString().trim();
        final String campus = mPostCampus.getText().toString().trim();

        final String user_id = auth.getCurrentUser().getUid();
        final String uid = user_id.substring(0, Math.min(user_id.length(), 5));

        if (mImageUri == null) {
            Toast.makeText(getApplicationContext(), "Add profile image!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPostName == null) {
            Toast.makeText(getApplicationContext(), "Add REAL Name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mPostCampus == null) {
            Toast.makeText(getApplicationContext(), "Hey add your CAMPUS!", Toast.LENGTH_SHORT).show();
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
                    mDatabaseUsers.child(user_id).child("year").setValue(year);
                    mDatabaseUsers.child(user_id).child("phone").setValue(phone);
                    mDatabaseUsers.child(user_id).child("skills").setValue(skills);
                    mDatabaseUsers.child(user_id).child("bio").setValue(bio);
                    mDatabaseUsers.child(user_id).child("community").setValue(community);
                    mDatabaseUsers.child(user_id).child("campus").setValue(campus);
                    mDatabaseUsers.child(user_id).child("gender").setValue(gender);
                    mDatabaseUsers.child(user_id).child("location").setValue(country);
                    mDatabaseUsers.child(user_id).child("faculty").setValue(faculty);
                    mDatabaseUsers.child(user_id).child("image").setValue(downloadUrl.toString());
                    mDatabaseUsers.child(user_id).child("uid").setValue(user_id);
                    mDatabaseUsers.child(user_id).child("campusmail_uid").setValue(uid);



                    mprogress.dismiss();

                    Intent cardonClick = new Intent(EditActivity.this, MainActivity.class);
                    cardonClick.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    cardonClick.putExtra("heartraise_id", post_key );
                    startActivity(cardonClick);

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


        }
        if (id == R.id.action_save) {

            AlertDialog diaBox = AskOption();
            diaBox.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog AskOption() {

        String community = spinner_community.getSelectedItem().toString().trim();
        final String faculty = spinner_course.getSelectedItem().toString().trim();
        final String year = spinner_year.getSelectedItem().toString().trim();
        String name = mPostName.getText().toString().trim();

        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(EditActivity.this)
                //set message, title, and icon
                .setTitle("Is this Info True!")
                .setMessage("I'm " + name + " a student at " +community + " specializing in the field of " + faculty + " currently on my " + year + "."  )

                .setPositiveButton("Yap! that's correct", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code

                        startPosting();

                        dialog.dismiss();
                    }

                })



                .setNegativeButton("Nop, Something's wrong", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();
        return myQuittingDialogBox;


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


        if (spinner_country.getSelectedItem().equals("Kenya")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.kenya_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_kenyan_flag);

        } else if (spinner_country.getSelectedItem().equals("Tanzania")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.tanzania_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_tanzania_flag);

        }  else if (spinner_country.getSelectedItem().equals("Uganda")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.uganda_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_uganda_flag);

        } else if (spinner_country.getSelectedItem().equals("South Africa")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.south_africa_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_southafrica_flag);

        }  else if (spinner_country.getSelectedItem().equals("Ghana")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.ghana_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_ghana_flag);

        }  else if (spinner_country.getSelectedItem().equals("Nigeria")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.nigeria_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_nigeria_flag);

        } else if (spinner_country.getSelectedItem().equals("Seychelles")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.seychelles_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_setchelles_flag);

        }else if (spinner_country.getSelectedItem().equals("Mauritius")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.mauritius_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_mauritius_flag);

        }else if (spinner_country.getSelectedItem().equals("Botswana")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.botswana_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_botswana_flag);

        }else if (spinner_country.getSelectedItem().equals("Cameroon")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.cameroon_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_cameroon_flag);

        }else if (spinner_country.getSelectedItem().equals("Gambia")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.gambia_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_gambia_flag);

        }else if (spinner_country.getSelectedItem().equals("Malawi")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.malawi_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_malawi_flag);

        }else if (spinner_country.getSelectedItem().equals("Lesotho")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.lesotho_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_lesotho_flag);

        }else if (spinner_country.getSelectedItem().equals("Liberia")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.liberia_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_liberia_flag);

        }else if (spinner_country.getSelectedItem().equals("Namibia")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.namibia_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_namibia_flag);

        }else if (spinner_country.getSelectedItem().equals("Zambia")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.zambia_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_zambia_flag);

        }else if (spinner_country.getSelectedItem().equals("Zimbabwe")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.zimbabwe_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_zimbabwe_flag);

        }else if (spinner_country.getSelectedItem().equals("Swaziland")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.swaziland_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_swaziland_flag);

        }else if (spinner_country.getSelectedItem().equals("Sierra Leone")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.sierra_leone_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_sierraleone_flag);

        }else if (spinner_country.getSelectedItem().equals("United Kingdom")) {

            community_adapter = ArrayAdapter.createFromResource(this,
                    R.array.uk_choose_community, android.R.layout.simple_dropdown_item_1line);
            spinner_community.setAdapter(community_adapter);

            mFlag.setImageResource(R.drawable.ic_uk_flag);

        }





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
