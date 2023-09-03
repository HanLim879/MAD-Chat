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
import com.example.groupassignmentrun1.adapters.RecentConversationsAdapter;
import com.example.groupassignmentrun1.databinding.ActivityMainBinding;
import com.example.groupassignmentrun1.models.ChatMessage;
import com.example.groupassignmentrun1.models.User;
import com.example.groupassignmentrun1.utilities.Constants;
import com.example.groupassignmentrun1.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class UserProfile extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ImageView profilePicture;
    private TextView userName;
    private TextView userEmail;
    private ImageView backToMainPage;
    private Button qrBtn;
    private Button accountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        preferenceManager = new PreferenceManager(getApplicationContext());
        profilePicture = findViewById(R.id.profilePicture);
        userName=findViewById(R.id.userName);
        userEmail=findViewById(R.id.userEmail);
        backToMainPage = findViewById(R.id.backBtn);
        qrBtn = findViewById(R.id.qrBtn);
        accountBtn = findViewById(R.id.accountBtn);

        loadUserDetails(); //Display user profile picture, name, email on screen
        setListeners(); //Back to main page

    }

    //navigate to main page
    private void setListeners() {
        backToMainPage.setOnClickListener(v -> onBackPressed());
        accountBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),Account.class)));
        qrBtn.setOnClickListener(v -> qrButton());
    }

    //Display user profile picture, name, email on screen
    private void loadUserDetails() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        profilePicture.setImageBitmap(bitmap);
        userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }


    //navigate to Account
    private void qrButton() {
        Intent intent = new Intent(getApplicationContext(), QRCodePage.class);
        User user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}
