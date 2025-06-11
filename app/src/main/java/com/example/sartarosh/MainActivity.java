package com.example.sartarosh;

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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.view.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Дастур: вақт интервалларини Firestore'га ёзиб/ўқиб,
 *          TimeSlotView'да банд (CYAN) ва бўш (GREEN) вақтларни кўрсатади.
 *
 *  minSdkVersion ≥ 26 (java.time)
 */
public class MainActivity extends AppCompatActivity {

    /* --------  UI  -------- */
    private EditText edit_hour_id;
    private Button   add_hour_id;
    private TimeSlotView timeSlotView;

    /* --------  Firestore  -------- */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        /* === Виджетлар === */
        edit_hour_id = findViewById(R.id.edit_hour_id);
        add_hour_id  = findViewById(R.id.add_hour_id);

        /* === Custom View'ни динамик қўшамиз === */
        FrameLayout container = findViewById(R.id.schedule_container);   // activity_main.xml'даги FrameLayout
        timeSlotView = new TimeSlotView(this);
        container.addView(timeSlotView,
                new FrameLayout.LayoutParams(
                        (int) getResources().getDisplayMetrics().density * 80, // ~80dp кенглик
                        ViewGroup.LayoutParams.MATCH_PARENT));

        /* === Insets === */
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
//            return insets;
//        });

        /* === Кнопка: ёзиш → ўқиш → redraw === */
        add_hour_id.setOnClickListener(v -> {
            WriteDb();
            ReadDb();
            edit_hour_id.setText("");
        });

        /* Илк бор ўқиб оламиз */
        ReadDb();
    }

    /* ------------------------------------------------------------
     * Firestore'га ["HH:mm-HH:mm"] кўринишида ёзамиз
     * ------------------------------------------------------------ */
    private void WriteDb() {
        // Масалан, EditText'га "08:00-11:40" ёки "12:20-13:00" ёзилган бўлади
        Map<String, Object> item = new HashMap<>();
        item.put("slot", edit_hour_id.getText().toString());

        db.collection("users")
                .add(item)
                .addOnSuccessListener(doc ->
                        Log.d("TAG", "Added: " + doc.getId()))
                .addOnFailureListener(e ->
                        Log.w("TAG", "Error adding", e));
    }

    /* ------------------------------------------------------------
     * Firestore'дан ўқиб, TimeSlot рўйхатига парс қиламиз
     * ------------------------------------------------------------ */
    private void ReadDb() {
        db.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TimeSlotView.TimeSlot> list = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String raw = doc.getString("slot");      // "08:00-11:40"
                            if (raw == null || !raw.contains("-")) continue;

                            try {
                                String[] parts = raw.split("-");
                                LocalTime start = LocalTime.parse(parts[0].trim());
                                LocalTime end   = LocalTime.parse(parts[1].trim());
                                list.add(new TimeSlotView.TimeSlot(start, end));
                            } catch (Exception e) {
                                Log.w("Parse", "Bad slot: " + raw);
                            }
                        }

                        /* --- Custom View'га юборамиз --- */
                        timeSlotView.setBusySlots(list);
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
        private List<TimeSlot> busy = new ArrayList<>();

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
        public void setBusySlots(List<TimeSlot> list) {
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
                for (TimeSlot t : busy) {
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