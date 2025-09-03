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

import com.bc.sartarosh.DateUtils;
//import com.bc.sartarosh.LocalDateTime;
import com.bc.sartarosh.MainActivity;
import com.bc.sartarosh.NotificationHelper;
import com.bc.sartarosh.R;
import com.bc.sartarosh.SharedPreferencesUtil;
import com.bc.sartarosh.SpinnerAdapter;
import com.bc.sartarosh.TimeModel;
import com.bc.sartarosh.profil.BarberActivity;
import com.bc.sartarosh.profil.BarberMapModel;
import com.bc.sartarosh.profil.BarberProfile;
import com.bc.sartarosh.profil.EditProfileActivity;
import com.bc.sartarosh.profil.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
    String getName, getPhone1, getPhone2, userID, fcmToken, hairTime, beardTime;
    int clickPlusCount = 0, plusDD;
    ProgressBar progressBar;
    private List<TimeSlotView.TimeSlot> busySlots = new ArrayList<>();
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

        dd = DateUtils.dateDD();
        mm = DateUtils.dateMM();
        yy = DateUtils.dateYYYY();
        data = DateUtils.dateDDMMYY();
        barbes_date.setText(data);
        plusDD = Integer.parseInt(dd);
        barbesReadDb(DateUtils.dateDDMMYY());
        ReadDb();
        initListeners();
//        findViewById(R.id.time_input).setOnClickListener(v -> showMaterialTimeBottomSheet());

        findViewById(R.id.minus_date).setOnClickListener(v -> minusDate());

        findViewById(R.id.plus_date).setOnClickListener(v -> plusDate());

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
        user_id.setText("ID: " + barberUserID);
    }

    private void plusDate() {
        if (clickPlusCount <= 1) {
            clickPlusCount++;
            plusDD = Integer.parseInt(dd) + clickPlusCount;
            String date = DateUtils.datePlusDays(clickPlusCount);
            barbes_date.setText(date);
            Log.d("TAG5", "plusDate: " + plusDD);
            barbesReadDb(date);
        } else {
            Toast.makeText(this, "Keyingi sanaga o'tib bo'lmaydi", Toast.LENGTH_SHORT).show();
        }
    }

    private void minusDate() {
        if (clickPlusCount >= 1) {
            clickPlusCount--;
            plusDD = Integer.parseInt(dd) + clickPlusCount;
            String date = DateUtils.datePlusDays(clickPlusCount);
            barbes_date.setText(date);
            Log.d("TAG5", "minusDate: " + plusDD);
            barbesReadDb(date);
        } else {
            Toast.makeText(this, "Keyingi sanaga o'tib bo'lmaydi", Toast.LENGTH_SHORT).show();
        }
    }

    public void barbesReadDb(String selectedDate) {
        db.collection("Barbers").document(barbershopId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        BarberProfile profile = documentSnapshot.toObject(BarberProfile.class);

                        Log.e("readDb", "Xatolik: 1 " + profile.getUserID());
                        hairTime = profile.getHairTime();
                        beardTime = profile.getBeardTime();
                        String strictStartHour = profile.getStrictStartHour();
                        String strictEndHour = profile.getStrictEndHour();

                        boolean found = false;
                        List<BarberMapModel> dateList = profile.getKey();
                        if (dateList != null && !dateList.isEmpty()) {
                            for (BarberMapModel entry : dateList) {
                                if (entry.getDate().equals(selectedDate)) {
                                    int startHour = Integer.parseInt(entry.getStartHour());
                                    int endHour = Integer.parseInt(entry.getEndHour());

                                    timeSlotView.setStartHour(startHour, endHour);
                                    Log.d("TAG11", "Date: 2 " + selectedDate +
                                            " Start: " + startHour +
                                            " End: " + endHour);
                                    findViewById(R.id.time_input).setOnClickListener(v -> showMaterialTimeBottomSheet(String.format("%02d", startHour), String.format("%02d", endHour) ));
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            timeSlotView.setStartHour(Integer.parseInt(strictStartHour), Integer.parseInt(strictEndHour));
                            findViewById(R.id.time_input).setOnClickListener(v -> showMaterialTimeBottomSheet(strictStartHour, strictEndHour));
                            Log.d("TAG11", "Default strict hours qo‘llandi: " + strictStartHour + " - " + strictEndHour);
                        }
                        ReadDb();
                    }

                }).addOnFailureListener(e -> {
                    Log.e("readDb", "Xatolik: " + e.getMessage());
                });

    }

    private void customerReadDb() {

        db.collection("Customer").document(customerId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        CustomerModel profile = snapshot.toObject(CustomerModel.class);
                        if (profile != null) {
                            getName = profile.getName();
                            getPhone1 = profile.getPhone1();
                            getPhone2 = profile.getPhone2();
                            userID = profile.getUserID();
                            fcmToken = profile.getFcmToken();

                            Log.d("TAG6", "customerReadDb: 1");
                            if (userID == null || fcmToken == null || customerId == null) {
                                Intent intent = new Intent(this, LoginActivity.class);
                                Log.d("TAG6", "customerReadDb: 2");

                                intent.putExtra("Customer", true);
                                startActivity(intent);
                                finish();
                            } else {
                                WriteDb(getName, getPhone1, getPhone2);
                            }


                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("readDb", "Xatolik: " + e.getMessage()));
    }


    private void WriteDb(String name, String phone1, String phone2) {
        String hourStr = hours;
        String minuteStr = min;

        if (hourStr.isEmpty() || minuteStr.isEmpty()) return;

        int hour = Integer.parseInt(hourStr);
        int startMinute = Integer.parseInt(minuteStr);

        if (startMinute >= 60) {
            Toast.makeText(this, "Daqiqa noto‘g‘ri kiritilgan", Toast.LENGTH_SHORT).show();
            return;
        }


        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        // Agar tanlangan sana bugungi kun bo‘lsa:
        String selectedDate = barbes_date.getText().toString(); // dd.MM.yyyy форматда
        String todayDate = DateUtils.dateDDMMYY();

        if (selectedDate.equals(todayDate)) {
            if (hour < currentHour || (hour == currentHour && startMinute <= currentMinute)) {
                Toast.makeText(this, "O‘tgan vaqtga navbat olib bo‘lmaydi", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int endMinute = startMinute + Integer.parseInt(hairTime);
        int endHour = hour + (endMinute >= 60 ? 1 : 0);
        endMinute = endMinute % 60;

        String slot = String.format("%02d:%02d-%02d:%02d", hour, startMinute, endHour, endMinute);

        Map<String, Object> item = new HashMap<>();
        item.put("slot", slot);
        item.put("barberUserID", barberUserID);
        item.put("customerUid", customerId);
        item.put("customerUserID", userID);
        item.put("name", name);
        item.put("phone1", phone1);
        item.put("phone2", phone2);
        item.put("data", data);
        String stringPlusDD = String.format("%02d", plusDD);
        item.put("day-time", stringPlusDD);

        Log.d("TAG12", "WriteDb: 3 " + data + " " + customerId + " " + stringPlusDD);

        db.collection("Barbers").document(barbershopId).collection("Customer1")
                .whereEqualTo("customerUid", customerId)
                .whereEqualTo("day-time", stringPlusDD)   // faqat tanlangan sana bo'yicha
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Toast.makeText(this, "Siz bir kunda faqat bitta navbat olishingiz mumkin", Toast.LENGTH_SHORT).show();
                    } else {

                        db.collection("Barbers").document(barbershopId).collection("Customer1")
                                .add(item)
                                .addOnSuccessListener(doc -> Log.d("TAG", "Added: " + doc.getId()))
                                .addOnFailureListener(e -> Log.w("TAG", "Error adding", e));

                        activityList.clear();
                        ReadDb();
                    }
                })
                .addOnFailureListener(e -> Log.e("TAG", "Xatolik: " + e.getMessage()));

    }

public void ReadDb() {
    progressBar.setVisibility(View.VISIBLE);

    String stringPlusDD = String.format("%02d", plusDD);

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

                        // busySlots globalda saqlanadi
                        busySlots = timeSlots;

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


    public static class TimeSlotView extends View {
        private List<TimeSlotView.TimeSlot> busy = new ArrayList<>();
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

        public void setBusySlots(List<TimeSlotView.TimeSlot> list) {
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
            for (TimeSlotView.TimeSlot slot : busy) {
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
            private LocalTime start;
            private LocalTime end;

            public TimeSlot(LocalTime start, LocalTime end) {
                this.start = start;
                this.end = end;
            }

            public LocalTime getStart() {
                return start;
            }

            public LocalTime getEnd() {
                return end;
            }
        }

    }


    private void showMaterialTimeBottomSheet(String startHour, String endHour) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_time_picker, null, false);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Spinner'ни view орқали оламиз (ЭЪТИБОР БЕРИН!)
        spinner_min = bottomSheetView.findViewById(R.id.spinner_min);
        spinner_hours = bottomSheetView.findViewById(R.id.spinner_hours);
        TextInputLayout edit_spinner_name_1 = bottomSheetView.findViewById(R.id.edit_spinner_name_1);
        edit_spinner_name_1.setVisibility(View.GONE);
        if (spinner_hours != null) {
            int start = Integer.parseInt(startHour);
            int end = Integer.parseInt(endHour) - 1;

            List<String> hoursList = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                hoursList.add(String.format("%02d", i)); // 08, 09, 10 ... 19
            }

            String[] spinner_hours_list = {"08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
            SpinnerAdapter adapter_hours = new SpinnerAdapter(this, hoursList, R.layout.spinner);
            spinner_hours.setAdapter(adapter_hours);
            spinner_hours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String selectedOption = parentView.getItemAtPosition(position).toString();
                    hours = selectedOption;
                    updateMinutesSpinner();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });

        }


        Button okButton = bottomSheetView.findViewById(R.id.btn_ok);
        if (okButton != null) {
            okButton.setOnClickListener(v -> {
                customerReadDb();
                bottomSheetDialog.dismiss();
            });
        }

        bottomSheetDialog.show();
    }



    private void updateMinutesSpinner() {
        if (spinner_min == null || hours == null || hours.isEmpty()) {
            return;
        }

        int rawServiceMin;
        try {
            rawServiceMin = Integer.parseInt(hairTime);
        } catch (NumberFormatException e) {
            rawServiceMin = 40;
            Log.e("updateMinutesSpinner", "hairTime parse error. Using default 40 min.", e);
        }

        int selectedHour = Integer.parseInt(hours);
        String[] allMinutes = {"00", "10", "20", "30", "40", "50"};
        List<String> allowed = new ArrayList<>();

        // Har bir 10 daqiqalik intervalni tekshirib chiqamiz
        for (String m : allMinutes) {
            int minInt = Integer.parseInt(m);
            LocalTime candidateStart = LocalTime.of(selectedHour, minInt);

            // TUZATISH: Navbatning tugash vaqtini YAXLITLANMAGAN, HAQIQIY xizmat vaqti bilan hisoblaymiz.
            // Bu 45 daqiqalik xizmatni 50 daqiqa deb xato hisoblashni oldini oladi.
            LocalTime candidateEnd = candidateStart.plusMinutes(rawServiceMin > 0 ? rawServiceMin : 10);

            boolean isAvailable = true; // Dastlab bu vaqtni bo'sh deb hisoblaymiz

            if (busySlots != null && !busySlots.isEmpty()) {
                for (TimeSlotView.TimeSlot busySlot : busySlots) {
                    LocalTime busyStart = busySlot.getStart();
                    LocalTime busyEnd = busySlot.getEnd();

                    // Asosiy va eng to'g'ri kesishish tekshiruvi.
                    // [A, B) va [C, D) intervallar kesishadi, qachonki (A < D) va (B > C) bo'lsa.
                    boolean overlaps = candidateStart.isBefore(busyEnd) && candidateEnd.isAfter(busyStart);

                    if (overlaps) {
                        isAvailable = false;
                        break; // Bitta kesishish topilsa, boshqalarini tekshirish shart emas
                    }
                }
            }

            if (isAvailable) {
                allowed.add(m);
            }
        }

        SpinnerAdapter adapter_young = new SpinnerAdapter(this, allowed, R.layout.spinner);
        spinner_min.setAdapter(adapter_young);
        spinner_min.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                min = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }






//    private void updateMinutesSpinner() {
//        if (spinner_min == null || hours == null) return;
//
//        List<String> minutesList = new ArrayList<>(Arrays.asList("00", "10", "20", "30", "40", "50"));
//
//        if (busySlots != null && !busySlots.isEmpty()) {
//            Iterator<String> iterator = minutesList.iterator();
//            while (iterator.hasNext()) {
//                String m = iterator.next();
//                LocalTime candidate = LocalTime.of(Integer.parseInt(hours), Integer.parseInt(m));
//
//                for (TimeSlotView.TimeSlot slot : busySlots) {
//                    if (!candidate.isBefore(slot.getStart()) && candidate.isBefore(slot.getEnd())) {
//                        iterator.remove(); // 🔥 бу вақт олиб ташланади
//                        break;
//                    }
//                }
//            }
//        }
//
//        SpinnerAdapter adapter_young = new SpinnerAdapter(this, minutesList, R.layout.spinner);
//        spinner_min.setAdapter(adapter_young);
//        spinner_min.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                min = parentView.getItemAtPosition(position).toString();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {}
//        });
//    }



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
        } else if (id == R.id.share_app) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,
                    "\n- Янги Сартарош иловаси" +
                    "\n- Онлайн навбат олиш имконияти!" +
                    "\n- Севимли устангизни танланг" +
                    "\n- Вақтингизни тежанг" +
                    "\n- Барча сартарошлар ва мижозлар учун қулай платформа!" +
                    "\n " +
                    "\nhttps://play.google.com/store/apps/details?id=com.bc.sartarosh");
            intent.setType("text/plain");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initListeners() {
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerBarberActivity.class));
            finish();
        });

    }


    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission берилган — хабар юбориш
                NotificationHelper.showNotification(this, "Салом!", "Сизда билдиришномалар фаоллаштирилди. Энди бемалол фойдаланишингиз мумкин!");
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
                NotificationHelper.showNotification(this, "Салом!", "Сизда билдиришномалар фаоллаштирилди. Энди улардан бемалол фойдаланинг!");
            } else {
                // Рухсат берилмади
                showRationaleDialog(); // Фойдаланувчига оғоҳлантириш
            }
        }
    }


}
