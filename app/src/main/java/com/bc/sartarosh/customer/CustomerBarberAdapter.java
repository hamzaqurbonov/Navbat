package com.bc.sartarosh.customer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bc.sartarosh.R;

import java.util.List;

public class CustomerBarberAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{

    private RecyclerViewClickListner listner;
    //    EditHameViewActivity homeViewActivity;
    List<CustomerBarbesModel> activityllist ;
    private CustomerBarberActivity barberActivity;



    public CustomerBarberAdapter(  List<CustomerBarbesModel> activityllist, RecyclerViewClickListner listner) {
        this.activityllist = activityllist;
//        this.barberActivity = barberActivity;
            this.listner = listner;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_barber_item, parent, false);

        return new CustomerBarberAdapter.HomeViewAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        String documentId = activityllist.get(position).getName();

        ((CustomerBarberAdapter.HomeViewAdapterHolder) holder).TextViewName.setText(activityllist.get(position).getName());
        ((CustomerBarberAdapter.HomeViewAdapterHolder) holder).phone.setText(activityllist.get(position).getPhone());
        ((CustomerBarberAdapter.HomeViewAdapterHolder) holder).userId.setText(String.format("ID %s", activityllist.get(position).getUserID()));




    }

    @Override
    public int getItemCount() {
        int dd =  activityllist.size();
//        Log.d("demo1", "Added1: " + dd );
        return dd;
    }



    public class HomeViewAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        TextView TextViewName, phone, userId;
        ImageView deleteSelect;
        public HomeViewAdapterHolder(View v) {
            super(v);
            view = v;

            TextViewName = view.findViewById(R.id.barber_item);
            phone = view.findViewById(R.id.phone);
            userId = view.findViewById(R.id.userId);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listner != null) {
                listner.onClick(view, getAdapterPosition());
            }
        }

    }

    public interface RecyclerViewClickListner {
        void onClick(View v, int position);
    }



}