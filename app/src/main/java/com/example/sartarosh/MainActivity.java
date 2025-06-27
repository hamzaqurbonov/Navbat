package com.example.sartarosh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.Insets;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sartarosh.customer.CustomerActivity;
import com.example.sartarosh.profil.BarberActivity;
import com.example.sartarosh.profil.BarberProfile;
import com.example.sartarosh.profil.LoginActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Дастур: вақт интервалларини Firestore'га ёзиб/ўқиб,
 *          TimeSlotView'да банд (CYAN) ва бўш (GREEN) вақтларни кўрсатади.
 *
 *  minSdkVersion ≥ 26 (java.time)
 */
public class MainActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d("demo7", "MainActivity 6");

//        FirebaseUser currentUser = mAuth.getCurrentUser();

//        if (currentUser == null) {
//            Log.d("demo7", "LoginActivity 1");
//            // Логин керак
//            startActivity(new Intent(this, LoginActivity.class));
//
//            finish();
//        } else {
//            String uid = currentUser.getUid();
//            // Автоматик сессия бор. Қайси профил эканини текширамиз:
//            db.collection("Barbers").document(uid).get().addOnSuccessListener(doc -> {
//                if (doc.exists()) {
//                    Log.d("demo7", "BarberActivity 2");
//                    // Сартарош экан
//                    startActivity(new Intent(this, BarberActivity.class));
//                } else {
//                    Log.d("demo7", "CustomerActivity 3");
//                    // Мижоз экан
//                    startActivity(new Intent(this, CustomerActivity.class));
//                }
//                finish();
//            }).addOnFailureListener(e -> {
//                Toast.makeText(this, "Маълумот юкланмади", Toast.LENGTH_SHORT).show();
//            });
//        }


        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            // Иловани биринчи бор очиляпти - LoginActivity очилади
            startActivity(new Intent(this, LoginActivity.class));

            // Бошқа сафарда LoginActivity кўрсатилмаслиги учун белги қўямиз
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.apply();
        } else {
            // Илова аввал очилган — сессияга қараб навигация
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(this, CustomerActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        }

        finish();
    }



}