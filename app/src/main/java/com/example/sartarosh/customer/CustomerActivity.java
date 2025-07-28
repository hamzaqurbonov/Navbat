package com.example.sartarosh.customer;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;


import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sartarosh.LocalDateTime;
import com.example.sartarosh.MainActivity;
import com.example.sartarosh.R;
import com.example.sartarosh.SharedPreferencesUtil;
import com.example.sartarosh.SpinnerAdapter;
import com.example.sartarosh.TimeModel;
import com.example.sartarosh.profil.BarberProfile;
import com.example.sartarosh.profil.EditProfileActivity;
import com.example.sartarosh.profil.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerActivity extends AppCompatActivity {

    private EditText editHour, editMinute;
    private Button addHourBtn;
    private ImageView backBtn, logoutBtn;
    private TimeSlotView timeSlotView;
    private RecyclerView recycler;
    private CustomerAdapter adapter;
    private final List<TimeModel> activityList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    Toolbar toolbar;
    String  data, dd, mm, yy, min, hours;
    private String barbershopId, customerId, customerName, customerPhone, barberUserID;
    TextView barbes_date_text, date_text, user_id;
    Spinner spinner_min, spinner_hours;
    String getName, getPhone1, getPhone2, userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer);

        // Ma'lumotlarni SharedPreferences'dan olish
        barbershopId = SharedPreferencesUtil.getString(this, "BarbesID", "");
        customerId = SharedPreferencesUtil.getString(this, "CustomerID", "");
        customerName = SharedPreferencesUtil.getString(this, "getName", "");
        customerPhone = SharedPreferencesUtil.getString(this, "getPhone", "");
        barberUserID = SharedPreferencesUtil.getString(this, "userID", "");

        initViews();
//        initListeners();

        mAuth = FirebaseAuth.getInstance();

        FrameLayout container = findViewById(R.id.schedule_container_barber);
        timeSlotView = new TimeSlotView(this);
        container.addView(timeSlotView, new FrameLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().density * 80),
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        dd = LocalDateTime.dateDD();
        mm = LocalDateTime.dateMM();
        yy = LocalDateTime.dateYYYY();
        data = LocalDateTime.dateDDMMYY();

        Log.d("demo56", "version: " +  dd + " - " + mm + " - " + yy + " - " + data );

        ReadDb();
        initListeners();
        findViewById(R.id.time_input).setOnClickListener(v -> showMaterialTimeBottomSheet() );
        customerReadDb();

    }


    private void customerReadDb() {
//        String uid = isCustomer ? customerId : barbesId;
//        String collection = isCustomer ? "Customer" : "Barbers";

        db.collection("Customer").document(customerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        BarberProfile profile = snapshot.toObject(BarberProfile.class);
                        if (profile != null) {
                            getName = profile.getName();
                            getPhone1 = profile.getPhone1();
                            getPhone2 = profile.getPhone2();
                            userID = profile.getUserID();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("readDb", "Xatolik: " + e.getMessage()));
    }






    private void showMaterialTimeBottomSheet() {
        // BottomSheetDialog яратиш
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_time_picker, null, false);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Spinner'ни view орқали оламиз (ЭЪТИБОР БЕРИН!)
        spinner_min = bottomSheetView.findViewById(R.id.spinner_min);
        spinner_hours = bottomSheetView.findViewById(R.id.spinner_hours);


        if (spinner_hours != null) {

            String[] spinner_hours_list = {"08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
            SpinnerAdapter adapter_hours = new SpinnerAdapter(this, Arrays.asList(spinner_hours_list), R.layout.spinner);
            spinner_hours.setAdapter(adapter_hours);
            spinner_hours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String selectedOption = parentView.getItemAtPosition(position).toString();
                    hours = selectedOption;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

        }
        if (spinner_min != null) {

            // SpinnerAdapter тайинлаш
            String[] spinner_young_list = {"00", "10", "20", "30", "40", "50"};
            SpinnerAdapter adapter_young = new SpinnerAdapter(this, Arrays.asList(spinner_young_list), R.layout.spinner);
            spinner_min.setAdapter(adapter_young);
            spinner_min.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String selectedOption = parentView.getItemAtPosition(position).toString();
                    min = selectedOption;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
        } else {
//            Log.w("ParseError", "Slot parsing failed: " + slot);
        }

        // OK тугма
        Button okButton = bottomSheetView.findViewById(R.id.btn_ok);
        if (okButton != null) {


            okButton.setOnClickListener(v -> {
                WriteDb();
                activityList.clear();
                ReadDb();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }




    private void initViews() {
//        time_input_text = findViewById(R.id.time_input_text);
        barbes_date_text = findViewById(R.id.barbes_date_text);
        date_text = findViewById(R.id.date_text);
//        editHour = findViewById(R.id.edit_hour_id);
//        editMinute = findViewById(R.id.edit_minut_id);
//        addHourBtn = findViewById(R.id.add_hour_id);
        backBtn = findViewById(R.id.back_Button);
        user_id = findViewById(R.id.user_id);
        recycler = findViewById(R.id.recycler);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barbes_date_text.setText(customerName );
        date_text.setText("Bugun " + LocalDateTime.dateDDMMYY());
        user_id.setText("ID " + barberUserID);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profil, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {

            startActivity(new Intent(CustomerActivity.this, EditProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()).signOut();

            SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
            editor.clear().apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initListeners() {
//        addHourBtn.setOnClickListener(v -> {
//            WriteDb();
//            Log.d("demo20", "Slot parsing failed: " + hours + " " + min );
//            activityList.clear();
//            ReadDb();
//        });

        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerBarberActivity.class));
            finish();
        });

    }
//    String hourStr = "spinner_min.toString().trim()";
//    String minuteStr = "spinner_hours.toString().trim()";
    private void WriteDb() {
        String hourStr = hours.toString();
        String minuteStr = min.toString();

        if (hourStr.isEmpty() || minuteStr.isEmpty()) return;

        int hour = Integer.parseInt(hourStr);
        int startMinute = Integer.parseInt(minuteStr);

        if (startMinute >= 60) {
            Toast.makeText(this, "Daqiqa noto‘g‘ri kiritilgan", Toast.LENGTH_SHORT).show();
            return;
        }

        int endMinute = startMinute + 40;
        int endHour = hour + (endMinute >= 60 ? 1 : 0);
        endMinute = endMinute % 60;

        String slot = String.format("%02d:%02d-%02d:%02d", hour, startMinute, endHour, endMinute);

        Map<String, Object> item = new HashMap<>();
        item.put("slot", slot);
        item.put("barberUserID", barberUserID);
        item.put("customerUserID", userID);
        item.put("name", getName);
        item.put("phone1", getPhone1);
        item.put("phone2", getPhone2);
        item.put("data", data);
        item.put("day-time", dd);

        db.collection("Barbers").document(barbershopId).collection("Customer1")
                .add(item)
                .addOnSuccessListener(doc -> Log.d("TAG", "Added: " + doc.getId()))
                .addOnFailureListener(e -> Log.w("TAG", "Error adding", e));

//        editHour.setText("");
//        editMinute.setText("");
    }

    public void ReadDb() {

        if (!barbershopId.isEmpty()) {
            db.collection("Barbers").document(barbershopId).collection("Customer1")
                    .whereGreaterThanOrEqualTo("day-time", dd)
                    .whereLessThanOrEqualTo("day-time", dd + "\uf8ff")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<TimeSlotView.TimeSlot> timeSlots = new ArrayList<>();
                            activityList.clear();

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String slot = doc.getString("slot");
                                if (slot == null || !slot.contains("-")) continue;

                                activityList.add(new TimeModel(barbershopId, customerId, doc.getId(), slot));

                                try {
                                    String[] parts = slot.split("-");
                                    LocalTime start = LocalTime.parse(parts[0].trim());
                                    LocalTime end = LocalTime.parse(parts[1].trim());
                                    timeSlots.add(new TimeSlotView.TimeSlot(start, end));
                                } catch (Exception e) {
                                    Log.w("ParseError", "Slot parsing failed: " + slot);
                                }
                            }

                            timeSlotView.setBusySlots(timeSlots);
                            recycler.setLayoutManager(new GridLayoutManager(this, 1));
                            adapter = new CustomerAdapter(this, activityList);
                            recycler.setAdapter(adapter);
                        } else {
                            Log.e("Firestore", "Failed to read slots", task.getException());
                        }
                    });
        }
        else {
            Log.e("ReadDb", "Barber ID is empty");
        }
    }

    // Ichki klass — vaqtни кўрсатувчи view
    public static class TimeSlotView extends View {
        private List<TimeSlot> busy = new ArrayList<>();
        private final int startHour = 8, endHour = 20;
        private final int totalMin = (endHour - startHour) * 60;
        private final Paint fill = new Paint();
        private final Paint label = new Paint();

        public TimeSlotView(Context c) {
            this(c, null);
        }

        public TimeSlotView(Context c, AttributeSet attrs) {
            this(c, attrs, 0);
        }

        public TimeSlotView(Context c, AttributeSet attrs, int defStyle) {
            super(c, attrs, defStyle);
            label.setColor(Color.BLACK);
            label.setTextSize(26f);
        }

        public void setBusySlots(List<TimeSlot> list) {
            busy = list;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float slotHeight = (float) getHeight() / totalMin;

            for (int i = 0; i < totalMin; i++) {
                LocalTime now = LocalTime.of(startHour, 0).plusMinutes(i);
                boolean isBusy = busy.stream().anyMatch(t -> !now.isBefore(t.start) && now.isBefore(t.end));
                fill.setColor(isBusy ? Color.CYAN : Color.GREEN);
                float top = i * slotHeight;
                canvas.drawRect(0, top, getWidth(), top + slotHeight, fill);
            }

            for (int h = startHour; h <= endHour; h++) {
                float y = (h - startHour) * 60 * slotHeight;
                canvas.drawLine(0, y, getWidth(), y, label);
                canvas.drawText(String.format("%02d:00", h), 10, y + 24, label);
            }
        }

        public static class TimeSlot {
            LocalTime start, end;

            public TimeSlot(LocalTime s, LocalTime e) {
                start = s;
                end = e;
            }
        }
    }

}
