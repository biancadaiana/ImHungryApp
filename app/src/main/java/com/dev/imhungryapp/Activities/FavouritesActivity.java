package com.dev.imhungryapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.dev.imhungryapp.Adapters.AdapterFavourites;
import com.dev.imhungryapp.Models.ModelProduct;
import com.dev.imhungryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavouritesActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    private String restaurantUid;
    private RecyclerView productsRv;
    FirebaseAuth firebaseAuth;
    private ArrayList<ModelProduct> productArrayList;
    private AdapterFavourites adapterFavourites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        drawerLayout=findViewById(R.id.drawer);
        restaurantUid="nslNOAjM0jUVqUQs3NPpCILDZWL2";
        firebaseAuth= FirebaseAuth.getInstance();

        productsRv=findViewById(R.id.productsRv);

        restaurantUid="nslNOAjM0jUVqUQs3NPpCILDZWL2";
        firebaseAuth= FirebaseAuth.getInstance();

        loadRestaurantProducts();
    }


    private void loadRestaurantProducts() {
        //init list
        productArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(restaurantUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        productArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productArrayList.add(modelProduct);
                        }
                        //setup adapter
                        adapterFavourites = new AdapterFavourites(FavouritesActivity.this, productArrayList);
                        //set adapter
                        productsRv.setAdapter(adapterFavourites);
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
        recreate();
    }
    public void ClickMycart(View view){
        MainActivity.redirectActivity(this, MyCartActivity.class);
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