package com.example.robychatapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mFriendsDatabases;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mLastMsg;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;
    private DatabaseReference mUserDatabase;



    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mFriendsDatabases = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabases.keepSynced(true);


        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mLastMsg = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mLastMsg.keepSynced(true);
        //mDatabase=FirebaseDatabase.getInstance();
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendsDatabase,Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends,FriendsFragment.FriendsViewHolder> adapter= new FirebaseRecyclerAdapter<Friends,FriendsFragment.FriendsViewHolder>(options){
            @NonNull
            @Override
            public FriendsFragment.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                FriendsFragment.FriendsViewHolder friendsViewHolder=new FriendsFragment.FriendsViewHolder(view);
                return friendsViewHolder;
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsFragment.FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {

                friendsViewHolder.setDate(friends.getDate());

                final String list_user_id = getRef(i).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();


                        if (dataSnapshot.hasChild("online")){

                            String userOnline=  dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);
                        }


                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setUserImage(userThumb,getContext());


                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        };
        mFriendsList.setAdapter(adapter);
        adapter.startListening();


    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDate(String date){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).into(userImageView);

        }

        public void setUserOnline(String online_status){

            ImageView userOnlineView=(ImageView)mView.findViewById(R.id.user_single_online_icon);
            if (online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }

        }


    }

}
