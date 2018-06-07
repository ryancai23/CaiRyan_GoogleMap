package com.example.cair0806.mymapsappp1;

import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private Location myLocation;
    private boolean gotMyLocationOneTime;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private LocationManager locationManager;
    private boolean notTrackingMyLocation = true;

    private static final long MIN_TIME_BW_UPDATE = 1000*5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng ottawa = new LatLng(45.4215, -75.6972);
        mMap.addMarker(new MarkerOptions().position(ottawa).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ottawa));

        // add a marker at your place of birth and move the camera to it
        //when the marker is tapped display born here
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            Log.d("myMapsAppP1","Failed FINE Permission Check");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            Log.d("myMapsAppP1", "Failed COARSE Permission Check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        if ((ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)||(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)){


            mMap.setMyLocationEnabled(true);
        }

        locationSearch = (EditText) findViewById(R.id.editText_addr);

        gotMyLocationOneTime = false;
        getLocation();
    }

    //add a view button and method to switch between satelllite and  map views
    public void changeView(View view){
        if(mMap.getMapType() == mMap.MAP_TYPE_SATELLITE){
            mMap.setMapType(mMap.MAP_TYPE_NORMAL);
        } else {
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        }
    }
    public void onSearch(View v) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;
        List<Address> addressListZip = null;

        //use locationmanager for user location
        //implemembt the locationlistener interface to setup location services
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("myMapsAppP1", "onSearch: location = " + location);
        Log.d("myMapsAppP1", "onSearch: provider " + provider);

        LatLng userLocation = null;

        //check the last known locatioin, need to specifically list the provider(network or gps)

        try {

            if (locationManager != null) {
                Log.d("myMapsAppP1", "onSearch: locationManager is not null");

                if ((myLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("myMapsAppP1", "onSearch: using the NETWORK_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if ((myLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("myMapsAppP1", "onSearch: using the NETWORK_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else {
                    Log.d("myMapsAppP1", "onSearch: myLocation is null from getLastKnownLocation");
                }
            }

        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("myMapsAppP1", "onSearch: Exception getLastKnownLocation");
            Toast.makeText(this, "onSearch: Exception getLastKnownLocation", Toast.LENGTH_SHORT);
        }
        if (!location.matches("")) {
            Log.d("myMapsAppP1", "onSearch: location field is populated");

            Geocoder geocoder = new Geocoder(this, Locale.US);
            try {
                //get a list of the address
                addressList = geocoder.getFromLocationName(location, 100, userLocation.latitude - (5.0 / 60), userLocation.longitude - (5.0 / 60), userLocation.latitude + (5.0 / 60), userLocation.longitude + (5.0 / 60));
                Log.d("myMapsAppP1", "onSearch: addressList is created");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("myMapsAppP1", "onSearch: addresList size is: " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    //place marker on the map
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }
    public void getLocation(){
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //Get GPS Status, isProviderEnabled returns true if user has enable gps
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled)Log.d("myMapsAppP1", "getLocation: GPS is enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isNetworkEnabled)Log.d("myMapsAppP1", "getLocation: GPS is enabled");

            if(!isGPSEnabled && !isNetworkEnabled){
                Log.d("myMapsAppP1", "getLocation: no provider enabled");
            }
            else{
                if(isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }


            }
            if(isGPSEnabled){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
            }

            }

        catch(Exception e){
            Log.d("myMapsAppP1", "getLocation: exception in getLocation");
            e.printStackTrace();
        }
    }
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            dropAmarker(LocationManager.NETWORK_PROVIDER);
            if(gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
                gotMyLocationOneTime =  true;

            }else{
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("myMapsAppP1", "locationListenerNetwork:status change");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
          dropAmarker(LocationManager.GPS_PROVIDER);
          // if doing one time, remove updates to both gps and network
            //else do nothing
            if(gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
                gotMyLocationOneTime =  true;

            }
            else{

            }
        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle extras) {
            switch(i) {

                case LocationProvider.AVAILABLE:
                    Log.d("myMapsAppP1", "onStatusChanged:available");
                break;
                case LocationProvider.OUT_OF_SERVICE:
                    if(isNetworkEnabled) {
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    if(isGPSEnabled) {
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                break;
                default:
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATE, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    public void dropAmarker(String provider){
        if(locationManager != null)
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

          myLocation = locationManager.getLastKnownLocation(provider);
            LatLng userLocation = null;
        if(myLocation == null){
            Log.d("MyMapsApp","dropAmarker: location is null");
        }
        else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate update= CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
              if(provider == LocationManager.GPS_PROVIDER) {

                        mMap.addCircle(new CircleOptions()
                           .center(userLocation)
                            .radius(1)
                            .strokeColor(Color.RED)
                            .strokeWidth(2)
                            .fillColor(Color.RED));
              }
              else
              {
                  mMap.addCircle(new CircleOptions()
                          .center(userLocation)
                          .radius(1)
                          .strokeColor(Color.BLUE)
                          .strokeWidth(2)
                          .fillColor(Color.BLUE));
              }

              mMap.animateCamera(update);
        }
    }
    public void trackMyLocation (View view){
        //kick off the location tracker using getLocation to start the LocationListener
        if(notTrackingMyLocation) {getLocation(); notTrackingMyLocation = false;}
        else {locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGps); notTrackingMyLocation =true;}
    }
}