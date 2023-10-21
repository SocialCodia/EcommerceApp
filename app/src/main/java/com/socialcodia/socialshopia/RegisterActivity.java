package com.socialcodia.socialshopia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.socialcodia.socialshopia.storage.Constants;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    String email,password;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Ui init
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

//       Firebase Init
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });

    }

    private void validateData()
    {
        email = inputEmail.getText().toString().trim();
        password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

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
            inputPassword.setError("Password should not be less than 6 character");
            inputPassword.requestFocus();
        }
        else if (confirmPassword.length()<6)
        {
            inputConfirmPassword.setError("Password should not be less than 6 character");
            inputConfirmPassword.requestFocus();
        }
        else if (!password.equals(confirmPassword))
        {
            inputConfirmPassword.setError("Password Not Matched");
            inputConfirmPassword.requestFocus();
            inputPassword.setError("Password Not Matched");
            inputPassword.requestFocus();
            inputPassword.setText("");
            inputConfirmPassword.setText("");
        }
        else
        {
            doRegister(email,password);
        }
    }

    private void doRegister(final String email, final String password)
    {
        btnRegister.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    String userId = task.getResult().getUser().getUid();
                    saveData(email,userId);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException)
                        {
                            btnRegister.setEnabled(true);
                            inputEmail.setError("Email Already Registered");
                            inputEmail.requestFocus();
                        }
                        else
                        {
                            btnRegister.setEnabled(true);
                            Toast.makeText(RegisterActivity.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveData(String email, String userId)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_EMAIL,email);
        map.put(Constants.USER_ID,userId);
        map.put(Constants.USER_IMAGE,"");
        map.put(Constants.USER_MOBILE,"");
        map.put(Constants.USER_NAME,"");
        map.put(Constants.TIMESTAMP,String.valueOf(System.currentTimeMillis()));
        map.put(Constants.CITY,"");
        map.put(Constants.STATE,"");
        map.put(Constants.COUNTRY,"");
        map.put(Constants.LATITUDE,"");
        map.put(Constants.LONGITUDE,"");
        map.put(Constants.ADDRESS,"");
        mRef.child(Constants.USERS).child(userId).setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendEmailVerification();
            }
        });
    }

    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToLogin()
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendEmailVerification()
    {
        if (mAuth.getCurrentUser()!=null)
        {
            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(RegisterActivity.this, "Email Verification Link has been sent to your email address", Toast.LENGTH_SHORT).show();
                        sendToLoginWithEmailAndPassword();
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, "Failed to send email verification link", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendToLoginWithEmailAndPassword()
    {
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.putExtra("intentEmail",email);
        intent.putExtra("intentPassword",password);
        startActivity(intent);
        finish();
    }

}
