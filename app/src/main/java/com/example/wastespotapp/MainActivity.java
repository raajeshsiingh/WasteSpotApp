package com.example.wastespotapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle( "Waste Spotter");

        if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        FancyToast.makeText(MainActivity.this, "SUCCESS", FancyToast.LENGTH_SHORT,FancyToast.SUCCESS, false).show();
                    } else {
                        FancyToast.makeText(MainActivity.this, "FAIL", FancyToast.LENGTH_SHORT,FancyToast.ERROR, false).show();
                    }
                }
            });
        } else {
            if (ParseUser.getCurrentUser().get("riderOrDriver") != null) {
                redirectActivity();
                FancyToast.makeText(MainActivity.this, "Logging as : "+ParseUser.getCurrentUser().get("riderOrDriver"), FancyToast.LENGTH_SHORT,FancyToast.SUCCESS, false).show();

            }
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                FancyToast.makeText(MainActivity.this, location + "", FancyToast.LENGTH_SHORT,FancyToast.INFO, false).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
    }

    public void login(View view) {

        Switch userTypeSwitch = (Switch) findViewById(R.id.mainSwitch);

        String userType = "rider";
        if(userTypeSwitch.isChecked()){
            userType = "driver";
        }
        ParseUser.getCurrentUser().put("riderOrDriver",userType);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                redirectActivity();
            }
        });
        redirectActivity();
        FancyToast.makeText(this, "Logging as : "+userType, FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
    }

    public void redirectActivity(){
        if(ParseUser.getCurrentUser().getString("riderOrDriver").equals("rider")){
            Intent intent = new Intent(MainActivity.this, RiderActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(MainActivity.this, RequestActivity.class);
            startActivity(intent);
        }
    }
}