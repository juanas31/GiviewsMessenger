package com.giviews.giviewsmessenger;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfilImage;
    private TextView mProfileName, mProfilStatus, mCountFriends;
    private Button mProfileSendReqBtn, mProfileDecReqBtn;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private String mCurrentState;
    private ProgressDialog mProgress, mSendProgress, mCancelProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mProgress = new ProgressDialog(this);
        mSendProgress = new ProgressDialog(this);
        mCancelProgress = new ProgressDialog(this);

        mProgress.setMessage("Loading User Data");
        mProgress.show();

        final String user_id = getIntent().getStringExtra("user_id");
        mCurrentState = "not_friends";

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUserDatabase.keepSynced(true);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendRequestDatabase.keepSynced(true);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendsDatabase.keepSynced(true);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notification");

        mProfilImage = (ImageView) findViewById(R.id.dispImage);
        mProfileName = (TextView) findViewById(R.id.dispName);
        mProfilStatus = (TextView) findViewById(R.id.dispStatus);
        mCountFriends = (TextView) findViewById(R.id.dispTotal);
        mProfileSendReqBtn = (Button) findViewById(R.id.profileReqBtn);
        mProfileDecReqBtn = (Button) findViewById(R.id.profileRejectBtn);

        mProfileDecReqBtn.setVisibility(View.INVISIBLE);
        mProfileDecReqBtn.setEnabled(false);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfilStatus.setText(display_status);
                Picasso.with(ProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(mProfilImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfilImage);
                    }
                });

                //-------------- FRIEND LIST / REQUEST FEATURE ---------------------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {

                                mCurrentState = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mProfileDecReqBtn.setVisibility(View.VISIBLE);
                                mProfileDecReqBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {

                                mCurrentState = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mProfileDecReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDecReqBtn.setEnabled(false);
                            }
                            mProgress.dismiss();

                        }else {

                            mFriendsDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrentState = "friends";
                                        mProfileSendReqBtn.setText("Unfriend this Person");

                                        mProfileDecReqBtn.setVisibility(View.INVISIBLE);
                                        mProfileDecReqBtn.setEnabled(false);
                                    }
                                    mProgress.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        mProgress.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendReqBtn.setEnabled(false);

                //------------------ NOT FRIEND STATE / SEND FRIEND REQ------------------------

                if (mCurrentState.equals("not_friends")) {

                    mSendProgress.setMessage("Sending Friend Request");
                    mSendProgress.show();

                    DatabaseReference newNotificationRef = mRootRef.child("Notification").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<String, String>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("Notification/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {
                                Toast.makeText(getApplicationContext(), "There was Error on Sending Request", Toast.LENGTH_LONG).show();
                            } else {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentState = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                mSendProgress.dismiss();

                                Toast.makeText(getApplicationContext(), "Friend Request Sending", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }

                // ------------------ REQ SENT STATE / CANCEL FRIEND REQ ----------------------
                if (mCurrentState.equals("req_sent")) {
                    mCancelProgress.setMessage("Canceled Friend Request");
                    mCancelProgress.show();

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    mProfileDecReqBtn.setVisibility(View.INVISIBLE);
                                    mProfileDecReqBtn.setEnabled(false);
                                    mCancelProgress.dismiss();

                                    Toast.makeText(getApplicationContext(), "Friend Request Canceled", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }


                // ------------------ RECEIVED STATE / ACCEPT FRIEND REQUEST ------------------
                if (mCurrentState.equals("req_received")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentState = "friends";
                                mProfileSendReqBtn.setText("Unfriend This Person");

                                mProfileDecReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDecReqBtn.setEnabled(false);
                            }
                        }
                    });
                }


                //------------------ FRIENDS STATE / UNFRIEND -------------------
                if (mCurrentState.equals("friends")) {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCurrentState = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mProfileDecReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDecReqBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        mProfileDecReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ------------------ RECEIVED STATE / DECLINE FRIEND REQ ----------------------
                if (mCurrentState.equals("req_received")) {
//                    mCancelProgress.setMessage("Canceled Friend Request");
//                    mCancelProgress.show();

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mCurrentState = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    mProfileDecReqBtn.setVisibility(View.INVISIBLE);
                                    mProfileDecReqBtn.setEnabled(false);

//                                    mCancelProgress.dismiss();

                                    Toast.makeText(getApplicationContext(), "Decline Request", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Profile", "http://[ENTER-YOUR-URL-HERE]");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction());
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }
}
