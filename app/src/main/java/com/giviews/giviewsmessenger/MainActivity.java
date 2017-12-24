package com.giviews.giviewsmessenger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Firebase
    private FirebaseAuth mAuth;
    private StorageReference mImageStorage;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;

    //Navbar Header Main
    private CircleImageView mProfilePic;
    private TextView mProfileName;
    private TextView mProfileStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        mAuth = FirebaseAuth.getInstance();

        // Memasang Toolbar pada Aplikasi
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Giviews Messenger");

        //Menerapkan TabLayout dan ViewPager pada Activity
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mViewPager = (ViewPager) findViewById(R.id.tabsPager);

//        mTabLayout.setupWithViewPager(mViewPager);

        //Memanggil dan Memasukan Value pada Class PagerAdapter(FragmentManager dan JumlahTab)
        mSectionPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());

        //Memasang Adapter pada ViewPager
        mViewPager.setAdapter(mSectionPagerAdapter);

        /*
         Menambahkan Listener yang akan dipanggil kapan pun halaman berubah atau
         bergulir secara bertahap, sehingga posisi tab tetap singkron
         */
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        //Callback Interface dipanggil saat status pilihan tab berubah.
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //Dipanggil saat tab keluar dari keadaan yang dipilih.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //Dipanggil ketika tab yang sudah dipilih, dipilih lagi oleh user.
            }
        });

        //
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUsersDatabase.keepSynced(true);
        }

        //-----------------------Navigation Drawer-----------------------
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_close, R.string.navigation_drawer_open);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //inflating view
//        View view = navigationView.inflateHeaderView(R.layout.nav_header_main);
        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
//        navigationView.addHeaderView(header); //digunakan untuk menmbah nav header
        navigationView.getHeaderCount();
        View view = navigationView.getHeaderView(0); // 0-index header

        //Casting view
        mProfilePic = (CircleImageView) view.findViewById(R.id.profile_pictures);
        mProfileName = (TextView) view.findViewById(R.id.profile_names);
        mProfileStat = (TextView) view.findViewById(R.id.profiles_status);

        //Retrive Data From Firebase
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mProfileName.setText(name);
                mProfileStat.setText(status);

                if (!image.equals("default")) {
                    Picasso.with(getApplicationContext()).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(mProfilePic, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(thumb_image).placeholder(R.drawable.default_avatar).into(mProfilePic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        }else {
            mUsersDatabase.child("online").setValue("online");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mUsersDatabase.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.action_logout) {
            mUsersDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
            sendToStart();
        } else if (item.getItemId() == R.id.action_setting) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        } else if (item.getItemId() == R.id.action_all_user) {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        } else if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(getApplicationContext(), UsersActivity.class));
        }

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
