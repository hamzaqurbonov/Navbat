package com.example.sartarosh.customer;

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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sartarosh.MainActivity;
import com.example.sartarosh.R;
import com.example.sartarosh.SharedPreferencesUtil;
import com.example.sartarosh.TimeModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private String barbershopId, customerId, customerName, customerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer);

        // Ma'lumotlarni SharedPreferences'dan olish
        barbershopId = SharedPreferencesUtil.getString(this, "BarbesID", "");
        customerId = SharedPreferencesUtil.getString(this, "CustomerID", "");
        customerName = SharedPreferencesUtil.getString(this, "getName", "");
        customerPhone = SharedPreferencesUtil.getString(this, "getPhone", "");

        initViews();
        initListeners();

        mAuth = FirebaseAuth.getInstance();

        FrameLayout container = findViewById(R.id.schedule_container_barber);
        timeSlotView = new TimeSlotView(this);
        container.addView(timeSlotView, new FrameLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().density * 80),
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        ReadDb();
    }

    private void initViews() {
        editHour = findViewById(R.id.edit_hour_id);
        editMinute = findViewById(R.id.edit_minut_id);
        addHourBtn = findViewById(R.id.add_hour_id);
        backBtn = findViewById(R.id.back_Button);
        logoutBtn = findViewById(R.id.custom_user_logput);
        recycler = findViewById(R.id.recycler);
    }

    private void initListeners() {
        addHourBtn.setOnClickListener(v -> {
            WriteDb();
            activityList.clear();
            ReadDb();
        });

        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerBarberActivity.class));
            finish();
        });

        logoutBtn.setOnClickListener(v -> {
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
        });
    }

    private void WriteDb() {
        String hourStr = editHour.getText().toString().trim();
        String minuteStr = editMinute.getText().toString().trim();

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
        item.put("name", customerName);
        item.put("phone", customerPhone);

        db.collection("Barbers").document(barbershopId).collection("Customer")
                .document(customerId)
                .set(item)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Slot saved"))
                .addOnFailureListener(e -> Log.w("Firestore", "Failed to save slot", e));

        editHour.setText("");
        editMinute.setText("");
    }

    public void ReadDb() {
        db.collection("Barbers").document(barbershopId).collection("Customer").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<TimeSlotView.TimeSlot> timeSlots = new ArrayList<>();
                        activityList.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String slot = doc.getString("slot");
                            if (slot == null || !slot.contains("-")) continue;

                            activityList.add(new TimeModel(doc.getId(), slot));

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

    // Ichki klass — vaqtни кўрсатувчи view
    public static class TimeSlotView extends View {
        private List<TimeSlot> busy = new ArrayList<>();
        private final int startHour = 8, endHour = 20;
        private final int totalMin = (endHour - startHour) * 60;
        private final Paint fill = new Paint();
        private final Paint label = new Paint();

        public TimeSlotView(Context c) { this(c, null); }
        public TimeSlotView(Context c, AttributeSet attrs) { this(c, attrs, 0); }
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
            public TimeSlot(LocalTime s, LocalTime e) { start = s; end = e; }
        }
    }
}
