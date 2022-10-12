package com.varsitycollege.dylan_10117211_opsc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ViewTripsActivity extends AppCompatActivity {

    ListView mainListView;
    CalendarView calendarView;
    ArrayAdapter<String> listAdapter;
    ListView listView;

    FirebaseAuth mAuthInstance = FirebaseAuth.getInstance();
    final FirebaseUser user = mAuthInstance.getCurrentUser();
    String uid = Objects.requireNonNull(user).getUid();
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference rootRef = db.getReference();
    DatabaseReference userRef ;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.viewtripsone);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                month += 1;
                String day = Integer.toString(dayOfMonth);
                String finalMonth = Integer.toString(month);
                if (dayOfMonth < 10) {
                    day = "0" + day;
                }
                if (month < 10) {
                    finalMonth = "0" + finalMonth;
                }
                String date = day + "" + finalMonth + "" + year;

                date = date.trim();

                Log.i("ViewTrips", "onSelectedDayChange: "+date);
                DatabaseReference userTrips=rootRef.child("Users").child(uid).child("Trips").child(date);
                userTrips.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Get map of users in datasnapshot
                                listView = findViewById(R.id.mainListView);
                                listView.setAdapter(null);  //clears old values
                                if (dataSnapshot.exists()) {
                                    collectTrips((Map<String, Object>) dataSnapshot.getValue());
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });
            }

        });









    }



    private void collectTrips(Map<String,Object> users) {

        ArrayList<String> From = new ArrayList<>();
        ArrayList<String> To = new ArrayList<>();
        ArrayList<String> finalList = new ArrayList<>();
        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list
            From.add((String) singleUser.get("From"));
            To.add((String) singleUser.get("To"));
        }

        System.out.println(From.toString());
        System.out.println(To.toString());
        for(int i = 0;i< From.size();i++){
            finalList.add("From: "+From.get(i) + "\n" +"To: " + To.get(i));
        }

        mainListView = findViewById( R.id.mainListView);
        listAdapter = new ArrayAdapter<String>(this, R.layout.row,finalList);

        mainListView.setAdapter( listAdapter );

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
            Intent intent = new Intent(ViewTripsActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_findDestination) {
            Intent intent = new Intent(ViewTripsActivity.this, MainActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_viewTrips) {
            Intent intent = new Intent(ViewTripsActivity.this, ViewTripsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_logout) {
            logOut();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(ViewTripsActivity.this, "Logged out",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ViewTripsActivity.this, StartUpActivity.class);
        startActivity(intent);

    }{
}}
