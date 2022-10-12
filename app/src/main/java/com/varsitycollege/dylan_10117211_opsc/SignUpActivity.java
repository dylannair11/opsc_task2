package com.varsitycollege.dylan_10117211_opsc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button login;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    protected void onStart() {
        super.onStart();

        FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Toast.makeText(SignUpActivity.this, "Signed in", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.btnSignUp2);

        mAuth = FirebaseAuth.getInstance();
    }

    public void onRegister(View view) {
        String emailString = email.getText().toString().trim();
        String passwordString = password.getText().toString().trim();
        if (!TextUtils.isEmpty(emailString) && !TextUtils.isEmpty(passwordString)) {

            mAuth.createUserWithEmailAndPassword(emailString, passwordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                Log.i("TAG", "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(SignUpActivity.this, "Authentication success.",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUpActivity.this, SettingsActivity.class);
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUpActivity.this, "" + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (TextUtils.isEmpty(emailString) && !TextUtils.isEmpty(passwordString)) {
            Toast.makeText(SignUpActivity.this, "Please enter an email address.",
                    Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isEmpty(emailString) && TextUtils.isEmpty(passwordString)) {
            Toast.makeText(SignUpActivity.this, "Please enter a password.",
                    Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(emailString) && TextUtils.isEmpty(passwordString)) {
            Toast.makeText(SignUpActivity.this, "Please enter an email and password.",
                    Toast.LENGTH_SHORT).show();
        }

    }}