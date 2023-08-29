package com.example.groupassignmentrun1.firebase;



import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
    }
}
