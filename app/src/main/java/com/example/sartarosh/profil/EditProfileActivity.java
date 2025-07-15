package com.example.sartarosh.profil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sartarosh.R;
import com.example.sartarosh.SharedPreferencesUtil;
import com.example.sartarosh.customer.CustomerActivity;
import com.example.sartarosh.customer.CustomerBarberActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
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
        initViews();
        Log.d("SharedPrefs", customerId + " : " + barbesId + " : " + customer);
    }

    private void initViews() {
        edit_name = findViewById(R.id.edit_name);
        edit_oblast = findViewById(R.id.edit_oblast);
        edit_region  = findViewById(R.id.edit_region);
        edit_address  = findViewById(R.id.edit_address);
        edit_phone  = findViewById(R.id.edit_phone);
        edit_phone2  = findViewById(R.id.edit_phone2);

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



        if(Objects.equals(customer, "Customer")) {
            Log.d("Profile1", "Saqlandi-1");
            FirebaseFirestore.getInstance().collection("Customer")
                    .document(customerId)
                    .set(profile)
                    .addOnSuccessListener(unused -> {
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
//            startActivity(new Intent(this, CustomerActivity.class));
        } else {
            Log.d("Profile1", "Saqlandi-2");
            FirebaseFirestore.getInstance().collection("Barbers")
                    .document(barbesId)
                    .set(profile)
                    .addOnSuccessListener(unused -> {
                        setResult(Activity.RESULT_OK);
                        finish();
                    });
//            startActivity(new Intent(this, BarberActivity.class));
        }



    }
}