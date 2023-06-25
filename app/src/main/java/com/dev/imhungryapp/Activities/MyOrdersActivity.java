package com.dev.imhungryapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.dev.imhungryapp.Adapters.AdapterOrderUser;
import com.dev.imhungryapp.Models.ModelOrderUser;
import com.dev.imhungryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyOrdersActivity extends AppCompatActivity {

    RelativeLayout ordersRl;
    RecyclerView ordersRv;
    private ArrayList<ModelOrderUser> ordersList;
    private AdapterOrderUser adapterOrderUser;
    FirebaseAuth firebaseAuth;


    DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        drawerLayout=findViewById(R.id.drawer);
        ordersRl=findViewById(R.id.ordersRl);
        ordersRv=findViewById(R.id.ordersRv);
        firebaseAuth = FirebaseAuth.getInstance();
        
        loadOrders();
    }

    private void loadOrders() {
        //init order list
        ordersList =new ArrayList<>();

        //get orders
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            ordersList.add(modelOrderUser);

                                        }
                                        //setup adapter
                                        adapterOrderUser =new AdapterOrderUser(MyOrdersActivity.this, ordersList);
                                        //set to rv
                                        ordersRv.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }

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
        MainActivity.redirectActivity(this, MyCartActivity.class);
    }
    public void ClickMyorders(View view){
        recreate();
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