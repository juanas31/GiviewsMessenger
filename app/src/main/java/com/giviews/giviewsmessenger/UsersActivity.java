package com.giviews.giviewsmessenger;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.giviews.giviewsmessenger.models.Users;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
//    private Toolbar mToolbar;
    private ImageView arrow_back;
    private RecyclerView mUsersList;

    private EditText searchForm;
    private ImageButton searchBtn;

    //Firebase
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

//        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle("All Users");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        arrow_back = findViewById(R.id.arrow_back);
        arrow_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsersActivity.super.onBackPressed();
            }
        });

        searchForm = findViewById(R.id.searchForm);
        searchBtn = findViewById(R.id.searchBtn);

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String searchText = searchForm.getText().toString();

                firebaseUserSearch(searchText);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        String searchText = searchForm.getText().toString();

        firebaseUserSearch(searchText);
    }

    private void firebaseUserSearch(String searchText) {

        Query firebaseSearchQuery = mUsersDatabase.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                firebaseSearchQuery
//                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int position) {

//                usersViewHolder.setName(users.getName());
//                usersViewHolder.setStatus(users.getStatus());
//                usersViewHolder.setImage(getApplicationContext(), users.getImage());

                usersViewHolder.setDetails(getApplicationContext(), users.getName(), users.getStatus(), users.getImage());

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDetails(final Context ctx, String name, String status, final String thumb_image) {
            TextView usersNameView = (TextView) mView.findViewById(R.id.username);
            TextView usersStatusView = (TextView) mView.findViewById(R.id.status);
            final CircleImageView profil_image = (CircleImageView) mView.findViewById(R.id.circleImageView);

            usersNameView.setText(name);
            usersStatusView.setText(status);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(profil_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(profil_image);
                }
            });
        }

//        public void setName(String name) {
//            TextView usersNameView = (TextView) mView.findViewById(R.id.username);
//            usersNameView.setText(name);
//        }
//
//        public void setStatus(String status) {
//            TextView usersStatusView = (TextView) mView.findViewById(R.id.status);
//            usersStatusView.setText(status);
//        }
//
//        public void setImage(final Context ctx, final String thumb_image) {
//            final CircleImageView profil_image = (CircleImageView) mView.findViewById(R.id.circleImageView);
//
//            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(profil_image, new Callback() {
//                @Override
//                public void onSuccess() {
//
//                }
//
//                @Override
//                public void onError() {
//                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(profil_image);
//                }
//            });
//        }
    }
}
