package com.example.groupassignmentrun1.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupassignmentrun1.R;
import com.example.groupassignmentrun1.utilities.Constants;
import com.example.groupassignmentrun1.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UserProfile extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ImageView profilePicture;
    private TextView userName;
    private TextView userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        preferenceManager = new PreferenceManager(getApplicationContext());
        profilePicture = findViewById(R.id.profilePicture);
        userName=findViewById(R.id.userName);
        userEmail=findViewById(R.id.userEmail);


        backToMainListener(); //Back to main page
        loadUserDetails(); //Display user profile picture, name, email on screen
        accountButton(); //Account
        logoutButton(); //Logout

    }

    //Navigate to main page
    private void backToMainListener() {
        ImageView backToMainPage = findViewById(R.id.backBtn);
        //Event listener for back button
        backToMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    //Display user profile picture, name, email on screen
    private void loadUserDetails() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        profilePicture.setImageBitmap(bitmap);
        userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }


    //Navigate to Account
    private void accountButton() {
        Button accountBtn = findViewById(R.id.accountBtn);
        accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, Account.class);
                startActivity(intent);
            }
        });
    }

    //Logout
    private void logoutButton() {
        Button logout = findViewById(R.id.logoutBtn);
        //Event listener for logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Signing out...");
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                DocumentReference documentReference =
                        database.collection(Constants.KEY_COLLECTION_USERS).document(
                                preferenceManager.getString(Constants.KEY_USER_ID)
                        );
                HashMap<String, Object> updates = new HashMap<>();
                updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                documentReference.update(updates)
                        .addOnSuccessListener(unused -> {
                            preferenceManager.clear();
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        })
                        .addOnFailureListener(e -> showToast("Unable to sign out"));
            }
        });
    }

    //Show toast message
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
