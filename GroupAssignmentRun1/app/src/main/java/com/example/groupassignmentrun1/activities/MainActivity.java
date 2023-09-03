package com.example.groupassignmentrun1.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.groupassignmentrun1.R;
import com.example.groupassignmentrun1.adapters.RecentConversationsAdapter;
import com.example.groupassignmentrun1.databinding.ActivityMainBinding;
import com.example.groupassignmentrun1.listeners.ConversionListener;
import com.example.groupassignmentrun1.models.ChatMessage;
import com.example.groupassignmentrun1.models.User;
import com.example.groupassignmentrun1.utilities.Constants;
import com.example.groupassignmentrun1.utilities.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends BaseActivity implements ConversionListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;
    private int PERMISSION_CODE = 1;
    private String cityName;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Handler weatherUpdateHandler = new Handler();
    private static final long WEATHER_UPDATE_INTERVAL = 30 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permissions are granted; request the current location
            requestLocation();
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init() {
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations,this);
        binding.conversationRecyclerView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.imageSignOut.setOnClickListener(v -> signOut());
        binding.fabNewChat.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
        binding.scanQR.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), QRCodeScannerActivity.class)));
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                    }else {
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }else if(documentChange.getType()==DocumentChange.Type.MODIFIED){
                    for(int i=0;i< conversations.size();i++) {
                        String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1,obj2)->obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility((View.GONE));

        }
    };

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)

                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

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

    //System will run this 1st
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted; request the current location
                requestLocation();
            } else {
                // Permission denied; handle the case where the user denies access to location.
                Log.d("Error","Location permission denied");
            }
        }
    }

    //System run this 2nd
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            GetCityNameTask cityNameTask = new GetCityNameTask();
                            cityNameTask.execute(latitude, longitude);
                        } else {
                            Log.d("Error","Location not available");
                        }
                    }
                });
    }

    //System run this 3rd
    private class GetCityNameTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];
            String cityName = "Not found";

            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
                for (Address adr : addresses) {
                    if (adr != null) {
                        String city = adr.getLocality();
                        if (city != null && !city.equals("")) {
                            cityName = city;
                            break; // Exit the loop once a city is found
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return cityName;
        }

        @Override
        protected void onPostExecute(String city) {
            // Handle the result, e.g., update UI elements with the city name.
            getWeatherInfo(city);
            startWeatherUpdates();
        }
    }

    //System run this 4th
    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=5443830c250942e0a3875435233108&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    String locationName = response.getJSONObject("location").getString("name");
                    binding.weatherLocation.setText(locationName);
                    binding.weatherTemperature.setText(temperature + " °C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    binding.weatherCondition.setText(condition);
//                    binding.condition1.setText(condition);
                    switch (condition) {
                        case "Partly cloudy":
                        case "Cloudy":
                            binding.frameLayout.setBackgroundResource(R.drawable.cloudy);
                            break;
                        case "Rain":
                            binding.frameLayout.setBackgroundResource(R.drawable.rain);
                            break;
                        case "Sunny":
                            binding.frameLayout.setBackgroundResource(R.drawable.sunny);
                            break;
                        case "Thunderstorm":
                            binding.frameLayout.setBackgroundResource(R.drawable.thunder);
                        default:
                            break;
                    }
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
//                    Picasso.get().load("http:".concat(conditionIcon)).into(binding.weatherIcon);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter valid city name",Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private Runnable weatherUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (cityName != null && !cityName.equals("Not found")) {
                // Fetch weather data based on the last retrieved city name
                getWeatherInfo(cityName);
            }

            // Schedule the next update
            weatherUpdateHandler.postDelayed(this, WEATHER_UPDATE_INTERVAL);
        }
    };

    // Call this function to start periodic updates
    private void startWeatherUpdates() {
        weatherUpdateHandler.post(weatherUpdateRunnable);
    }

    // Call this function to stop periodic updates (e.g., when your activity is paused)
    private void stopWeatherUpdates() {
        weatherUpdateHandler.removeCallbacks(weatherUpdateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop periodic updates when the activity is paused
        stopWeatherUpdates();
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent (getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}