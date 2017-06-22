package theappfoundry.lifestyles;

import android.animation.ArgbEvaluator;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
import android.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices; // Had to put 2 dependencies in gradle

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import yuku.ambilwarna.AmbilWarnaDialog; // For Color Picker

public class Geofencing extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener , OnMapReadyCallback, View.OnTouchListener, AmbilWarnaDialog.OnAmbilWarnaListener, PlaceSelectionListener, DialogWindowPopUp.NoticeDialogListener, GoogleMap.OnMarkerClickListener {


    private ConstraintLayout searchBarContainer;
    private LinearLayout leftToolBarContainer;


    /**
     * Bounds of the rectangle used to create the geofence
     */
    private float left;
    private float top;
    private float right;
    private float bot;

    private ShapeDrawable mDrawable; // The geofence rectangle being drawn

    private Boolean isDrawingGeofence = false;
    private Boolean fill; // checking to see if fill or stroke button was last clicked.

    private GoogleApiClient mGoogleApiClient;
    private String mLatitudeText;
    private String mLongitudeText;
    private String TAG = "GeoFencing";

    private Button addMapButton; // Button to add the MapFragmentActivity
    private Button locationButton; // Toasts lat/long to user
    private Button addGeofenceButton; // adds Geofences when clicked
    private Button startGeoFencingButton;
    private Button confirmLocation;

    // Toolbar Buttons
    private ImageButton fillButton;  // fill bucket for Geofence
    private ImageButton strokeButton; // stroke bucket for Geofence
    private ImageButton drawGeoButton; //
    private ImageButton panGMapsButton;

    private Spinner jumpToSpinner;
    private ArrayList<String> spinnerList; // List of strings of locations used in Spinner.

    private Location mCurrentLocation; // holds references to location passed in, in onLocationChanged
    private LocationRequest mLocationRequest; // for a location request..

    private TextView latText; // To display location updates
    private TextView longText; // To display location updates
    private static TextView timeText;

    private PlaceAutocompleteFragment searchFragment;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MapFragment mapFragment;

    private android.support.v4.app.FragmentManager dialogFragmentManager;

    private GoogleMap myMap;
    private GoogleMapOptions options;

    private CameraPosition cameraPosition; // position for camera to be

    private AmbilWarnaDialog colorPicker; // ColorPicker for geofence

    // Have to have resolved color to pass it into paint.setColor for the drawable..
    // whatever color is currently selected by the user.. Light blue is default
    private int currentFillColor;
    private int currentStrokeColor;
    // this is set in colorPicker onOk listener. Then currentColor = fill or Stroke..
    private int currentColor;

    Toolbar myToolbar; // custom toolbar.. use toolbar to set ActionBar
    private int statusBarHeight; // Status bar - where the time and wifi is
    public float actionBarHeight; // (Top Nav Bar)
    private ActionBar myActionBar; // The top nav bar


    //////////////////////// Drawing Rectangle /////////////////////////////////////

    private CustomDrawableView mCustomDrawableView; // Extends view and makes the rectangle.
    private RelativeLayout myRectLayout;
    private ConstraintLayout mapLayout;

    private Rect outline; // Making rectangle outline
    private Paint strokePaint; // For rectangle outline color, thickness (stroke)
    private Paint paint; // setting fill color of rectangle.

    /////////////////////////////////////////////////////////////////////////////////

    private DatabaseAdapter databaseAdapter;





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

        setStatusBarHeight();

        Log.d(TAG, "onCreate: ");

        myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar); // sets toolbar as app bar for the activity
        // Returns reference to an appcompat ActionBar object
        myActionBar = getSupportActionBar();


        myActionBar.setDisplayHomeAsUpEnabled(true); // sets the up button on the action bar

        Typeface quickSandRegular = Typeface.createFromAsset(getAssets(), "Quicksand-Regular.otf");
        // Set Title :)



        myActionBar.setIcon(R.drawable.cronos_logo_v1);



        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;


        locationButton = (Button) findViewById(R.id.locationButton);
        addGeofenceButton = (Button) findViewById(R.id.addGeofenceButton);
       // addMapButton =  (Button) findViewById(R.id.addMapButton);
        drawGeoButton = (ImageButton) findViewById(R.id.drawGeoButton);
        //confirmLocation = (Button)findViewById(R.id.confirmLocation);
        fillButton = (ImageButton)findViewById(R.id.fillButton);
        strokeButton = (ImageButton)findViewById(R.id.strokeButton);



        locationButton.setOnTouchListener(this);
        addGeofenceButton.setOnTouchListener(this);
//        addMapButton.setOnTouchListener(this);
        drawGeoButton.setOnTouchListener(this);
//        confirmLocation.setOnTouchListener(this);
        fillButton.setOnTouchListener(this);
        strokeButton.setOnTouchListener(this);

        colorPicker = new AmbilWarnaDialog(this, Color.GREEN, this);

        latText = (TextView) findViewById(R.id.latText);
        longText = (TextView) findViewById(R.id.longText);
        timeText = (TextView) findViewById(R.id.CurrentTimeAmt);


        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();

        buildGoogleApiClient(); // Makes an instance of the google API

        // Get the geofences used. Geofence data is hard coded in this sample.
        // populateGeofenceList(); // What geofences do I want added
        // specifies geofences, and how they should act


        setBarHeights(); // see func. definition.
        setColorPaint(); // For setting color of rectangle. Don't need to recreate
        myRectLayout = (RelativeLayout) findViewById(R.id.rectangleLayout);
        mCustomDrawableView = new Geofencing.CustomDrawableView(this); // Make instan0-ce of CustomDrawableView

        searchBarContainer = (ConstraintLayout)findViewById(R.id.searchBarContainer);
        leftToolBarContainer = (LinearLayout)findViewById(R.id.leftToolBarContainer);


        // Have to have resolved color to pass it into paint.setColor for the drawable..
        // whatever color is currently selected by the user.. Light blue is default
        currentColor = ContextCompat.getColor(this, R.color.color_line_dark_blue);
        currentStrokeColor = ContextCompat.getColor(this, R.color.color_line_dark_blue);

        PlaceAutocompleteFragment searchFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        searchFragment.setOnPlaceSelectedListener(this); // to recieve the place that the user searches for

        dialogFragmentManager = getSupportFragmentManager();


        loadGoogleMaps(); // Essentially initializes and starts GoogleMaps
        initLocationSpinner();



        // Get Database connection
        databaseAdapter = new DatabaseAdapter(this);
        if(!databaseAdapter.rowExists("Away"))
            databaseAdapter.insertTime("Away"); // When user isn't in a geofence

        //populateGeofenceList(); // What geofences do I want added







    }


    @Override
    protected void onResume() {

        startTimeService();


        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, DatabaseService.class);
        stopService(intent);
        super.onDestroy();
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



//        confirmLocation.setVisibility(View.INVISIBLE);


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

        Log.d(TAG, "onConnected: onStartCommand: ");
        DatabaseService.mGoogleApiClient = mGoogleApiClient; // Give database service a ref to the google api clienet
        startDatabaseService();


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
        myMap.setOnMarkerClickListener(this);


        // Get polygons from database (geofences) and add them to the map.
        ArrayList<PolygonOptions> rectOptions = databaseAdapter.getPolygonRectOptions();
        ArrayList<MarkerOptions> markerOptions = databaseAdapter.getMarkerOptions();
        if(rectOptions != null) {
            for (int i = 0; i < rectOptions.size() && i < markerOptions.size(); i++) {
                Log.d(TAG, "onMapReady: "+ rectOptions);
                myMap.addPolygon(rectOptions.get(i));
                myMap.addMarker(markerOptions.get(i));

            }
        }



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



        Log.d(TAG, "onTouch: TEST " + event);

        int action = MotionEventCompat.getActionMasked(event);
        int i = v.getId();

        if (action == MotionEvent.ACTION_DOWN) {
            switch (v.getId()) {
                case R.id.locationButton:
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
                    }
                    mLatitudeText = (String.valueOf(mCurrentLocation.getLatitude())); // get updated value
                    mLongitudeText = (String.valueOf(mCurrentLocation.getLongitude())); // get updated value
                    Toast.makeText(this, mLatitudeText + " " + mLongitudeText, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.addGeofenceButton:
                    if (GeofenceLocations.Geofences != null) {
//                        populateGeofenceList(); // add the rectangle the user created to geofence list.
                        // probably don't need.. It get's called in addGeofences getGeofencingRequest();
//                        addGeofences();
                    }
                    break;
                case R.id.drawGeoButton:
                    isDrawingGeofence = !isDrawingGeofence;
                    Log.d(TAG, "BENNY" + isDrawingGeofence);
                    break;
                case R.id.imageButton:

                    // New instance
                    final DialogWindowPopUp dialogWindow = new DialogWindowPopUp();


                    /**
                     *  This is a convenience for explicitly creating a transaction, adding the fragment to it
                     *  with the given tag, and committing it. This does not add the transaction to the back
                     *  stack. When the fragment is dismissed, a new transaction will be executed to remove it
                     *  from the activity.
                     */
                    dialogWindow.show(dialogFragmentManager, "test");

                    isDrawingGeofence = false;
                    break;
                // If it's cancel button.. remove drawn geofence.
                case R.id.create_delete_selector:
                    myRectLayout.removeAllViews();
                    break;
                case R.id.fillButton:
                    colorPicker.show();
                    fill = true;
                    break;
                case R.id.strokeButton:
                    colorPicker.show();
                    fill = false;
                    break;

                default:
                    Log.d(TAG, "onTouch: testetsetsetsets");
                    return false;

            }
            // If switch statement break gets executed.. touch event should be consumed
            return true;
        }
        Log.d(TAG, "onTouch: returned to normal");
            return false; // If action event was not ActionDown

    }


    /**
     * User closes color picker dialog window. This is implementing
     * AmbilWarnaDialog.OnAmbilWarnaListener
     * @param dialog
     */
    @Override
    public void onCancel(AmbilWarnaDialog dialog) {
            // User cancels color picker.. do nothing
    }

    /**
     * Recieve color selected. For changing Geofence color.
     * @param dialog
     * @param color   -- Color recieved.
     */
    @Override
    public void onOk(AmbilWarnaDialog dialog, int color) {
            //Recieve color selecter

            if(fill){
                currentFillColor = color;
                fillButton.setBackgroundColor(color);
            }
            else{
                currentStrokeColor = color;
                strokeButton.setBackgroundColor(color);
            }

    }


    /**
     * After a user selects an address/place
     * @param place - the place that the user searched for.
     */
    @Override
    public void onPlaceSelected(Place place) {

        LatLng jumpToAddress = getGeoLocationFromAddress(place.getAddress().toString());

        // Update Camera Position with new Longitude Latitude
        if(jumpToAddress != null) {
            Log.d(TAG, "onPlaceSelected: " + jumpToAddress.latitude);
            CameraPosition newCameraPosition = new CameraPosition.Builder()
                    .target(jumpToAddress) // Where? Lat/Long
                    .zoom(20) // Increasing zoom size, doubles width of visible world
                    .build(); // returns a cameraPosition instance

            // Move the camera
            myMap.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
        }
        else{
            Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onError(Status status) {

    }

    //When a marker is clicked on GoogleMaps..
    @Override
    public boolean onMarkerClick(Marker marker) {


        Log.d(TAG, "onMarkerClick: ");
        LayoutInflater inflater = getLayoutInflater();
        View editInspect = inflater.inflate(R.layout.edit_cancel_popup, myRectLayout, false);

        RelativeLayout.LayoutParams markerParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        markerParams.leftMargin = (int)left;
        markerParams.topMargin = (int)top;

        // Add the inflated view with the specified layout parameters..
        myRectLayout.addView(editInspect,markerParams);

        // When Marker gets click add the title to the spinner List
        spinnerList.add(marker.getTitle());

        return false;
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



            this.width = event.getX() - (leftToolBarContainer.getWidth());
            // Have to account for the things between the top of the screen and the top of drawing area
            this.height = event.getY() - (actionBarHeight + statusBarHeight + searchBarContainer.getHeight());
            Log.d(TAG, "draw: " + actionBarHeight + " " + statusBarHeight + " " + searchBarContainer.getHeight());
            Log.d(TAG, "draw: " + height);
            Log.d(TAG, "draw: " + y);



//            Testing to see if values are correct.
//            Log.d(TAG, "Width: " + this.width);
//            Log.d(TAG, "height: " + this.height);
//            Log.d(TAG, "x: " + this.x);
//            Log.d(TAG, "y: " + this.y);


            //Methods that take a color in the form of an integer should be passed an RGB triple,
            // not the actual color resource id. You must call getResources.getColor(resource).
            // Deprecated.. now use ContextCompat.getColor(getContext(),your_color_id)
            mDrawable.getPaint().setColor(currentFillColor); // Color of rectangle
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

            strokePaint.setColor(currentStrokeColor);


        }


        /**
         * Callback function for extending a view. Where stuff will be drawn onto the screen
         * @param canvas - Have to draw through this canvas, which is actually being drawn on
         *               whatever bitmap that is passed to the canvas (Done automatically
         *               Canvas size is based on the layout that it's being drawn in.. For example
         *               on my Note 3 the Rectangle layout is 1206px high, so is the canvas!
         */
        @Override
        protected void onDraw(Canvas canvas) {
            Log.d(TAG, "onDraw: MapLayout" + getHeight());
            Log.d(TAG, "onDraw: " + canvas.getHeight() );
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

        //actionBarHeight = myActionBar.getHeight();


        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
//
//        Rect rectangle = new Rect();
//        Window window = getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
//        int statusBarHeight = rectangle.top;
//        int contentViewTop =
//                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        titleBarHeight = contentViewTop - statusBarHeight;
//
//        Log.d(TAG, "setBarHeights: " + titleBarHeight);

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

//

        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "dispatchTouchEvent: is motionActionEventDown");

            //Making a region where we can deterimine if the click was on GoogleMaps or not
            //Same region that holds the canvas tp draw the rectangle
            Rect rect = new Rect(leftToolBarContainer.getWidth(), ((int) actionBarHeight + searchBarContainer.getHeight() + statusBarHeight), myRectLayout.getWidth(), myRectLayout.getHeight());
            if (rect.contains((int) event.getX(), (int) event.getY())) {
                isInSideClicked = true;
                Log.d(TAG, "dispatchTouchEvent: inside clicked");
            } else isInSideClicked = false; // Reset

            if (isInSideClicked && isDrawingGeofence) {

                Log.d(TAG, "Action was DOWN");
                // Rectangle may have been drawn somewhere else.
                myRectLayout.removeAllViews(); // remove old one
                // Where finger is touching. update x,y
                mCustomDrawableView.x = event.getX() - (leftToolBarContainer.getWidth());
                mCustomDrawableView.y = event.getY() - (actionBarHeight + statusBarHeight + searchBarContainer.getHeight());
//                mCustomDrawableView.draw(event); // Might not need to2 call. No wid/height on// initial touch
//                myLinearLayout.addView(mCustomDrawableView);
//                myLinearLayout.bringToFront();
                return true;
            } else {
                Log.d(TAG, "dispatchTouchEvent: return to norsdsdasdasdmal");
                return super.dispatchTouchEvent(event);
            }


        } else if (action == MotionEvent.ACTION_MOVE && isInSideClicked) {

            Log.d(TAG, "isdraw " + isDrawingGeofence);
            if (isDrawingGeofence) {
                Log.d(TAG, "Action was MOVE");
                myRectLayout.removeAllViews(); // remove old drawn rectangle from LinearLayout
                mCustomDrawableView.draw(event); // draw new rectangle passing in the MotionEvent
                myRectLayout.addView(mCustomDrawableView); //Once it's been drawn.. add to LinearLayout
                myRectLayout.bringToFront();

                return true;
            } else {
                return super.dispatchTouchEvent(event);

            }
        } else if (action == MotionEvent.ACTION_UP && isInSideClicked && isDrawingGeofence) {

            Log.d(TAG, "Action was UP");
            left = Math.min(Math.round(mCustomDrawableView.x), Math.round(mCustomDrawableView.width));
            top = Math.min(Math.round(mCustomDrawableView.y), Math.round(mCustomDrawableView.height));
            right = Math.max(Math.round(mCustomDrawableView.x), Math.round(mCustomDrawableView.width));
            bot = Math.max(Math.round(mCustomDrawableView.y), Math.round(mCustomDrawableView.height));


            // confirmLocation.setVisibility(View.VISIBLE);
            isDrawingGeofence = false;

            LayoutInflater inflater = getLayoutInflater();
            // Inflate the create/delete dialog popup for when users make geofence, do not want
            // to attach to root (myRectLayout) yet, hence the false
            ConstraintLayout myView = (ConstraintLayout) inflater.inflate(R.layout.create_delete_dialog, myRectLayout, false);

            // Instantiate layoutParams object
            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            /**
             * Since the rectangles real width and height take up the entire parent layout I can't
             * use it as reference in the relative layout. However, since I have bounds at which the
             * rectangle is drawn on the canvas, then I can use margins to position the button relative
             * to the rectangle.
             */
            buttonParams.topMargin = (int) top;
            buttonParams.leftMargin = (int) left;

            myRectLayout.addView(myView, buttonParams);

            myView.setId(R.id.createDeleteButton);
            mCustomDrawableView.setId(R.id.myRectangle);

            Log.d(TAG, "dispatchTouchEvent: ");

            LinearLayout anotherView = (LinearLayout) myView.getChildAt(0);

            // For all the children in the Linear Inflated layout, set an onTouch listener
            for (int i = 0; i < anotherView.getChildCount(); i++) {
                anotherView.getChildAt(i).setOnTouchListener(this);
            }


            Log.d(TAG, "Left: " + left + " Top: " + top + " Right: " + right + "Bottom: " + bot);

        } else {
            // Because a click is action down and up I have to return it to normal flow on both
            // up and down?
            return super.dispatchTouchEvent(event);
        }


        // No touch event is consumed. Put touch event back to normal flow.
        Log.d(TAG, "dispatchTouchEvent: Touch returned");
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

    public void setStatusBarHeight(){

        statusBarHeight = (int) (24* getResources().getDisplayMetrics().density);

    }


    LatLng getGeoLocationFromAddress(String strAddress){


        List<android.location.Address> address;// store the converted address
        Geocoder coder = new Geocoder(this); // to retrieve latlong from an address
        LatLng cordinates = null;

        try {
            Log.d(TAG, "getGeoLocationFromAddress: " + strAddress);
            address = coder.getFromLocationName(strAddress, 5);
            Log.d(TAG, "getLocationFromAddress");
            for(int i = 0; i < address.size(); i++){
                if(address.get(i).hasLatitude() && address.get(i).hasLongitude()){
                    cordinates = new LatLng(address.get(i).getLatitude(),
                            address.get(i).getLongitude());
                    Log.d(TAG, "getGeoLocationFromAddress: " + address.get(i).getLatitude());
                }
            }
            Log.d(TAG, "getGeoLocationFromAddress: dsmfksdfklsdflksdlkfmsdlkf");
        }catch (Exception e){

        }



        return cordinates;
    }


    /**
     * Callback function for the dialog pop-up. Get information from user selections in the dialog
     * here. This is the interface communicating between the dialog and the activity
     * @param dialog - Dialog window that was closed
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        // Extract variables references from dialog. Then get the data from those references
        NumberPicker hourPicker = (NumberPicker)dialog.getDialog().findViewById(R.id.hourPicker);
        NumberPicker minutePicker = (NumberPicker)dialog.getDialog().findViewById(R.id.minutePicker);
        EditText nameField  = (EditText) dialog.getDialog().findViewById(R.id.nameField);

        String geofenceName = nameField.getText().toString();


        Log.d(TAG, "onDialogPositiveClick: was clicked");

        myRectLayout.removeAllViews();
        GeofenceLocations geofenceLocations = new GeofenceLocations(this); // pass in context
        GeofenceLocations.fillColor = currentFillColor; // for GMaps Geofence Polygon
        GeofenceLocations.strokeColor = currentStrokeColor; // same ^^
        geofenceLocations.convertAndAddGeofence(this ,geofenceName, left, top, right, bot, myMap);

        drawMarker(geofenceName);
        myMap.setOnMarkerClickListener(this);
        Log.d(TAG, "onDialogPositiveClick: I SET THE DAMN LISTENER");

        isDrawingGeofence = false;
    }

    // If user cancels window remove the rectangle. Not drawing anymore.
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        myRectLayout.removeAllViews();
        isDrawingGeofence = false;

    }


    /**
     * Need to convert drawable into a BitmapDescriptor object that is used for GoogleMaps Icons
     *
     * @param drawable  -- icon that is being converted
     * @return BitmapDescriptor - what is needed for setting GoogleMap  marker icons
     */
    public BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {


        /**
         * Create bitmap which the drawing will be in., wid, height, Config.
         * ARGB_8888 is each pixel is stored on 8 bytes. Basically changes how its stored
         * Make it the size of the icon that we are using for a maker
         */
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);

        /**
         * * A Canvas works for you as a pretense, or interface, to the actual surface upon which
         * your graphics will be drawn — it holds all of your "draw" call
         */
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, 200, 200);
        // Draw in it's bounds via setBounds
        drawable.draw(canvas);


        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Have the converted drawable.. Configure marker now
     */
    public void drawMarker(String geofenceName) {

        //Drawable icon = getResources().getDrawable(R.drawable.ic_home_icon_svg_please);
        //Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home_icon);
        //BitmapDescriptor iconBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(iconBitmap);
        //itmap iconBitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(),icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Drawable icon = getResources().getDrawable(R.drawable.home_icon_v2 );
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);

        // Insert drawable ID into database. Most of the row has been inserted, so need to updated
        // the place where a value hasn't been assigned.
        int id = R.drawable.home_icon_v2;
        databaseAdapter.updateIcon(geofenceName, id);

        // Center Marker for geofence
        myMap.addMarker(new MarkerOptions()
                .position(GeofenceLocations.Geofences.get(geofenceName))
                .icon(markerIcon)
                .title(geofenceName)
                .zIndex(5));

    }

    public void loadGoogleMaps(){
        /**
         * Builds and returns a camera position for the google maps
         */
        cameraPosition = CameraPosition.builder()
                .target(Constants.STUDY_LOCATIONS.get("LivingRoom")) // Where? Lat/Long
                .zoom(18) // Increasing zoom size, doubles width of visible world
                .build(); // returns a cameraPosition instance


        options = new GoogleMapOptions(); // options for map

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
    }


    /**
     * Initialize Spinner
     */
    public void initLocationSpinner(){

        spinnerList = new ArrayList<>();
//        if(GeofenceLocations.sharedPreferences.getAll().size() == null)
//            spinnerList.add("Empty");

        jumpToSpinner = (Spinner)findViewById(R.id.jumpToSpinner); // Make Spinner View
        // // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.simple_spinner_item, spinnerList);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        jumpToSpinner.setAdapter(spinnerAdapter);


    }

    public static class TimeHandler extends Handler{

        public TimeHandler(){
            super();
        }

        @Override
        public void handleMessage(Message msg) {

//            if(msg.getData().containsKey("CurrentDayTime")){

                // Update the UI (Time text 00:00:00 with data from TimeService)
                String time = msg.getData().getString("CurrentDayTime"); // Time in seconds
                timeText.setText(time);

//            }

            //Default constructor associates this handler with the Looper for the current thread.
            super.handleMessage(msg);
        }
    }

    public void startTimeService(){

        Handler handler = new TimeHandler();
        if(handler == null){
            Log.d(TAG, "startTimeService: null");
        }
        Intent serviceIntent = new Intent(this,TimeService.class);
        Messenger messenger = new Messenger(handler); // Messenger is subclass of Parcelable
        serviceIntent.putExtra("Handler", messenger);
        serviceIntent.putExtra("key", "startService");
        startService(serviceIntent);


        Log.d(TAG, "startTimeService: ");

    }

    // Offload Database tasks
    public void startDatabaseService(){


        Intent intent = new Intent(this, DatabaseService.class);
        intent.putExtra("functionName", Constants.POPULATE_GEOFENCE_LIST); // function name
        startService(intent);


    }













}
