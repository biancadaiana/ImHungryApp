package com.dev.imhungryapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.imhungryapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity implements LocationListener {

    private TextView TextViewTitleR, TextViewSTitleReg, login;
    private EditText editTextFullName, editTextEmail, editTextPhone, editTextPassword, editTextAddress;
    private Button regBtn;
    private ProgressBar progressBar;
    private ImageButton gpsB;

    private FirebaseAuth auth;
    //in plus
    //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://im-hungry-app-8f312-default-rtdb.firebaseio.com/");

    //permission constants
    private static final int LOCATION_REQUEST_CODE= 100;
    private static final int CAMERA_REQUEST_CODE= 200;
    private static final int STORAGE_REQUEST_CODE= 300;

    //permission arrays
    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri image_uri;

    private double latitude, longitude;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth = FirebaseAuth.getInstance();
        login = (TextView) findViewById(R.id.loginBtn);
        regBtn = (Button) findViewById(R.id.regBtn);
        gpsB = (ImageButton) findViewById(R.id.gpsB);
        editTextFullName = (EditText) findViewById(R.id.editText);
        editTextEmail = (EditText) findViewById(R.id.editText2);
        editTextPhone = (EditText) findViewById(R.id.editText3);
        editTextPassword = (EditText) findViewById(R.id.editText4);
        editTextAddress = (EditText) findViewById(R.id.editText5);

        //init permissions array
        locationPermissions =new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions =new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions =new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = editTextEmail.getText().toString().trim();
                String pass = editTextPassword.getText().toString().trim();
                String fullName= editTextFullName.getText().toString().trim();
                String phone= editTextPhone.getText().toString().trim();
                String address= editTextAddress.getText().toString().trim();

                if(TextUtils.isEmpty(fullName)){
                    editTextFullName.setError("Full name is required!");
                }
                if(TextUtils.isEmpty(phone)){
                    editTextPhone.setError("Phone number is required!");
                }
                if(TextUtils.isEmpty(address)){
                    editTextAddress.setError("Address is required!");
                }
                if (TextUtils.isEmpty(user)){
                    editTextEmail.setError("Email cannot be empty!");
                }
                if(Patterns.EMAIL_ADDRESS.matcher(user).matches()){
                    editTextEmail.setError("Please provide valid email!");
                }
                if(TextUtils.isEmpty(pass)){
                    editTextPassword.setError("Password cannot be empty!");
                }
                if(pass.length() < 6){
                    editTextPassword.setError("Min password length should be 6 characters!");
                }

                //creat account
                else if(!(TextUtils.isEmpty(phone)) && !(TextUtils.isEmpty(address))){
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //Toast.makeText(RegistrationActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                                saverFirebaseData();
                                //startActivity(new Intent(RegistrationActivity.this, SigninActivity.class));
                            } else {
                                Toast.makeText(RegistrationActivity.this, "Registration failed!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        private void saverFirebaseData() {
                            Toast.makeText(RegistrationActivity.this, "Saving data..", Toast.LENGTH_SHORT).show();
                            String timestamp = "" + System.currentTimeMillis();
                            //setup data to save
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("uid", "" + FirebaseAuth.getInstance().getUid());
                            hashMap.put("email", "" + user);
                            hashMap.put("name", "" + fullName);
                            hashMap.put("phone", "" + phone);
                            hashMap.put("address", "" + address);
                            //save to db
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.child(FirebaseAuth.getInstance().getUid()).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //db updated
                                            Toast.makeText(RegistrationActivity.this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegistrationActivity.this, SigninActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed updating db
                                            startActivity(new Intent(RegistrationActivity.this, SigninActivity.class));
                                            finish();
                                        }
                                    });

                        }
                    });
                }//endofelse

                //

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, SigninActivity.class));
            }
        });

        gpsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                if (checkLocationPermission()){
                    //already allowed
                    detectLocation();

                }
                else {
                    //not allowed, request
                    requestLocationPermission();

                }
            }
        });

    }

    //new added

    private void detectLocation() {
        Toast.makeText(this, "Please wauit..", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    private boolean checkLocationPermission() {
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude= location.getLatitude();
        longitude= location.getLongitude();
        findAddress();
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
            editTextAddress.setText(address);
            //..contry state city

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        //LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //LocationListener.super.onProviderDisabled(provider);
        Toast.makeText(this, "Please turn on location!", Toast.LENGTH_SHORT).show();
    }


    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
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

            break;
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



}