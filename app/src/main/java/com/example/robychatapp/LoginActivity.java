package com.example.robychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    //Firebase
    private FirebaseAuth mAuth;

    //Toolbar
    private Toolbar mToolbar;

    //progressBas
    private ProgressDialog mLoginProgress;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Toolbar
        mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        //Progress
        mLoginProgress = new ProgressDialog(this);

        mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.login_password);

    }


    public void lb(View view) {

        String email = mLoginEmail.getEditText().getText().toString();
        String password = mLoginPassword.getEditText().getText().toString();

        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

            mLoginProgress.setTitle("Loggin In");
            mLoginProgress.setMessage("Please Waite while Login...");
            mLoginProgress.setCanceledOnTouchOutside(false);
            mLoginProgress.show();

            loginUser(email, password);
        }
        else{
            Toast.makeText(LoginActivity.this,"Please Enter Email and Password.....",Toast.LENGTH_LONG).show();
        }

    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    mLoginProgress.dismiss();

                    String mCurrentUserId= mAuth.getCurrentUser().getUid();

                    String deviceToken= FirebaseInstanceId.getInstance().getToken();
                    mUserDatabase.child(mCurrentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

                } else {
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, "Invalid ID or password. Please try again.", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

}
