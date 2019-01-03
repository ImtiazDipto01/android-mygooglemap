package com.example.dipto.mygooglemap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.etSearchLocation)
    EditText etSearchLocation;
    @BindView(R.id.ivLocateMe)
    ImageView ivLocateMe;

    public static final float DEFAULT_ZOOM = 15f;
    private static final String TAG = "MyMapActivity";
    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_map);
        ButterKnife.bind(this);
        initMap();
        getSearchValue();
    }

    // getting values from Edittext from TextWatcher
    private void getSearchValue() {
        etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String serachKey = s.toString();
                if (serachKey.length() > 2) {
                    geoLocating(serachKey);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    // geoLocating method helps to get the searchResult for Location
    private void geoLocating(String searchStr) {
        Geocoder geocoder = new Geocoder(MyMapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchStr, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocating IOException: " + e);
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.e(TAG, "geoLocating Address: " + address.toString());
            if (searchStr.contains(".")) {
                moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MyMapActivity.this);
    }

    // getting device current location and moving the camera
    private void getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete : Location Successfull");
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                    } else {
                        Log.d(TAG, "onComplete : Location UnSuccessfull");
                    }
                }
            });
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation SecurityException: " + e);
        }
    }

    // moving the camera into map
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera : moving the camera to, lat: " + latLng.latitude + " lng:" + latLng.longitude);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);

            myMap.addMarker(options);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        getDeviceLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(false);
    }


    @OnClick({R.id.ivLocateMe})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLocateMe:{
                getDeviceLocation();
                break;
            }
        }
    }
}
