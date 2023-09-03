package com.example.groupassignmentrun1.adapters;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignmentrun1.databinding.ItemContainerUserBinding;
import com.example.groupassignmentrun1.listeners.UserListener;
import com.example.groupassignmentrun1.models.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener=userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
        Bitmap qrCode = generateQRCodeFromUser(users.get(position),300,300);
        ImageView qrCodeImageView = holder.binding.qrCodeImageView; // Replace with the correct view reference
        if (qrCode != null) {
            // Set the generated QR code to the ImageView
            qrCodeImageView.setImageBitmap(qrCode);
        } else {
            // Handle error case
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        ItemContainerUserBinding binding;

        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v->userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap generateQRCodeFromUser(User user, int width, int height) {
        try {
            // Convert the User object to a string representation (customize this part based on your User class)
            String userData = user.toString();

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
}
