package theappfoundry.lifestyles;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
import android.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices; // Had to put 2 dependencies in gradle

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Geofencing extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> , OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnTouchListener{


    /**
     * Bounds of the rectangle used to create the geofence
     */
    private float left;
    private float top;
    private float right;
    private float bot;

    private int nameCounter;

    private ShapeDrawable mDrawable; // The geofence rectangle being drawn

    private Boolean isDrawingGeofence = false;

    private GoogleApiClient mGoogleApiClient;
    private String mLatitudeText;
    private String mLongitudeText;
    private String TAG = "GeoFencing";

    private Button addMapButton; // Button to add the MapFragmentActivity
    private Button locationButton; // Toasts lat/long to user
    private Button addGeofenceButton; // adds Geofences when clicked
    private Button startGeoFencingButton;
    private Button confirmLocation;

    private Location mCurrentLocation; // holds references to location passed in, in onLocationChanged
    private LocationRequest mLocationRequest; // for a location request..

    private TextView latText; // To display location updates
    private TextView longText; // To display location updates

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MapFragment mapFragment;

    private GoogleMap myMap;


    private CameraPosition cameraPosition; // position for camera to be


    //////////////////////// Drawing Rectangle /////////////////////////////////////

    private CustomDrawableView mCustomDrawableView; // Extends view and makes the rectangle.
    private RelativeLayout myLinearLayout;

    public float actionBarHeight;
    public float titleBarHeight;

    private Rect outline; // Making rectangle outline
    private Paint strokePaint; // For rectangle outline color, thickness (stroke)
    private Paint paint; // setting fill color of rectangle.




    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;


    // List of Geofences
    protected ArrayList<Geofence> mGeofenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofencing);

        Log.d(TAG, "onCreate: ");


        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;


        locationButton = (Button) findViewById(R.id.locationButton);
        addGeofenceButton = (Button) findViewById(R.id.addGeofenceButton);
        addMapButton =  (Button) findViewById(R.id.addMapButton);
        startGeoFencingButton = (Button) findViewById(R.id.startGeoFencing);
        confirmLocation = (Button)findViewById(R.id.confirmLocation);


        locationButton.setOnTouchListener(this);
        addGeofenceButton.setOnTouchListener(this);
        addMapButton.setOnTouchListener(this);
        startGeoFencingButton.setOnTouchListener(this);
        confirmLocation.setOnTouchListener(this);

        latText = (TextView) findViewById(R.id.latText);
        longText = (TextView) findViewById(R.id.longText);


        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();

        buildGoogleApiClient(); // Makes an instance of the google API

        // Get the geofences used. Geofence data is hard coded in this sample.
        // populateGeofenceList(); // What geofences do I want added
        // specifies geofences, and how they should act


        setBarHeights(); // see func. definition.
        setColorPaint(); // For setting color of rectangle. Don't need to recreate
        myLinearLayout = (RelativeLayout)findViewById(R.id.rectangleLayout);
        mCustomDrawableView = new Geofencing.CustomDrawableView(this); // Make instan0-ce of CustomDrawableView


    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    // To actually connect to Gplay Services
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();



        confirmLocation.setVisibility(View.INVISIBLE);


    }

    // To actually disconnect from Gplay Services
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    // To request the last known location, call the getLastLocation() method, passing it your
    // instance of the GoogleApiClient object. Do this in the onConnected() callback provided by
    // Google API Client, which is called when the client is ready
    @Override
    public void onConnected(Bundle connectionHint) {

        /**
         * suppose to be called in onConnected (Bc you need it to be connected first.)
         * Before requesting location updates, your app must connect to location services and make a location request
         */
        createLocationRequest();
        /**
         * suppose to be called in onConnected (Bc you need it to be connected first.)
         * invokes callback onLocationChanged
         */
        startLocationUpdates();


    }


    /**
     *
     */
    protected void startLocationUpdates() {

        // need to request permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    // setFastestInterval() - This method sets the fastest rate in milliseconds at which your app
    // can handle location updates need to set this rate because other apps also affect the rate at
    // which updates are sent... (GPS,WIFI, and CELLULAR) are used to calcualte location. WIFI is the
    // slowest.
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }




    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location; // location object containing lat/long.. Most recent location update
        // mLastUpdateTime = DateFormat.getTimeInstance().format(new Date()); Need higher min API
        updateUI(); // Changes the textviews on the screen to display Lat/Long

    }


    private void updateUI() {

        latText.setText(String.valueOf(mCurrentLocation.getLatitude()));
        longText.setText(String.valueOf(mCurrentLocation.getLongitude()));
        //mLastUpdateTimeTextView.setText(mLastUpdateTime);

    }


    /**
     * Map.Entry<String, LatLng> is the data type. Entry is essentially a container for
     * each entry in the STUDY_LOCATONS hashmap. It iterates through all of the entries and puts
     * them in the variable entry. Then builds a geofence using new Geofence.Builder() which returns
     * a reference to a geofence.
     */
    public void populateGeofenceList() {

        // Map.Entry<String, LatLng> is the data type. Entry is essentially a container for
        // each entry in the STUDY_LOCATONS hashmap. It iterates through all of the entries and puts
        // them in the variable entry.
        for (Map.Entry<String, LatLng> entry : GeofenceLocations.Geofences.entrySet())
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setLoiteringDelay(10000) // 30 sesc till Initiali DWELL trigger is set off


                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
                            | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build());

    }


    //. The GEOFENCE_TRANSITION_ENTER transition triggers when a device enters a geofence,
    // and the GEOFENCE_TRANSITION_EXIT transition triggers when a device exits a geofence.
    // Specifying INITIAL_TRIGGER_ENTER tells Location services that GEOFENCE_TRANSITION_ENTER
    // should be triggered if the the device is already inside the geofence.
    // Dwell - User has to be in the geofence for a certain amount of time.
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Toast.makeText(this, "GetGeofencePendingIntent", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    public void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }catch(SecurityException securityException){
            Log.d(TAG, securityException.getMessage());
            Toast.makeText(this, "Did not add geofence", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "ADDED GEOFENCE", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onResult(@NonNull Status status) {
        Toast.makeText(this, "ONRESULT", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show();
    }



    /**
     * CallBack method from getMapAsync(), callback triggered when map is ready for use
     * Can use GoogleMap object to set view options for the map or a marker for ex.
     * @param googleMap  non null instance of GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
//        googleMap.addMarker(new MarkerOptions()
//                .position(new LatLng(29.894529,-97.908082))
//                .title("Marker"));

        myMap = googleMap; // googleMap holds a reference to myMap


    }


    @Override
    public void onMapClick(LatLng latLng) {



    }

    /**
     * If dispatch event doesn't consume the event it will come here. This is used instead of
     * onClick as well
     * @param v - The veiw that was touched
     * @param event - The touch event. Was it a touch up,down,move.. ect..
     * @return true if the touch event was consumed
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {


        Log.d(TAG, "onTouch: TEST");

        int action = MotionEventCompat.getActionMasked(event);
        int i = v.getId();

        if(action == MotionEvent.ACTION_DOWN) {
            if (i == R.id.locationButton) {

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    // The below statement requests permissions
//                    ActivityCompat.requestPermissions(this,
//                            new String[]{
//                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                            REQUEST_COURSE_ACCESS)
                    return true;
                }
                mLatitudeText = (String.valueOf(mCurrentLocation.getLatitude())); // get updated value
                mLongitudeText = (String.valueOf(mCurrentLocation.getLongitude())); // get updated value
                Toast.makeText(this, mLatitudeText + " " + mLongitudeText, Toast.LENGTH_SHORT).show();

            } else if (i == R.id.addGeofenceButton) {
                if(GeofenceLocations.Geofences != null){
                    populateGeofenceList(); // add the rectangle the user created to geofence list.
                    // probably don't need.. It get's called in addGeofences getGeofencingRequest();
                    addGeofences();
                    return true;
                }
            } else if (i == R.id.addMapButton) {

                /**
                 * Builds and returns a camera position for the google maps
                 */
                cameraPosition = CameraPosition.builder()
                        .target(Constants.STUDY_LOCATIONS.get("LivingRoom")) // Where? Lat/Long
                        .zoom(19) // Increasing zoom size, doubles width of visible world
                        .build(); // returns a cameraPosition instance


                GoogleMapOptions options = new GoogleMapOptions(); // options for map

                /**
                 * Specify GoogleMapOptions.. Can add more!
                 */
                options.mapType(GoogleMap.MAP_TYPE_SATELLITE)
                        .camera(cameraPosition);

                /**
                 * Make an instance of the MapsActivity which is a FragmentActivity
                 */
                mapFragment = MapFragment.newInstance(options);

                /**
                 * You need this to start adding fragments. Used getSupportFragmentManager() instead of
                 * getFragmentManager because its a FragmentActivity not just Fragment
                 */
                fragmentManager = getFragmentManager();
                /**
                 *FragmentTransaction class to perform fragment transactions (such as add,
                 remove, or replace) in your activity:
                 */

                /**
                 * Makes FragmentTransaction instance to add, replace, remove ect.
                 */
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.mapLayout, mapFragment); // adds the mapFragment to the Linear Layout
                mapFragment.getMapAsync(this); // set the callback for the fragment
                fragmentTransaction.commit(); // Have to commit for it to go through!
                return true;


            } else if (i == R.id.startGeoFencing) {
                isDrawingGeofence = !isDrawingGeofence;
                Log.d(TAG, "BENNY" + isDrawingGeofence);
                return true;

            }else if(i == R.id.confirmLocation){
                confirmLocation.setVisibility(View.INVISIBLE);
                myLinearLayout.removeAllViews();
                GeofenceLocations.convertAndAddGeofence("Bens House",left,top,right,bot,myMap);

                myMap.addMarker(new MarkerOptions()
                        .position(GeofenceLocations.Geofences.get("Bens House"))
                        .title("Marker"));
                isDrawingGeofence = false;
                return true;
            }
        }
        return false; // If false then no touch event has been consumed in OnTouch


    }


    /**
     * Makes the CustomDrawableView ( The rectangle ) Has all the rectangles attributes as variables
     * These attributes are updated within it's draw method that is called when a MotionEvent occurs
     * Inside the draw method the rectangle and it's outline (stroke) are also created.
     * This class extends view which invokes the onDraw callback method. This is where the shape
     * is draw with the canvas that is passed in.
     */
    public class CustomDrawableView extends View {


        public float x = 0;
        public float y = 0;
        public float width;
        public float height;
        public final int lightBlue = ContextCompat.getColor(getContext(), R.color.lightBlue);


        public CustomDrawableView(Context context) {
            super(context);
            mDrawable = new ShapeDrawable(new RectShape());
        }

        public void draw(MotionEvent event) {



            this.width = event.getX();
            this.height = event.getY() - (actionBarHeight + titleBarHeight);


//            Testing to see if values are correct.
//            Log.d(TAG, "Width: " + this.width);
//            Log.d(TAG, "height: " + this.height);
//            Log.d(TAG, "x: " + this.x);
//            Log.d(TAG, "y: " + this.y);


            mDrawable.getPaint().setColor(lightBlue); // Color of rectangle
            mDrawable.getPaint().setAlpha(150); // Transparency from 0-255



            /**
             * Left,Top,Right,Bottom: where width = right - left, and height = bottom - top
             * Do min and max for these (x & width) and (y and height) to find absolute values.
             * You can't have negative values for setBounds or it won't draw.
             * It would be negative because of how you find the height and width.
             */
            mDrawable.setBounds(Math.min(Math.round(x),Math.round(width)),
                    Math.min(Math.round(y), Math.round(height)),
                    Math.max(Math.round(x), Math.round(width)),
                    Math.max(Math.round(y), Math.round(height)));


            // Have to apply the same rules as above when we set the bounds of mDrawable.
            outline = new Rect(Math.min(Math.round(x),Math.round(width)),
                    Math.min(Math.round(y), Math.round(height)),
                    Math.max(Math.round(x), Math.round(width)),
                    Math.max(Math.round(y), Math.round(height)));


        }


        /**
         * Callback function for extending a view. Where stuff will be drawn onto the screen
         * @param canvas - Have to draw through this canvas, which is actually being drawn on
         *               whatever bitmap that is passed to the canvas (Done automatically
         */
        @Override
        protected void onDraw(Canvas canvas) {
            mDrawable.draw(canvas); // canvas handles what to draw.
            // Canvas acts like a pen, drawsRect onto bitmap
            canvas.drawRect(outline, strokePaint);

        }




    }

    /**
     * The activity Bar + the Status bar throw off Where exactly the drawing occurs for the
     * rectangle. So have to calculate these heights and subtract them from both the y, and the
     * height from CustomShapeDrawable. I believe it's because the Motion Event is tracking the
     * whole screen but the LinearLayout that the view (CustomViewDrawable) is being placed in
     * is not the whole screen. --- Calculate Activity Bar & Status Bar heights
     */
    public void setBarHeights() {

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        titleBarHeight = contentViewTop - statusBarHeight;

        Log.d(TAG, "setBarHeights: " + titleBarHeight);

    }





    /**
     * Activity.dispatchTouchEvent() <-- What I'm doing.. because I'm implementing it in the class
     * Watch: https://www.youtube.com/watch?v=SYoN-OvdZ3M for Android Touch Events
     * Always first to be called. Sends event to root view attached to Window (In this case the
     * constraint layout) The reason I implemented this was to re-route the touch events coming in
     * for Google Maps to be re routed to drawing geofences whenever needed.
     * @param event - MotionEvent is passed in whenever user touches the screen.
     * @return - True if the touch event has been consumed. Return super.dispatchTouchEvent(event)
     * if you want the touch event to be handled normally. (Which is what I want for every case but
     * when I'm drawing geofences over Google Maps.)
     */

    private boolean isInSideClicked = false; // To determine if a click occured inside GoogleMaps
    public boolean dispatchTouchEvent(MotionEvent event) {

        int action = event.getAction();

        if(action == MotionEvent.ACTION_DOWN) {

            //Making a region where we can deterimine if the click was on GoogleMaps or not
            Rect rect = new Rect(0, 0, myLinearLayout.getWidth(), myLinearLayout.getHeight());
            if (rect.contains((int) event.getX(), (int) event.getY())) {
                isInSideClicked = true;
            }
            else isInSideClicked = false; // Reset

            if(isInSideClicked && isDrawingGeofence){

                Log.d(TAG, "Action was DOWN");
                // Rectangle may have been drawn somewhere else.
                myLinearLayout.removeAllViews(); // remove old one
                mCustomDrawableView.x = event.getX(); // Where finger is touching. update x,y
                mCustomDrawableView.y = event.getY() - (actionBarHeight + titleBarHeight);
                mCustomDrawableView.draw(event); // Might not need to2 call. No wid/height on// initial touch
                myLinearLayout.addView(mCustomDrawableView);
                myLinearLayout.bringToFront();
                return true;
            }
            else{
                return super.dispatchTouchEvent(event);
            }


        }else if (action == MotionEvent.ACTION_MOVE && isInSideClicked) {

                Log.d(TAG, "isdraw " + isDrawingGeofence);
                if (isDrawingGeofence) {
                    Log.d(TAG, "Action was MOVE");
                    myLinearLayout.removeAllViews(); // remove old drawn rectangle from LinearLayout
                    mCustomDrawableView.draw(event); // draw new rectangle passing in the MotionEvent
                    myLinearLayout.addView(mCustomDrawableView); //Once it's been drawn.. add to LinearLayout
                    myLinearLayout.bringToFront();

                    return true;
                } else {
                    return super.dispatchTouchEvent(event);

                }
        }else if(action == MotionEvent.ACTION_UP && isInSideClicked){

            Log.d(TAG, "Action was UP");
            left = Math.min(Math.round(mCustomDrawableView.x),Math.round(mCustomDrawableView.width));
            top = Math.min(Math.round(mCustomDrawableView.y), Math.round(mCustomDrawableView.height));
            right = Math.max(Math.round(mCustomDrawableView.x), Math.round(mCustomDrawableView.width));
            bot =  Math.max(Math.round(mCustomDrawableView.y), Math.round(mCustomDrawableView.height));

            if(isDrawingGeofence) {
                confirmLocation.setVisibility(View.VISIBLE);
                isDrawingGeofence = false;
            }

            Log.d(TAG, "Left: " + left + " Top: " + top + " Right: " + right + "Bottom: " + bot);

            return true;
        }


        // No touch event is consumed. Put touch event back to normal flow.
        return super.dispatchTouchEvent(event);

    }


    /**
     * Setting the stroke of a rectangle which acts as an outline to our CustomDrawableView
     * paint handles, How to draw
     */
    public void setColorPaint(){

        paint = new Paint();
        strokePaint = paint;
        strokePaint.setARGB(125, 51, 89, 150);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(10);
    }









}
