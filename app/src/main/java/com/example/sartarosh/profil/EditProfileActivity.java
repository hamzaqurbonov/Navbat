package com.example.sartarosh.profil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.sartarosh.R;
import com.example.sartarosh.SharedPreferencesUtil;
import com.example.sartarosh.TimeModel;
import com.example.sartarosh.customer.CustomerActivity;
import com.example.sartarosh.customer.CustomerBarberActivity;
import com.example.sartarosh.customer.CustomerModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<BarberProfile> activityList = new ArrayList<>();
    private EditText edit_name, edit_oblast, edit_region, edit_address, edit_phone, edit_phone2;
    //    boolean isCustomer;
    String customerId, barbesId, customer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
//        isCustomer = getIntent().getBooleanExtra("Customer", false);
        customerId = SharedPreferencesUtil.getString(this, "CustomerID", "");
        barbesId = SharedPreferencesUtil.getString(this, "BarbesID", "");
        customer = SharedPreferencesUtil.getString(this, "Customer", "");

        readDb();
        initViews();
        Log.d("SharedPrefs", customerId + " : " + barbesId + " : " + customer);
    }


    public void readDb() {


//        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = Objects.equals(customer, "Customer")
                ? db.collection("Customer").document(customerId)
                : db.collection("Barbers").document(barbesId);

            userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        BarberProfile profile = documentSnapshot.toObject(BarberProfile.class);

                        if (profile != null) {
                            edit_name.setText(profile.getName());
                            edit_oblast.setText(profile.getProvince());
                            edit_region.setText(profile.getRegion());
                            edit_address.setText(profile.getDestination());
                            edit_phone.setText(profile.getPhone());
                            edit_phone2.setText(profile.getBackupPhone());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("readDb", "Xatolik: " + e.getMessage());
                });
    }



    private void initViews() {
        edit_name = findViewById(R.id.edit_name);
        edit_oblast = findViewById(R.id.edit_oblast);
        edit_region = findViewById(R.id.edit_region);
        edit_address = findViewById(R.id.edit_address);
        edit_phone = findViewById(R.id.edit_phone);
        edit_phone2 = findViewById(R.id.edit_phone2);

        findViewById(R.id.save_btn).setOnClickListener(v -> editProfil());
    }

    private void editProfil() {
        String name = edit_name.getText().toString();
        String olast = edit_oblast.getText().toString();
        String region = edit_region.getText().toString();
        String adress = edit_address.getText().toString();
        String phone = edit_phone.getText().toString();
        String phone2 = edit_phone2.getText().toString();

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("province", olast);
        profile.put("region", region);
        profile.put("destination", adress);
        profile.put("phone", phone);
        profile.put("backupPhone", phone2);


        if (Objects.equals(customer, "Customer")) {
            Log.d("Profile1", "Saqlandi-1");
            FirebaseFirestore.getInstance().collection("Customer").document(customerId).set(profile).addOnSuccessListener(unused -> {
                setResult(Activity.RESULT_OK);
                finish();
            });
//            startActivity(new Intent(this, CustomerActivity.class));
        } else {
            Log.d("Profile1", "Saqlandi-2");
            FirebaseFirestore.getInstance().collection("Barbers").document(barbesId).set(profile).addOnSuccessListener(unused -> {
                setResult(Activity.RESULT_OK);
                finish();
            });
//            startActivity(new Intent(this, BarberActivity.class));
        }


    }
}