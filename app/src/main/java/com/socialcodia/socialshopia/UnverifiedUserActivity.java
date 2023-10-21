package com.socialcodia.socialshopia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class UnverifiedUserActivity extends AppCompatActivity {

    private TextView tvEmailAddress;
    private Button btnSendVerificationEmail, btnSignOut;

    private ActionBar actionBar;
    private Toolbar toolbar;
    //Firebase

    String email;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unverified_user);

        tvEmailAddress = findViewById(R.id.tvEmailAddress);
        btnSendVerificationEmail = findViewById(R.id.btnSendVerificationEmail);
        btnSignOut = findViewById(R.id.btnSignOut);
        toolbar = findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);

        //Firebase Init
        mAuth = FirebaseAuth.getInstance();

        email = mAuth.getCurrentUser().getEmail();
        tvEmailAddress.setText(email);

        btnSendVerificationEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEmailVerified();
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogout();
            }
        });

    }

    private void doLogout()
    {
        btnSignOut.setEnabled(false);
        mAuth.signOut();
        sendToLoginWithEmail();
    }

    private void sendToLoginWithEmail()
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.putExtra("intentEmail",email);
        startActivity(intent);
        finish();
    }

    private void isEmailVerified()
    {
        btnSendVerificationEmail.setEnabled(false);
        mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (mAuth.getCurrentUser()!=null)
                {
                    boolean isEmailVerified = mAuth.getCurrentUser().isEmailVerified();
                    if (isEmailVerified)
                    {
                        btnSendVerificationEmail.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "You email has been verified. Now you can login to your account", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        sendEmailVerification();
                    }
                }
            }
        });
    }

    private void sendEmailVerification()
    {
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    btnSendVerificationEmail.setEnabled(true);
                    Toast.makeText(UnverifiedUserActivity.this, "A verification email has been sent to your email address", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    btnSendVerificationEmail.setEnabled(true);
                    Toast.makeText(UnverifiedUserActivity.this, "Too fast! Try again after 30 Seconds...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
