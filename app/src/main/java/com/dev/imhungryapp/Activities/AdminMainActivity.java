package com.dev.imhungryapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.imhungryapp.Adapters.AdapterOrderRestaurant;
import com.dev.imhungryapp.Adapters.AdapterProductAdmin;
import com.dev.imhungryapp.Constants.Constants;
import com.dev.imhungryapp.Models.ModelOrderRestaurant;
import com.dev.imhungryapp.Models.ModelProduct;
import com.dev.imhungryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminMainActivity extends AppCompatActivity {

    private TextView nameapp, emailadmin, tabProductTv, tabOrdersTv, filteredProductsTv, filteredOrdersTv;
    private ImageButton logoutBtn, editBtn, addBtn, filterProductBtn, filterOrderBtn;
    private RelativeLayout productsRl, ordersRl;
    private EditText searchProductEt;
    private RecyclerView productsRv, ordersRv;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductAdmin adapterProductAdmin;

    private ArrayList<ModelOrderRestaurant> orderRestaurantList;
    private AdapterOrderRestaurant adapterOrderRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        nameapp=findViewById(R.id.nameapp);
        emailadmin=findViewById(R.id.emailapp);
        logoutBtn=findViewById(R.id.logoutBtn);
//        editBtn=findViewById(R.id.editBtn);
        addBtn=findViewById(R.id.addBtn);
        tabProductTv=findViewById(R.id.tabProductsTv);
        tabOrdersTv=findViewById(R.id.tabOrdersTv);
        productsRl=findViewById(R.id.productsRl);
        ordersRl=findViewById(R.id.ordersRl);
        searchProductEt=findViewById(R.id.searchProductEt);
        productsRv=findViewById(R.id.productsRv);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        filteredProductsTv=findViewById(R.id.filteredProductsTv);
        filteredOrdersTv=findViewById(R.id.filteredOrdersTv);
        filterOrderBtn=findViewById(R.id.filterOrderBtn);
        ordersRv=findViewById(R.id.ordersRv);


        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Please wait!");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();
        showProductsUI();

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    adapterProductAdmin.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();
                startActivity(new Intent(AdminMainActivity.this, SigninActivity.class));
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open edit add product activity
                startActivity(new Intent(AdminMainActivity.this, AddProductActivity.class));
            }
        });

        tabProductTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showProductsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();
            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminMainActivity.this);
                builder.setTitle("Choose Category: ")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //get selected item
                                String selected = Constants.productCategories1[which];
                                filteredProductsTv.setText(selected);
                                if(selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }else {
                                    //load filtered
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //options to display in dialog
                String[] options = {"All", "In Progress", "Completed", "Cancelled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminMainActivity.this);
                builder.setTitle("Filter Orders: ")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handel item clicks
                                if(which==0){
                                    //All clicked
                                    filteredOrdersTv.setText("Show All Orders");
                                    adapterOrderRestaurant.getFilter().filter(""); //show all orders
                                }
                                else {
                                    String optionClicked = options[which];
                                    filteredOrdersTv.setText("Show "+ optionClicked + " Orders");
                                    adapterOrderRestaurant.getFilter().filter(optionClicked);
                                }
                            }
                        })
                        .show();
            }
        });

    }

    private void loadAllOrders() {
        //init array list
        orderRestaurantList =new ArrayList<>();

        //load orders of restaurant
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding new data in it
                        orderRestaurantList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderRestaurant modelOrderRestaurant = ds.getValue(ModelOrderRestaurant.class);
                            //add to list
                            orderRestaurantList.add(modelOrderRestaurant);
                        }
                        //setup adapter
                        adapterOrderRestaurant =new AdapterOrderRestaurant(AdminMainActivity.this, orderRestaurantList);
                        //set adapter to rv
                        ordersRv.setAdapter(adapterOrderRestaurant);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }



    private void loadFilteredProducts(String selected) {
        productList= new ArrayList<>();
        //get all products
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();

                        for( DataSnapshot dataSnapshot : snapshot.getChildren()){

                            String productCategory = ""+dataSnapshot.child("productCategory").getValue();
                            //if selected category matches product category then add in list
                            if(selected.equals(productCategory)){
                                ModelProduct modelProduct = dataSnapshot.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        //setup adapter
                        adapterProductAdmin= new AdapterProductAdmin(AdminMainActivity.this, productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadAllProducts() {

        productList= new ArrayList<>();
        //get all products
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();

                       for( DataSnapshot dataSnapshot : snapshot.getChildren()){
                           ModelProduct modelProduct = dataSnapshot.getValue(ModelProduct.class);
                           productList.add(modelProduct);
                       }
                       //setup adapter
                        adapterProductAdmin= new AdapterProductAdmin(AdminMainActivity.this, productList);
                       //set adapter
                        productsRv.setAdapter(adapterProductAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void showProductsUI() {
        //show products UI and hide orders UI
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabProductTv.setTextColor(getResources().getColor(R.color.dark_brown));
        tabProductTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders UI and hide products UI
        ordersRl.setVisibility(View.VISIBLE);
        productsRl.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.dark_brown));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);

        tabProductTv.setTextColor(getResources().getColor(R.color.white));
        tabProductTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makeMeOffline() {
        progressDialog.setMessage("Logging out..");
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("online", "false");

        //update value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AdminMainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void loadMyInfo() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            //get data from db
                            String email=""+ds.child("email").getValue();
                            String address=""+ds.child("address").getValue();
                            String phone= ""+ds.child("phone").getValue();
                            //set data to ui
                            emailadmin.setText(email);

//                            try {
//
//                            }catch (Exception e){
//
//                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkUser() {
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(AdminMainActivity.this, SigninActivity.class));
            finish();
        }else{
            loadMyInfo();
        }
    }




}