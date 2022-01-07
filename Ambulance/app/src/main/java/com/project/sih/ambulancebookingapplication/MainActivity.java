package com.project.sih.ambulancebookingapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PersistableBundle;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private SharedPreferences sharedPreferences;
    private FirebaseUser firebaseUser;
    public static String category = null;

    // variables for storing and fetching data from Firebase or SharedPreference
    public static DatabaseReference databaseReferenceRoot, databaseReferenceRootDriver, databaseReferenceRootRider;
    public static FirebaseAuth firebaseAuth;
    public static final String USER_FILE_KEY = "SIH17.AMBULANCE_APP.KEY0";

    // variables for sending notification
    public static final String CHANNEL_ID = "NOTIFICATION_CHANNEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        FirebaseApp.initializeApp(this);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        sharedPreferences = getSharedPreferences(USER_FILE_KEY, MODE_PRIVATE);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReferenceRoot = firebaseDatabase.getReference("Users");
        databaseReferenceRootDriver = firebaseDatabase.getReference("Drivers");
        databaseReferenceRootRider = firebaseDatabase.getReference("Riders");


        if(firebaseUser == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("KE").build());
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(), RC_SIGN_IN);
        }

        else {
            category = sharedPreferences.getString("category", "default");
            sendUserHome();
        }
    }

    // this function will be called as a response(success or failure) to signup
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                firebaseUser = firebaseAuth.getCurrentUser();
                String userId = firebaseUser.getUid();
                final String userPhone = firebaseUser.getPhoneNumber();

                // store some data locally
                SharedPreferences.Editor sharedEditor = sharedPreferences.edit();
                sharedEditor.putString("user_number", userPhone);
                sharedEditor.putString("user_id", userId);
                sharedEditor.apply();

                databaseReferenceRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userPhone)) {
                            category = dataSnapshot.child(userPhone).child("Category").getValue().toString();
                            sendUserHome();
                        }

                        else
                            startActivity(new Intent(MainActivity.this, SetUpProfileActivity.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                /*Intent intent = new Intent(MainActivity.this, SetUpProfileActivity.class);
                startActivity(intent);*/

            } else {
                Toast.makeText(MainActivity.this, "Signin failed, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    // A utility function for creating notification channel
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "CHANNEL",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("This is a Notification Channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("category", category);
    }


    private void sendUserHome() {
        if(category.equals("default")) {
            Intent intent = new Intent(MainActivity.this, SetUpProfileActivity.class);
            startActivity(intent);
        }

        if(category.equals("Rider")) {
            Intent intent = new Intent(MainActivity.this, Home.class);
            Home.store_data = false;
            startActivity(intent);
        }

        else if(category.equals("Driver")) {
            DriverHome.shouldShowDialog = false;
            DriverHome.store_data = false;
            Intent intent = new Intent(MainActivity.this, DriverHome.class);
            startActivity(intent);
        }
    }
}
