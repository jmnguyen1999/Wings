package com.example.wings.mainactivity.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wings.DataParser;
import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.User;
import com.example.wings.models.WingsGeoPoint;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * HomeFragment.java
 * Purpose:         This displays the default screen of the app!
 *
 * Hello Coders! Here is a good sample of how the interface works with the Fragments! Please read through the documentation and let me know if you have any questions or comments on how we could be
 * implementing this better!
 *
 */

public class HomeFragment extends Fragment implements LocationListener {
    private static final String TAG = "HomeFragment";
    private static final long UPDATE_INTERVAL = 5000;
    private static final long FASTEST_INTERVAL = 3000;

    ParseUser currUser = ParseUser.getCurrentUser();

    private MAFragmentsListener listener;       //notice we did not "implements" it! We are just using an object of this interface!
    private FloatingActionButton fabEndRoute;

    Location currentLocation;
    private GoogleMap map;
    private UiSettings mapUI;
    private SupportMapFragment mapFragment;

    //Routing stuff:
    private LatLng startLocation;
    private LatLng destination;
    private Polyline polyline;
    ArrayList<LatLng> markerPoints;
    MarkerOptions startMarker;
    MarkerOptions endMarker;

    List<LatLng> mapCoordinates;            //after we get the route, stores all intermediate coordinates (LatLngs) needed to get from startLocation to destination (froom decoding the polyline)
    //layout views:
    Button btnSearch;
    EditText etSearchBar;


    public HomeFragment() {}    // Required empty public constructor

    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement MAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }

    @Override
    /**
     * Purpose:         Called automatically when creating Fragment instance. To inflate a corresponding layout file "fragment_home.xml"
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //1.) Initialize view:
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //2.) Initialize map fragment:
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        markerPoints = new ArrayList<>();

        //3.) Error check mapFragment
        if (mapFragment != null) {

            //3a.) getMapAsync() --> initializes maps system and view:
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    //when map is ready --> load it
                    loadMap(googleMap);
                }
            });
        } else {
            Toast.makeText(getContext(), "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    /*

        @Override
        /**
         * Purpose;         Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
         */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Changes the Fragment to the ChooseBuddyFragment via the MainActivity!
        /*chooseBuddyBttn = view.findViewById(R.id.registerBttn);
        chooseBuddyBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.toChooseBuddyFragment();
            }
        });*/

        btnSearch = view.findViewById(R.id.btnSearch);
        etSearchBar = view.findViewById(R.id.etSearchBar);
        fabEndRoute = (FloatingActionButton) view.findViewById(R.id.fabEndRoute);

        //To delete/cancel a route
        fabEndRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset destination to 0:
                WingsGeoPoint currDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_MAPDESTINATION);
                currDestination.setLatitude(0);
                currDestination.setLongitude(0);
                currDestination.setLocation(0,0);
                currUser.put(User.KEY_MAPDESTINATION, currDestination);
                currDestination.saveInBackground();
                fabEndRoute.setVisibility(View.INVISIBLE);
            }
        });

        //set listener: onClick() --> search for and route to a location
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchArea();
                fabEndRoute.setVisibility(View.VISIBLE);    //TODO: only do this when user is for sure on a route and not just clicking the button many times
                //Testing creation of dialog:
                /*final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.cancel_route_dialog);
                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(getContext(),"Dismissed..!!",Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.show();*/
            }
        });

    }

    //Purpose:          Initializes our "map" field, starts continuously checking for location updates
    protected void loadMap(GoogleMap googleMap) {
        Log.d(TAG, "in loadMap():");
        map = googleMap;

        //SHOULD never have to worry about permissions as MainActivity does it for us, TODO:would be best to create method to call the MainActivity to ask for permissons again instead of assumming
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "loadMap(): Permissions are not granted to do the map");
            return;
        }

        map.setMyLocationEnabled(true);                 //enables the "my-location-layer"

        //Get the saved current location from database, move map to it
        ParseUser currUser = ParseUser.getCurrentUser();
        WingsGeoPoint initalLoc = (WingsGeoPoint) currUser.getParseObject(User.KEY_CURRENTLOCATION);
        WingsGeoPoint initalDestination = (WingsGeoPoint) currUser.getParseObject(User.KEY_MAPDESTINATION);

        //If the user's "mapDestination" field is not yet declared:
        if(initalDestination == null) {
            initalDestination = new WingsGeoPoint(currUser, 0, 0);
            currUser.put(User.KEY_MAPDESTINATION, initalDestination);
            currUser.saveInBackground();
        }

            try {
                //Actually fetch the data:
                initalLoc.fetchIfNeeded();
                initalDestination.fetchIfNeeded();

                //Check if there is already a destination we should be routing to:
                if (initalDestination.getLatitude() != 0) {
                    Log.d(TAG, "loadMap(): an inital destination exists! Routing to it...");
                    startLocation = new LatLng(initalLoc.getLatitude(), initalLoc.getLongitude());
                    destination = new LatLng(initalDestination.getLatitude(), initalDestination.getLongitude());
                    route();

                    //move map camera to current location for now
                    map.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
                    map.animateCamera(CameraUpdateFactory.zoomTo(11));      //12 = how much to zoom to, can make constant
                }
                //Otherwise --> there is no destination to map to yet
                else{
                    fabEndRoute.setVisibility(View.INVISIBLE);

                    Log.d(TAG, "loadMap(): no inital destination yet, just show current location!");
                    //Place current location marker
                    startLocation = new LatLng(initalLoc.getLatitude(), initalLoc.getLongitude());
                    Log.d(TAG, "loadMap(): location from parse: " + initalLoc.getLatitude() + " , " + initalLoc.getLongitude());

                    //move map camera
                    map.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
                    map.animateCamera(CameraUpdateFactory.zoomTo(15));      //12 = how much to zoom to, can make constant
                }
            } catch (ParseException e) {
                Log.d(TAG, "loadmap(): could not fetch the current user's currentLocation field or mapDestination field");
                e.printStackTrace();
            }

        //Set UI of google map:
        mapUI = map.getUiSettings();
        mapUI.setMyLocationButtonEnabled(true);
        mapUI.setZoomControlsEnabled(true);
        mapUI.setZoomGesturesEnabled(true);

        startLocationUpdates();             //To constantly get current location and display update on the map
    }


    //Purpose:          Obtains the current location of the user to display on the map constantly, DOES NOT do anything with the Parse database. This is simply to display on the Fragment ONLY!
    protected void startLocationUpdates() {
        Log.d(TAG, "in startLocationUpdates()");

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        //2.) Start building a LocationSettingsRequest object to make send the LocationRequest:
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        //3.) Get the SettingsClient object to interact with the settings --> can tell whether certain settings are on/off
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());

        //3a.) Check if our LocationRequest was fulfilled
        settingsClient.checkLocationSettings(locationSettingsRequest);


        //4.) Check if we have granted permissions for "ACCESS_FINE_LOCATION" and "ACCESS_COURSE_LOCATION", MainActivity should have done this already
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "startLocationUpdates(): Permissions are not granted to do the map");
            return;
        }

        // Obtain the FusedLocationProviderClient --> to interact with the location provider:
        // to request updates on the current location constantly on a Looper Thread
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    //When obtains the location update --> updates our Location field, "currentLocation"
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "in onLocationResult()");
                        onLocationChanged(locationResult.getLastLocation());        //our method

                    }
                },
                Looper.myLooper()
        );
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "in onLocationChanged");
        if (location != null) {
            currentLocation = location;
            startLocation = new LatLng(location.getLatitude(), location.getLongitude());        //to change map display automatically
        }
    }

    //Purpose:          Called when user clicks on the search button, Get the text in the Searchbar, use Geocoder to find the LatLng, set a marker at that LatLng, attempt to make a Network request to the Google Directions API
    private void searchArea() {
        Log.d(TAG, "in searchArea()");

        String destinationTxt = etSearchBar.getText().toString();
        List<Address> addressList = new ArrayList<>();
        MarkerOptions markerOptions = new MarkerOptions();

        //Check if there is a location text to search for:
        if(!destinationTxt.equals("")){
            Geocoder geocoder = new Geocoder(getContext().getApplicationContext());         //May need to be getApplicationContext or getContext

            //1.) Get the address list from the geocoder:
            try {
                addressList = geocoder.getFromLocationName(destinationTxt, 5);
            } catch (IOException e) {
                Log.d(TAG, "searchArea(): error with geocode to List<Address>, error=" + e.getLocalizedMessage());
                e.printStackTrace();
            }

            //2.) Ensure addressList is not null:
            if(addressList != null){
                for(int i = 0; i < addressList.size(); i++){

                    //Initialize LatLng of destination field
                    Address destinAddress = addressList.get(i);

                    destination = new LatLng(destinAddress.getLatitude(), destinAddress.getLongitude());
                    setUserDestination();           //uses the destination class field, may need to change this

                    //Set and add marker to that destination
                    markerOptions.position(destination);
                    map.addMarker(markerOptions);
                    map.animateCamera(CameraUpdateFactory.newLatLng(destination));

                    MarkerOptions mo = new MarkerOptions();
                    mo.title("Distance");
                    float[] results = new float[10];
                    Location.distanceBetween(startLocation.latitude, startLocation.longitude, destination.latitude, destination.longitude, results);

                    String s = String.format("%.1f", results[0]/1000);      //convert distance to km

                    //Set and show startMarker and endMarker:
                    startMarker = new MarkerOptions().position(startLocation).title("HSR Layout").snippet("startMarker");
                    endMarker = new MarkerOptions().position(destination).title(destinationTxt).snippet("Distance = " + s + " KM");
                    map.addMarker(endMarker);
                    Toast.makeText(getContext(), "Distance = " + s + " KM", Toast.LENGTH_SHORT).show();

                    //route - will use the class fields startLocstion an destination as endpoints
                    route();
                }
            }
        }
    }

    //Purpose:      Finds API URL needed, makes API call through DownloadTask, draws Polyline (and all decoding needewd) in ParserTask
    private void route(){
        String url = getDirectionsURL();

        DownloadTask downloadTask = new DownloadTask();         //postexecute() will call ParserTask automatically
        downloadTask.execute(url);
    }

    private String getDirectionsURL(){
        String startStr = "origin=" + startLocation.latitude + "," + startLocation.longitude;
        String endStr = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=walking";
        String parameters = startStr + "&" + endStr + "&" + mode;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/"+output + "?" + parameters + "&key=AIzaSyC1c3vYFZDb2Ebr1uZbtt-IjGNzARXgVho";
    }

    private void setMappingCoordinates(List<LatLng> newMapCoordinates){
        Log.d(TAG, "setMappingCoordinates(): newMapCoordinates= " + newMapCoordinates.toString());
        mapCoordinates = newMapCoordinates;
    }

    //Save the destination in Parse Database, technically should be doing this here
    private void setUserDestination(){
        Log.d(TAG, "setUserDestination()");
        ParseUser currUser = ParseUser.getCurrentUser();
        currUser.put(User.KEY_MAPDESTINATION, new WingsGeoPoint(currUser, destination.latitude, destination.longitude));
        currUser.saveInBackground();
    }
/*
    @Override
    public void onResume() {
        super.onResume();

        // Display the connection status

        if (currentLocation != null) {
            Toast.makeText(getContext(), "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);            //animates the movement of the camera to the position in this Camera Update
        } else {
            Toast.makeText(getContext(), "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        startLocationUpdates();
    }*/

    //Purpose:          Makes the network request to the Google Directions API given the correct URL, returns the entire JSONObject as a string
    private String downloadURL(String urlStr) throws IOException {
        String data = "";
        InputStream inStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            inStream = urlConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuffer.append(line);
                line = bufferedReader.readLine();
            }

            data = stringBuffer.toString();
            bufferedReader.close();
        }catch(IOException e){
            Log.d(TAG, "in downloadURL(): exception=" + e.getLocalizedMessage());
        }
        finally {
            inStream.close();
            urlConnection.disconnect();
        }

        Log.d(TAG, "in downloadURL(): data=" + data);
        return data;
    }

    //Purpose:          To make network request in an async task (in the background), finds the JSONObject that has the routes --> gives to ParserTask to parse the JSONObject
    //                  and get the LatLngs needed while going on the trip. ParseTask --> initializes mapCoordinates (List<LatLng>)
    public class DownloadTask extends AsyncTask<String, Void, String>{
        @Override
        //s = the entire JSONObject result from the api call, but it's a string --> Parser task will
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "DownloadTask: in onPostExecute()");
            ParserTask parserTask = new ParserTask();
            parserTask.execute(s);
        }

        @Override
        //Purpose:          called when you use "execute()", calls downloadURL to make the actual request to the Google Directions API, data = the entire JSON Object result
        protected String doInBackground(String[] url) {
            String data = "";
            Log.d(TAG, "DownloadTask: doInBackground(): url[0] = " + url[0]);

            try {
                data = downloadURL(url[0]);
            } catch (IOException e) {
                Log.d(TAG, "DownloadTask: downloadURL exception, error = " + e.getLocalizedMessage());
                e.printStackTrace();
            }

            return data;
        }
    }

    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>{

        @Override
        //Purpose:      Given all possible routes from a trip, basically draws the last one, but this assumes there's only one route anyway I think
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            super.onPostExecute(result);
            Log.d(TAG, "ParserTask: in onPostExecute()");

            ArrayList<LatLng> points = new ArrayList<LatLng>();
            PolylineOptions lineOptions = new PolylineOptions();
            //For every route in the List:
            for(int i = 0; i < result.size(); i++){
                List<HashMap<String, String>> path = result.get(i);

                //Go through every LatLng stored in that route/every LatLng need to pass to get to destination
                for( int j = 0; j < path.size(); j++){
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    //Add the LatLng to a List of LatLng
                    points.add(position);
                }

                //After you save all the LatLngs of a route into a List --> set the display of the route
                //TODO: technically this logic is flawed as it assumes there is only 1 route, I think
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
            Log.d(TAG, "ParserTask: in onPostExecute(): points=" + points.toString());

            setMappingCoordinates(points);          //save our List of LatLngs into a field so we have access to it

            //Displays the route through the seeetings of the lineOptions, this technically being here means it would only display the last route listed in the given List of routes
            if(points.size() != 0){
                Log.d(TAG, "ParserTask: in onPostExecute(): adding polyline to map");
                map.addPolyline(lineOptions);
            }
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String[] jsonData) {
            Log.d(TAG, "ParserTask: in doInBackground()");
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jsonObject = new JSONObject(jsonData[0]);        //Makes the String representing a JSONObject into an actual JSONObject
                DataParser parser = new DataParser();           //DataParser does the actual parsing of the JSONObject
                routes = parser.parse(jsonObject);              //returns all possible routes from the JSONObject

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }
    }
}