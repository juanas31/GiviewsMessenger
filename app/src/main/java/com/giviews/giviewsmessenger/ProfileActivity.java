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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfilImage;
    private TextView mProfileName, mProfilStatus, mCountFriends;
    private Button mProfileSendReqBtn;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser mCurrentUser;

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

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfilImage = (ImageView) findViewById(R.id.dispImage);
        mProfileName = (TextView) findViewById(R.id.dispName);
        mProfilStatus = (TextView) findViewById(R.id.dispStatus);
        mCountFriends = (TextView) findViewById(R.id.dispTotal);
        mProfileSendReqBtn = (Button) findViewById(R.id.profileReqBtn);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfilStatus.setText(display_status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfilImage);

                //-------------- FRIEND LIST / REQUEST FEATURE ---------------------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                        if (req_type.equals("received")) {

                            mCurrentState = "req_received";
                            mProfileSendReqBtn.setText("Accept Friend Request");

                        }else if(req_type.equals("sent")){

                            mCurrentState = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");
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

                //------------------ NOT FRIEND STATE ------------------------

                if (mCurrentState.equals("not_friends")) {
                    mSendProgress.setMessage("Sending Friend Request");
                    mSendProgress.show();

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mProfileSendReqBtn.setEnabled(true);
                                        mCurrentState = "req_sent";
                                        mProfileSendReqBtn.setText("Cancel Friend Request");
                                        mSendProgress.dismiss();
                                        Toast.makeText(getApplicationContext(), "Friend Request Sending", Toast.LENGTH_LONG).show();

                                    }
                                });

                            }else {
                                Toast.makeText(getApplicationContext(), "Send Request Failed", Toast.LENGTH_LONG).show();
                                mSendProgress.hide();
                            }
                        }
                    });
                }

                // ------------------ Cancel Friend Req ----------------------
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
                                    mCancelProgress.dismiss();

                                    Toast.makeText(getApplicationContext(), "Friend Request Canceled", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
