package com.giviews.giviewsmessenger.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.giviews.giviewsmessenger.GetTimeAgo;
import com.giviews.giviewsmessenger.R;
import com.giviews.giviewsmessenger.models.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by asus on 04/09/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private List<Messages> mMessageList;
    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.message_single_layout, parent, false);
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case VIEW_TYPE_ME:
                View viewChatMine = layoutInflater.inflate(R.layout.item_chat_mine, parent, false);
                viewHolder = new MyChatViewHolder(viewChatMine);
                break;
            case VIEW_TYPE_OTHER:
                View viewChatOther = layoutInflater.inflate(R.layout.item_chat_other, parent, false);
                viewHolder = new OtherChatViewHolder(viewChatOther);
                break;
        }
        return viewHolder;
    }

//    public class MessageViewHolder extends RecyclerView.ViewHolder {
//        public TextView messageText;
//        public CircleImageView profileImage;
//        public TextView displayName;
//        public TextView displayTime;
//
//        public MessageViewHolder(View view) {
//            super(view);
//
//            messageText = (TextView) view.findViewById(R.id.message_text_layout);
//            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
//            displayName = (TextView) view.findViewById(R.id.name_text_layout);
//            displayTime = (TextView) view.findViewById(R.id.message_time_layout);
//        }
//    }

    //get from
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        if (from_user.equals(current_user_id)) {
            configureMyChatViewHolder((MyChatViewHolder) holder, position);
        } else {
            configureOtherChatViewHolder((OtherChatViewHolder) holder, position);
        }

    }

    private void configureOtherChatViewHolder(final OtherChatViewHolder holder, final int position) {
        //mAuth = FirebaseAuth.getInstance();
        //String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        long time = c.getTime();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString().substring(0, 1);
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                //nama Pengirim pesan
                holder.txtUserAlphabet.setText(name);

                //gambar profile yang mengirim pesan
                //Picasso.with(holder.profileImage.getContext()).load(image)
                //        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Memasukan pesan
        //OtherChatViewHolder.txtChatMessage.setText(c.getMessage());

        //Memparsing time
        GetTimeAgo getTimeAgo = new GetTimeAgo();
        long lastTime = Long.parseLong(String.valueOf(time));
        String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getTimeAgo);

        //memasukan pesan dan waktu ke textview
        // holder.displayTime.setText(lastSeenTime);
        holder.txtChatMessage.setText(c.getMessage());
    }

    private void configureMyChatViewHolder(final MyChatViewHolder holder, final int position) {
        //mAuth = FirebaseAuth.getInstance();
        //String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
//        long time = c.getTime();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString().substring(0, 1);
//                String image = dataSnapshot.child("thumb_image").getValue().toString();

                //nama Pengirim pesan
                holder.txtUserAlphabet.setText(name);

                //gambar profile yang mengirim pesan
                //Picasso.with(holder.profileImage.getContext()).load(image)
                //        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Memasukan pesan ke dalam textview
        //MyChatViewHolder.txtChatMessage.setText(c.getMessage());

        //Memparsing time
//        GetTimeAgo getTimeAgo = new GetTimeAgo();
//        long lastTime = Long.parseLong(String.valueOf(time));
//        String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getTimeAgo);

        //memasukan pesan dan waktu ke textview
//         holder.displayTime.setText(lastSeenTime);
        holder.txtChatMessage.setText(c.getMessage());
    }

    //get item count
    @Override
    public int getItemCount() {
        if (mMessageList != null) {
            return mMessageList.size();
        }
        return 0;
    }

    //Menentukan viewtype me / other
    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        if (from_user.equals(current_user_id)) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    //casting view mychat viewholder
    private static class MyChatViewHolder extends RecyclerView.ViewHolder {
        private static TextView txtChatMessage;
        private static TextView txtUserAlphabet;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (TextView) itemView.findViewById(R.id.text_view_chat_message);
            txtUserAlphabet = (TextView) itemView.findViewById(R.id.text_view_user_alphabet);
        }
    }

    //Casting view other chat viewholder
    private static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        private static TextView txtChatMessage;
        private static TextView txtUserAlphabet;

        public OtherChatViewHolder(View itemView) {
            super(itemView);
            txtChatMessage = (TextView) itemView.findViewById(R.id.text_view_chat_message);
            txtUserAlphabet = (TextView) itemView.findViewById(R.id.text_view_user_alphabet);
        }
    }
}
