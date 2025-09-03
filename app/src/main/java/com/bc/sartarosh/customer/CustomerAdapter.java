package com.bc.sartarosh.customer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bc.sartarosh.R;
import com.bc.sartarosh.SharedPreferencesUtil;
import com.bc.sartarosh.TimeModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

public class CustomerAdapter extends RecyclerView.Adapter< RecyclerView.ViewHolder>{

    private CustomerAdapter.RecyclerViewClickListner listner;
    //    EditHameViewActivity homeViewActivity;
    List<TimeModel> activityllist ;
    private CustomerActivity customerActivity;


    public CustomerAdapter( CustomerActivity customerActivity , List<TimeModel> activityllist) {
        this.activityllist = activityllist;
        this.customerActivity = customerActivity;
//            this.listner = listner;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rec_item, parent, false);

        return new CustomerAdapter.HomeViewAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        String BarbersId = activityllist.get(position).getBarbersId();
        String CustomerId = activityllist.get(position).getCustomerId();
        String DocId = activityllist.get(position).getDocId();
        String getCustomerUserID = activityllist.get(position).getCustomerUserID();

        ((CustomerAdapter.HomeViewAdapterHolder) holder).TextViewName.setText(activityllist.get(position).getFirst());
        ((CustomerAdapter.HomeViewAdapterHolder) holder).name.setText(activityllist.get(position).getName());
        ((CustomerAdapter.HomeViewAdapterHolder) holder).phone.setText(activityllist.get(position).getPhone1());


        if(!Objects.equals(getCustomerUserID, ((CustomerAdapter.HomeViewAdapterHolder) holder).customerUserID)){
            Log.d("TAG4", "onBindViewHolder: " + DocId  + " " + getCustomerUserID + " " + ((CustomerAdapter.HomeViewAdapterHolder) holder).customerUserID);
            ((CustomerAdapter.HomeViewAdapterHolder) holder).deleteSelect.setVisibility(View.GONE);
        }
        ((CustomerAdapter.HomeViewAdapterHolder) holder).edit_select.setVisibility(View.GONE);


        ((CustomerAdapter.HomeViewAdapterHolder) holder).deleteSelect.setOnClickListener(new View.OnClickListener() {
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
                        customerActivity.ReadDb();


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
        TextView TextViewName, phone, name;
        ImageView deleteSelect, edit_select;
        String customerUserID;

        public HomeViewAdapterHolder(View v) {
            super(v);
            view = v;
            customerUserID = SharedPreferencesUtil.getString(v.getContext(), "customerUserID", "");
            TextViewName = view.findViewById(R.id.time);
            phone = view.findViewById(R.id.phone);
            name = view.findViewById(R.id.name);

            deleteSelect = view.findViewById(R.id.delete_select);
            edit_select = view.findViewById(R.id.edit_select);


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