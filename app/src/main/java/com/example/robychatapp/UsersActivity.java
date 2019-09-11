package com.example.robychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUserDatabase;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar =findViewById(R.id.users_appBas);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

//        mDatabase=FirebaseDatabase.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }





    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUserDatabase, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                options
        ) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull Users model) {
                holder.mName.setText(model.getName());
                holder.mStatus.setText(model.getStatus());
                Picasso.get().load(model.getThumb_image()).into(holder.mDisplayImage);

                final String user_Id=getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_Id",user_Id);
                        startActivity(profileIntent);
                    }
                });
            }
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                UsersViewHolder usersViewHolder = new UsersViewHolder(view);
                return usersViewHolder;
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView mName, mStatus;
        CircleImageView mDisplayImage;
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.user_single_name);
            mStatus = itemView.findViewById(R.id.user_single_status);
            mDisplayImage = itemView.findViewById(R.id.user_single_image);
            mView = itemView;


        }

    }
}
