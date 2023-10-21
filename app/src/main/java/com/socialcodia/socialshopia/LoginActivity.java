package com.socialcodia.socialshopia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialcodia.socialshopia.storage.Constants;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    //UI
    private EditText inputEmail, inputPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    //Firebase

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //UI init
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        //Firebase Init
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        Intent intent = getIntent();
        if (intent.getStringExtra("intentEmail")!=null)
        {
            String intentEmail = intent.getStringExtra("intentEmail");
            inputEmail.setText(intentEmail);
        }
        if (intent.getStringExtra("intentPassword")!=null)
        {
            String intentPassword = intent.getStringExtra("intentPassword");
            inputPassword.setText(intentPassword);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegister();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToForgotPassword();
            }
        });

        if (mAuth.getCurrentUser()!=null)
        {
            sendToHome();
        }

    }

    private void sendToForgotPassword()
    {
        Intent intent = new Intent(getApplicationContext(),ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void sendToRegister()
    {
        Intent intent = new Intent(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
    }

    private void validateData()
    {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty())
        {
            inputEmail.setError("Enter Email Address");
            inputEmail.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            inputEmail.setError("Enter Valid Email Address");
            inputEmail.requestFocus();
        }
        else if (password.isEmpty())
        {
            inputPassword.setError("Enter Password");
            inputPassword.requestFocus();
        }
        else if (password.length()<6)
        {
            inputPassword.setError("Password is less than 6 digit");
            inputPassword.requestFocus();
        }
        else
        {
            doLogin(email,password);
        }

    }

    private void doLogin(String email, String password)
    {
        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    isEmailVerified();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException)
                {
                    btnLogin.setEnabled(true);
                    inputPassword.setError("Wrong Password");
                    inputPassword.requestFocus();
                }
                else if (e instanceof FirebaseAuthInvalidUserException)
                {
                    btnLogin.setEnabled(true);
                    inputEmail.setError("Email Not Registered");
                    inputEmail.requestFocus();
                }
                else
                {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Oops! Something went wrong. "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void isEmailVerified()
    {
        if (mAuth.getCurrentUser()!=null)
        {
            boolean isEmailVerified = mAuth.getCurrentUser().isEmailVerified();
            if (isEmailVerified)
            {
                checkLoginState();
            }
            else
            {
                sendToUnverifiedUser();
            }
        }
    }

    private void sendToUnverifiedUser()
    {
        Intent intent = new Intent(getApplicationContext(),UnverifiedUserActivity.class);
        startActivity(intent);
        finish();
    }

    private void checkLoginState()
    {
        if (mAuth.getCurrentUser()!=null)
        {
            mRef.child(Constants.USERS).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child(Constants.USER_NAME).getValue(String.class);
                    if (name.isEmpty())
                    {
                        sendToSecondStepUpdateProfile();
                    }
                    else
                    {
                        sendToHome();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void sendToSecondStepUpdateProfile()
    {
        Intent intent = new Intent(getApplicationContext(),SecondStepUpdateProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }


}
