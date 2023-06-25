package com.dev.imhungryapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.imhungryapp.Activities.EditProductActivity;
import com.dev.imhungryapp.Filters.FilterProduct;
import com.dev.imhungryapp.Models.ModelProduct;
import com.dev.imhungryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductAdmin extends RecyclerView.Adapter<AdapterProductAdmin.HolderProductAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList, filterList;
    private FilterProduct filter;

    public AdapterProductAdmin(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_product_admin, parent, false);
        return new HolderProductAdmin(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductAdmin holder, int position) {
        //get data
        final ModelProduct modelProduct=productList.get(position);
        String id= modelProduct.getProductId();
        String uid= modelProduct.getUid();
        String discountAvailable= modelProduct.getDiscountAvailable();
        String discountNote= modelProduct.getDiscountNote();
        String discountPrice= modelProduct.getDiscountPrice();
        String productCategory= modelProduct.getProductCategory();
        String productDescription= modelProduct.getProductDescription();
        String icon= modelProduct.getProductIcon();
        String quantity= modelProduct.getProductQuantity();
        String title= modelProduct.getProductTitle();
        String originalPrice= modelProduct.getOriginalPrice();
        String timestamp= modelProduct.getTimestamp();

        //set data
        holder.titleTv.setText(title);
        holder.quantityTv.setText(quantity);
        holder.discountedNoteTv.setText(discountNote);
        holder.discountedPriceTv.setText(discountPrice + "RON");
        holder.originalPriceTv.setText(originalPrice + "RON");

        if(discountAvailable.equals("true")){
            //product on discount
            holder.discountedPriceTv.setVisibility(View.VISIBLE);
            holder.discountedNoteTv.setVisibility(View.VISIBLE);
            holder.originalPriceTv.setPaintFlags(holder.originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike to on original price

        }else {
                    //product with original price
                    holder.discountedPriceTv.setVisibility(View.GONE);
                    holder.discountedNoteTv.setVisibility(View.GONE);
                    holder.originalPriceTv.setPaintFlags(0);

        } try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_brown).into(holder.productIconIv);
        }catch (Exception e){
                    holder.productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_brown);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks, show item detailes in bottom sheet
                detailesBottomSheet(modelProduct);
            }
        });

    }

    private void detailesBottomSheet(ModelProduct modelProduct) {
        //bs
        BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(context);
        //inflate view for bs
        View view= LayoutInflater.from(context).inflate(R.layout.bs_product_detailes_admin, null);
        //set view to bs
        bottomSheetDialog.setContentView(view);

        //init views of bs
        ImageButton backBtn= view.findViewById(R.id.backBtn);
        ImageButton deleteBtn= view.findViewById(R.id.deleteBtn);
        ImageButton editBtn= view.findViewById(R.id.editBtn);
        ImageView productIconIv= view.findViewById(R.id.productIconIv);
        TextView discountedNoteTv= view.findViewById(R.id.discountedNoteTv);
        TextView titleTv= view.findViewById(R.id.titleTv);
        TextView descriptionTv= view.findViewById(R.id.descriptionTv);
        TextView categoryTv= view.findViewById(R.id.categoryTv);
        TextView quantityTv= view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv= view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv= view.findViewById(R.id.originalPriceTv);

        //get data
        String id= modelProduct.getProductId();
        String uid= modelProduct.getUid();
        String discountAvailable= modelProduct.getDiscountAvailable();
        String discountNote= modelProduct.getDiscountNote();
        String discountPrice= modelProduct.getDiscountPrice();
        String productCategory= modelProduct.getProductCategory();
        String productDescription= modelProduct.getProductDescription();
        String icon= modelProduct.getProductIcon();
        String quantity= modelProduct.getProductQuantity();
        String title= modelProduct.getProductTitle();
        String originalPrice= modelProduct.getOriginalPrice();
        String timestamp= modelProduct.getTimestamp();

        //set data
        titleTv.setText(title);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(quantity);
        discountedNoteTv.setText(discountNote);
        discountedPriceTv.setText("RON"+discountPrice);
        originalPriceTv.setText("RON"+originalPrice);
        if(discountAvailable.equals("true")){
            //product on discount
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountedNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike to on original price

        }else {
            //product with original price
            discountedPriceTv.setVisibility(View.GONE);
            discountedNoteTv.setVisibility(View.GONE);

        }try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_baseline_add_shopping_cart_white).into(productIconIv);
        }catch (Exception e){
            productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_white);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //open edit product activity, pass id of the product
                Intent intent= new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", id);
                context.startActivity(intent);

            }
        });
        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete the product? ")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            //delete
                                deleteProduct(id); //product id
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            //cancel, dismiss dialog
                            }
                        }).show();
            }
        });
        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dismiss bs
                bottomSheetDialog.dismiss();
            }
        });

    }

    private void deleteProduct(String id) {
        //delete the product using the id
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //product delete
                        Toast.makeText(context, "Product deleted..", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting product
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }

    class HolderProductAdmin extends RecyclerView.ViewHolder{
        // holds view  of recyclerview

        private ImageView productIconIv;
        private TextView discountedNoteTv, titleTv, quantityTv, discountedPriceTv, originalPriceTv;

        public HolderProductAdmin(@NonNull View itemView) {
            super(itemView);

            productIconIv= itemView.findViewById(R.id.productIconIv);
            discountedNoteTv= itemView.findViewById(R.id.discountedNoteTv);
            titleTv= itemView.findViewById(R.id.titleTv);
            quantityTv= itemView.findViewById(R.id.quantityTv);
            discountedPriceTv= itemView.findViewById(R.id.discountedPriceTv);
            originalPriceTv= itemView.findViewById(R.id.originalPriceTv);


        }
    }
}
