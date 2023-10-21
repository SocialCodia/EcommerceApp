package com.socialcodia.socialshopia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText inputEmail;
    private Button btnForgotPassword;
    private ImageButton btnBack;

    //Firebase
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Ui init

        inputEmail = findViewById(R.id.inputEmail);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnBack = findViewById(R.id.btnBack);

        //Firebase Init
        mAuth = FirebaseAuth.getInstance();

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void validateData()
    {
        String email = inputEmail.getText().toString().trim();
        if (email.isEmpty())
        {
            inputEmail.setError("Enter Email");
            inputEmail.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            inputEmail.setError("Enter Valid Email");
            inputEmail.requestFocus();
        }
        else
        {
            sendPasswordResetEmail(email);
        }
    }

    private void sendPasswordResetEmail(final String email)
    {
        btnForgotPassword.setEnabled(false);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(ForgotPasswordActivity.this, "A Password Reset Email has been sent to your email address", Toast.LENGTH_LONG).show();
                    sendToLoginWithEmail(email);
                }
                else
                {
                    btnForgotPassword.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Oops! Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToLoginWithEmail(String email)
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.putExtra("intentEmail",email);
        startActivity(intent);
        finish();
    }


}
