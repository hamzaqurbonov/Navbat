package com.bc.sartarosh;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "Yangi token generatsiya qilindi: " + token);
        // Yangi tokenni serverga (Firestore'ga) yuborish
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Foydalanuvchi sartaroshmi yoki mijoz ekanligini bilish qiyin,
            // shuning uchun ikkala kolleksiyada ham yangilab ko'ramiz.
            // Yaxshiroq yechim - ro'yxatdan o'tganda rolni (masalan, "isBarber") SharedPreferences'ga saqlash.
            Map<String, Object> data = new HashMap<>();
            data.put("fcmToken", token);

            db.collection("Barbers").document(userId).update(data)
                    .addOnSuccessListener(v -> Log.d("FCM_TOKEN", "Sartarosh tokeni yangilandi."))
                    .addOnFailureListener(e -> Log.d("FCM_TOKEN", "Bu foydalanuvchi sartarosh emas."));

            db.collection("Customers").document(userId).update(data)
                    .addOnSuccessListener(v -> Log.d("FCM_TOKEN", "Mijoz tokeni yangilandi."))
                    .addOnFailureListener(e -> Log.d("FCM_TOKEN", "Bu foydalanuvchi mijoz emas."));
        }
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        // Agar xabar notification qismini o'z ichiga olsa
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Ilova ochiq bo'lganda bildirishnomani qo'lda yasab ko'rsatish
            NotificationHelper.showNotification(getApplicationContext(), title, body);
        }
    }


}