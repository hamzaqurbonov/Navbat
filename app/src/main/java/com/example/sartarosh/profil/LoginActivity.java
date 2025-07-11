package com.example.sartarosh.profil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.sartarosh.SharedPreferencesUtil;
import com.example.sartarosh.customer.CustomerActivity;
import com.example.sartarosh.customer.CustomerBarberActivity;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class LoginActivity extends AppCompatActivity {
    private EditText edit_name, edit_oblast, edit_region, edit_address, edit_phone, edit_phone2;
    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    boolean isCustomer;

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

        isCustomer = getIntent().getBooleanExtra("Customer", false);



        // Ro'yxatdan o'tish
        edit_name = findViewById(R.id.edit_name);
        edit_oblast = findViewById(R.id.edit_oblast);
        edit_region  = findViewById(R.id.edit_region);
        edit_address  = findViewById(R.id.edit_address);
        edit_phone  = findViewById(R.id.edit_phone);
        edit_phone2  = findViewById(R.id.edit_phone2);
        findViewById(R.id.google_signin_btn).setOnClickListener(v -> signIn());

        if(isCustomer) {
            edit_oblast.setVisibility(View.GONE);
            edit_region.setVisibility(View.GONE);
            edit_address.setVisibility(View.GONE);
        }



        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // google-services.json'dan
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }



    private void register() {

        String uid = mAuth.getCurrentUser().getUid();

        String phone = edit_name.getText().toString();
        String password = edit_oblast.getText().toString();

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", phone);
        profile.put("phone", password);



        if(isCustomer) {
            FirebaseFirestore.getInstance().collection("Customer")
                    .document(uid)
                    .set(profile)
                    .addOnSuccessListener(unused -> {
                        Log.d("Profile", "Saqlandi");
                    });
        } else {
            FirebaseFirestore.getInstance().collection("Barbers")
                    .document(uid)
                    .set(profile)
                    .addOnSuccessListener(unused -> {
                        Log.d("Profile", "Saqlandi");
                    });
        }


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
                        register();

                        // CustomerActivity'га ўтиш
                        if(isCustomer) {
                            SharedPreferencesUtil.saveString(this, "CustomerMain", "CustomerMain");
                            SharedPreferencesUtil.saveString(this, "CustomerID", user.getUid());

                            startActivity(new Intent(LoginActivity.this, CustomerBarberActivity.class));

                        } else {


                            startActivity(new Intent(LoginActivity.this, BarberActivity.class));

                        }


                        finish();
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
