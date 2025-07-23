package com.example.sartarosh.profil;

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

import com.example.sartarosh.R;
import com.example.sartarosh.TimeModel;

import com.example.sartarosh.customer.CustomerAdapter;
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        String BarbersId = activityllist.get(position).getBarbersId();
        String DocId = activityllist.get(position).getDocId();
//        String documentId = activityllist.get(position).getBarbersId();
        ((BarberAdapter.HomeViewAdapterHolder) holder).TextViewName.setText(activityllist.get(position).getFirst());



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
                                .document(BarbersId).collection("Customer2").document(DocId)
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
                        barberActivity.readDb();


                    }
                });
                builder.setNegativeButton("Yo'q", null);
                builder.create().show();
            }
        });



//        ((BarberAdapter.HomeViewAdapterHolder) holder).deleteSelect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//                builder.setTitle("Matinni o'chirish");
//                builder.setMessage("Matinni o'chirishni istaysizmi?");
//                builder.setPositiveButton("Ha", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//                        db.collection("users").document(documentId)
//                                .delete()
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Toast.makeText(v.getContext(), "Matin o'chirildi!", Toast.LENGTH_SHORT).show();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(v.getContext(), "Xatolik: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                        activityllist.clear();
//                        barberActivity.readDb();
//
//
//                    }
//                });
//                builder.setNegativeButton("Yo'q", null);
//                builder.create().show();
//            }
//        });

    }

    @Override
    public int getItemCount() {
        int dd =  activityllist.size();
        return dd;
    }



    public class HomeViewAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        TextView TextViewName, TextViewSum;
        ImageView deleteSelect;
        public HomeViewAdapterHolder(View v) {
            super(v);
            view = v;

            TextViewName = view.findViewById(R.id.time);
//                TextViewSum = view.findViewById(R.id.text_view_sum);

            deleteSelect = view.findViewById(R.id.delete_select);

//                view.setOnClickListener(this);
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