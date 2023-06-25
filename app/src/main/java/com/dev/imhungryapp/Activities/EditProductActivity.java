package com.dev.imhungryapp.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dev.imhungryapp.Constants.Constants;
import com.dev.imhungryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descriptionEt, quantityEt, priceEt, discountedPriceEt, discountedNoteEt;
    private TextView categoryTv;
    private SwitchCompat discountSw;
    private Button updateProductBtn;

    private String productId;

    //permission constants
    private static final int CAMERA_REQUEST_CODE= 200;
    private static final int STORAGE_REQUEST_CODE= 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int IMAGE_PICK_CAMERA_CODE=500;
    //permission arrays
    private String[] cameraPermission;
    private String[] storagePermission;
    //image uri
    private Uri image_uri;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<PickVisualMediaRequest> pickImage;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //init ui views
        backBtn= findViewById(R.id.backBtn);
        productIconIv= findViewById(R.id.productIconIv);
        titleEt= findViewById(R.id.titleEt);
        descriptionEt= findViewById(R.id.descriptionEt);
        quantityEt= findViewById(R.id.quantityEt);
        priceEt= findViewById(R.id.priceEt);
        discountedPriceEt= findViewById(R.id.discountedPriceEt);
        categoryTv= findViewById(R.id.categoryTv);
        discountSw= findViewById(R.id.discountSw);
        discountedNoteEt= findViewById(R.id.discountedNoteEt);
        updateProductBtn= findViewById(R.id.updateProductBtn);

        //get the product id from intent
        productId= getIntent().getStringExtra("productId");

        //on start is unchecked, so hide them
        discountedPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        firebaseAuth= FirebaseAuth.getInstance();
        loadProductDetails(); //to set on views

        //setup progress dialog
        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Please wait!");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permission arrays
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};

        pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->{

        });


        //if discountSw is checked- show discountPriceEt, discountNoteEt | else- hide them
        discountSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    // show discountPriceEt, discountNoteEt
                    discountedPriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);

                }else {
                    //hide discountPriceEt, discountNoteEt
                    discountedPriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);

                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });

        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick category
                categoryDialog();
            }
        });

        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1-update data, 2-validate data, 3-update data to db
                inputData();
            }
        });

        activityResultLauncher= registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        //handle image pick result
                        if(result.getResultCode() == RESULT_OK){
                            if(result.getResultCode() == IMAGE_PICK_GALLERY_CODE){
                                //picked from gallery
                                image_uri=result.getData().getData();
                                try {
                                    InputStream inputStream=getContentResolver().openInputStream(image_uri);
                                    Bitmap image_uri_sel = BitmapFactory.decodeStream(inputStream);
                                    //ImageView imageView = findViewById(R.id.imgprofile);
                                    productIconIv.setImageBitmap(image_uri_sel);
                                }catch (FileNotFoundException e){
                                    e.printStackTrace();
                                }
//                                //set to imageview
//                                productIconIv.setImageURI(image_uri);

                            }
                            else if(result.getResultCode() ==IMAGE_PICK_CAMERA_CODE){
                                image_uri=result.getData().getData();
                                try {
                                    InputStream inputStream=getContentResolver().openInputStream(image_uri);
                                    Bitmap image_uri_sel = BitmapFactory.decodeStream(inputStream);
                                    //ImageView imageView = findViewById(R.id.imgprofile);
                                    productIconIv.setImageBitmap(image_uri_sel);
                                }catch (FileNotFoundException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });



    }

    private void loadProductDetails() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                        //get data
                        String productId=""+snapshot.child("productId").getValue();
                        String productTitle=""+snapshot.child("productTitle").getValue();
                        String productDescription=""+snapshot.child("productDescription").getValue();
                        String productCategory=""+snapshot.child("productCategory").getValue();
                        String productQuantity=""+snapshot.child("productQuantity").getValue();
                        String productIcon=""+snapshot.child("productIcon").getValue();
                        String originalPrice=""+snapshot.child("originalPrice").getValue();
                        String discountPrice=""+snapshot.child("discountPrice").getValue();
                        String discountNote=""+snapshot.child("discountNote").getValue();
                        String discountAvailable=""+snapshot.child("discountAvailable").getValue();
                        String timestamp=""+snapshot.child("timestamp").getValue();
                        String uid=""+snapshot.child("uid").getValue();

                        //set data to views
                        if(discountAvailable.equals("true")){
                            discountSw.setChecked(true);

                            discountedPriceEt.setVisibility(View.VISIBLE);
                            discountedNoteEt.setVisibility(View.VISIBLE);
                        }else{
                            discountSw.setChecked(false);

                            discountedPriceEt.setVisibility(View.GONE);
                            discountedNoteEt.setVisibility(View.GONE);
                        }
                        titleEt.setText(productTitle);
                        descriptionEt.setText(productDescription);
                        categoryTv.setText(productCategory);
                        discountedNoteEt.setText(discountNote);
                        quantityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountedPriceEt.setText(discountPrice);
                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_baseline_add_shopping_cart_brown).into(productIconIv);
                        }
                        catch (Exception e){
                            productIconIv.setImageResource(R.drawable.ic_baseline_add_shopping_cart_brown);
                        }

                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                    }
                });
    }


    private String pTitle, pDescription, pQuantity, pCategory, originalPrice, discountPrice, discountNote;
    private  boolean discountAvailable = false;

    private void inputData() {
        //input data
        pTitle= titleEt.getText().toString().trim();
        pDescription= descriptionEt.getText().toString().trim();
        pQuantity= quantityEt.getText().toString().trim();
        pCategory= categoryTv.getText().toString().trim();
        originalPrice= priceEt.getText().toString().trim();
        discountAvailable= discountSw.isChecked(); // true/false

        //validate data
        if(TextUtils.isEmpty(pTitle)){
            Toast.makeText(this, "Title is required..", Toast.LENGTH_SHORT).show();
            return; //do not proceed further
        }
        if(TextUtils.isEmpty(pCategory)){
            Toast.makeText(this, "Category is required..", Toast.LENGTH_SHORT).show();
            return; //do not proceed further
        }
        if(TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price is required..", Toast.LENGTH_SHORT).show();
            return; //do not proceed further
        }
        if(discountAvailable){
            //product has discount
            discountPrice= discountedPriceEt.getText().toString().trim();
            discountNote= discountedNoteEt.getText().toString().trim();
            if(TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "Discount Price is required..", Toast.LENGTH_SHORT).show();
                return; //do not proceed further
            }
        } else {
            //product is without discount
            discountPrice= "0";
            discountNote= "";
        }

        updateProduct();

    }

    private void updateProduct() {
        //show progress
        progressDialog.setMessage("Updating product..");
        progressDialog.show();

        if(image_uri == null){
            //update without image
            //setup data in hashmap to update
            HashMap<String, Object> hashMap= new HashMap<>();
            hashMap.put("pTitle", "" + pTitle);
            hashMap.put("productDescription", "" + pDescription);
            hashMap.put("productCategory", "" + pCategory);
            hashMap.put("productQuantity", "" + pQuantity);
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);
            //update to db
            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updated to db successfully
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Product updated..", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {
                            //failed updating to db
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


        }else {
            //update with image
            //first upload image
            //name and path of image to uploaded
            String filePathAndName= "product_images/"+ "" + productId; //override previous image using same id
            //upload image
            StorageReference storageReference= FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded , get its url
                            Task<Uri> uriTask= taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if(uriTask.isSuccessful()){
                                //url of image received , upload to db

                                //setup data to upload
                                HashMap<String, Object> hashMap= new HashMap<>();
                                hashMap.put("pTitle", "" + pTitle);
                                hashMap.put("productDescription", "" + pDescription);
                                hashMap.put("productCategory", "" + pCategory);
                                hashMap.put("productIcon", "" + downloadImageUri);
                                hashMap.put("productQuantity", "" + pQuantity);
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                //update to db
                                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users");
                                databaseReference.child(firebaseAuth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //added to db
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Product updated..", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                                //failed uploading to db
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });


                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {
                            //failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get picked category
                        String category= Constants.productCategories[which];
                        //set picked category
                        categoryTv.setText(category);

                    }
                })
                .show();
    }


    private void showImagePickDialog() {
        //options to display in dialog
        String[] options={"Gallery"};
        //dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Pick image: ")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if(which==0){
                            //camera clicked
                            if(checkCameraPermission()){
                                //allowed, open camera
                                pickFromGallery();
                            }else{
                                //not allowed, request
                                requestCameraPermission();
                            }
                        }
//                        else {
//                            //gallery clicked
//                            if (checkStoragePermission()){
//                                //allowed, open gallery
//                                pickFromGallery();
//                            }else {
//                                //not allowed, request
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                                    requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
//                                }
//                                requestStoragePermission();
//                            }
//                        }
                    }
                })
                .show();
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

//    private void  pickFromCamera(){
//        ContentValues contentValues= new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");
//
//        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//
//        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
//        //activityResultLauncher.launch(intent);
//        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
//    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);

    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
//                if(grantResults.length>0){
//                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    if(cameraAccepted && storageAccepted){
//                        //permission allowed
//                        pickFromCamera();
//                    }else {
//                        //permission denied
//                        Toast.makeText(this, "Camera and Storage permissions are necessary!", Toast.LENGTH_SHORT).show();
//
//                    }
//                }
            }

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //permission allowed
                        pickFromGallery();
                    }else {
                        //permission denied
                        Toast.makeText(this, "Storage permission is necessary!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //handle image pick result
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //picked from gallery
                image_uri = data.getData();
                //set to imageview
                productIconIv.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                productIconIv.setImageURI(image_uri);
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}