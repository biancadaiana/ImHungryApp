package com.dev.imhungryapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.imhungryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SigninActivity extends AppCompatActivity{

    private FirebaseAuth auth;
    private TextView registerSIB, forgotpass;
    private EditText signinEmail, signinpass;
    private Button signinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        auth = FirebaseAuth.getInstance();
        signinEmail = findViewById(R.id.editText);
        signinpass = findViewById(R.id.editText2);
        signinBtn = findViewById(R.id.signinBtn);
        registerSIB = findViewById(R.id.registerSIB);
        forgotpass = findViewById(R.id.forgotpass);

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = signinEmail.getText().toString();
                String pass = signinpass.getText().toString();

                if(!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    if(!pass.isEmpty()){
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult){
                                        if(email.equals("adminhungry@gmail.com")){
                                            Toast.makeText(SigninActivity.this, "Successfully signed in!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SigninActivity.this, AdminMainActivity.class));
                                            finish();
                                        }
                                        else {
                                            //after login make user online
                                            HashMap hashMap= new HashMap<>();
                                            hashMap.put("online", "true");

                                            //update value to db
                                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");

                                            Toast.makeText(SigninActivity.this, "Successfully signed in!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SigninActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SigninActivity.this, "Sign in failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else{
                        signinpass.setError("Password cannot be empty!");
                    }
                } else if(email.isEmpty()){
                    signinEmail.setError("Email cannot be empty!");
                } else{
                    signinEmail.setError("Please enter a valid email!");
                }


            }//en onclick


        });

        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SigninActivity.this, ForgotPasswordActivity.class));
            }
        });
        registerSIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SigninActivity.this, RegistrationActivity.class));
            }

        });
    }

};