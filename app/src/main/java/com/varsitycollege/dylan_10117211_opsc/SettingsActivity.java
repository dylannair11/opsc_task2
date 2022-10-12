package com.varsitycollege.dylan_10117211_opsc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
//import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    RadioGroup radioGroup1;
    RadioButton radBut;
    Button but;
    private AlertDialog.Builder alert;

    FirebaseAuth mAuthInstance = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference rootRef = db.getReference();
    DatabaseReference userRef = rootRef.child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        radioGroup1 = findViewById(R.id.radioGroup);

        final FirebaseUser user = mAuthInstance.getCurrentUser();

        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        String uid = user.getUid();
        DatabaseReference myRef = database.getReference("Users").child(uid).child("Settings");

        but = findViewById(R.id.btnApply);

        if(user != null)
        {
            but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alert = new AlertDialog.Builder(SettingsActivity.this);
                    alert.setTitle(getResources().getString(R.string.title));
                    alert.setIcon(android.R.drawable.ic_popup_reminder);
                    alert.setMessage(getResources().getString(R.string.messageDialog));
                    alert.setCancelable(false);
                    alert.setPositiveButton(getResources().getString(R.string.yess), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String uid = user.getUid();
                            if (radioGroup1.getCheckedRadioButtonId() == -1) {
                                Toast.makeText(SettingsActivity.this, "Please select transport mode", Toast.LENGTH_LONG).show();
                            } else {
                                int radioId = radioGroup1.getCheckedRadioButtonId();
                                radBut = findViewById(radioId);

                                HashMap<String, String> userMap = new HashMap<>();

                                if(radBut.getText().equals("Walking"))
                                {
                                    radBut.setChecked(true);
                                    userMap.put("TransportMode", "PROFILE_WALKING");
                                }
                                else if(radBut.getText().equals("Cycling"))
                                {
                                    radBut.setChecked(true);
                                    userMap.put("TransportMode", "PROFILE_CYCLING");
                                }
                                else
                                {
                                    radBut.setChecked(true);
                                    userMap.put("TransportMode", "PROFILE_DRIVING");
                                }

                                userRef.child(uid).child("Settings").setValue(userMap);
                                Toast.makeText(SettingsActivity.this, "Transportation mode set to: " + radBut.getText(), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });

                    alert.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog dia = alert.create();
                    dia.show();
                }//end on click
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_findDestination) {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_viewTrips) {
            Intent intent = new Intent(SettingsActivity.this, ViewTripsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_logout) {
            logOut();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(SettingsActivity.this, "Logged out",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SettingsActivity.this, StartUpActivity.class);
        startActivity(intent);
        {
}
    }
}
