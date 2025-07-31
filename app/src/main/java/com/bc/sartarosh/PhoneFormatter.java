package com.bc.sartarosh;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneFormatter {

    public static void attachTo(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private int previousLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String raw = s.toString().replaceAll("[^\\d]", "");
                if (raw.length() > 12) raw = raw.substring(0, 12);

                String formatted = formatPhone(raw);
                editText.setText(formatted);
                editText.setSelection(formatted.length());

                isFormatting = false;
            }
        });
    }

    private static String formatPhone(String raw) {
        if (!raw.startsWith("998")) {
            raw = "998" + raw;
        }

        if (raw.length() <= 3) {
            return "+" + raw;
        } else if (raw.length() <= 5) {
            return "+998 (" + raw.substring(3);
        } else if (raw.length() <= 8) {
            return "+998 (" + raw.substring(3, 5) + ") " + raw.substring(5);
        } else if (raw.length() <= 10) {
            return "+998 (" + raw.substring(3, 5) + ") " + raw.substring(5, 8) + "-" + raw.substring(8);
        } else if (raw.length() <= 12) {
            return "+998 (" + raw.substring(3, 5) + ") " + raw.substring(5, 8) + "-" + raw.substring(8, 10) + "-" + raw.substring(10);
        } else {
            return "+998 (" + raw.substring(3, 5) + ") " + raw.substring(5, 8) + "-" + raw.substring(8, 10) + "-" + raw.substring(10, 12);
        }
    }
}
