package com.dev.imhungryapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.imhungryapp.Adapters.AdapterCartItem;
import com.dev.imhungryapp.Models.ModelCartItem;
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
import java.util.HashMap;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class MyCartActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    private Button checkoutBtn;
    private RecyclerView cartItemsRv;
    private TextView sTotalLabelTv;
    private FirebaseAuth firebaseAuth;

    private String phone, address;

    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    public TextView sTotalTv;
    private String restaurantUid;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);

        drawerLayout=findViewById(R.id.drawer);
         cartItemsRv = findViewById(R.id.cartItemsRv);
        sTotalLabelTv = findViewById(R.id.sTotalLabelTv);
         sTotalTv = findViewById(R.id.sTotalTv);
        restaurantUid="nslNOAjM0jUVqUQs3NPpCILDZWL2";
        checkoutBtn=findViewById(R.id.checkoutBtn);

        //init progress dialog
        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Please wait!");
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        loadRestaurantDetails();
        //show cart dialog
        loadMyInfo();
        showCartDialog();
        deleteCartData();
    }

    private void deleteCartData() {
        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();
        easyDB.deleteAllDataFromTable();//delete all data from cart
    }

    public double allTotalPrice = 0.00;
    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();

        //inflate cart layout
        //View view = LayoutInflater.from(this).inflate(R.layout.activity_my_cart, null);
        //init views
         cartItemsRv = findViewById(R.id.cartItemsRv);
         sTotalTv = findViewById(R.id.sTotalTv);
         checkoutBtn = findViewById(R.id.checkoutBtn);

        //dialog
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //set view to dialog
       // builder.setView(view);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
             .doneTableColumn();

        //get all records from db
        Cursor cursor = easyDB.getAllData();
        while (cursor.moveToNext()){
            String id = cursor.getString(1);
            String pId = cursor.getString(2);
            String name = cursor.getString(3);
            String price = cursor.getString(4);
            String cost = cursor.getString(5);
            String quantity = cursor.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );
            cartItemList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this, cartItemList);
        //set to recyclerview
        cartItemsRv.setAdapter(adapterCartItem);

        /// nu ia de aici dFeeTv.setText("RON"+ deliveryFee);
        sTotalTv.setText("RON"+String.format("%.2f", allTotalPrice));
//        allTotalPriceTv.setText("RON" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("RON", ""))));

        //show dialog
        //AlertDialog dialog = builder.create();
        //dialog.show();

        //reset total price on dialog dismiss
//        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                allTotalPrice = 0.00;
//
//            }
//        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate address
                if(address.equals("") || address.equals("null")) {
                    Toast.makeText(MyCartActivity.this, "Please enter your address in profile settings!", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                //phone validation
                if(phone.equals("") || phone.equals("null")){
                    Toast.makeText(MyCartActivity.this, "Please enter your phone in profile settings!", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if(cartItemList.size() == 0){
                    //cart list is empty
                    Toast.makeText(MyCartActivity.this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                submitOrder();
            }
        });
    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing order..");
        progressDialog.show();
        //for order id and order time
        String timestamp = ""+System.currentTimeMillis();
        String cost = sTotalTv.getText().toString().trim().replace("RON", ""); // remove RON if contains

        //setup order data
        HashMap<String, String> hashMap= new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", "In Progress"); //In progress/Completed/Cancelled
        hashMap.put("orderCost", ""+cost);
        hashMap.put("orderBy", ""+firebaseAuth.getUid());
        hashMap.put("orderTo", ""+restaurantUid);

        //add to db
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(restaurantUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added, now add order items
                        for(int i=0; i<cartItemList.size(); i++){
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", pId);
                            hashMap1.put("cost", cost );
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(MyCartActivity.this,"Order Placed Successfully!",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(MyCartActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //show the placed orders, row_order_user.xml for Rl

    public void loadMyInfo() {
        //load user info and set to views
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@android.support.annotation.NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String email = "" + ds.child("email").getValue();
                            String name = "" + ds.child("name").getValue();
                             phone = "" + ds.child("phone").getValue();
                            address = "" + ds.child("address").getValue();
                            //latitude= Double.parseDouble(""+ds.child("latitude").getValue());
                            //longitude= Double.parseDouble(""+ds.child("longitude").getValue());
                            String profileImage = "" + ds.child("profileImage").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@android.support.annotation.NonNull DatabaseError error) {

                    }
                });

    }


    private void loadRestaurantDetails() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(restaurantUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get restaurant data

                //deliveryFee = ""+snapshot.child("deliveryFee").getValue();

                //set data
                //dFeeTv.setText("RON"+ deliveryFee);
                sTotalTv.setText("RON" + allTotalPrice);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void ClickMenu(View view){
        MainActivity.openDrawer(drawerLayout);
    }
    public void ClickLogo(View view){
        MainActivity.closeDrawer(drawerLayout);
    }
    public void Clickhome(View view){
        MainActivity.redirectActivity(this, MainActivity.class);
    }
    public void ClickSettings(View view){
        MainActivity.redirectActivity(this, SettingsActivity.class);
    }
    public void ClickMap(View view){
        MainActivity.redirectActivity(this, ViewMapActivity.class);
    }
    public void ClickFavourites(View view){
        MainActivity.redirectActivity(this, FavouritesActivity.class);
    }
    public void ClickMycart(View view){
        recreate();
    }
    public void ClickMyorders(View view){
        MainActivity.redirectActivity(this, MyOrdersActivity.class);
    }
    public void ClickLogout(View view){
        //MainActivity.redirectActivity(this, SigninActivity.class);
        MainActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.closeDrawer(drawerLayout);
    }
}