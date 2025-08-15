package com.bc.sartarosh.profil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bc.sartarosh.PhoneFormatter;
import com.bc.sartarosh.R;
import com.bc.sartarosh.SharedPreferencesUtil;
import com.bc.sartarosh.SpinnerAdapter;
import com.bc.sartarosh.customer.CustomerBarberActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private EditText edit_name, edit_oblast, edit_region, edit_address, edit_phone, edit_phone2;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int RC_SIGN_IN = 1001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    boolean isCustomer;
    Map<String, Object> profile = new HashMap<>();
    Spinner spinner_oblast, spinner_region;

    String DocName, NameSubDoc;
    List<String> oblastList = new ArrayList<>();
    List<String> regionList = new ArrayList<>();

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

        spinner_oblast = findViewById(R.id.spinner_oblast);
        spinner_region = findViewById(R.id.spinner_region);
        // Ro'yxatdan o'tish
        edit_name = findViewById(R.id.edit_name);
        edit_address = findViewById(R.id.edit_address);
        edit_phone = findViewById(R.id.edit_phone);
        edit_phone2 = findViewById(R.id.edit_phone2);
        findViewById(R.id.google_signin_btn).setOnClickListener(v -> signIn());

        if (isCustomer) {
            spinner_oblast.setVisibility(View.GONE);
            spinner_region.setVisibility(View.GONE);
            edit_address.setVisibility(View.GONE);
        } else {
            Collection();
        }
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)) // google-services.json'dan
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        PhoneFormatter.attachTo(edit_phone);
        PhoneFormatter.attachTo(edit_phone2);

    }

    //-------------------------Бошланиш-----------------------------------
    private void Collection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Вилоят -> Туманлар мапи
        Map<String, List<String>> regionMap = new HashMap<>();

        db.collection("Region")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String regionName = document.getId(); // Масалан: "Тошкент"
                            oblastList.add(regionName);

                            List<String> tumans = (List<String>) document.get("regions");

                            if (tumans != null) {
                                regionMap.put(regionName, tumans);
                            } else {
                                regionMap.put(regionName, new ArrayList<>()); // Агар туманлар бўлмаса
                            }


                            Log.d("Firestore1", "Маълумотлар: " + regionMap.toString() + " " + regionName);
                        }

                        setupDocSpinner();

//                        spinner_oblast.setAdapter(new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>(regionMap.keySet())));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Маълумотларни олишда хатолик", e);
                    }
                });

    }

    private void setupDocSpinner() {
        SpinnerAdapter adapterDoc = new SpinnerAdapter(LoginActivity.this, oblastList, R.layout.spinner_region);
        spinner_oblast.setAdapter(adapterDoc);
        spinner_oblast.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DocName = parentView.getItemAtPosition(position).toString();
                loadSubDocuments(DocName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void loadSubDocuments(String docName) {

        db.collection("Region")
                .document(docName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            regionList.clear();
                            List<String> tumans = (List<String>) documentSnapshot.get("regions");
                            if (tumans != null) {
                                regionList.addAll(tumans);
                                // Шу ерда tumanList тайёр
//                                Log.d("Firestore", "Туманлар: " + tumanList);
                            } else {
                                Log.d("Firestore", "Туманлар топилмади.");
                            }
                        } else {
                            Log.d("Firestore", "Ҳужжат мавжуд эмас.");
                        }
                        setupSubDocSpinner();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore", "Хатолик юз берди", e);
                    }
                });
    }

    private void setupSubDocSpinner() {

        SpinnerAdapter adapter = new SpinnerAdapter(LoginActivity.this, regionList, R.layout.spinner_region);
        spinner_region.setAdapter(adapter);
        spinner_region.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                NameSubDoc = parentView.getItemAtPosition(position).toString();
//                Toast.makeText(MainActivity.this, "Tanlangan: " + NameSubDoc, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }


    //--------------------------тугаш-----------------------------



    private void register() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String name = edit_name.getText().toString();
        String province = DocName;
        String region = NameSubDoc;
        String address = edit_address.getText().toString();
        String phone1 = edit_phone.getText().toString();
        String phone2 = edit_phone2.getText().toString();

        DocumentReference userRef = isCustomer ? db.collection("Customer").document(uid) : db.collection("Barbers").document(uid);

        // TOKEN оламиз
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tokenTask -> {
            if (!tokenTask.isSuccessful()) {
                Log.e("FCM", "Токен олишда хатолик", tokenTask.getException());
                return;
            }
            String token = tokenTask.getResult();

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                        DocumentSnapshot snapshot = transaction.get(userRef);

                        long newId;
                        if (snapshot.exists() && snapshot.contains("userID")) {
                            // Мавжуд user, ID ўзгармайди
                            newId = Long.parseLong(snapshot.getString("userID"));






                        } else {
                            // Янги user
                            DocumentReference counterRef = db.collection("UserID").document("users_counter");
                            DocumentSnapshot counterSnap = transaction.get(counterRef);
                            long lastId = counterSnap.contains("last_id") ? counterSnap.getLong("last_id") : 0;
                            newId = lastId + 1;
                            transaction.update(counterRef, "last_id", newId);
                        }

                        profile.put("name", name);
                        profile.put("phone1", phone1);
                        profile.put("phone2", phone2);
                        profile.put("fcmToken", token);
                        profile.put("userID", String.format("%04d", newId));
                        SharedPreferencesUtil.saveString(this, "customerUserID", String.format("%04d", newId));
                        Log.d("TAG4", "LoginAct: " +  String.format("%04d", newId));
                        if (!isCustomer) {
                            profile.put("province", province);
                            profile.put("region", region);
                            profile.put("address", address);
                        }

                        transaction.set(userRef, profile, SetOptions.merge());
                        return null;
                    }).addOnSuccessListener(unused -> {
                        Log.d("REGISTER", "Маълумотлар сақланди");

                        // CustomerActivity'га ўтиш
                        if (isCustomer) {
                            SharedPreferencesUtil.saveString(this, "CustomerMain", "CustomerMain");
                            SharedPreferencesUtil.saveString(this, "CustomerID", user.getUid());

                            startActivity(new Intent(LoginActivity.this, CustomerBarberActivity.class));

                        } else {
                            SharedPreferencesUtil.saveString(this, "BarbesID", user.getUid());
                            startActivity(new Intent(LoginActivity.this, BarberActivity.class));
                        }

                        finish();
                    })
                    .addOnFailureListener(e -> Log.e("REGISTER", "Transaction хатоси", e));
        });
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
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();




                register();

                Toast.makeText(this, "Kirish muvaffaqiyatli", Toast.LENGTH_SHORT).show();
//                // CustomerActivity'га ўтиш
//                if (isCustomer) {
//                    SharedPreferencesUtil.saveString(this, "CustomerMain", "CustomerMain");
//                    SharedPreferencesUtil.saveString(this, "CustomerID", user.getUid());
//
//                    startActivity(new Intent(LoginActivity.this, CustomerBarberActivity.class));
//
//                } else {
//                    SharedPreferencesUtil.saveString(this, "BarbesID", user.getUid());
//                    startActivity(new Intent(LoginActivity.this, BarberActivity.class));
//                }



            } else {
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
