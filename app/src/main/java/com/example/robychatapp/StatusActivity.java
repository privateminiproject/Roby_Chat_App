package com.example.robychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;


    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mProgress = new ProgressDialog(this);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String Current_uid = mCurrentUser.getUid();


        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(Current_uid);


        mToolbar = findViewById(R.id.status_appBars);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout) findViewById(R.id.status_input);

        String status_value = getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(status_value);


    }

    public void ss(View view) {

        mProgress.setTitle("Saving Changes");
        mProgress.setMessage("Please Wait While Changes");
        mProgress.show();

        String status = mStatus.getEditText().getText().toString();
        mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mProgress.dismiss();
                    //Timer
                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            Intent settingIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingIntent);
                            finish();
                        }
                    }, 500);
                } else {
                    Toast.makeText(StatusActivity.this, "something wrong", Toast.LENGTH_LONG).show();

                }

            }
        });

    }
}
