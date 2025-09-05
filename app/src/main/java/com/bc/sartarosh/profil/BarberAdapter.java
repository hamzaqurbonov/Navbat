package com.bc.sartarosh.profil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bc.sartarosh.R;
import com.bc.sartarosh.TimeModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BarberAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{

    private BarberAdapter.RecyclerViewClickListner listner;
    List<TimeModel> activityllist ;
    private BarberActivity barberActivity;


    public BarberAdapter( BarberActivity barberActivity , List<TimeModel> activityllist) {
        this.activityllist = activityllist;
        this.barberActivity = barberActivity;

    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_item, parent, false);
        return new BarberAdapter.HomeViewAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        String BarbersId = activityllist.get(position).getBarbersId();
        String DocId = activityllist.get(position).getDocId();
        TimeModel currentTimeSlot = activityllist.get(position);

        ((BarberAdapter.HomeViewAdapterHolder) holder).name.setText(activityllist.get(position).getName());
        ((HomeViewAdapterHolder) holder).phone.setText(activityllist.get(position).getPhone1());
        ((BarberAdapter.HomeViewAdapterHolder) holder).TextViewName.setText(activityllist.get(position).getFirst());

        ((BarberAdapter.HomeViewAdapterHolder) holder).edit_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(), "Matin tahrirlandi!" + currentTimeSlot, Toast.LENGTH_SHORT).show();
                barberActivity.showMaterialTimeBottomSheet(activityllist.get(position));
            }
        });

        ((BarberAdapter.HomeViewAdapterHolder) holder).deleteSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Matinni o'chirish");
                builder.setMessage("Matinni o'chirishni istaysizmi?");
                builder.setPositiveButton("Ha", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        db.collection("Barbers").document(BarbersId).collection("Customer1")
                                .document(DocId)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(v.getContext(), "Matin o'chirildi!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(v.getContext(), "Xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        activityllist.clear();
                        barberActivity.readDb(barberActivity.getSelectedDate());
//                        activityllist.clear();
//                        barberActivity.readDb();
                    }
                });
                builder.setNegativeButton("Yo'q", null);
                builder.create().show();
            }
        });

    }

    @Override
    public int getItemCount() {
        int dd =  activityllist.size();
        return dd;
    }

    public class HomeViewAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        TextView TextViewName, name, phone;
        ImageView deleteSelect, edit_select;
        public HomeViewAdapterHolder(View v) {
            super(v);
            view = v;
            phone = view.findViewById(R.id.phone);
            name = view.findViewById(R.id.name);
            TextViewName = view.findViewById(R.id.time);
            deleteSelect = view.findViewById(R.id.delete_select);
            edit_select = view.findViewById(R.id.edit_select);
        }

        @Override
        public void onClick(View v) {
            listner.onClick(view, getAdapterPosition());
        }

    }

    public interface RecyclerViewClickListner {
        void onClick(View v, int position);
    }


}

