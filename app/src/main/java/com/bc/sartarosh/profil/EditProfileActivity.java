package com.bc.sartarosh.profil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.bc.sartarosh.PhoneFormatter;
import com.bc.sartarosh.R;
import com.bc.sartarosh.SharedPreferencesUtil;
import com.bc.sartarosh.SpinnerAdapter;
import com.bc.sartarosh.customer.CustomerActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText edit_name, edit_address, edit_phone, edit_phone2;
    private Spinner spinner_oblast, spinner_region;

    private String customerId, barbesId, docType, userID, fcmToken;
    private boolean isCustomer;

    private String selectedOblast, selectedRegion;
    private final List<String> oblastList = new ArrayList<>();
    private final List<String> regionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        customerId = SharedPreferencesUtil.getString(this, "CustomerID", "");
        barbesId = SharedPreferencesUtil.getString(this, "BarbesID", "");
        docType = SharedPreferencesUtil.getString(this, "Customer", ""); // "Customer" ёки ""

        isCustomer = "Customer".equals(docType);

        initViews();
        readDb();
        setupRegionCollection();
        PhoneFormatter.attachTo(edit_phone);
        PhoneFormatter.attachTo(edit_phone2);
        if (isCustomer) {
            spinner_oblast.setVisibility(View.GONE);
            spinner_region.setVisibility(View.GONE);
            edit_address.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        edit_name = findViewById(R.id.edit_name);
        edit_address = findViewById(R.id.edit_address);
        edit_phone = findViewById(R.id.edit_phone);
        edit_phone2 = findViewById(R.id.edit_phone2);
        spinner_oblast = findViewById(R.id.spinner_oblast);
        spinner_region = findViewById(R.id.spinner_region);
        findViewById(R.id.save_btn).setOnClickListener(v -> saveProfile());
    }

    private void readDb() {
        String uid = isCustomer ? customerId : barbesId;
        String collection = isCustomer ? "Customer" : "Barbers";

        db.collection(collection).document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        BarberProfile profile = snapshot.toObject(BarberProfile.class);
                        if (profile != null) {
                            edit_name.setText(profile.getName());
                            edit_address.setText(profile.getAddress());
                            edit_phone.setText(profile.getPhone1());
                            edit_phone2.setText(profile.getPhone2());
                            userID = (profile.getUserID());
                            fcmToken = (profile.getFcmToken());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("readDb", "Xatolik: " + e.getMessage()));
    }

    private void saveProfile() {
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", edit_name.getText().toString());
        profile.put("phone1", edit_phone.getText().toString());
        profile.put("phone2", edit_phone2.getText().toString());
        profile.put("userID", userID);
        profile.put("fcmToken", fcmToken);

        if (!isCustomer) {
            profile.put("province", selectedOblast);
            profile.put("region", selectedRegion);
            profile.put("address", edit_address.getText().toString());
        }

        String uid = isCustomer ? customerId : barbesId;
        String collection = isCustomer ? "Customer" : "Barbers";

        db.collection(collection).document(uid)
                .set(profile)
                .addOnSuccessListener(unused -> {
                    setResult(Activity.RESULT_OK);
                    Intent intent;
                    if (isCustomer) {
                        intent = new Intent(this, CustomerActivity.class);
                    } else {
                        intent = new Intent(this, BarberActivity.class);
                    }
                    startActivity(intent);
                    finish();
                });
    }

    private void setupRegionCollection() {
        db.collection("Region").get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        String regionName = doc.getId();
                        oblastList.add(regionName);
                    }
                    setupOblastSpinner();
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Маълумотларни олишда хатолик", e));
    }

    private void setupOblastSpinner() {
        SpinnerAdapter adapter = new SpinnerAdapter(this, oblastList, R.layout.spinner_region);
        spinner_oblast.setAdapter(adapter);
        spinner_oblast.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOblast = oblastList.get(position);
                loadRegionsFor(selectedOblast);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadRegionsFor(String oblast) {
        db.collection("Region").document(oblast)
                .get()
                .addOnSuccessListener(doc -> {
                    regionList.clear();
                    List<String> regions = (List<String>) doc.get("regions");
                    if (regions != null) regionList.addAll(regions);
                    setupRegionSpinner();
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Хатолик юз берди", e));
    }

    private void setupRegionSpinner() {
        SpinnerAdapter adapter = new SpinnerAdapter(this, regionList, R.layout.spinner_region);
        spinner_region.setAdapter(adapter);
        spinner_region.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRegion = regionList.get(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}

