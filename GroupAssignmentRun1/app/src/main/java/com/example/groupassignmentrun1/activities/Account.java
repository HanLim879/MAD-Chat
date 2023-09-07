package com.example.groupassignmentrun1.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.groupassignmentrun1.R;
import com.example.groupassignmentrun1.utilities.Constants;
import com.example.groupassignmentrun1.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class Account extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ImageView profilePicture;
    private EditText editPassword, editName, editEmail;
    private String encodedImage;
    private ImageView backToMainPage;
    private Button saveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        preferenceManager = new PreferenceManager(getApplicationContext());
        profilePicture = findViewById(R.id.profilePicture);
        editPassword = findViewById(R.id.editPassword);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        backToMainPage = findViewById(R.id.backBtn);
        saveChanges = findViewById(R.id.saveChanges);

        setListeners(); //back to User Profile
        loadUserDetails(); //Display user details
        editProfile(); //Edit Profile
    }

    private void setListeners() {
        backToMainPage.setOnClickListener(v -> onBackPressed());
    }

    //navigate to User Profile

    //Display user details
    private void loadUserDetails() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        profilePicture.setImageBitmap(bitmap); //Display profile picture
        editPassword.setText(preferenceManager.getString(Constants.KEY_PASSWORD)); //Display User Password
        editName.setText(preferenceManager.getString(Constants.KEY_NAME)); //Display User Name
        editEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL)); //Display User Email
    }

    //Edit Profile
    private void editProfile(){
        //Event listener for profile picture
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfilePicture();  //Select the picture to change in gallery
            }
        });



        //Event listener for Save Changes button
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isNameChanged = isNameChanged();
                //boolean isProfilePictureChanged = isProfilePictureChanged();
                boolean isPasswordChanged = isPasswordChanged();
                boolean isEmailChanged = isEmailChanged();

                //If user make any changes in the user details
                if (isNameChanged  || isPasswordChanged || isEmailChanged) {
                    showToast("User profile successfully updated!");
                }else if(isProfilePictureChanged()){
                    showToast("User profile successfully updated!");
                }
                else{
                    showToast("Nothing has been changed!");
                }
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    //Edit name
    private boolean isNameChanged(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        String editedName=editName.getText().toString();
        //If user edit the name
        if(!preferenceManager.getString(Constants.KEY_NAME).equals(editedName)){
            preferenceManager.putString(Constants.KEY_NAME, editedName); //update name in PreferenceManager

            //Unique identifier
            String uid = preferenceManager.getString(Constants.KEY_USER_ID);
            if(uid != null){
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(uid) // search for userID
                        .update(Constants.KEY_NAME,editedName) //update name in database
                        .addOnFailureListener(exception -> {
                            showToast("Update Fail");
                        });
            }
            return true;
        }
        else{
            return false;
        }
    }

    private boolean isEmailChanged(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String editedEmail=editEmail.getText().toString();


        //If user edit the Email
        if(!preferenceManager.getString(Constants.KEY_EMAIL).equals(editedEmail)){
            preferenceManager.putString(Constants.KEY_EMAIL, editedEmail); //update name in PreferenceManager

            //Unique identifier
            String uid = preferenceManager.getString(Constants.KEY_USER_ID);
            if(uid != null){
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(uid) // search for userID
                        .update(Constants.KEY_EMAIL,editedEmail) //update Email in database
                        .addOnFailureListener(exception -> {
                            showToast("Update Fail");
                        });
            }
            return true;
        }
        else{
            return false;
        }
    }

    private boolean isPasswordChanged(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        String editedPassword=editPassword.getText().toString();
        //If user edit the Email
        if(!preferenceManager.getString(Constants.KEY_PASSWORD).equals(editedPassword)){
            preferenceManager.putString(Constants.KEY_PASSWORD, editedPassword); //update password in PreferenceManager

            //Unique identifier
            String uid = preferenceManager.getString(Constants.KEY_USER_ID);
            if(uid != null){
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(uid) // search for userID
                        .update(Constants.KEY_PASSWORD,editedPassword) //update password in database
                        .addOnFailureListener(exception -> {
                            showToast("Update Fail");
                        });
            }
            return true;
        }
        else{
            return false;
        }
    }

    //Edit profile picture
    private boolean isProfilePictureChanged(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        //If user edit the profile picture
        if(!encodedImage.equals(preferenceManager.getString(Constants.KEY_IMAGE))){
            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage); //update profile picture in PreferenceManager

            //Unique identifier
            String uid = preferenceManager.getString(Constants.KEY_USER_ID);
            if(uid != null){
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(uid) // search for userID
                        .update(Constants.KEY_IMAGE, encodedImage) //update profile picture in database
                        .addOnFailureListener(exception -> {
                            showToast("Update Fail");
                        });
            }
            return true;
        }
        else{
            return false;
        }
    }

    //Show toast message
    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    //Select the picture to change in gallery
    private void changeProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickImage.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            profilePicture.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
}