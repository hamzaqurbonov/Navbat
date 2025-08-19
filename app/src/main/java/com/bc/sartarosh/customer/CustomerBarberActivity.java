package com.bc.sartarosh.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bc.sartarosh.R;
import com.bc.sartarosh.SharedPreferencesUtil;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CustomerBarberActivity extends AppCompatActivity {
    RecyclerView recycler;
    CustomerBarberAdapter adapter;
    List<CustomerBarbesModel> activityList = new ArrayList<>();
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

                    activityList.add(new CustomerBarbesModel(name, phone, barbesID, userID));

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
                            String phone = doc.getString("phone1");
                            String barbesID = doc.getId();
                            String userID = doc.getString("userID");

                            activityList.add(new CustomerBarbesModel(name, phone, barbesID, userID));
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
            CustomerBarbesModel model = activityList.get(position);
            openCustomerActivity(model);
        });

        recycler.setAdapter(adapter);
    }

    private void openCustomerActivity(CustomerBarbesModel model) {
        SharedPreferencesUtil.saveString(this, "BarbesID", model.getBarbesId());
        SharedPreferencesUtil.saveString(this, "getName", model.getName());
        SharedPreferencesUtil.saveString(this, "getPhone", model.getPhone());
        SharedPreferencesUtil.saveString(this, "userID", model.getUserID());

        Intent intent = new Intent(this, CustomerActivity.class);
        startActivity(intent);
        finish();
    }

}
