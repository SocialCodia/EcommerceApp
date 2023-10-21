package com.socialcodia.socialshopia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEditProfile = findViewById(R.id.btnEditProfile);


        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToEditProfile();
            }
        });

    }

    private void sendToEditProfile()
    {
        Intent intent = new Intent(getApplicationContext(),EditProfileActivity.class);
        startActivity(intent);
    }
}