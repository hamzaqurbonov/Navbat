package com.bc.sartarosh.customer;

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
import com.bc.sartarosh.profil.BarberProfile;
import com.bc.sartarosh.profil.EditProfileActivity;
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

public class CustomerActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private EditText editHour, editMinute;
    private Button addHourBtn;
    private ImageView backBtn, minus_date, plus_date;
    private TimeSlotView timeSlotView;
    private RecyclerView recycler;
    private CustomerAdapter adapter;
    private final List<TimeModel> activityList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    Toolbar toolbar;
    String data, dd, mm, yy, min, hours;
    private String barbershopId, customerId, customerName, customerPhone, barberUserID;
    TextView barbes_date_text, date_text, user_id, barbes_date;
    Spinner spinner_min, spinner_hours;
    String getName, getPhone1, getPhone2, userID;
    int clickPlusCount = 0, plusDD;
    ProgressBar progressBar;

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
        barbes_date.setText(data);
        plusDD = Integer.parseInt(dd);
        ReadDb();
        initListeners();
        findViewById(R.id.time_input).setOnClickListener(v -> showMaterialTimeBottomSheet());

        findViewById(R.id.minus_date).setOnClickListener(v -> minusDate());

        findViewById(R.id.plus_date).setOnClickListener(v -> plusDate());


    }

    private void notification() {


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101);
            } else {
                // Рухсат берилган, notification чиқариш мумкин
                NotificationHelper.showNotification(this, "Салом!", "Бу сизга келган хабар.");
            }
        } else {
            // Android 12 ёки қуйироқ — permission керак эмас
            NotificationHelper.showNotification(this, "Салом!", "Бу сизга келган хабар.");
        }

    }



    private void plusDate() {
        if (clickPlusCount <= 2) {
            clickPlusCount++;
            plusDD = Integer.parseInt(dd) + clickPlusCount;
            String date = LocalDateTime.datePlusDays(clickPlusCount);
            barbes_date.setText(date);
            Log.d("TAG5", "plusDate: " + plusDD);
            ReadDb();
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
            ReadDb();
        } else {
            Toast.makeText(this, "Keyingi sanaga o'tib bo'lmaydi", Toast.LENGTH_SHORT).show();
        }
    }

    private void customerReadDb() {

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
                            WriteDb(getName, getPhone1, getPhone2);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("readDb", "Xatolik: " + e.getMessage()));
    }

    private void showMaterialTimeBottomSheet() {
//        notification();


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
                customerReadDb();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }


    private void initViews() {
        barbes_date_text = findViewById(R.id.barbes_date_text);
        progressBar = findViewById(R.id.progressBar);
        backBtn = findViewById(R.id.back_Button);
        user_id = findViewById(R.id.user_id);
        recycler = findViewById(R.id.recycler);
        barbes_date = findViewById(R.id.barbes_date);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barbes_date_text.setText("Sartarosh: " + customerName);
//        date_text.setText("Bugun: " + LocalDateTime.dateDDMMYY());
        user_id.setText("ID: " + barberUserID);


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
        } else if (id == R.id.notification) {
            checkNotificationPermission();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initListeners() {
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerBarberActivity.class));
            finish();
        });

    }

    private void WriteDb(String name, String phone1, String phone2) {

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
        item.put("name", name);
        item.put("phone1", phone1);
        item.put("phone2", phone2);
        item.put("data", data);
        String stringPlusDD = String.format("%02d", plusDD);
        item.put("day-time", stringPlusDD);

        db.collection("Barbers").document(barbershopId).collection("Customer1")
                .add(item)
                .addOnSuccessListener(doc -> Log.d("TAG", "Added: " + doc.getId()))
                .addOnFailureListener(e -> Log.w("TAG", "Error adding", e));
        activityList.clear();
        ReadDb();
    }

    public void ReadDb() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("TAG5", "ReadDb: " + plusDD);
        String stringPlusDD = String.format("%02d", plusDD);
        Log.d("TAG5", "ReadDb stringPlusDD: " + stringPlusDD);
        if (!barbershopId.isEmpty()) {
            db.collection("Barbers").document(barbershopId).collection("Customer1")
                    .whereGreaterThanOrEqualTo("day-time", stringPlusDD)
                    .whereLessThanOrEqualTo("day-time", stringPlusDD + "\uf8ff")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<TimeSlotView.TimeSlot> timeSlots = new ArrayList<>();
                            activityList.clear();

                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String slot = doc.getString("slot");
                                String name = doc.getString("name");
                                String phone1 = doc.getString("phone1");
                                String customerUserID = doc.getString("customerUserID");
                                if (slot == null || !slot.contains("-")) continue;

                                activityList.add(new TimeModel(barbershopId, customerId, doc.getId(), slot, name, phone1, customerUserID));

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
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Log.e("Firestore", "Failed to read slots", task.getException());
                        }
                    });
        } else {
            Log.e("ReadDb", "Barber ID is empty");
        }
    }

    // Ichki klass — vaqtни кўрсатувчи view
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

            //  Фон градиенти
            canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

            //  Фақат банд вақтларни чизмага чиқарамиз
            slotPaint.setColor(Color.parseColor("#FFAB91")); // Pastel Orange

            for (TimeSlot slot : busy) {
                float top = Duration.between(LocalTime.of(startHour, 0), slot.start).toMinutes() * slotH;
                float bottom = Duration.between(LocalTime.of(startHour, 0), slot.end).toMinutes() * slotH;
                canvas.drawRoundRect(0, top, getWidth(), bottom, 24f, 24f, slotPaint);
            }

            //  Ҳар бир соат учун йўналиш чизиқлари ва вақтлар
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







    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission берилган — хабар юбориш
                NotificationHelper.showNotification(this, "Салом!", "Сизга билдиришнома юборилди.");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Рухсат олдин рад этилган, тушунтириш бериш мумкин
                showRationaleDialog();
            } else {
                // Биринчи марта ёки "Don't ask again" танланган
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        } else {
            // Android 12 ёки паст — permission керак эмас
            NotificationHelper.showNotification(this, "Салом!", "Сизга билдиришнома юборилди.");
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Билдиришномалар учун рухсат")
                .setMessage("Илованинг билдиришнома чиқариши учун рухсат керак. Рухсатни қайта ёқиш учун илова созламаларига ўтинг.")
                .setPositiveButton("Созламаларга ўтиш", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Бекор қилиш", null)
                .show();
    }

    // Permission натижасини қабул қилиш
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Рухсат берилди
                NotificationHelper.showNotification(this, "Салом!", "Сизга билдиришнома юборилди.");
            } else {
                // Рухсат берилмади
                showRationaleDialog(); // Фойдаланувчига оғоҳлантириш
            }
        }
    }




}
