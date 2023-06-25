package com.dev.imhungryapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.imhungryapp.Adapters.AdapterOrderedMenuP;
import com.dev.imhungryapp.Models.ModelOrderedMenuProduct;
import com.dev.imhungryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class OrderDetailsAdminActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedMenuProduct> orderedMenuProductsArrayList;
    private AdapterOrderedMenuP adapterOrderedMenuP;

    //ui views
    private ImageButton backBtn, editBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, emailTv, phoneTv, totalMenuTv, amountTv, addressTv;
    private RecyclerView menuRv;

    String orderId, orderBy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_admin);

        //init ui views
        backBtn= findViewById(R.id.backBtn);
        editBtn= findViewById(R.id.editBtn);
        orderIdTv= findViewById(R.id.orderIdTv);
        dateTv= findViewById(R.id.dateTv);
        orderStatusTv= findViewById(R.id.orderStatusTv);
        emailTv= findViewById(R.id.emailTv);
        phoneTv= findViewById(R.id.phoneTv);
        totalMenuTv= findViewById(R.id.totalMenuTv);
        amountTv= findViewById(R.id.amountTv);
        addressTv= findViewById(R.id.addressTv);
        menuRv= findViewById(R.id.menuRv);

        //get data from intent
        orderId= getIntent().getStringExtra("orderId");
        orderBy= getIntent().getStringExtra("orderBy");

        firebaseAuth= FirebaseAuth.getInstance();
        //loadMyInfo();
        loadClientInfo();
        loadOrderDetails();
        loadOrderedMenu();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to go back
                onBackPressed();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement the set/edit status order button(In Progress, Completed, Cancelled)
                setOrderStatusDialog();
            }
        });

    }

    private void setOrderStatusDialog() {
        //a dialog with options to be displayed
        String[] options = {"In Progress", "Completed", "Cancelled"};
        //the dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Set Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        String statusOption = options[which];
                        setOrderStatus(statusOption);
                    }
                })
                .show();
    }

    private void setOrderStatus(String statusOption) {
        //setup data in order to upload in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+statusOption);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child("orderId")
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //the status has been updates successfully
                        Toast.makeText(OrderDetailsAdminActivity.this, "The order is now "+statusOption, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //fail on updating the status
                        Toast.makeText(OrderDetailsAdminActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //sourceLatitude= ""+ snapshot.child("latitude").getValue();
                        //sourceLongitude= ""+snapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadClientInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       // sourceLatitude= ""+ snapshot.child("latitude").getValue();
                        //sourceLongitude= ""+snapshot.child("longitude").getValue();
                        String email= ""+snapshot.child("email").getValue();
                        String phone= ""+snapshot.child("phone").getValue();

                        //set information
                        emailTv.setText(email);
                        phoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
//                        startActivity(new Intent(OrderDetailsAdminActivity.this, RegistrationActivity.class));

                    }
                });
    }

    private void loadOrderDetails() {
        //based on order id, load info about the order
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //get order information
                    String orderBy= ""+snapshot.child("orderBy").getValue();
                    String orderCost= ""+snapshot.child("orderCost").getValue();
                    String orderId= ""+snapshot.child("orderId").getValue();
                    String orderStatus= ""+snapshot.child("orderStatus").getValue();
                    String orderTime= ""+snapshot.child("orderTime").getValue();
                    String orderTo= ""+snapshot.child("orderTo").getValue();
                    String deliveryFee= ""+snapshot.child("deliveryFee").getValue();

                    //convert timestamp
                        Calendar calendar= Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormated= DateFormat.format("dd/MM/yyyy", calendar).toString();

                    //order status
                    if(orderStatus.equals("In Progress")){
                        orderStatusTv.setTextColor(getResources().getColor(R.color.dark_brown));
                        }else if(orderStatus.equals("Completed")){
                         orderStatusTv.setTextColor(getResources().getColor(R.color.teal_700));
                        }else if(orderStatus.equals("Cancelled")){
                        orderStatusTv.setTextColor(getResources().getColor(R.color.red));
                        }
                    //set data
                    orderIdTv.setText(orderId);
                    orderStatusTv.setText(orderStatus);
                    amountTv.setText("RON"+orderCost);
                    dateTv.setText(dateFormated);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private  void loadOrderedMenu(){
        //load all the menu products of the order
        //init list
        orderedMenuProductsArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear the list before adding data
                        orderedMenuProductsArrayList.clear();
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            ModelOrderedMenuProduct modelOrderedMenuProduct= dataSnapshot.getValue(ModelOrderedMenuProduct.class);
                            //add into list
                            orderedMenuProductsArrayList.add(modelOrderedMenuProduct);
                        }
                        //setup adapter
                        adapterOrderedMenuP = new AdapterOrderedMenuP(OrderDetailsAdminActivity.this,orderedMenuProductsArrayList);
                        //set adapter to RV
                        menuRv.setAdapter(adapterOrderedMenuP);
                        //set the total nr of menu products from the order
                        totalMenuTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}