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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sartarosh.customer.CustomerActivity;
import com.example.sartarosh.customer.CustomerBarberActivity;
import com.example.sartarosh.profil.BarberActivity;
import com.example.sartarosh.profil.BarberProfile;
import com.example.sartarosh.profil.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);

        // Google Sign-In конфигурацияси
        configureGoogleSignIn();

        if (isFirstLaunch) {
            showRoleSelection(); // Мижоз ёки сартарош танланади
            prefs.edit().putBoolean("isFirstLaunch", false).apply();
        } else {
            navigateBasedOnSession();
        }
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void showRoleSelection() {
        setContentView(R.layout.activity_main);
        findViewById(R.id.barber).setOnClickListener(v -> navigateToLogin(false));
        findViewById(R.id.customer).setOnClickListener(v -> navigateToLogin(true));
    }

    private void navigateBasedOnSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String role = SharedPreferencesUtil.getString(this, "CustomerMain", "");
            Intent intent;
            if ("CustomerMain".equals(role)) {
                intent = new Intent(this, CustomerActivity.class);
            } else {
                intent = new Intent(this, BarberActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            showRoleSelection(); // Агар логин бўлмаса — рол танлаш экрани
        }
    }

    private void navigateToLogin(boolean isCustomer) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("Customer", isCustomer);
        startActivity(intent);
        finish(); // login'dан кейин қайта келмаслиги учун
    }
}