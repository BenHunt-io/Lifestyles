package theappfoundry.lifestyles;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ben on 6/20/2017.
 */

public class DatabaseService extends Service implements ResultCallback<Status> {

    public static GoogleApiClient mGoogleApiClient;


    private String TAG = "DatabaseService: ";

    /**
     * Used when requesting to add or remove geofences.
     * Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
     */
    private PendingIntent mGeofencePendingIntent = null;


    // List of Geofences
    protected ArrayList<Geofence> mGeofenceList;
    DatabaseAdapter databaseAdapter;
    Intent intentData;


    @Override
    public void onCreate() {

        databaseAdapter = new DatabaseAdapter(this); // Initialize Database Adapter
        super.onCreate();
    }
    

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");
        intentData = intent;
        String functionName = intent.getExtras().getString("functionName");
        // Get which function is wanting to be ran in the thread

        // For each task that gets sent through this service, a new thread will be made an access
        // to the old thread will be gone. (it will still finish executing though)
        Thread databaseThread = new Thread(new DatabaseRunnable(functionName));
        databaseThread.start();


        // START_NOT_STICKY Service is not restarted in the event that it is destroyed.
        return START_NOT_STICKY;
    }

    public class DatabaseRunnable implements Runnable {

        private String functionName;

        public DatabaseRunnable(String functionName){
            this.functionName = functionName;
        }


        // Thread for executing everything Database Related
        @Override
        public void run() {

            switch(functionName){

                case Constants.POPULATE_GEOFENCE_LIST:
                    if(populateGeofenceList())
                        addGeofences();
                    break;

            }
        }





    }



    /**
     * Map.Entry<String, LatLng> is the data type. Entry is essentially a container for
     * each entry in the geofenceLatLng hashmap. It iterates through all of the entries and puts
     * them in the variable entry. Then builds a geofence using new Geofence.Builder() which returns
     * a reference to a geofence. The geofenceLatLng HashMap, is being read in from the sql database
     */
    public boolean populateGeofenceList() {

        // Map.Entry<String, LatLng> is the data type. Entry is essentially a container for
        // each entry in the STUDY_LOCATONS hashmap. It iterates through all of the entries and puts
        // them in the variable entry. It's a For Each Loop. For each element in Geofences.entrySet


        HashMap<String,LatLng> geofenceLatLng = databaseAdapter.getGeofenceLatLng();
        mGeofenceList = new ArrayList<>(); // Empty list for storing geofences

        if(!geofenceLatLng.isEmpty()) {
            for (Map.Entry<String, LatLng> entry : geofenceLatLng.entrySet()) {
                Log.d(TAG, "populateGeofenceList: " + entry.getValue().latitude);
                Log.d(TAG, "populateGeofenceList: " + entry.getValue().longitude);
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
            return true;
        }
        else return false;

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


    // Pending Intent - Want to run in the background, Your process doesn't have to be tightly bound
    // to the Location process. Pending Intent is a token your app process gives to location process
    // And the location process will use it to wake up your app process when an action of interest happens.
    // Your app process doesn't always have to be running
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
//        Toast.makeText(this, "GetGeofencePendingIntent", Toast.LENGTH_SHORT).show();
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
//        Toast.makeText(this, "ADDED GEOFENCE", Toast.LENGTH_SHORT).show();
    }


    // From setResultCallback(this) in addGeofences
    @Override
    public void onResult(@NonNull Status status) {
        Toast.makeText(this, "ONRESULT", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, status.toString(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: SERVICE");
        super.onDestroy();
    }
}
