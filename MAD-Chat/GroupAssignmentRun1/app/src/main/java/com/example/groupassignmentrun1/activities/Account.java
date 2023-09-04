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

public class Account extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private ImageView profilePicture;
    private EditText editID, editName, editEmail;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        preferenceManager = new PreferenceManager(getApplicationContext());
        profilePicture = findViewById(R.id.profilePicture);
        editID = findViewById(R.id.editID);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);

        backToUserProfile(); //back to User Profile
        loadUserDetails(); //Display user details
        editProfile(); //Edit Profile
    }

    //navigate to User Profile
    private void backToUserProfile(){
        ImageView backToMainPage = findViewById(R.id.backBtn);
        //Event listener for back button
        backToMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account.this, UserProfile.class);
                startActivity(intent);
            }
        });
    }

    //Display user details
    private void loadUserDetails() {
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        profilePicture.setImageBitmap(bitmap); //Display profile picture
        editID.setText(preferenceManager.getString(Constants.KEY_USER_ID)); //Display User ID
        editName.setText(preferenceManager.getString(Constants.KEY_NAME)); //Display User Name
        editEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL)); //Display User Email
    }

    //Edit Profile
    private void editProfile(){
        //Event listener for profile picture
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfilePicture();;  //Select the picture to change in gallery
            }
        });

        Button saveChanges = findViewById(R.id.saveChanges);
        //Event listener for Save Changes button
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isNameChanged = isNameChanged();
                boolean isProfilePictureChanged = isProfilePictureChanged();

                //If user make any changes in the user details
                if(isNameChanged || isProfilePictureChanged){
                    showToast("User profile updated");
                }
                else{
                    showToast("Update Fail");
                }
            }
        });

    }

    //Edit name
    private boolean isNameChanged(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        //If user edit the name
       if(!preferenceManager.getString(Constants.KEY_NAME).equals(editName.getText().toString())){
           preferenceManager.putString(Constants.KEY_NAME, editName.getText().toString()); //update name in PreferenceManager

           //Unique identifier
           String uid = preferenceManager.getString(Constants.KEY_USER_ID);
           if(uid != null){
               database.collection(Constants.KEY_COLLECTION_USERS)
                       .document(uid) // search for userID
                       .update(Constants.KEY_NAME,editName.getText().toString()) //update name in database
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