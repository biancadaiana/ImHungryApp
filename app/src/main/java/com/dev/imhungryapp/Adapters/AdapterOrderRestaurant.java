package com.dev.imhungryapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.imhungryapp.Activities.OrderDetailsAdminActivity;
import com.dev.imhungryapp.Filters.FilterOrderRestaurant;
import com.dev.imhungryapp.Models.ModelOrderRestaurant;
import com.dev.imhungryapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderRestaurant extends RecyclerView.Adapter<AdapterOrderRestaurant.HolderOrderRestaurant> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderRestaurant> orderRestaurantList, filterList;
    private FilterOrderRestaurant filter;

    public AdapterOrderRestaurant(Context context, ArrayList<ModelOrderRestaurant> orderRestaurantList) {
        this.context = context;
        this.orderRestaurantList = orderRestaurantList;
        this.filterList = orderRestaurantList;
    }

    @NonNull
    @Override
    public HolderOrderRestaurant onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_admin, parent, false);
        return new HolderOrderRestaurant(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderRestaurant holder, int position) {
        //get data at position
        ModelOrderRestaurant modelOrderRestaurant = orderRestaurantList.get(position);

        String orderId = modelOrderRestaurant.getOrderId();
        String orderBy = modelOrderRestaurant.getOrderBy();
        String orderCost = modelOrderRestaurant.getOrderCost();
        String orderStatus = modelOrderRestaurant.getOrderStatus();
        String orderTime = modelOrderRestaurant.getOrderTime();
        String orderTo = modelOrderRestaurant.getOrderTo();

        //load user/client info
        loadUserInfo(modelOrderRestaurant, holder);
        //sv...
        //set data
        holder.amountTv.setText("Amount: RON" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Order ID: "+ orderId);
        //change order status color
        if(orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.dark_brown));
        }
        else if(orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.teal_700));
        }
        else if(orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convert time to proper format dd/mm/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.orderDateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details
                Intent intent= new Intent(context, OrderDetailsAdminActivity.class);
                //to load info about the order
                intent.putExtra("orderId", orderId);
                //to load the info about the client that placed the order
                intent.putExtra("orderBy", orderBy);
                context.startActivity(intent);
            }
        });
    }

    private void loadUserInfo(ModelOrderRestaurant modelOrderRestaurant, final HolderOrderRestaurant holder) {
        //to load email of the client: modelOrderRestaurant.getOrderBy() -contains uid of that client
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderRestaurant.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        holder.emailTv.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderRestaurantList.size(); //nr of records
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            //init filter
            filter = new FilterOrderRestaurant(this, filterList);
        }
        return filter;
    }

    //view holder class for row_order_admin.xml
    class HolderOrderRestaurant extends RecyclerView.ViewHolder{

        //ui views of row_order_admin.xml
        private TextView orderIdTv, orderDateTv, emailTv, amountTv, statusTv;
        private ImageView nextIv;

        public HolderOrderRestaurant(@NonNull View itemView) {
            super(itemView);

            //init ui views
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            nextIv = itemView.findViewById(R.id.nextIv);

        }
    }
}
