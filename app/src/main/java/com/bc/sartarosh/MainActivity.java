package com.bc.sartarosh;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.Insets;

import com.bc.sartarosh.customer.CustomerActivity;
import com.bc.sartarosh.profil.BarberActivity;
import com.bc.sartarosh.profil.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private static final int UPDATE_REQUEST_CODE = 123; // Янгиланиш учун request code

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences prefs;

    private AppUpdateManager appUpdateManager; // Yangilanish boshqaruvchisi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  AppUpdateManager ини инициализация қиламиз
        appUpdateManager = AppUpdateManagerFactory.create(this);
        checkForUpdate(); // 💡 Иловани очганда янгиланиш бор-йўқлигини текширади

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

    //  Google Sign-In конфигурацияси
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Ролни танлаш интерфейси
    private void showRoleSelection() {
        setContentView(R.layout.activity_main);
        findViewById(R.id.barber).setOnClickListener(v -> navigateToLogin(false));
        findViewById(R.id.customer).setOnClickListener(v -> navigateToLogin(true));
    }

    // Сессия асосида йўналтириш
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
            showRoleSelection();
        }
    }

    //  Логинга ўтиш
    private void navigateToLogin(boolean isCustomer) {
        Intent intent = new Intent(this, LoginActivity.class);
        SharedPreferencesUtil.clearAll(MainActivity.this);

        if (isCustomer) {
            SharedPreferencesUtil.saveString(this, "Customer", "Customer");
        }
        intent.putExtra("Customer", isCustomer);
        startActivity(intent);
        finish();
    }

    // ЯНГИЛАНИШНИ ТЕКШИРИШ (иммедиате update)
    private void checkForUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            UPDATE_REQUEST_CODE
                    );
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //  Янгиланиш жараёнини кузатиш
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
            }
        }
    }
}