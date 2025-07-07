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

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    FirebaseFirestore db;
    boolean isFirstLaunch;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            // Иловани биринчи бор очиляпти
            setContentView(R.layout.activity_main);
            findViewById(R.id.barber).setOnClickListener(v -> Barber());
            findViewById(R.id.customer).setOnClickListener(v -> Customer());
            // Бошқа сафарда LoginActivity кўрсатилмаслиги учун белги қўямиз
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.apply();
        } else {
            // Илова аввал очилган — сессияга қараб навигация
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                Log.d("TAG1", "onCreate: ");
                startActivity(new Intent(this, BarberActivity.class));
            } else {
                setContentView(R.layout.activity_main);
                findViewById(R.id.barber).setOnClickListener(v -> Barber());
                findViewById(R.id.customer).setOnClickListener(v -> Customer());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirstLaunch", false);
                editor.apply();
//                startActivity(new Intent(this, LoginActivity.class));
            }
        }

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // google-services.json'dan
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }



    private void Customer() {
//        signIn();
//        startActivity(new Intent(this, LoginActivity.class));
        Log.d("TAG1", "onCreate: Customer");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("Customer", true);
        startActivity(intent);

//        finish();
    }

    private void Barber() {
//        signIn();
//        startActivity(new Intent(this, LoginActivity.class));
        Log.d("TAG1", "onCreate: Barber");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("Customer", false);
        startActivity(intent);
//        finish();
    }


//    public void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account.getIdToken());
//            } catch (ApiException e) {
//                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Toast.makeText(this, "Kirish muvaffaqiyatli", Toast.LENGTH_SHORT).show();
//
//                        // CustomerActivity'га ўтиш
//                        startActivity(new Intent(this, BarberActivity.class));
//                        finish();
//                    } else {
//                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


}