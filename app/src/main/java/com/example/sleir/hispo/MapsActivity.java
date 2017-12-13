package com.example.sleir.hispo;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import com.beyondar.android.world.World;
import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.plugin.googlemap.GoogleMapWorldPlugin;
import com.beyondar.android.util.location.BeyondarLocationManager;
import com.beyondar.android.world.GeoObject;

import butterknife.InjectView;

import static com.example.sleir.hispo.R.id.thumbnails;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowClickListener{


    private GoogleMap mMap;
    double latitude;
    double longitude;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private Marker mSelectedMarker;
    private Marker newMarker;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    private BeyondarFragmentSupport mBeyondarFragment;
    private GoogleMapWorldPlugin mGoogleMapPlugin;
    private World mWorld;
    ArrayList<Markers> Markers = new ArrayList<>();
    ArrayList<Images> Images = new ArrayList<>();
    LinearLayout dropDownMenuIconItem;
    @InjectView(thumbnails) LinearLayout _thumbnails;

    @Override
    public void onInfoWindowClick(Marker marker) {
        String type = "";
        for(int i = 0; i<Markers.size();i++)
        {
            if(marker.getPosition().latitude == Double.parseDouble(Markers.get(i).getLatitude()) || marker.getPosition().longitude == Double.parseDouble(Markers.get(i).getLongitude()) ) {
                type = Markers.get(i).getType();
            }
        }
        startGalleryActivity(marker.getTitle().toString(),marker.getSnippet().toString(), distanceToString(calculateDistance(marker.getPosition())),type);
    }

    public void verticalDropDownIconMenu(View view) {
        if (dropDownMenuIconItem.getVisibility() == View.VISIBLE) {
            dropDownMenuIconItem.setVisibility(View.INVISIBLE);
        } else {
            dropDownMenuIconItem.setVisibility(View.VISIBLE);
        }
    }

    public void menuSettingsClick(View view) {
        dropDownMenuIconItem.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void menuSearchClick(View view) {
        dropDownMenuIconItem.setVisibility(View.INVISIBLE);

    }

    public void menuProfileClick(View view) {
        dropDownMenuIconItem.setVisibility(View.INVISIBLE);

    }

    /** Customizing the info window and/or its contents. */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;
        private final View mContents;



        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }


        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {

            /**TODO: get markers title info, compare with array of markers, if match load url, else default to 0*/
            int badge;
           //final ImageView img = (ImageView)findViewById(R.id.badge);
            //String url = "https://firebasestorage.googleapis.com/v0/b/hispo-54754.appspot.com/o/linna_ovaska_3.png?alt=media&token=dcfe4d05-47f3-48e6-8e46-2910339caa82";

            // Use the equals() method on a Marker to check for equals.  Do not use ==.
            if (marker.getTitle().equals("Oulu Castle")) {
                badge = R.drawable.castlepin;
            }else if (marker.getTitle().equals("KOP-talo")){
                badge = R.drawable.pegman;
            } else {
                //default badge
               badge = R.drawable.greypin;
            }

            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);


    }}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }

        mStorageRef = FirebaseStorage.getInstance().getReference();

        //Initalize dropdownmenu
        dropDownMenuIconItem = findViewById(R.id.vertical_dropdown_icon_menu_items);

        //Initialize bottomnavigationview
        final BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setItemIconTintList(null);

        //bottomnavigationview onitemselected
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.oradot:
                        buildThumbnails("ruin");
                        Toast.makeText(MapsActivity.this, "pressed Ruin", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.bludot:
                        Toast.makeText(MapsActivity.this, "pressed Building", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.gredot:
                        Toast.makeText(MapsActivity.this, "pressed Statue", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.rosdot:
                        Toast.makeText(MapsActivity.this, "pressed Art", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });


    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    private void buildThumbnails(String type){


        // Get the border size to show around each image
        //int borderSize = _thumbnails.getPaddingTop();

        // Get the size of the actual thumbnail image
        //int thumbnailSize = ((FrameLayout.LayoutParams)
        //        _thumbnails.getLayoutParams()).bottomMargin - (borderSize*2);

        // Set the thumbnail layout parameters. Adjust as required
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, ViewGroup.LayoutParams.MATCH_PARENT, 0);

        final ImageView thumbView = new ImageView(_thumbnails.getContext());
        thumbView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbView.setLayoutParams(params);
        thumbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set the pager position when thumbnail clicked
            }
        });


        ArrayList<String> images = new ArrayList<String>();

        for(int i=0; i<Images.size(); i++) {
            if (Images.get(i).getType().equals(type)) {
                images.add(Images.get(i).getUrl());
                _thumbnails.addView(thumbView);
                Glide.with(this)
                        .load(images.get(i))
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            thumbView.setImageBitmap(bitmap);
                        }
                });
            }
        }

    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_retro);
        mMap.setMapStyle(style);


        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // We create the world and fill the world
        //mWorld = CustomWorldHelper.generateObjects(this);
        mWorld = new World(this);

        // The user can set the default bitmap. This is useful if you are
        // loading images form Internet and the connection get lost
        mWorld.setDefaultImage(R.drawable.arrow);
        // As we want to use GoogleMaps, we are going to create the plugin and
        // attach it to the World
        mGoogleMapPlugin = new GoogleMapWorldPlugin(this);
        // Then we need to set the map in to the GoogleMapPlugin
        mGoogleMapPlugin.setGoogleMap(mMap);
        // Now that we have the plugin created let's add it to our world.
        // NOTE: It is better to load the plugins before start adding object in
        // to the world.
        mWorld.addPlugin(mGoogleMapPlugin);

        // Lets add the user position to the map
        GeoObject user = new GeoObject(1000l);
        user.setGeoPosition(mWorld.getLatitude(), mWorld.getLongitude());
        user.setImageResource(R.drawable.pegman);
        user.setName("User position");
        mWorld.addBeyondarObject(user);


        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        //Initialize Google Play Services
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

        /** Fetching data from Firebase database and adding images*/
        mDatabase = FirebaseDatabase.getInstance().getReference("Urls");
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {


                String title = dataSnapshot.child("title").getValue().toString();
                String url = dataSnapshot.child("url").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();

                addImages(title,type,url);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

                String title = dataSnapshot.child("title").getValue().toString();
                String url = dataSnapshot.child("url").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();

                addImages(title,type,url);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


            }


            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });


        /** Fetching data from Firebase database and adding markers*/
        mDatabase = FirebaseDatabase.getInstance().getReference("Markers");
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {


                String latitude = dataSnapshot.child("latitude").getValue().toString();
                String longitude = dataSnapshot.child("longitude").getValue().toString();
                String title = dataSnapshot.child("title").getValue().toString();
                String snippet = dataSnapshot.child("snippet").getValue().toString();
                String type = dataSnapshot.child("type").getValue().toString();

                Markers newmarker = new Markers();
                newmarker.setLatitude(latitude);
                newmarker.setLongitude(longitude);
                newmarker.setTitle(title);
                newmarker.setSnippet(snippet);
                newmarker.setType(type);
                Markers.add(newmarker);

                addMarkersToMap(latitude,longitude,title,snippet,type);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


            }


            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        BeyondarLocationManager.addWorldLocationUpdate(mWorld);
        BeyondarLocationManager.addGeoObjectLocationUpdate(user);

        // We need to set the LocationManager to the BeyondarLocationManager.
        BeyondarLocationManager
                .setLocationManager((LocationManager) getSystemService(Context.LOCATION_SERVICE));

    }

    /**Add all markers from array*/
    private  void addAllMarkers(){

        for(int i=0; i<Markers.size(); i++)
        {
            //Get position
            double lat= Double.parseDouble(Markers.get(i).getLatitude());
            double lng= Double.parseDouble(Markers.get(i).getLongitude());
            LatLng position = new LatLng(lat, lng);

            String title = Markers.get(i).getTitle();
            String snippet = Markers.get(i).getSnippet();
            String type = Markers.get(i).getType();

            newMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .infoWindowAnchor(0.5f, 0.5f));

            if(type.equals("building")) {
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.orapin));
            }
            else  if(type.equals("ruin")) {
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.blupin));
            }
            else  if(type.equals("statue")) {
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.grepin));
            }
            else {
                newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.rospin));
            }
        }

    }

    /** Adding markers one at a time to the map */
    private void addMarkersToMap(String latitude, String longitude, String title, String snippet, String type) {



        double lat= Double.parseDouble(latitude);
        double lng= Double.parseDouble(longitude);
        LatLng position = new LatLng(lat, lng);

        GeoObject go3 = new GeoObject(3l);
        go3.setName(title);
        go3.setGeoPosition(position.latitude, position.longitude);

        newMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .snippet(snippet)
                .infoWindowAnchor(0.5f, 1.0f));


        if(type.equals("building")) {
            newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.orapin));
            go3.setImageResource(R.mipmap.orapin);
        }
        else  if(type.equals("ruin")) {
            newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.blupin));
            go3.setImageResource(R.mipmap.blupin);
        }
        else  if(type.equals("statue")) {
            newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.grepin));
            go3.setImageResource(R.mipmap.grepin);
        }
        else {
            newMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.rospin));
            go3.setImageResource(R.mipmap.rospin);
        }

        //if(calculateDistance(position)==false)
        // newMarker.setVisible(false);

        go3.setVisible(false);

        //mWorld.addBeyondarObject(go3);
        //CustomWorldHelper.sharedWorld.addBeyondarObject(go3);
    }

    public void sendNotification(String title, String message, String type) {


        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        int icon;

        if(type.equals("building")) {
            icon = R.mipmap.orapin;
        }
        else  if(type.equals("ruin")) {
            icon = R.mipmap.blupin;
        }
        else  if(type.equals("statue")) {
            icon = R.mipmap.grepin;
        }
        else {
            icon = R.mipmap.rospin;
        }

        b.setAutoCancel(true)
                    .setDefaults(0)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(icon)
                    .setTicker("{your tiny message}")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(intent)
                    .setContentInfo("INFO");


            NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            nm.notify(1, b.build());

    }

    @Override
    public void onMapClick(final LatLng point) {
        // Any showing info window closes when the map is clicked.
        // Clear the currently selected marker.
        mSelectedMarker = null;
        //dropDownMenuIconItem.setVisibility(View.INVISIBLE);

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setMaxWaitTime(1000);
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
        Log.d("onLocationChanged", "entered");



        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }


        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.visible(false);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mWorld.setGeoPosition(latitude,longitude);

        boolean placenearby = false;
        double closestmarkerdistance = 3000;
        String title = "";
        String type = "";


        //Look for nearby markers and send a notification for the closest point
        for(int i=0; i<Markers.size(); i++)
        {
            double lat= Double.parseDouble(Markers.get(i).getLatitude());
            double lng= Double.parseDouble(Markers.get(i).getLongitude());
            LatLng position = new LatLng(lat, lng);

            double distance = calculateDistance(position);

        //get the actual closest point
            if(distance<3000) {
                placenearby = true;
                if(distance<closestmarkerdistance && placenearby == true) {
                    closestmarkerdistance = distance;
                    title = Markers.get(i).getTitle();
                    type = Markers.get(i).getType();
                }

            }
        }

        //Only send a notification if there is a place nearby
        if (placenearby) {
            sendNotification(title + " nearby!", distanceToString(closestmarkerdistance),type);
        }

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

/**     //stop location updates
 *
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }
        Log.d("onLocationChanged", "Exit");
*/
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {
        // The user has re-tapped on the marker which was already showing an info window.
        if (marker.equals(mSelectedMarker)) {
            // The showing info window has already been closed - that's the first thing to happen
            // when any marker is clicked.
            // Return true to indicate we have consumed the event and that we do not want the
            // the default behavior to occur (which is for the camera to move such that the
            // marker is centered and for the marker's info window to open, if it has one).
            mSelectedMarker = null;
            return true;
        }
        GeoObject geoObject = mGoogleMapPlugin.getGeoObjectOwner(marker);
        if (geoObject != null) {
            Toast.makeText(this,
                    "Click on a marker owned by a GeoOject with the name: " + geoObject.getName(),
                    Toast.LENGTH_SHORT).show();
        }

        mSelectedMarker = marker;

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur.
        return false;
    }

    /**Calculates distance between current location and selected marker*/
    public double calculateDistance(LatLng location){

            Double Distance = SphericalUtil.computeDistanceBetween(mCurrLocationMarker.getPosition(), location);
            return Distance;

    }

    public String distanceToString(double distance){

        String distancestring;

        if(distance>999){
            distancestring = Double.toString(distance).substring(0,3);
            distancestring = distancestring.substring(0,1) + "." + distancestring.substring(1,3)  + "km away";
        }

        else
        distancestring = Double.toString(distance).substring(0,3) + "m away";

        return distancestring;
    }

    public void startGalleryActivity(String title, String snippet, String distance, String type) {

        ArrayList<String> images = new ArrayList<String>();

        for(int i=0; i<Images.size(); i++) {
            if (Images.get(i).getTitle().equals(title)) {
                images.add(Images.get(i).getUrl());
            }
        }

        Intent intent = new Intent(MapsActivity.this, GalleryActivity.class);
        intent.putStringArrayListExtra(GalleryActivity.EXTRA_NAME, images);
        intent.putExtra(GalleryActivity.TYPE, type);
        intent.putExtra(GalleryActivity.SNIPPET, snippet);
        intent.putExtra(GalleryActivity.TITLE, title);
        intent.putExtra(GalleryActivity.DISTANCE, distance);
        startActivity(intent);
    }

    public void addImages(String title, String type, String url){
        Images newimage = new Images();
        newimage.setTitle(title);
        newimage.setType(type);
        newimage.setUrl(url);
        Images.add(newimage);
    }


}
