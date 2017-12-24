package com.giviews.giviewsmessenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.giviews.giviewsmessenger.models.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {
    private RecyclerView mFriendsList;
    private DatabaseReference mChatsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.chats_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder> chatsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder>(
                Chats.class,
                R.layout.message_single_layout,
                ChatsFragment.ChatsViewHolder.class,
                mChatsDatabase
        ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder chatsViewHolder, final Chats chats, int position) {
//                friendsViewHolder.setDate(friends.getDate());

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
//                        String time = dataSnapshot.child("time").getValue().toString();
//                        String message = dataSnapshot.child("message").getValue().toString();
                        //Memparsing time
//                        GetTimeAgo getTimeAgo = new GetTimeAgo();
//                        long lastTime = Long.parseLong(String.valueOf(time));
//                        String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getTimeAgo);

                        chatsViewHolder.setName(userName);
                        chatsViewHolder.setUserImage(userThumb, getContext());
//                        chatsViewHolder.setMessage(message);
//                        chatsViewHolder.setDate(lastSeenTime);

                        chatsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
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
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(chatsRecyclerViewAdapter);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.name_text_layout);
            userNameView.setText(name);
        }

        public void setMessage(String message) {
            TextView userMessageView = (TextView) mView.findViewById(R.id.message_text_layout);
            userMessageView.setText(message);
        }

        public void setDate(String date) {
            TextView userStatusView = (TextView) mView.findViewById(R.id.message_time_layout);
            userStatusView.setText(date);
        }

        public void setUserImage(String thumb_image, Context ctx) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.message_profile_layout);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }
    }
}
