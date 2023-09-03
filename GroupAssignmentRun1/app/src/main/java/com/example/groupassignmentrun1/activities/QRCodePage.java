package com.example.groupassignmentrun1.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.groupassignmentrun1.databinding.ActivityQrcodePageBinding;
import com.example.groupassignmentrun1.models.User;
import com.example.groupassignmentrun1.utilities.Constants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRCodePage extends AppCompatActivity {

    private ActivityQrcodePageBinding binding;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrcodePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        setQRCodeImageView();
        Toast.makeText(getApplicationContext(), toQRCodeString(user),Toast.LENGTH_SHORT).show();
    }

    public void setQRCodeImageView(){
        Bitmap qrCode = generateQRCodeFromUser(user,1500,1500);
        ImageView qrCodeImageView = binding.QRCode; // Replace with the correct view reference
        if (qrCode != null) {
            // Set the generated QR code to the ImageView
            qrCodeImageView.setImageBitmap(qrCode);
        } else {
            // Handle error case
            Toast.makeText(getApplicationContext(),"qrCode is null",Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap generateQRCodeFromUser(User user, int width, int height) {
        try {
            // Convert the User object to a string representation (customize this part based on your User class)
            String userData = toQRCodeString(user);

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = new MultiFormatWriter().encode(userData, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xFF000000; // Black color
                    } else {
                        pixels[y * width + x] = 0xFFFFFFFF; // White color
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            Log.e("QRCodeGenerator", "Error generating QR code: " + e.getMessage());
            return null;
        }
    }

    public static String toQRCodeString(User user) {
        // Serialize the user data as a JSON string (or any other format you prefer)
        JSONObject jsonObject = new JSONObject();
        String base64ImageString = user.image;
        byte[] decodedByteArray = Base64.decode(base64ImageString, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        int newWidth = 25; // Specify your desired width
        int newHeight = 25; // Specify your desired height
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(decodedBitmap, newWidth, newHeight, false);

// Compress the resized image to reduce file size
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // Adjust the compression quality as needed

// Encode the compressed image to base64
        byte[] compressedImageData = outputStream.toByteArray();
        String base64Image = Base64.encodeToString(compressedImageData, Base64.DEFAULT);

        try {
            jsonObject.put("name", user.name);
            jsonObject.put("image", base64Image);
            jsonObject.put("email", user.email);
            jsonObject.put("token", user.token);
            jsonObject.put("id", user.id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}