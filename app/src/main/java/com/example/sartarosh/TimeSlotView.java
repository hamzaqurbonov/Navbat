package com.example.sartarosh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TimeSlotView extends View {


    static class TimeSlot {
        LocalTime start;
        LocalTime end;

        public TimeSlot(int startHour, int startMin, int endHour, int endMin) {
            this.start = LocalTime.of(startHour, startMin);
            this.end = LocalTime.of(endHour, endMin);
        }
    }

    private final List<TimeSlot> busySlots = Arrays.asList(


            new TimeSlot(8, 0, 11, 0),
            new TimeSlot(12, 30, 13, 0),
            new TimeSlot(14, 0, 14, 40),
            new TimeSlot(16, 0, 17, 40)
    );

    private final Paint paint = new Paint();
    private final Paint labelPaint = new Paint();

    private final int startHour = 8;
    private final int endHour = 22;
    private final int totalMinutes = (endHour - startHour) * 60;

    public TimeSlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(30f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float slotHeight = (float) getHeight() / totalMinutes;

        for (int i = 0; i < totalMinutes; i++) {
            LocalTime currentTime = LocalTime.of(startHour, 0).plusMinutes(i);

            boolean isBusy = false;
            for (TimeSlot slot : busySlots) {
                if (!currentTime.isBefore(slot.start) && currentTime.isBefore(slot.end)) {
                    isBusy = true;
                    break;
                }
            }

            paint.setColor(isBusy ? Color.CYAN : Color.GREEN);
            float top = i * slotHeight;
            canvas.drawRect(0, top, getWidth(), top + slotHeight, paint);
        }

        // Соат белгиларини чизиш
        for (int hour = startHour; hour <= endHour; hour++) {
            float y = (hour - startHour) * 60 * slotHeight;
            canvas.drawLine(0, y, getWidth(), y, labelPaint);
            canvas.drawText(String.format("%02d:00", hour), 10, y + 25, labelPaint);
        }
    }
}

