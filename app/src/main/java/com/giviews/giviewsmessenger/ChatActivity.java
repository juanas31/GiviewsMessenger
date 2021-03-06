package com.giviews.giviewsmessenger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.giviews.giviewsmessenger.adapter.MessageAdapter;
import com.giviews.giviewsmessenger.models.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    //menyimpan string id user yang diajak chatting
    private String mChatUser;

    //inisialisasi toolbar
    private Toolbar mChatToolbar;
    private TextView mTitleView, mLastSeenView;
    private CircleImageView mProfileImageView;

    // Firebase
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    //Storage Reference
    private StorageReference mImageStorage;

    private ProgressDialog mProgress;

    //inisialisasi chat add btn, edittext, dan send btn
    private ImageButton mChatAddBtn;
    private EditText mChatMessageView;
    private ImageButton mChatSendBtn;
    private FrameLayout file_attach;

    //Messages
    private RecyclerView mMessagesList;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    //attachment and voicenotes
    private ImageButton mgallery, mcamera, mvideo, mfiles, mcontact, mmusic;
    private ImageButton mvoice;

    //Swipe refresh
//    private SwipeRefreshLayout mRefreshLayout;
//    private static final int TOTAL_ITEMS_TO_LOAD = 20;
//    private int mCurrentPage = 1;

    private static final int GALLERY_PICK = 1;

    private String stat;

    //--------------- ON CRETE METHODS --------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Firebase
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        //--------Image Storage------------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mProgress = new ProgressDialog(this);

        //mendapatkan ID user yang diajak chatting
        mChatUser = getIntent().getStringExtra("user_id");
        String chat_user_name = getIntent().getStringExtra("user_name");

        //mendaftarkan toolbar
        mChatToolbar = (Toolbar) findViewById(R.id.chat_bar);
        setSupportActionBar(mChatToolbar);

        //---------------custom action bar---------------------------------
        ActionBar actionBar = getSupportActionBar();
        setSupportActionBar(mChatToolbar);
        //tombol kembali di toolbar <-
        actionBar.setDisplayHomeAsUpEnabled(true);
        //mengaktifkan toolbar custom
        actionBar.setDisplayShowCustomEnabled(true);

        //attachment
        mgallery = findViewById(R.id.mgallery);
        mcamera = findViewById(R.id.mcamera);
        mvideo = findViewById(R.id.mvideo);
        mfiles = findViewById(R.id.mfiles);
        mcontact = findViewById(R.id.mcontact);
        mmusic = findViewById(R.id.mmusic);

        mgallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        //voicenotes
        mvoice = findViewById(R.id.voice);

        //mengambil view chat custom bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        // add button
        mChatAddBtn = findViewById(R.id.btnAdd);
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stat == "invisible") {
                    stat = "visible";
                    file_attach.setVisibility(View.VISIBLE);
                } else {
                    stat = "invisible";
                    file_attach.setVisibility(View.INVISIBLE);
                }
            }
        });
        stat = "invisible";
        file_attach = findViewById(R.id.file_attach);
        file_attach.setVisibility(View.INVISIBLE);

        //----------------Custom Action bar Items --------------------
        mTitleView = (TextView) findViewById(R.id.userChat);
        mLastSeenView = (TextView) findViewById(R.id.last_seen);
        mProfileImageView = (CircleImageView) findViewById(R.id.userAvatar);

        //----------------inisialisasi chat addbtn, sendbtn, edittext------------------------------------
        mChatAddBtn = (ImageButton) findViewById(R.id.btnAdd);
        mChatSendBtn = (ImageButton) findViewById(R.id.btnSend);
        mChatMessageView = (EditText) findViewById(R.id.textMessage);

        //------------------------------------ Messages core---------------------------------------------
        mAdapter = new MessageAdapter(messageList);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
//        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        //mengambil pesan yang sudah ada
        loadMessages();

        //untuk judul siapa yang ngechat
        mTitleView.setText(chat_user_name);
        //Chat Toolbar
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //mengambil status online dari database
                String online = dataSnapshot.child("online").getValue().toString();

                //avatar orang yang ngechat di toolbar
                String image = dataSnapshot.child("thumb_image").getValue().toString();
                Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImageView);

                //------------------------------last seen view------------------------------------------------------------
                if (online.equals("online")) {
                    mLastSeenView.setText("Online");
                }else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //---------------------------Membuat Database Chat User di database-------------------------------------
        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamps", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Send Message Btn Click
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        //Refresh Layout
//        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mCurrentPage++;
//                messageList.clear();
//                loadMessages();
//            }
//        });
    }

    //LoadMessage
    private void loadMessages() {
        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                messageList.add(message);
                mAdapter.notifyDataSetChanged();

                //Ketika pesan baru muncul akan otomatis scroll ke pesan terbaru
                mMessagesList.scrollToPosition(messageList.size() -1);

                //Menghentikan refresh ketika pesan sudah muncul
//                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //--------------------------- FUNGSI KIRIM PESAN---------------------------------------------
    private void sendMessage() {
        //mengambil nilai dari edittext
        String message = mChatMessageView.getText().toString();

        //jika edittext tidak kosong
        if (!TextUtils.isEmpty(message)) {

            //mengambil ID user yang melakukan chat
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            //membuat database messages
            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            String push_id = user_message_push.getKey();

            //value untuk disimpan di database
            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new LinkedHashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            //mengosongkan edittext ketika pesan telah dikirim
            mChatMessageView.setText("");
            loadMessages();

            //mengirim value ke database, valuenya dari hasMap
            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        //menyimpan pesan error jika terjadi kesalahan
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            mProgress.setMessage("Uploading image...");
            mProgress.show();

            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" +mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");
                        loadMessages();

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                //menyimpan pesan error jika terjadi kesalahan
                                mProgress.hide();
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Chat", "http://[ENTER-YOUR-URL-HERE]");
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







