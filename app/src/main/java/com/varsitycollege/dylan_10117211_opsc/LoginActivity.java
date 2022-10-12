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

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button login;

    SignInButton googleButton;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mauthListener;

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mauthListener);
        mauthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.btnLogin2);

        mAuth = FirebaseAuth.getInstance();

        mauthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    public void onLogin(View view)
    {
        String emailLogin=email.getText().toString().trim();
        String passwordLogin=password.getText().toString().trim();
        if(!TextUtils.isEmpty(emailLogin) && !TextUtils.isEmpty(passwordLogin))
            mAuth.signInWithEmailAndPassword(emailLogin, passwordLogin)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                Log.i("TAG", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Log.i("TAG", "USER: " + user.toString());
                                Toast.makeText(LoginActivity.this, "Log in success.",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i("TAG", "", task.getException());
                                Toast.makeText(LoginActivity.this, "" + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        else if(TextUtils.isEmpty(emailLogin) && !TextUtils.isEmpty(passwordLogin))
        {
            Toast.makeText(LoginActivity.this, "Please enter an email address..",
                    Toast.LENGTH_SHORT).show();
        }
        else if (!TextUtils.isEmpty(emailLogin) && TextUtils.isEmpty(passwordLogin))
        {
            Toast.makeText(LoginActivity.this, "Please enter a password",
                    Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(emailLogin) && TextUtils.isEmpty(passwordLogin))
        {
            Toast.makeText(LoginActivity.this, "Please enter an email and password",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
