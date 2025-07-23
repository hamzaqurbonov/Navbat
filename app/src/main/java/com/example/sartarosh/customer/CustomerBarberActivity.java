package com.example.sartarosh.customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sartarosh.MainActivity;
import com.example.sartarosh.R;
import com.example.sartarosh.SharedPreferencesUtil;
import com.example.sartarosh.TimeModel;
import com.example.sartarosh.profil.BarberActivity;
import com.example.sartarosh.profil.EditProfileActivity;
import com.example.sartarosh.profil.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerBarberActivity extends AppCompatActivity {
    RecyclerView recycler;
    CustomerBarberAdapter adapter;
    List<CustomerModel> activityList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_barber);

        recycler = findViewById(R.id.recycler);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ReadDb();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        MenuItem searchItem = menu.findItem(R.id.search_1);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("ID бўйича қидирув...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchByUserId(query.trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.trim().isEmpty()) {
                    activityList.clear();
                    ReadDb();
                }
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            activityList.clear();
            ReadDb();
            return false;
        });


        return true;
    }

    private void searchByUserId(String userId) {
        activityList.clear();

        db.collection("Barbers")
                .whereEqualTo("userID", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();

                    if (docs.isEmpty()) {
                        Toast.makeText(this, "Бундай ID топилмади", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = docs.get(0);
                    String name = doc.getString("name");
                    String phone = doc.getString("phone");
                    String barbesID = doc.getId();
                    String userID = doc.getString("userID");

                    activityList.add(new CustomerModel(name, phone, barbesID, userID));

                    updateRecycler();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void ReadDb() {
        db.collection("Barbers").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            String phone = doc.getString("phone");
                            String barbesID = doc.getId();
                            String userID = doc.getString("userID");

                            activityList.add(new CustomerModel(name, phone, barbesID, userID));
                        }

                        updateRecycler();
                    } else {
                        Log.e("ReadDb", "Error: ", task.getException());
                    }
                });
    }

    private void updateRecycler() {
        recycler.setLayoutManager(new GridLayoutManager(this, 1));

        adapter = new CustomerBarberAdapter(activityList, (view, position) -> {
            CustomerModel model = activityList.get(position);
            openCustomerActivity(model);
        });

        recycler.setAdapter(adapter);
    }

    private void openCustomerActivity(CustomerModel model) {
        SharedPreferencesUtil.saveString(this, "BarbesID", model.getBarbesId());
        SharedPreferencesUtil.saveString(this, "getName", model.getName());
        SharedPreferencesUtil.saveString(this, "getPhone", model.getPhone());
        SharedPreferencesUtil.saveString(this, "userID", model.getUserID());

        Intent intent = new Intent(this, CustomerActivity.class);
        startActivity(intent);
        finish();
    }

}
