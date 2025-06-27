package com.example.sartarosh.customer;

import android.content.Context;
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
import com.example.sartarosh.TimeModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerActivity extends AppCompatActivity {

    private EditText edit_hour_id, edit_minut_id, barberNameEditText, barberLocationEditText, barberDescEditText;
    private Button add_hour_id;
    private TimeSlotView timeSlotView;
    RecyclerView recycler;
    CustomerAdapter adapter;
    public List<TimeModel> activityList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        /* === Виджетлар === */
        edit_hour_id = findViewById(R.id.edit_hour_id);
        edit_minut_id = findViewById(R.id.edit_minut_id);
        add_hour_id  = findViewById(R.id.add_hour_id);
        recycler  = findViewById(R.id.recycler);

        /* === Custom View'ни динамик қўшамиз === */
        FrameLayout container = findViewById(R.id.schedule_container);   // activity_main.xml'даги FrameLayout
        timeSlotView = new TimeSlotView(this);
        container.addView(timeSlotView,
                new FrameLayout.LayoutParams(
                        (int) getResources().getDisplayMetrics().density * 80, // ~80dp кенглик
                        ViewGroup.LayoutParams.MATCH_PARENT));



        /* === Кнопка: ёзиш → ўқиш → redraw === */
        add_hour_id.setOnClickListener(v -> {
            WriteDb();
            activityList.clear();
            ReadDb();

        });

        /* Илк бор ўқиб оламиз */
        ReadDb();

    }


    private void WriteDb() {
        Map<String, Object> item = new HashMap<>();
        String hourStr = edit_hour_id.getText().toString();
        String hourMinut = edit_minut_id.getText().toString();

        // Agar bo'sh bo'lsa, tekshir
        if (hourStr.isEmpty()) return;
        if (hourMinut.isEmpty()) return;

        int hour = Integer.parseInt(hourStr);
        int endtime = hour + 1;

        int starMinut = Integer.parseInt(hourMinut);
        int endMinut = starMinut + 40;
        int realminutMinut;

        // Soat 2 xonali ko'rinish uchun formatlash
        String formattedStart = String.format("%02d", hour);
        String formattedEnd = String.format("%02d", endtime);

        // Minut 2 xonali ko'rinish uchun formatlash
        String formattedStartMin = String.format("%02d", starMinut);
        String formattedEndMin = String.format("%02d", endMinut);

        if (starMinut>=60) {
            Toast.makeText(this,  "Daqiqa kiritishda xatolik", Toast.LENGTH_SHORT).show();
            return;
        } if (endMinut>=59) {
            realminutMinut =  endMinut - 60;
            String formattedrealminutMinut = String.format("%02d", realminutMinut);

            String slot = formattedStart + ":" + formattedStartMin + "-" + formattedEnd + ":" + formattedrealminutMinut ;
            item.put("slot", slot);
            Log.d("demo3", "if "  + realminutMinut);

            edit_hour_id.setText("");
            edit_minut_id.setText("");
        }else {
            String slot = formattedStart + ":" + formattedStartMin + "-" + formattedStart + ":" + formattedEndMin ;
            item.put("slot", slot);
            Log.d("demo3", "else "  + endMinut);
            edit_hour_id.setText("");
            edit_minut_id.setText("");
        }



        db.collection("users")
                .add(item)
                .addOnSuccessListener(doc -> Log.d("TAG", "Added: " + doc.getId()))
                .addOnFailureListener(e -> Log.w("TAG", "Error adding", e));
    }

    /* ------------------------------------------------------------
     * Firestore'дан ўқиб, TimeSlot рўйхатига парс қиламиз
     * ------------------------------------------------------------ */
    public void ReadDb() {
        db.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TimeSlotView.TimeSlot> list = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String raw = doc.getString("slot");

                            Log.d("demo28", "users doc "  + doc.getId());

                            activityList.add(new TimeModel(doc.getId(), raw));

                            Log.d("demo1", "Added1: " + raw );

                            // "08:00-11:40"
                            if (raw == null || !raw.contains("-")) continue;

                            try {
                                String[] parts = raw.split("-");
                                LocalTime start = LocalTime.parse(parts[0].trim());
                                LocalTime end   = LocalTime.parse(parts[1].trim());
                                list.add(new TimeSlotView.TimeSlot(start, end));

                                Log.d("demo1", "Added: " + start + " " + end);
                            } catch (Exception e) {
                                Log.w("Parse", "Bad slot: " + raw);
                            }
                        }

                        /* --- Custom View'га юборамиз --- */
                        timeSlotView.setBusySlots(list);


                        recycler.setLayoutManager(new GridLayoutManager(CustomerActivity.this, 1));

                        adapter = new CustomerAdapter(CustomerActivity.this, activityList);
                        recycler.setAdapter(adapter);


                    } else {
                        Log.e("ReadDb", "Error: ", task.getException());
                    }
                });
    }

    /* ============================================================
     *  ↓↓↓  INNER  CUSTOM  VIEW  (TimeSlotView)  ↓↓↓
     * ============================================================ */
    public static class TimeSlotView extends View {

        /* Бирор жойдан (Firestore'dан) келадиган рўйхат */
        private List<TimeSlotView.TimeSlot> busy = new ArrayList<>();

        /* Константалар: кун 08:00–20:00 */
        private final int startHour = 8;
        private final int endHour   = 20;
        private final int totalMin  = (endHour - startHour) * 60;

        /* Paint'лар */
        private final Paint fill = new Paint();
        private final Paint label = new Paint();

        /* ==== Конструкторлар ==== */
        public TimeSlotView(Context c)                      { this(c, null); }
        public TimeSlotView(Context c, AttributeSet attrs)  { this(c, attrs, 0); }
        public TimeSlotView(Context c, AttributeSet a, int s) {
            super(c, a, s);
            label.setColor(Color.BLACK);
            label.setTextSize(26f);
        }

        /* ==== Set & Redraw ==== */
        public void setBusySlots(List<TimeSlotView.TimeSlot> list) {
            busy = list;
            invalidate();   // қайта чизиш
        }

        /* ==== Чизиш ==== */
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float slotH = (float) getHeight() / totalMin;

            /* --- фон бўйлаб цикл --- */
            for (int i = 0; i < totalMin; i++) {
                LocalTime now = LocalTime.of(startHour, 0).plusMinutes(i);
                boolean isBusy = false;
                for (TimeSlotView.TimeSlot t : busy) {
                    if (!now.isBefore(t.start) && now.isBefore(t.end)) {
                        isBusy = true; break;
                    }
                }
                fill.setColor(isBusy ? Color.CYAN : Color.GREEN);
                float top = i * slotH;
                c.drawRect(0, top, getWidth(), top + slotH, fill);
            }

            /* --- соат қисм чизиқ ва ёзувлар --- */
            for (int h = startHour; h <= endHour; h++) {
                float y = (h - startHour) * 60 * slotH;
                c.drawLine(0, y, getWidth(), y, label);
                c.drawText(String.format("%02d:00", h), 10, y + 24, label);
            }
        }

        /* ==== Helper model ==== */
        public static class TimeSlot {
            LocalTime start, end;
            public TimeSlot(LocalTime s, LocalTime e) { start = s; end = e; }
        }
    }



}