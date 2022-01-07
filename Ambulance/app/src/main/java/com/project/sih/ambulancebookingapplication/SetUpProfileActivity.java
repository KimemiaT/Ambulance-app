package com.project.sih.ambulancebookingapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SetUpProfileActivity extends AppCompatActivity {

    TextView nameText;
    RadioGroup radioGroup;
    Button continueButton, logoutButton;
    String category = "Rider", name;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profile);

        final FirebaseAuth firebaseAuth = MainActivity.firebaseAuth;

        setContentView(R.layout.activity_set_up_profile);
        sharedPreferences = getSharedPreferences(getResources().getString(R.string.FILE_NAME_KEY), MODE_PRIVATE);

        nameText = findViewById(R.id.nameText);
        radioGroup = findViewById(R.id.radioGroup);
        continueButton = findViewById(R.id.continueButton);
        logoutButton = findViewById(R.id.logoutButton);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == R.id.userRadioButton)
                    category = "Rider";
                else
                    category = "Driver";
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = nameText.getText().toString();
                if (name.length() < 3)
                    Toast.makeText(SetUpProfileActivity.this, "Name must be atleast 3 characters long", Toast.LENGTH_LONG)
                            .show();
                else {
                    Intent intent;

                    if(category.equals("Rider"))  {
                        Home.store_data = true;
                        intent = new Intent(SetUpProfileActivity.this, Home.class);
                        intent.putExtra("details", new String[]{name, category});
                    }
                    else {
                        DriverHome.shouldShowDialog = true;
                        DriverHome.store_data = true;
                        intent = new Intent(SetUpProfileActivity.this, DriverSetupProfile.class);
                        intent.putExtra("details", new String[]{name, category});
                    }

                    startActivity(intent);
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firebaseAuth != null) {
                    firebaseAuth.signOut();
                    startActivity(new Intent(SetUpProfileActivity.this, MainActivity.class));
                }
                else
                    Toast.makeText(SetUpProfileActivity.this, "An unknown error occurred!", Toast.LENGTH_LONG).show();
            }
        });
    }

}
