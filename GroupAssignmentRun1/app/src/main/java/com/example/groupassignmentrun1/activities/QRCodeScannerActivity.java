package com.example.groupassignmentrun1.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.groupassignmentrun1.databinding.ActivityQrcodeScannerBinding;
import com.example.groupassignmentrun1.listeners.UserListener;
import com.example.groupassignmentrun1.models.User;
import com.example.groupassignmentrun1.utilities.Constants;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRCodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler, UserListener {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private ZXingScannerView scannerView;
    private ActivityQrcodeScannerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrcodeScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the ZXingScannerView
        scannerView = binding.scannerView;

        // Check for camera permission and request if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            startScanner();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the scanner when the activity is paused
        scannerView.stopCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, start the scanner
                startScanner();
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(this, "Camera permission denied. Cannot scan QR codes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startScanner() {
        // Configure the scanner settings, if needed
        scannerView.setAutoFocus(true);
        scannerView.setResultHandler(this);

        // Start the scanner
        scannerView.startCamera();
    }

    @Override
    public void handleResult(Result result) {
        try {
            // Handle the scanned QR code result here
            String contents = result.getText();
            User user = fromQRCodeString(contents);
            onUserClicked(user);
            // To continue scanning, you can call:
            // scannerView.resumeCameraPreview(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error scanning QR code.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }

    public static User fromQRCodeString(String jsonString) {
        User user = new User();

        try {
            // Parse the JSON string into a JSONObject
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract the values from the JSONObject and set them in the User object
            user.name = jsonObject.optString("name", "");
            user.image = jsonObject.optString("image", "");
            user.email = jsonObject.optString("email", "");
            user.token = jsonObject.optString("token", "");
            user.id = jsonObject.optString("id", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

}
