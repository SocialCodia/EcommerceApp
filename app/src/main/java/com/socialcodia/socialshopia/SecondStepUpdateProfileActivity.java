package com.socialcodia.socialshopia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialcodia.socialshopia.storage.Constants;

import java.io.IOException;
import java.util.HashMap;

public class SecondStepUpdateProfileActivity extends AppCompatActivity {

    private EditText inputName, inputMobileNumber;
    private ImageView userProfileImage;
    private Button btnUpdateProfile;

    Uri filePath;
    String userId,name,mobileNumber;
    String storagePermission[];

    //Firebase
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_step_update_profile);

        inputName = findViewById(R.id.inputName);
        inputMobileNumber = findViewById(R.id.inputMobileNumber);
        userProfileImage = findViewById(R.id.userProfileImage);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        //Firebase Init
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mUser = mAuth.getCurrentUser();

        if (mUser!=null)
        {
            userId = mUser.getUid();
        }

        // Init storage permission array
        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStoragePermission())
                {
                    requestStoragePermission();
                }
                else
                {
                    chooseImage();
                }
            }
        });


    }

    private boolean checkStoragePermission()
    {
        boolean result = ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result;
    }

    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this,storagePermission,100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==100 && grantResults.length>0)
        {
            boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (storageAccepted)
            {
                chooseImage();
            }
            else
            {
                Toast.makeText(this, "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100 && resultCode==RESULT_OK && data!=null)
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                userProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void validateData()
    {
        name = inputName.getText().toString().trim();
        mobileNumber = inputMobileNumber.getText().toString().trim();

        if (name.isEmpty())
        {
            inputName.setError("Enter Name");
            inputName.requestFocus();
        }
        else if (name.length()<3)
        {
            inputName.setError("Name should be greater than 3 character");
            inputName.requestFocus();
        }
        else if (filePath!=null)
        {
            uploadImage();
        }
        else
        {
            updateProfile("");
        }
    }

    private void uploadImage()
    {
        btnUpdateProfile.setEnabled(false);
        mStorageRef.child("userProfileImage").child(userId).putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageDownloadUrl = uri.toString();
                            updateProfile(imageDownloadUrl);
                        }
                    });
                }
            }
        });
    }

    private void updateProfile(String imageDownloadUrl)
    {
        HashMap<String,Object> map = new HashMap<>();
        map.put(Constants.USER_NAME,name);
        if (mobileNumber.isEmpty())
        {
            map.put(Constants.USER_MOBILE,"");
        }
        else
        {
            map.put(Constants.USER_MOBILE,mobileNumber);
        }
        map.put(Constants.USER_IMAGE,imageDownloadUrl);
        mRef.child(Constants.USERS).child(userId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendToHome();
            }
        });
    }

    private void sendToHome()
    {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }
}
