package com.example.robychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    FirebaseStorage storage = FirebaseStorage.getInstance();
    DataSnapshot dataSnapshot;


    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private static final int GALLERY_PICK = 1;

    private Toolbar mToolbar;

    //Android Layout
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    //Store Image in FirebaseDatabase
    private StorageReference mImageStorage;

    //progress dialog
    private ProgressDialog mProgressDialog;

    private StorageReference thumb_imageRef;
    Bitmap thumb_bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mName = (TextView) findViewById(R.id.settings_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mImageStorage = FirebaseStorage.getInstance().getReference();
        thumb_imageRef = FirebaseStorage.getInstance().getReference().child("thumb_images");


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());


        //Toolbar
        mToolbar = findViewById(R.id.setting_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();


        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //retrive value from database
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                if (!image.equals("default")) {


//                    Picasso.get().load(image).placeholder(R.drawable.pp).into(mDisplayImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.pp).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.pp).into(mDisplayImage);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //Move To Status Activity and set Status
    public void cs(View view) {
        String status_value = mStatus.getText().toString();
        Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
        statusIntent.putExtra("status_value", status_value);
        startActivity(statusIntent);
        finish();
    }


    //Change Profile Image
    public void ci(View view) {
        CropImage.activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setBorderLineColor(Color.rgb(57, 76, 168))
                .setGuidelinesColor(Color.WHITE)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wite while uploading");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                final String current_user_id = mCurrentUser.getUid();

                //image Url
                final Uri resultUri = result.getUri();


                //Thumb_File
                final File thumb_filePath = new File(resultUri.getPath());


                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();


                //Thumb_File


                final StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    final String download_url = uri.toString();
                                    mUserDatabase.child("image").setValue(download_url);


                                    thumb_filepath.putBytes(thumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                            if (thumb_task.isSuccessful()) {
                                                thumb_filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        mProgressDialog.dismiss();
                                                        final String thumb_download_url = uri.toString();
                                                        mUserDatabase.child("thumb_image").setValue(thumb_download_url);
                                                        Toast.makeText(SettingsActivity.this, "Image added Successfully....", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            Toast.makeText(SettingsActivity.this, "Error in uploading", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                mDisplayImage.setImageURI(resultUri);

                //image Url Close


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SettingsActivity.this, "" + error, Toast.LENGTH_LONG).show();
            }
        }

    }

}

