package com.example.groupassignmentrun1.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groupassignmentrun1.R;
import com.example.groupassignmentrun1.models.User;
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
    private ImageView backToMainPage;
    private Button qrBtn;
    private Button accountBtn;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        preferenceManager = new PreferenceManager(getApplicationContext());
        profilePicture = findViewById(R.id.profilePicture);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        backToMainPage = findViewById(R.id.backBtn);
        logout = findViewById(R.id.logoutBtn);
        qrBtn = findViewById(R.id.qrBtn);
        accountBtn = findViewById(R.id.accountBtn);

        setListeners();//set listeners
        loadUserDetails();
    }

    private void setListeners() {
        backToMainPage.setOnClickListener(v -> onBackPressed());
        accountBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Account.class)));
        qrBtn.setOnClickListener(v -> qrButton());
        logout.setOnClickListener(v -> logoutButton());
        accountBtn.setOnClickListener(v->accountButton());
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


        Intent intent = new Intent(UserProfile.this, Account.class);
        startActivity(intent);

    }

    //Logout
    private void logoutButton() {


        //Event listener for logout button


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

    private void qrButton() {
        Intent intent = new Intent(getApplicationContext(), QRCodePage.class);
        User user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }

    //Show toast message
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
