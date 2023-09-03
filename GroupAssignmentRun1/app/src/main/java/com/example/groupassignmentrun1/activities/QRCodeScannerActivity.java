package com.example.groupassignmentrun1.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignmentrun1.databinding.ActivityMainBinding;
import com.example.groupassignmentrun1.databinding.ActivityQrcodeScannerBinding;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import me.dm7.barcodescanner.zxing.ZXingScannerView.ResultHandler;

public class QRCodeScannerActivity extends AppCompatActivity implements ResultHandler {
    private ZXingScannerView scannerView;;
    private ActivityQrcodeScannerBinding binding;
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrcodeScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the ZXingScannerView
        scannerView = binding.scannerView;

        // Configure the scanner settings, if needed
        scannerView.setAutoFocus(true);
        scannerView.setResultHandler(result -> {
            // Handle the scanned QR code result here
            String contents = result.getText();
            // Do something with the scanned data
        });

        // Start the scanner
        scannerView.startCamera();

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
                // Permission was granted, you can use the camera
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }

    @Override
    public void handleResult(Result result) {
        try {
            // Handle the scanned QR code result here
            String contents = result.getText();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            // Perform your desired action with the scanned data here
            // For example, you can display it in a TextView, start a new activity, or send it to a server.
            // Here, we'll display it in a Toast message.
            Toast.makeText(this, "Scanned: " + contents, Toast.LENGTH_SHORT).show();

            // To continue scanning, you can call:
            // scannerView.resumeCameraPreview(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error scanning QR code.", Toast.LENGTH_SHORT).show();
        }
    }
}

