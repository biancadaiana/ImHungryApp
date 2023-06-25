package com.dev.imhungryapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.imhungryapp.Models.ModelOrderedMenuProduct;
import com.dev.imhungryapp.R;

import java.util.ArrayList;

public class AdapterOrderedMenuP extends RecyclerView.Adapter<AdapterOrderedMenuP.HolderOrderedMenuP> {
    private Context context;
    private ArrayList<ModelOrderedMenuProduct> orderedMenuProductArrayList;

    public AdapterOrderedMenuP(Context context, ArrayList<ModelOrderedMenuProduct> orderedMenuProductArrayList){
        this.context = context;
        this.orderedMenuProductArrayList= orderedMenuProductArrayList;
    }


    @NonNull
    @Override
    public HolderOrderedMenuP onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_menu_ordered, parent, false);
        return new HolderOrderedMenuP(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedMenuP holder, int position) {
        //get data at position
        ModelOrderedMenuProduct orderedMenuProduct= orderedMenuProductArrayList.get(position);
        String getpId= orderedMenuProduct.getpId();
        String name= orderedMenuProduct.getName();
        String cost= orderedMenuProduct.getCost();
        String price= orderedMenuProduct.getPrice();
        String quantity= orderedMenuProduct.getQuantity();

        //set data
        holder.itemTitleTv.setText(name);
        holder.itemPriceEachTv.setText("RON"+price);
        holder.itemPriceTv.setText("RON"+cost);
        holder.itemQuantityTv.setText("["+ quantity + "]");
    }

    @Override
    public int getItemCount() {
        return orderedMenuProductArrayList.size();
    }

    //view holder class
    class HolderOrderedMenuP extends RecyclerView.ViewHolder{
        //views of roe_menu_ordered
        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv;

        public HolderOrderedMenuP(@NonNull View view){
            super(view);

            //init views
            itemTitleTv= view.findViewById(R.id.itemTitleTv);
            itemPriceTv= view.findViewById(R.id.itemPriceTv);
            itemPriceEachTv= view.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv= view.findViewById(R.id.itemQuantityTv);
        }
    }
}
