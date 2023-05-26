package com.example.geofencingapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.geofencingapp.databinding.ActivityMapsBinding;
import com.google.android.gms.location.GeofencingRequest;

import android.util.Log;

import java.util.UUID;

public class Maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final int REQUEST_CODE = 1;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private final int radius = 20;
    private LatLng latLng;

    //  geoFencing reference variables
    GeofencingClient geofencingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        geofencingClient = LocationServices.getGeofencingClient(this);

//      Taking users permission
        enableUserLocation();
//      showing mark and circle region and initialising geofence
        mMap.setOnMapLongClickListener(this);

    }

    @Override
//  when user long press on map.
    public void onMapLongClick(@NonNull LatLng latLng) {
        this.latLng = latLng;
//      clearing previous marks first
        mMap.clear();
//      setting new marks on map
        addMarker(latLng);
        addCircle(latLng, radius);

//      checking is phone have google play services enabled
        boolean bool = isGooglePlayServicesAvailable();
//      if user have google play services then It go forward and create a geo fence.
        if (bool) {
            addGeoFence();
        }
        else {
            Log.e("play services", "not enables");
        }

    }


    private void addGeoFence() {
        Log.d("Google play services", "Yes, your phone has google play services enabled");


        String id = UUID.randomUUID().toString();
        Geofence geofence = new Geofence.Builder()
                .setRequestId(id)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setCircularRegion(latLng.latitude,latLng.longitude, radius) // Try changing your radius
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setLoiteringDelay(5000)
                .build();

        Log.d("latitude and lognitude" ,""+latLng.latitude + " next : "+latLng.longitude);

        // Create geofence request
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_EXIT)
            .addGeofence(geofence)
            .build();

        // Create pending intent for geofence transitions
        Intent intent = new Intent(this, GeofenceTransitionBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission has not been granted, so request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);

        } else {
            // Permission has already been granted
            // Your code to handle the location access
            Log.d("permission granted?" , "yes bro , permission is granted. you are good to go.");
        }


        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener(this, aVoid -> {
                Log.d("sucessfull bro", "you did it.");
            })
            .addOnFailureListener(this, e -> {
                // Failed to add geofences
                Log.e("Geofence", "Sorry bro, you again failed..... don't worry try agin..." + e.getMessage());
            });
    }


    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

//  checking is the user phone has play service available or not.
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position
                (latLng);
        mMap.addMarker(markerOptions);
    }

    public void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(70, 255, 0, 0));
        circleOptions.strokeWidth(3);
        mMap.addCircle(circleOptions);
    }
}