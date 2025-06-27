package com.example.sartarosh.profil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sartarosh.MainActivity;
import com.example.sartarosh.R;
import com.example.sartarosh.customer.CustomerActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // google-services.json'dan
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.google_signin_btn).setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(this, "Kirish muvaffaqiyatli", Toast.LENGTH_SHORT).show();

                        // CustomerActivity'га ўтиш
                        startActivity(new Intent(LoginActivity.this, CustomerActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}




//public class LoginActivity extends AppCompatActivity {
//    EditText editPhone, editPassword;
//    Button btnRegister, btnLogin;
//    FirebaseAuth mAuth = FirebaseAuth.getInstance();
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_login);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//
//
//        editPhone = findViewById(R.id.edit_phone);
//        editPassword = findViewById(R.id.edit_password);
//        btnRegister = findViewById(R.id.btn_register);
//        btnLogin = findViewById(R.id.btn_login);
//
//        btnRegister.setOnClickListener(v -> {
//            String phone = editPhone.getText().toString();
//            String password = editPassword.getText().toString();
//            registerWithPhone(phone, password);
//        });
//
//        btnLogin.setOnClickListener(v -> {
//            String phone = editPhone.getText().toString();
//            String password = editPassword.getText().toString();
//            loginWithPhone(phone, password);
//        });
//
//
//
//    }
//
//
//    public void loginWithPhone(String phone, String password) {
//        String email = phone + "@barberapp.uz";
//
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d("demo7", "CustomerActivity 4");
//                        startActivity(new Intent(this, CustomerActivity.class));
//                        Toast.makeText(this, "Tizimga kirdingiz!", Toast.LENGTH_SHORT).show();
//                        // Профил ёки бош меню
//                    } else {
//                        Toast.makeText(this, "Login xatolik: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    public void registerWithPhone(String phone, String password) {
//        String email = phone + "@barberapp.uz"; // Телефонни "email" сифатида тайёрлаш
//
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d("demo7", "BarberActivity 5");
//                        startActivity(new Intent(this, BarberActivity.class));
//                        Toast.makeText(this, "Ro'yxatdan o'tdingiz!", Toast.LENGTH_SHORT).show();
//                        // Профил яратиш ёки Dashboard га ўтиш
//                    } else {
//                        Toast.makeText(this, "Xatolik:1 " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//}