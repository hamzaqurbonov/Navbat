package com.bc.sartarosh.profil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bc.sartarosh.LocalDateTime;
import com.bc.sartarosh.MainActivity;
import com.bc.sartarosh.R;
import com.bc.sartarosh.SharedPreferencesUtil;
import com.bc.sartarosh.SpinnerAdapter;
import com.bc.sartarosh.TimeModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarberActivity extends AppCompatActivity {

    private EditText editHour, editMinute;
    private Button addHourButton;
    private TimeSlotView timeSlotView;
    private RecyclerView recyclerView;
    private BarberAdapter adapter;
    private final List<TimeModel> activityList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private ImageView userButton;
    Toolbar toolbar;
    TextView barbes_date_text, date_text, user_id;
    String barbershopId;
    String data, dd, mm, yy, min, hours;
    Spinner spinner_min, spinner_hours;
    TimeModel timeModel;
    String getName, getPhone1, userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_barber);

        barbershopId = SharedPreferencesUtil.getString(this, "BarbesID", "");

        mAuth = FirebaseAuth.getInstance();
        barbesReadDb();
        initViews();
//        setupListeners();
        addTimeSlotView();
        readDb();


//        userButton.setOnClickListener(v -> logout());
    }



    private void initViews() {
//        editHour = findViewById(R.id.edit_hour_id_barber);
        barbes_date_text = findViewById(R.id.barbes_date_text);
        date_text = findViewById(R.id.date_text);
        user_id = findViewById(R.id.user_id);
        recyclerView = findViewById(R.id.recycler);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.time_input).setOnClickListener(v -> showMaterialTimeBottomSheet());

        dd = LocalDateTime.dateDD();
        mm = LocalDateTime.dateMM();
        yy = LocalDateTime.dateYYYY();
        data = LocalDateTime.dateDDMMYY();


        date_text.setText("Bugun " + LocalDateTime.dateDDMMYY());


    }


    public void barbesReadDb() {

        db.collection("Barbers").document(barbershopId)

                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        BarberProfile profile = documentSnapshot.toObject(BarberProfile.class);

                        if (profile != null) {

                            user_id.setText("ID " + profile.getUserID());
                            barbes_date_text.setText(profile.getName());
                        }
                    }
                }).addOnFailureListener(e -> {
                    Log.e("readDb", "Xatolik: " + e.getMessage());
                });
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
                writeDb();
                activityList.clear();
                readDb();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
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

            startActivity(new Intent(BarberActivity.this, EditProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()).signOut();

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
    private void addTimeSlotView() {
        FrameLayout container = findViewById(R.id.schedule_container_barber);
        timeSlotView = new TimeSlotView(this);
        container.addView(timeSlotView, new FrameLayout.LayoutParams((int) (getResources().getDisplayMetrics().density * 80), ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        GoogleSignIn.getClient(this, gso).signOut();

        getSharedPreferences("app_prefs", MODE_PRIVATE).edit().clear().apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void writeDb() {
        String hourStr = hours.toString();
        String minuteStr = min.toString();

        if (hourStr.isEmpty() || minuteStr.isEmpty()) return;

        int hour = Integer.parseInt(hourStr);
        int minute = Integer.parseInt(minuteStr);

        if (minute >= 60) {
            Toast.makeText(this, "Daqiqa noto‘g‘ri kiritilgan", Toast.LENGTH_SHORT).show();
            return;
        }

        int endMinute = minute + 40;
        int endHour = hour + (endMinute >= 60 ? 1 : 0);
        endMinute = endMinute % 60;

        String slot = String.format("%02d:%02d-%02d:%02d", hour, minute, endHour, endMinute);

        Map<String, Object> item = new HashMap<>();
        item.put("slot", slot);
        item.put("name", "Sartarosh band etdi");
//        item.put("phone", customerPhone);
        item.put("data", data);
        item.put("day-time", dd);

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("Barbers").document(uid).collection("Customer1").add(item).addOnSuccessListener(doc -> Log.d("TAG", "Added: " + doc.getId())).addOnFailureListener(e -> Log.w("TAG", "Error adding", e));

//        editHour.setText("");
//        editMinute.setText("");
    }

    public void readDb() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Barbers")
                .document(uid).collection("Customer1")
                .whereGreaterThanOrEqualTo("day-time", dd).whereLessThanOrEqualTo("day-time", dd + "\uf8ff")
                .get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("ReadDb", "Error: ", task.getException());
                return;
            }

            List<TimeSlotView.TimeSlot> timeSlots = new ArrayList<>();
            activityList.clear();

            for (QueryDocumentSnapshot doc : task.getResult()) {

                String raw = doc.getString("slot");
                String name = doc.getString("name");
                String phone1 = doc.getString("phone1");


                if (raw == null || !raw.contains("-")) continue;

                activityList.add(new TimeModel(uid, "", doc.getId(), raw, name,phone1, ""));


                try {
                    String[] parts = raw.split("-");
                    LocalTime start = LocalTime.parse(parts[0].trim());
                    LocalTime end = LocalTime.parse(parts[1].trim());
                    timeSlots.add(new TimeSlotView.TimeSlot(start, end));
                } catch (Exception e) {
                    Log.w("Parse", "Invalid slot: " + raw);
                }
            }

            timeSlotView.setBusySlots(timeSlots);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
            adapter = new BarberAdapter(this, activityList);
            recyclerView.setAdapter(adapter);
        });
    }





    public static class TimeSlotView extends View {
        private List<TimeSlot> busy = new ArrayList<>();
        private final int startHour = 8, endHour = 20, totalMin = (endHour - startHour) * 60;

        private final Paint slotPaint = new Paint();
        private final Paint labelPaint = new Paint();
        private final Paint linePaint = new Paint();
        private final Paint bgPaint = new Paint();

        public TimeSlotView(Context context) {
            this(context, null);
        }

        public TimeSlotView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public TimeSlotView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            // Вақт белгиси шрифт
            labelPaint.setColor(Color.parseColor("#37474F"));
            labelPaint.setTextSize(32f);
            labelPaint.setAntiAlias(true);

            // Ҳар бир соат чизиғи
            linePaint.setColor(Color.LTGRAY);
            linePaint.setStrokeWidth(2f);

            // Shadow
            slotPaint.setAntiAlias(true);
            slotPaint.setShadowLayer(6f, 0, 2, Color.GRAY);
            setLayerType(LAYER_TYPE_SOFTWARE, slotPaint);

            // Фон учун градиент
            Shader shader = new LinearGradient(
                    0, 0, 0, getHeight(),
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#E0F7FA"),
                    Shader.TileMode.CLAMP
            );
            bgPaint.setShader(shader);
        }

        public void setBusySlots(List<TimeSlot> list) {
            busy = list;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float slotH = (float) getHeight() / totalMin;

            // ✅ Фон градиенти
            canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

            // ✅ Фақат банд вақтларни чизмага чиқарамиз
            slotPaint.setColor(Color.parseColor("#FFAB91")); // Pastel Orange

            for (TimeSlot slot : busy) {
                float top = Duration.between(LocalTime.of(startHour, 0), slot.start).toMinutes() * slotH;
                float bottom = Duration.between(LocalTime.of(startHour, 0), slot.end).toMinutes() * slotH;
                canvas.drawRoundRect(0, top, getWidth(), bottom, 24f, 24f, slotPaint);
            }

            // ✅ Ҳар бир соат учун йўналиш чизиқлари ва вақтлар
            for (int h = startHour; h <= endHour; h++) {
                float y = (h - startHour) * 60 * slotH;
                canvas.drawLine(0, y, getWidth(), y, linePaint);
                canvas.drawText(String.format("%02d:00", h), 20, y + 30, labelPaint);
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




//    public static class TimeSlotView extends View {
//        private List<TimeSlot> busy = new ArrayList<>();
//        private final int startHour = 8, endHour = 20, totalMin = (endHour - startHour) * 60;
//        private final Paint fill = new Paint(), label = new Paint();
//
//        public TimeSlotView(Context c) {
//            this(c, null);
//        }
//
//        public TimeSlotView(Context c, AttributeSet a) {
//            this(c, a, 0);
//        }
//
//        public TimeSlotView(Context c, AttributeSet a, int s) {
//            super(c, a, s);
//            label.setColor(Color.BLACK);
//            label.setTextSize(26f);
//        }
//
//        public void setBusySlots(List<TimeSlot> list) {
//            busy = list;
//            invalidate();
//        }
//
//        @Override
//        protected void onDraw(Canvas c) {
//            super.onDraw(c);
//            float slotH = (float) getHeight() / totalMin;
//
//            for (int i = 0; i < totalMin; i++) {
//                LocalTime now = LocalTime.of(startHour, 0).plusMinutes(i);
//                boolean isBusy = busy.stream().anyMatch(t -> !now.isBefore(t.start) && now.isBefore(t.end));
//                fill.setColor(isBusy ? Color.CYAN : Color.GREEN);
//                float top = i * slotH;
//                c.drawRect(0, top, getWidth(), top + slotH, fill);
//            }
//
//            for (int h = startHour; h <= endHour; h++) {
//                float y = (h - startHour) * 60 * slotH;
//                c.drawLine(0, y, getWidth(), y, label);
//                c.drawText(String.format("%02d:00", h), 10, y + 24, label);
//            }
//        }
//
//        public static class TimeSlot {
//            LocalTime start, end;
//
//            public TimeSlot(LocalTime s, LocalTime e) {
//                start = s;
//                end = e;
//            }
//        }
//    }
}
