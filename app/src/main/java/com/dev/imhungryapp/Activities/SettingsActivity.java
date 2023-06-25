package com.dev.imhungryapp.Activities;

import static android.content.ContentValues.TAG;
import static androidx.activity.result.contract.ActivityResultContracts.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dev.imhungryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements LocationListener {
    DrawerLayout drawerLayout;

    private ImageView imgprofile;
    private EditText nameEt, emailEt, phoneEt, addressEt;
    private Button updateB;
    private ImageView gpsB;
    private SwitchCompat shopOpenSwitch;

    //permision constants
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    //permission arrays
    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;
    //image uri
    private Uri image_uri;

    private double latitude = 0.0;
    private double longitude = 0.0;

    //progress dialog
    private ProgressDialog progressDialog;
    //firebase auth
    private FirebaseAuth firebaseAuth;

    private LocationManager locationManager;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<PickVisualMediaRequest> pickImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        drawerLayout = findViewById(R.id.drawer);


        gpsB = findViewById(R.id.gpsB);
        imgprofile = findViewById(R.id.imgprofile);
        nameEt = findViewById(R.id.nameEt);
        emailEt = findViewById(R.id.emailEt);
        phoneEt = findViewById(R.id.phoneEt);
        addressEt = findViewById(R.id.addressEt);
        updateB = findViewById(R.id.updateB);

        //init permission arrays
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        pickImage = registerForActivityResult(new PickVisualMedia(), uri ->{

        });

//        activityResultLauncher = registerForActivityResult(new StartActivityForResult(),
//                result -> {
//                    //handle image pick result
//                    if (result.getResultCode() == RESULT_OK) {
//                        if (result.getResultCode() == IMAGE_PICK_GALLERY_CODE) {
//                            //picked from gallery
//                            image_uri = result.getData().getData();
//                            //set to imageview
//                            imgprofile.setImageURI(image_uri);
//
//                        } else if (result.getResultCode() == IMAGE_PICK_CAMERA_CODE) {
//                            imgprofile.setImageURI(image_uri);
//                        }
//                    }
//                });



        imgprofile.setOnClickListener(v -> {
                    //check runtime permissions
//                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
//                        if(checkSelfPermission(Manifest.permission.CAMERA)
//                        ==PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_DENIED){
//                            //perm not granted, request it
//                            String[] permissions= {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};
//                            //show popup for runtime perm
//                            requestPermissions(permissions, STORAGE_REQUEST_CODE);
//                        }
//                    }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//                        try {
//                            Log.d(TAG, "Request permission: try");
//                            Intent intent= new Intent();
//                            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                            image_uri= Uri.fromParts("package", this.getPackageName(), null);
//                            intent.setData(image_uri);
//                            activityResultLauncher.launch(intent);
//                        }
//                        catch (Exception e){
//                            Log.e(TAG, "Request permission: catch", e);
//                            Intent intent= new Intent();
//                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                            activityResultLauncher.launch(intent);
//
//                        }
//                    }

            //pick image
            showImagePickDialog();
            //pickFromGallery();
        });


        gpsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();

                } else {
                    //not allowed, request
                    requestLocationPermission();

                }

            }
        });

        updateB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin profile update
                inputData();
            }
        });

        //return v;

    }

    private String name, phone, address, email;

    private void inputData() {

        email = emailEt.getText().toString().trim();
        name = nameEt.getText().toString().trim();
        phone = phoneEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();

        updateProfile();

    }

    private void updateProfile() {
        progressDialog.setMessage("Updating profile.. ");
        progressDialog.show();

        if (image_uri == null) {
            //update without image

            //setup data to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + name);
            hashMap.put("phone", "" + phone);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);

            //update to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updated
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {
                            //failed to update
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            //update with image
            //------Upload image first------
            String filePathAndName = "profile_images/" + "" + firebaseAuth.getUid();
            //get storage ref
            StorageReference storageRef = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageRef.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url of it
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadedImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //image url received, now update db

                                //setup data to update
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("email", "" + email);
                                hashMap.put("name", "" + name);
                                hashMap.put("phone", "" + phone);
                                hashMap.put("address", "" + address);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("profileImage", "" + downloadedImageUri);

                                //update to db
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updated
                                                progressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                                //failed to update
                                                progressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@androidx.annotation.NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void ClickMenu(View view) {
        MainActivity.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view) {
        MainActivity.closeDrawer(drawerLayout);
    }

    public void Clickhome(View view) {
        MainActivity.redirectActivity(this, MainActivity.class);
    }

    public void ClickSettings(View view) {
        recreate();
    }

    public void ClickMap(View view) {
        MainActivity.redirectActivity(this, ViewMapActivity.class);
    }

    public void ClickFavourites(View view) {
        MainActivity.redirectActivity(this, FavouritesActivity.class);
    }

    public void ClickMycart(View view) {
        MainActivity.redirectActivity(this, MyCartActivity.class);
    }

    public void ClickMyorders(View view) {
        MainActivity.redirectActivity(this, MyOrdersActivity.class);
    }

    public void ClickLogout(View view) {
        //MainActivity.redirectActivity(this, SigninActivity.class);
        MainActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.closeDrawer(drawerLayout);
    }


    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), SigninActivity.class));
            finish();
        } else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        //load user info and set to views
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String email = "" + ds.child("email").getValue();
                            String name = "" + ds.child("name").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String address = "" + ds.child("address").getValue();
                            //latitude= Double.parseDouble(""+ds.child("latitude").getValue());
                            //longitude= Double.parseDouble(""+ds.child("longitude").getValue());
                            String profileImage = "" + ds.child("profileImage").getValue();

                            emailEt.setText(email);
                            nameEt.setText(name);
                            phoneEt.setText(phone);
                            addressEt.setText(address);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_baseline_account_circle_gray).into(imgprofile);

                            } catch (Exception e) {
                                imgprofile.setImageResource(R.drawable.ic_baseline_account_circle_gray);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Media"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick the image: ")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item clicks
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                //allowed, open camera
                                pickFromGallery();
                            } else {
                                //not allowed, request
                                requestCameraPermission();
                            }
                        }
//                        } else {
//                            //gallery clicked
//                            if (checkStoragePermission()) {
//                                //allowed, open gallery
//                                pickFromGallery();
//                            } else {
//                                //not allowed, request
//                                requestStoragePermission();
//                            }
//                        }
                    }
                })
                .show();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);

    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }



    private boolean checkStoragePermission() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
//        {
//            return Environment.isExternalStorageManager();
//        }else {
//            boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
//                    (PackageManager.PERMISSION_GRANTED);
//            boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED);
//            return result && result2;
//        }
        boolean result =ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromGallery() {
       // intent to pick image from gallery
//        pickImage.launch(new PickVisualMediaRequest.Builder()
//                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
//                .build());


//       Intent intent = new Intent(Intent.ACTION_PICK);
//
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//        intent.setType("image/*");
//
//       activityResultLauncher.launch(intent);
       // startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

//    private void pickFromMedia() {
//        //intent to pick image from media
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE, "Image Title");
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image Description");
//
//        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
//
//        //activityResultLauncher.launch(intent);
//        //startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
//    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait..", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }


    private void findAddress(){
        //find address
        Geocoder geocoder;
        List<Address> addresses;
        geocoder= new Geocoder(this, Locale.getDefault() );

        try{
            addresses= geocoder.getFromLocation(latitude,longitude, 1);
            String address= addresses.get(0).getAddressLine(0);
//            String city= addresses.get(0).getLocality();
//            String state= addresses.get(0).getAdminArea();
//            String country= addresses.get(0).getCountryName();

            //set addresses
            addressEt.setText(address);
            //..contry state city

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude= location.getLatitude();
        longitude= location.getLongitude();
        findAddress();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);

        Toast.makeText(this, "Location is disabled!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean locationAccepted= grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(locationAccepted){
                        //permission allowed
                        detectLocation();
                    }
                    else{
                        //permission denied
                        Toast.makeText(this, "Location permission is necessary!", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            case CAMERA_REQUEST_CODE:{
                //permission is allowed

//                if(grantResults.length>0){
//                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
////                    if(cameraAccepted && storageAccepted){
////                        //permission allowed
////                        //pickFromMedia();
////                    }else {
////                        //permission denied
////                        Toast.makeText(this, "Camera permission are necessary!", Toast.LENGTH_SHORT).show();
////
////                    }
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
                imgprofile.setImageURI(image_uri);

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                imgprofile.setImageURI(image_uri);
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    //    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//        //handle image pick result
//        if(resultCode == RESULT_OK){
//            if(requestCode == IMAGE_PICK_GALLERY_CODE){
//                //picked from gallery
//                image_uri=data.getData();
//                //set to imageview
//                imgprofile.setImageURI(image_uri);
//
//            }
//            else if(requestCode ==IMAGE_PICK_CAMERA_CODE){
//                imgprofile.setImageURI(image_uri);
//            }
//        }
//
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }


}