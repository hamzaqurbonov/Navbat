package com.bc.sartarosh;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.Insets;

import com.bc.sartarosh.customer.CustomerActivity;
import com.bc.sartarosh.profil.BarberActivity;
import com.bc.sartarosh.profil.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        SharedPreferencesUtil.clearAll(MainActivity.this);

        if (isCustomer) {
            SharedPreferencesUtil.saveString(this, "Customer", "Customer");
        }
        intent.putExtra("Customer", isCustomer);
        startActivity(intent);
        finish(); // login'dан кейин қайта келмаслиги учун
    }
}