package com.varsitycollege.dylan_10117211_opsc;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.ui.geocoder.GeocoderAutoCompleteView;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    //Declaring variables
    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location startLocation;
    private Point currentPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private Button startNavigation;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";

    SimpleDateFormat currentDate = new SimpleDateFormat("ddMMyyyy");
    Date todayDate = new Date();
    String thisDate = currentDate.format(todayDate);



    FirebaseAuth mAuthInstance = FirebaseAuth.getInstance();
    final FirebaseUser user = mAuthInstance.getCurrentUser();
    String uid = Objects.requireNonNull(user).getUid();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users").child(uid).child("Settings");
    String transportMode;
    String finalAddress;
    String startAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Mapbox.getInstance(this, getString(R.string.access_token));
        mapView = findViewById(R.id.mapView);
        startNavigation = findViewById(R.id.startButton);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync((OnMapReadyCallback) this);

        //Obtaining the transport mode settings from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transportMode = (String) dataSnapshot.child("TransportMode").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
                Toast.makeText(MainActivity.this, "Unable to read transport mode.", Toast.LENGTH_LONG).show();
            }
        });

        // Set up autocomplete widget on UI
        GeocoderAutoCompleteView autocomplete = findViewById(R.id.query);
        autocomplete.setAccessToken(Mapbox.getAccessToken());

        //Filters the types of addresses to search by
        autocomplete.setTypes(new String[]{GeocodingCriteria.TYPE_POI,
                GeocodingCriteria.TYPE_ADDRESS,
                GeocodingCriteria.TYPE_POSTCODE});

        //Attaches a listener to the widget
        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void onFeatureClick(CarmenFeature feature) {
                hideOnScreenKeyboard();

                //Obtains the position of the place that was searched
                Position position1 = feature.asPosition();

                //if a current marker exits, remove the marker
                if (destinationMarker != null) {
                    map.removeMarker(destinationMarker);
                }

                //Adding a marker to the given co-ordinates
                destinationMarker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(position1.getLatitude(), position1.getLongitude()))
                        //Displays the title of the location on the marker
                        .title(feature.getPlaceName()));

                finalAddress = feature.getPlaceName();

                //Sets destination position to a longitude and latitude point
                destinationPosition = Point.fromLngLat(position1.getLongitude(), position1.getLatitude());

                //Sets the current position to a longitude and latitude point
                currentPosition = Point.fromLngLat(startLocation.getLongitude(), startLocation.getLatitude());

                //Calls the get route method which will graphically display the route.
                getRoute(currentPosition, destinationPosition);

                startNavigation.setVisibility(View.VISIBLE);
                startNavigation.setEnabled(true);
                startNavigation.setBackgroundResource(R.color.mapbox_blue);

                updateMap(position1.getLatitude(), position1.getLongitude());
            }
        });

        startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (transportMode.equalsIgnoreCase("PROFILE_WALKING")) {
                    //launch navigation UI
                    NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                            .origin(currentPosition)
                            .destination(destinationPosition)
                            .directionsProfile(DirectionsCriteria.PROFILE_WALKING)
                            .shouldSimulateRoute(true)
                            .build();
                    NavigationLauncher.startNavigation(MainActivity.this, options);
                } else if (transportMode.equalsIgnoreCase("PROFILE_CYCLING")) {
                    NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                            .origin(currentPosition)
                            .destination(destinationPosition)
                            .directionsProfile(DirectionsCriteria.PROFILE_WALKING)
                            .shouldSimulateRoute(true)
                            .build();
                    NavigationLauncher.startNavigation(MainActivity.this, options);
                } else {
                    NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                            .origin(currentPosition)
                            .destination(destinationPosition)
                            .directionsProfile(DirectionsCriteria.PROFILE_DRIVING)
                            .shouldSimulateRoute(true)
                            .build();
                    NavigationLauncher.startNavigation(MainActivity.this, options);
                }

                final FirebaseUser user = mAuthInstance.getCurrentUser();
                FirebaseDatabase database =  FirebaseDatabase.getInstance();

                //Getting the address of the start position to store in db for displaying
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(currentPosition.latitude(), currentPosition.longitude(), 1);
                    startAddress = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "onClick: " +startAddress);


                DatabaseReference myRef = database.getReference("Users").child(uid).child("Trips").child(thisDate);
                String uid = user.getUid();
                HashMap<String, String> userMap = new HashMap<>();
                userMap.put("From",startAddress);
                userMap.put("To",finalAddress);
                myRef.push().setValue(userMap);

            }
        });
    }

    private void getRoute(Point origin, Point destination) {

        if(transportMode.equalsIgnoreCase("PROFILE_DRIVING"))
        {
            NavigationRoute.builder()
                    .accessToken(Mapbox.getAccessToken())
                    .origin(origin)
                    .destination(destination)
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .annotations(DirectionsCriteria.ANNOTATION_DURATION)
                    .build()
                    .getRoute(new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                            if (response.body() == null) {
                                Log.e(TAG, "No routes found");
                                return;
                            } else if (response.body().routes().size() == 0) {
                                Log.e(TAG, "No routes found");
                            }

                            Log.i(TAG, "onResponse: " + response.body().routes().size());

                            DirectionsRoute currentroute = response.body().routes().get(0);

                            if (navigationMapRoute != null) {
                                navigationMapRoute.removeRoute();
                            } else {
                                navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                                Log.i(TAG, "onResponse: Route nothing");
                            }

                            navigationMapRoute.addRoute(currentroute);
                            Log.i(TAG, "onResponse: Route added");
                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                            Log.e(TAG, "Error" + t.getMessage());
                        }
                    });
        }
        else if(transportMode.equalsIgnoreCase("PROFILE_WALKING"))
        {
            NavigationRoute.builder()
                    .accessToken(Mapbox.getAccessToken())
                    .origin(origin)
                    .destination(destination)
                    .profile(DirectionsCriteria.PROFILE_WALKING)
                    .annotations(DirectionsCriteria.ANNOTATION_DURATION)
                    .build()
                    .getRoute(new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                            if (response.body() == null) {
                                Log.e(TAG, "No routes found");
                                return;
                            } else if (response.body().routes().size() == 0) {
                                Log.e(TAG, "No routes found");
                            }

                            Log.i(TAG, "onResponse: " + response.body().routes().size());

                            DirectionsRoute currentroute = response.body().routes().get(0);

                            if (navigationMapRoute != null) {
                                navigationMapRoute.removeRoute();
                            } else {
                                navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                                Log.i(TAG, "onResponse: Route nothing");
                            }

                            navigationMapRoute.addRoute(currentroute);
                            Log.i(TAG, "onResponse: Route added");
                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                            Log.e(TAG, "Error" + t.getMessage());
                        }
                    });
        }
        else
        {
            NavigationRoute.builder()
                    .accessToken(Mapbox.getAccessToken())
                    .origin(origin)
                    .destination(destination)
                    .profile(DirectionsCriteria.PROFILE_CYCLING)
                    .annotations(DirectionsCriteria.ANNOTATION_DURATION)
                    .build()
                    .getRoute(new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                            if (response.body() == null) {
                                Log.e(TAG, "No routes found");
                                return;
                            } else if (response.body().routes().size() == 0) {
                                Log.e(TAG, "No routes found");
                            }

                            Log.i(TAG, "onResponse: " + response.body().routes().size());

                            DirectionsRoute currentroute = response.body().routes().get(0);

                            if (navigationMapRoute != null) {
                                navigationMapRoute.removeRoute();
                            } else {
                                navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                                Log.i(TAG, "onResponse: Route nothing");
                            }

                            navigationMapRoute.addRoute(currentroute);
                            Log.i(TAG, "onResponse: Route added");
                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                            Log.e(TAG, "Error" + t.getMessage());
                        }
                    });
        }

    }



    //@Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.addOnMapClickListener((MapboxMap.OnMapClickListener) this);
        enableLocation();
    }

    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            //do some stuff
            initialiseLocationEngine();
            initialiseLocationLayer();
        } else {
            permissionsManager = new PermissionsManager((PermissionsListener) this);
            permissionsManager.requestLocationPermissions(this);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    private void initialiseLocationEngine() {
        //gets the last location using the location engine
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();

        if (lastLocation != null) {
            startLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener((LocationEngineListener) this);
        }
    }


    @SuppressWarnings("MissingPermission")
    private void initialiseLocationLayer() {
        //displays users location
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.GPS);
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 15.0));
    }

    private void hideOnScreenKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }

        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
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
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_viewTrips) {
            Intent intent = new Intent(MainActivity.this, ViewTripsActivity.class);
            startActivity(intent);
        }

        if (id == R.id.action_logout) {
            logOut();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(MainActivity.this, "Logged out",
                Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, StartUpActivity.class);
        startActivity(intent);

    }

    private void updateMap(double latitude, double longitude) {

        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(12)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
    }{
}}
