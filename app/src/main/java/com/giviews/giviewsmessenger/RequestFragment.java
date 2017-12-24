package com.giviews.giviewsmessenger;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.giviews.giviewsmessenger.models.Friends;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private ImageView mUserOnline;
    private FirebaseUser mCurrentUser;
    private String mCurrentState;

    // view
    private CircleImageView mProfilImage;
    private TextView mProfileName, mProfilStatus, mCountFriends;
    private Button mProfileSendReqBtn, mProfileDecReqBtn;
    private RelativeLayout mRootView;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        mProfilImage = mMainView.findViewById(R.id.circleImageView);
        mProfileSendReqBtn = mMainView.findViewById(R.id.btnAccept);
        mProfileDecReqBtn = mMainView.findViewById(R.id.btnDecline);
        mRootView = mMainView.findViewById(R.id.rootView);

        mFriendsList = mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);

        // mendapatkan id user
        final String user_id = mFriendsDatabase.getKey();
        mCurrentState = "not_friends";

//        // jika userId = yang sedang login jangan tampilkan btn pertemanan
//        if (mCurrent_user_id == user_id) {
////            mRootView.setVisibility(View.GONE);
//            mProfileSendReqBtn.setVisibility(View.GONE);
//            mProfileSendReqBtn.setEnabled(false);
//
//            mProfileDecReqBtn.setVisibility(View.GONE);
//            mProfileDecReqBtn.setEnabled(false);
//        }

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, RequestFragment.RequestViewHolder> requestRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, RequestFragment.RequestViewHolder>(
                Friends.class,
                R.layout.users_single_layout_request,
                RequestFragment.RequestViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestFragment.RequestViewHolder requestViewHolder, final Friends friends, int position) {
                requestViewHolder.setDate(friends.getDate());

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        requestViewHolder.setName(userName);
                        requestViewHolder.setUserImage(userThumb, getContext());

                        requestViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //Click Event for each item
                                        if (i == 0) {

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        } else if (i == 1) {

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(requestRecyclerViewAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date) {
            TextView userStatusView = mView.findViewById(R.id.status);
            userStatusView.setText(date);
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.username);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context ctx) {
            CircleImageView userImageView = mView.findViewById(R.id.circleImageView);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }
    }

}
