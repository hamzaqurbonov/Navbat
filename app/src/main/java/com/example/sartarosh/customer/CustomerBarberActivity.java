package com.example.sartarosh.customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sartarosh.R;
import com.example.sartarosh.SharedPreferencesUtil;
import com.example.sartarosh.TimeModel;
import com.example.sartarosh.profil.BarberActivity;
import com.example.sartarosh.profil.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
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
    private FirebaseAuth mAuth;
    String BarbesId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_barber);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        recycler = findViewById(R.id.recycler);
        mAuth = FirebaseAuth.getInstance();


        ReadDb();
    }

    public void ReadDb() {

        db.collection("Barbers").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CustomerActivity.TimeSlotView.TimeSlot> list = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String barbesID = doc.getId();

                            String name = doc.getString("name");
                            String phone = doc.getString("phone");


                            Log.d("demo28", "users doc " + barbesID);

                            activityList.add(new CustomerModel(name, phone, barbesID));

                            Log.d("demo1", "Added1: " + name);

                        }

                        recycler.setLayoutManager(new GridLayoutManager(this, 1));

                        adapter = new CustomerBarberAdapter(activityList, new CustomerBarberAdapter.RecyclerViewClickListner() {
                            @Override
                            public void onClick(View v, int position) {
                                String barbesid = activityList.get(position).getBarbesId();
                                String getName = activityList.get(position).getName();
                                String getPhone = activityList.get(position).getPhone();

                                Intent intent = new Intent(CustomerBarberActivity.this, CustomerActivity.class);
//
                                SharedPreferencesUtil.saveString(CustomerBarberActivity.this, "BarbesID", barbesid);
                                SharedPreferencesUtil.saveString(CustomerBarberActivity.this, "getName", getName);
                                SharedPreferencesUtil.saveString(CustomerBarberActivity.this, "getPhone", getPhone);


                                startActivity(intent);
                            }
                        });

                        recycler.setAdapter(adapter);

                    } else {
                        Log.e("ReadDb", "Error: ", task.getException());
                    }
                });
    }


}