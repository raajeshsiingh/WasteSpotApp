package com.example.wastespotapp;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShareActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private static final int CAMERA_REQUEST = 1888;
    Bitmap receivedImageBitmap;
//    EditText edtShareImage;
    ImageView imgShare;
    Button btnShareImage;
    FusedLocationProviderClient mFusedLocationClient;
    SimpleDateFormat sdf;
    Button callButton;
    String currentDate;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imgShare = (ImageView) findViewById(R.id.imgShare);
        callButton = (Button) findViewById(R.id.btnCall);
        sdf = new SimpleDateFormat("yyyy/MM/dd G 'at' HH:mm:ss z");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(ShareActivity.this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        updateMap(location);


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void updateMap(Location location){
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

    }

    public void imgShareActivity(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public void btnCallActivity(View view) {
        if (receivedImageBitmap != null) {
//            if (edtShareImage.getText().toString().equals("")) {
//                FancyToast.makeText(ShareActivity.this, "Add a Caption", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
//            } else {
                currentDate = sdf.format(new Date());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                receivedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                ParseFile parseFile = new ParseFile("img.png", bytes);
                ParseObject parseObject = new ParseObject("Request");
                parseObject.put("picture", parseFile);
                if(count == 0) {
                    parseObject.put("image_des", "LOW");
                }else if(count == 1) {
                    parseObject.put("image_des", "MEDIUM");
                }else {
                    parseObject.put("image_des", "HIGH");
                }
                parseObject.put("Date", currentDate);


                parseObject.put("riderOrDriver", ParseUser.getCurrentUser().get("riderOrDriver"));


                if (mLastLocation != null) {
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    parseObject.put("location", parseGeoPoint);
                }
                final ProgressDialog dialog = new ProgressDialog(ShareActivity.this);
                dialog.setMessage("Loading...");
                dialog.show();
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            if(count == 0){
                                FancyToast.makeText(ShareActivity.this, "waste index is : LOW", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                                count++;
                            }
                            else if(count == 1){
                                FancyToast.makeText(ShareActivity.this, "wastet index is : MEDIUM", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                                count++;
                            }
                            else{
                                FancyToast.makeText(ShareActivity.this, "waste index is : HIGH", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                                count = 0;
                            }
                            FancyToast.makeText(ShareActivity.this, "Done!!!", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                        } else {
                            FancyToast.makeText(ShareActivity.this, "Unknown error: " + e.getMessage(), FancyToast.LENGTH_LONG, FancyToast.CONFUSING, false).show();
                        }
                        dialog.dismiss();
                    }
                });
//            }

        } else {
            FancyToast.makeText(ShareActivity.this, "Select Image", FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            receivedImageBitmap = (Bitmap) data.getExtras().get("data");
            imgShare.setImageBitmap(receivedImageBitmap);
        }
    }
}