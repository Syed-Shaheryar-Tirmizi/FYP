// YA ALI (AS) madad
package com.syed.map_crimes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GeoQueryEventListener, NavigationView.OnNavigationItemSelectedListener {

    boolean flag = true;
    private AppBarConfiguration mAppBarConfiguration;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker currentUser;
    private GeoFire geoFire;
    private List<LatLng> dangerousAreas;
    private Location lastLocation;
    private GeoQuery geoQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_maps);


            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.MapCrimes, R.id.Hotspot_areas, R.id.graphs, R.id.aboutus, R.id.help)
                    .setDrawerLayout(drawer)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
            navigationView.setItemIconTintList(null);





            buildLocationRequest();  // set location parameters for fused location client

            // Fused Location Provider Cient to deal with location updates and retrival
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

            buildLocationCallBack(); // get location updates from fused location client

            // Check if user accept the location permission or not
            Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {

                    initArea();  // Get areas from firebase
                    settingsGeoFire();  // Get user location from firebase and set geofire on it (allows to store and query a set of keys based on their geographic location.)
                    startLocationService();
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    Toast.makeText(MapsActivity.this, "Please Enable permission", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                }
            }).check();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

       if (id == R.id.exit)
       {
           stopLocationService();
           finish();
       }

       else if(id == R.id.start_background)
       {
           startLocationService();
       }
       else if (id == R.id.send_location)
       {
           sendLocation();
       }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    // Get areas from the firebase
    private void initArea()
    {
        try
        {
            DatabaseReference myCity = FirebaseDatabase.getInstance().getReference("DangerousAreas").child("MyCity");
            myCity.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    List<MyLatLng> latLngList = new ArrayList<>();
                    for (DataSnapshot locationSnapShot : snapshot.getChildren())
                    {
                        MyLatLng latLng = locationSnapShot.getValue(MyLatLng.class);
                        latLngList.add(latLng);
                    }
                    onLoadLocationSuccess(latLngList);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        catch (Exception e)
        {
            Toast.makeText(this, "Updating", Toast.LENGTH_SHORT).show();
        }

    }


    // Fetch User location from database
    private void settingsGeoFire()
    {
        try
        {
            DatabaseReference myLocation = FirebaseDatabase.getInstance().getReference("MyLocation");
            geoFire = new GeoFire(myLocation);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }


    // Add marker on user location
    private void addUserMarker()
    {
        try
        {
            geoFire.setLocation("You", new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error)
                {

                    if (currentUser != null) currentUser.remove();

                    currentUser = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()))
                            .title("You"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUser.getPosition(), 12.0f));
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }



    // Set Parameters for Fused Location
    private void buildLocationRequest()
    {
        try
        {
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setSmallestDisplacement(400f);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);

            Task<LocationSettingsResponse> task=LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

            task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {


                // This complete function is auto genrated. Otherwise, it throws error


                @Override
                public void onComplete(Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        // All location settings are satisfied. The client can initialize location
                        // requests here.

                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the
                                // user a dialog.
                                try {
                                    // Cast to a resolvable exception.
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    resolvable.startResolutionForResult(
                                            MapsActivity.this,
                                            101);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                } catch (ClassCastException e) {
                                    // Ignore, should be an impossible error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                }
            });


        }
        catch (Exception e)
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }




    // Get updates from fused location when location changes
    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mMap != null)
                {
                    lastLocation = locationResult.getLastLocation();
                    addUserMarker();
                }
            }
        };
    }


    // This function is used to ask user to turn on gps (if not opened previously)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 101:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Toast.makeText(MapsActivity.this, states.isLocationPresent() + "", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(MapsActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera (Default location by Google Maps)
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                // Permission check for Marshmallow and higher (throws error if not included)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
        addCircleArea();

    }

    // Add circle on hotspot areas . GeoQuerry is just like a virual fence which triggers notifications
    private void addCircleArea()
    {
        try
        {
            if(geoQuery != null)
            {
                geoQuery.removeGeoQueryEventListener(this);
                geoQuery.removeAllListeners();
            }
            for(LatLng latLng:dangerousAreas)
            {
                mMap.addCircle(new CircleOptions().center(latLng).radius(300).strokeColor(Color.BLUE)
                        .fillColor(Color.BLUE).strokeWidth(5f));


                geoQuery =geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),0.3);
                geoQuery.addGeoQueryEventListener(MapsActivity.this);
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onKeyEntered(String key, GeoLocation location)
    {
        if(flag)
        {
            flag = false;
            sendNotification("Alert",String.format("%s Entered in dangerous area",key));
        }


    }


    @Override
    public void onKeyExited(String key)
    {
        if(!flag)
        {
            flag = true;
            sendNotification("Alert",String.format("%s left the dangerous area",key));
        }

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }



    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
    }


    // generate notifications
    private void sendNotification(String title, String context)
    {
        String NOTIFICATION_CHANNEL_ID ="edmt_multiple_location";
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent();
        PendingIntent pendingIntent1 = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notification",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Notification description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[] {0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title).setContentText(context).setAutoCancel(false).setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)).setContentIntent(pendingIntent1) ;

        Notification notification = builder.build();
        notificationManager.notify(0,notification);

    }



    // used by init area function to add areas and current location on google maps
    public void onLoadLocationSuccess(List<MyLatLng> latLngs) {
        try
        {
            dangerousAreas = new ArrayList<>();
            for(MyLatLng myLatLng : latLngs)
            {
                LatLng convert = new LatLng(myLatLng.getLatitude(),myLatLng.getLongitude());
                dangerousAreas.add(convert);
            }
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(MapsActivity.this);
            if(mMap != null)
            {
                mMap.clear();

                addCircleArea();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Location Load", Toast.LENGTH_SHORT).show();
        }

    }


    // this function checks if app is running in background or not
    private boolean isLocationServiceRunning()
    {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null)
        {
            for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE))
            {
                if(BackgroundNotification.class.getName().equals(service.service.getClassName()))
                {
                    if(service.foreground)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }


    // this function allows app to run in back ground
    private void startLocationService()
    {
        if(!isLocationServiceRunning())
        {
            Intent intent = new Intent(getApplicationContext(), BackgroundNotification.class);
            intent.setAction("Start");
            startService(intent);
            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        }
    }


    //this function stops app to run in back ground
    private void stopLocationService() {
        if(isLocationServiceRunning())
        {
            Intent intent = new Intent(getApplicationContext(), BackgroundNotification.class);
            intent.setAction("Stop");
            startService(intent);
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        }
    }


    // this function must be override to allow app run in background (otherwise won't run due to strcit security of hgher android)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        startLocationService();
    }


    // Send cuurent user location via whatsapp
    public void sendLocation()
    { try
        {
            String whatsAppMessage = "http://maps.google.com/maps?saddr=" + lastLocation.getLatitude() + "," + lastLocation.getLongitude();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Please install whatsapp", Toast.LENGTH_SHORT).show();
        }

    }


    // floating action button to send location
    public void Emergency(View view)
    {
        sendLocation();
    }


}
