package com.bc.sartarosh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SpinnerAdapter  extends ArrayAdapter<String> {

        private int layoutResourceId;

        public SpinnerAdapter(Context context, List<String> options, int layoutResourceId) {
                super(context, 0, options);
                this.layoutResourceId = layoutResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                        convertView = LayoutInflater.from(getContext()).inflate(layoutResourceId, parent, false);
                }

                TextView textView = convertView.findViewById(R.id.spinner_item_text);
                textView.setText(getItem(position));

                return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                return getView(position, convertView, parent);
        }
}
