package com.giviews.giviewsmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giviews.giviewsmessenger._sliders.FragmentSlider;
import com.giviews.giviewsmessenger._sliders.SliderIndicator;
import com.giviews.giviewsmessenger._sliders.SliderPagerAdapter;
import com.giviews.giviewsmessenger._sliders.SliderView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button linkReg;
    private Button loginLink;
    private Button googleBtn;
    private ProgressDialog pd;

    //Login with google
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "LOGIN_PAGE";

    //image slider
    private SliderPagerAdapter mAdapter;
    private SliderIndicator mIndicator;

    private SliderView sliderView;
    private LinearLayout mLinearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //Transparent Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

            }
        };

        linkReg = (Button) findViewById(R.id.registerLink);
        loginLink = (Button) findViewById(R.id.loginLink);
        googleBtn = (Button) findViewById(R.id.googleBtn);
        pd = new ProgressDialog(this);

        //------------------------Register Intent-------------------------------------------
        linkReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });

        //-------------------------Login Intent-------------------------------------------
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        //--------------------------Login dengan google-------------------------------------
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.setMessage("Signing with google...");
                pd.show();
                signIn();
            }
        });


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getApplicationContext(), "You Got an error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        //image slider
        sliderView = (SliderView) findViewById(R.id.sliderView);
        mLinearLayout = (LinearLayout) findViewById(R.id.pagesContainer);
        setupSlider();
    }

    //image slider
    private void setupSlider() {
        sliderView.setDurationScroll(800);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(FragmentSlider.newInstance("http://www.menucool.com/slider/prod/image-slider-1.jpg"));
        fragments.add(FragmentSlider.newInstance("http://www.menucool.com/slider/prod/image_slider_2.jpg"));
        fragments.add(FragmentSlider.newInstance("http://www.menucool.com/slider/prod/image-slider-3.jpg"));
        fragments.add(FragmentSlider.newInstance("http://www.menucool.com/slider/prod/image-slider-4.jpg"));

        mAdapter = new SliderPagerAdapter(getSupportFragmentManager(), fragments);
        sliderView.setAdapter(mAdapter);
        mIndicator = new SliderIndicator(this, mLinearLayout, sliderView, R.drawable.indicator_circle);
        mIndicator.setPageCount(fragments.size());
        mIndicator.show();
    }

    //image slider
    private void setupSliderOffline() {
        sliderView.setDurationScroll(800);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(FragmentSlider.newInstance("image_slider_1.jpg"));
        fragments.add(FragmentSlider.newInstance("image_slider_2.jpg"));
        fragments.add(FragmentSlider.newInstance("image-slider-1.jpg"));
        fragments.add(FragmentSlider.newInstance("image_slider_2.jpg"));

        mAdapter = new SliderPagerAdapter(getSupportFragmentManager(), fragments);
        sliderView.setAdapter(mAdapter);
        mIndicator = new SliderIndicator(this, mLinearLayout, sliderView, R.drawable.indicator_circle);
        mIndicator.setPageCount(fragments.size());
        mIndicator.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        setupSlider();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        final String uid = currentUser.getUid();
                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.hasChild(uid)) {
                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                                    String device_token = FirebaseInstanceId.getInstance().getToken();

                                    HashMap<String, String> userMap = new HashMap<String, String>();
                                    userMap.put("name", mAuth.getCurrentUser().getDisplayName());
                                    userMap.put("status", "Hi there, I'm using giviews");
                                    userMap.put("image", "default");
                                    userMap.put("thumb_image", "default");
                                    userMap.put("device_token", device_token);

                                    //jika belum ada otomatis didaftarkan
                                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            pd.dismiss();
                                            startIntent();
                                            Toast.makeText(getApplicationContext(), "Anda mendaftar dengan akun google", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {}


                        });

                        //jika login
                        pd.dismiss();
                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(mainIntent);
                        Toast.makeText(getApplicationContext(), "Selamat Datang kembali", Toast.LENGTH_LONG).show();

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful())

                        {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }

                });
    }

    private void startIntent() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}
