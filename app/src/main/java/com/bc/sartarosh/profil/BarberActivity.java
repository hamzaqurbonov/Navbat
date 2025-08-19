package com.bc.sartarosh.profil;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bc.sartarosh.LocalDateTime;
import com.bc.sartarosh.MainActivity;
import com.bc.sartarosh.NotificationHelper;
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
import com.google.firebase.firestore.Source;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarberActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
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
    TextView barbes_date_text, user_id, barbes_date;
    String barbershopId;
    String data, dd, mm, yy, min, hours;
    Spinner spinner_min, spinner_hours;
    TimeModel timeModel;
    String getName, getPhone1, userID, hairTime, beardTime, statDate;
    int clickPlusCount = 0, plusDD;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_barber);

        barbershopId = SharedPreferencesUtil.getString(this, "BarbesID", "");

        mAuth = FirebaseAuth.getInstance();


        initViews();
        addTimeSlotView();

//        readDb();

        barbesReadDb(LocalDateTime.dateDDMMYY());

    }


    private void plusDate() {
        if (clickPlusCount <= 2) {
            clickPlusCount++;
            plusDD = Integer.parseInt(dd) + clickPlusCount;
            String date = LocalDateTime.datePlusDays(clickPlusCount);
            barbes_date.setText(date);
            Log.d("TAG5", "plusDate: " + date);

            barbesReadDb(date);
//            readDb();
        } else {
            Toast.makeText(this, "Keyingi sanaga o'tib bo'lmaydi", Toast.LENGTH_SHORT).show();
        }
    }

    private void minusDate() {
        if (clickPlusCount >= 1) {
            clickPlusCount--;
            plusDD = Integer.parseInt(dd) + clickPlusCount;
            String date = LocalDateTime.datePlusDays(clickPlusCount);
            barbes_date.setText(date);
            Log.d("TAG5", "minusDate: " + plusDD);

            barbesReadDb(date);
//            readDb();
        } else {
            Toast.makeText(this, "Keyingi sanaga o'tib bo'lmaydi", Toast.LENGTH_SHORT).show();
        }
    }


    private void initViews() {
        barbes_date = findViewById(R.id.barbes_date);
        barbes_date_text = findViewById(R.id.barbes_date_text);
        progressBar = findViewById(R.id.progressBar);
        user_id = findViewById(R.id.user_id);
        recyclerView = findViewById(R.id.recycler);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.time_input).setOnClickListener(v -> showMaterialTimeBottomSheet());

        dd = LocalDateTime.dateDD();
        mm = LocalDateTime.dateMM();
        yy = LocalDateTime.dateYYYY();
        data = LocalDateTime.dateDDMMYY();

        barbes_date.setText(data);
        plusDD = Integer.parseInt(dd);

        findViewById(R.id.minus_date).setOnClickListener(v -> minusDate());

        findViewById(R.id.plus_date).setOnClickListener(v -> plusDate());
    }


    public void barbesReadDb(String selectedDate) {
        db.collection("Barbers").document(barbershopId)
                .get(Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    BarberProfile profile = documentSnapshot.toObject(BarberProfile.class);
                    if (profile == null) return;

                    Log.e("readDb", "Xatolik: 1 " + profile.getUserID());
                    hairTime = profile.getHairTime();
                    beardTime = profile.getBeardTime();
                    String strictStartHour = profile.getStrictStartHour();
                    String strictEndHour = profile.getStrictEndHour();

                    user_id.setText("ID " + profile.getUserID());
                    barbes_date_text.setText(profile.getName());

                    if (profile.getUserID() == null || profile.getFcmToken() == null ||
                            profile.getHairTime() == null || profile.getBeardTime() == null) {
                        Log.e("readDb", "Xatolik: 2");
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }

                    Log.d("TAG7", "date: 1 " + selectedDate);
                    boolean found = false;

                    List<BarberMapModel> dateList = profile.getKey();
                    if (dateList != null && !dateList.isEmpty()) {
                        for (BarberMapModel entry : dateList) {
                            if (entry.getDate().equals(selectedDate)) {
                                int startHour = Integer.parseInt(entry.getStartHour());
                                int endHour = Integer.parseInt(entry.getEndHour());

                                timeSlotView.setStartHour(startHour, endHour);
                                Log.d("TAG7", "Date: 2 " + selectedDate +
                                        " Start: " + startHour +
                                        " End: " + endHour);

                                found = true;
                                break;
                            }
                        }
                    }

                    // ️ Агар санани топа олмаса → strictStartHour / strictEndHour
                    if (!found) {
                        timeSlotView.setStartHour(
                                Integer.parseInt(strictStartHour),
                                Integer.parseInt(strictEndHour)
                        );
                        Log.d("TAG7", "Default strict hours qo‘llandi: " + strictStartHour + " - " + strictEndHour);
                    }

                    // Ҳар доим busy slots қайта чизилади
                    readDb();
                })
                .addOnFailureListener(e -> Log.e("readDb", "Xatolik: " + e.getMessage()));
    }





    private void addTimeSlotView() {

        FrameLayout container = findViewById(R.id.schedule_container_barber);
        timeSlotView = new TimeSlotView(this);
        container.addView(timeSlotView, new FrameLayout.LayoutParams((int) (getResources().getDisplayMetrics().density * 80), ViewGroup.LayoutParams.MATCH_PARENT));
    }


    public void readDb() {

        String uid = mAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);

        String stringPlusDD = String.format("%02d", plusDD);
        db.collection("Barbers").document(uid).collection("Customer1").whereGreaterThanOrEqualTo("day-time", stringPlusDD).whereLessThanOrEqualTo("day-time", stringPlusDD + "\uf8ff").get().addOnCompleteListener(task -> {
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

                activityList.add(new TimeModel(uid, "", doc.getId(), raw, name, phone1, ""));


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
            progressBar.setVisibility(View.GONE);
        });

    }

    public static class TimeSlotView extends View {
        private List<TimeSlot> busy = new ArrayList<>();
        private int startHour = 7;   // default qiymat
        private int endHour = 23;

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

            // vaqt belgilari
            labelPaint.setColor(Color.parseColor("#37474F"));
            labelPaint.setTextSize(32f);
            labelPaint.setAntiAlias(true);

            // soat liniyalari
            linePaint.setColor(Color.LTGRAY);
            linePaint.setStrokeWidth(2f);

            // slot uchun effekt
            slotPaint.setAntiAlias(true);
            slotPaint.setShadowLayer(6f, 0, 2, Color.GRAY);
            setLayerType(LAYER_TYPE_SOFTWARE, slotPaint);

            // fon gradyenti
            Shader shader = new LinearGradient(
                    0, 0, 0, getHeight(),
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#E0F7FA"),
                    Shader.TileMode.CLAMP
            );
            bgPaint.setShader(shader);
        }

        public void setBusySlots(List<TimeSlot> list) {
            this.busy = list;
            invalidate(); // qayta chiz
        }
        public void setStartHour(int startHour, int endHour) {
            this.startHour = startHour;
            this.endHour = endHour;
            invalidate(); // qayta chiz
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int totalMin = (endHour - startHour) * 60;
            if (totalMin <= 0) return;

            float slotH = (float) getHeight() / totalMin;

            // fon
            canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

            // busy slotlarni chizamiz
            slotPaint.setColor(Color.parseColor("#FFAB91"));
            for (TimeSlot slot : busy) {
                float top = Duration.between(LocalTime.of(startHour, 0), slot.start).toMinutes() * slotH;
                float bottom = Duration.between(LocalTime.of(startHour, 0), slot.end).toMinutes() * slotH;
                canvas.drawRoundRect(0, top, getWidth(), bottom, 24f, 24f, slotPaint);
            }

            // soat liniyalari va vaqt yozuvlari
            for (int h = startHour; h <= endHour; h++) {
                float y = (h - startHour) * 60 * slotH;
                canvas.drawLine(0, y, getWidth(), y, linePaint);
                canvas.drawText(String.format("%02d:00", h), 20, y + 30, labelPaint);
            }
        }

        // 🔹 model
        public static class TimeSlot {
            LocalTime start, end;

            public TimeSlot(LocalTime s, LocalTime e) {
                start = s;
                end = e;
            }
        }
    }


    private void writeDb() {
        String hourStr = hours.toString();
        String minuteStr = min.toString();
        String uid = mAuth.getCurrentUser().getUid();

        if (hourStr.isEmpty() || minuteStr.isEmpty()) return;

        int hour = Integer.parseInt(hourStr);
        int minute = Integer.parseInt(minuteStr);

        if (minute >= 60) {
            Toast.makeText(this, "Daqiqa noto‘g‘ri kiritilgan", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("hairTime", "Xatolik: 2 " + hairTime);
        int endMinute = Integer.parseInt(minute + hairTime);
        int endHour = hour + (endMinute >= 60 ? 1 : 0);
        endMinute = endMinute % 60;

        String slot = String.format("%02d:%02d-%02d:%02d", hour, minute, endHour, endMinute);

        Map<String, Object> item = new HashMap<>();
        item.put("slot", slot);
        item.put("name", "Sartarosh band etdi");
        item.put("customerUid", uid);
//        item.put("phone", customerPhone);
        item.put("data", data);
        String stringPlusDD = String.format("%02d", plusDD);
        item.put("day-time", stringPlusDD);


        db.collection("Barbers").document(uid).collection("Customer1").add(item).addOnSuccessListener(doc -> Log.d("TAG", "Added: " + doc.getId())).addOnFailureListener(e -> Log.w("TAG", "Error adding", e));
//        NotificationHelper.showNotification(this, "Навбат банд этилди", data + " " + slot);
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
        } else if (id == R.id.notification) {
            checkNotificationPermission();
        }

        return super.onOptionsItemSelected(item);
    }

    /// ---------------Bildirishnoma--------

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission берилган — хабар юбориш
                NotificationHelper.showNotification(this, "Салом!", "Сизда билдиришномалар фаоллаштирилди. Энди бемалол фойдаланишингиз мумкин!");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Рухсат олдин рад этилган, тушунтириш бериш мумкин
                showRationaleDialog();
            } else {
                // Биринчи марта ёки "Don't ask again" танланган
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        } else {
            // Android 12 ёки паст — permission керак эмас
            NotificationHelper.showNotification(this, "Sizda bildirishnoma yoqilgan!", "");
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(this).setTitle("Билдиришномалар учун рухсат").setMessage("Илованинг билдиришнома чиқариши учун рухсат керак. Рухсатни қайта ёқиш учун илова созламаларига ўтинг.").setPositiveButton("Созламаларга ўтиш", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }).setNegativeButton("Бекор қилиш", null).show();
    }

    // Permission натижасини қабул қилиш
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Рухсат берилди
                NotificationHelper.showNotification(this, "Салом!", "Сизда билдиришномалар фаоллаштирилди. Энди улардан бемалол фойдаланинг!");
            } else {
                // Рухсат берилмади
                showRationaleDialog(); // Фойдаланувчига оғоҳлантириш
            }
        }
    }
}
