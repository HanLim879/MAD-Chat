package com.example.groupassignmentrun1.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.groupassignmentrun1.R;
import com.example.groupassignmentrun1.adapters.RecentConversationsAdapter;
import com.example.groupassignmentrun1.databinding.ActivityMainBinding;
import com.example.groupassignmentrun1.models.ChatMessage;
import com.example.groupassignmentrun1.utilities.Constants;
import com.example.groupassignmentrun1.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;

public class UserProfile extends BaseActivity{
    //error
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        backToMainListener(); //back to main page
        accountButtonLister(); //Account
        logoutButtonLister(); //Logout
    }

    //navigate to main page
    private void backToMainListener(){
        ImageView backToMainPage = findViewById(R.id.backBtn);
        backToMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    //navigate to Account
    private void accountButtonLister(){
        Button accountBtn = findViewById(R.id.accountBtn);
        accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserProfile.this, Account.class);
                startActivity(intent);
            }
        });
    }

    //error
    private void logoutButtonLister(){
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    //error
    private void signOut() {
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

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}