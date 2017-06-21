package theappfoundry.lifestyles;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ben on 6/20/2017.
 */

public class DatabaseService extends Service{

    // List of Geofences
    protected ArrayList<Geofence> mGeofenceList;
    DatabaseAdapter databaseAdapter;
    Intent intentData;


    @Override
    public void onCreate() {

        databaseAdapter = new DatabaseAdapter(this); // Initialize Database Adapter
        super.onCreate();
    }

    public static String functionName;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        intentData = intent;
        intent.getExtras().getString("functionName");
        // Get which function is wanting to be ran in the thread

        // For each task that gets sent through this service, a new thread will be made an access
        // to the old thread will be gone. (it will still finish executing though)
        Thread databaseThread = new Thread(new DatabaseRunnable());
        databaseThread.start();

        return START_STICKY;
    }

    public class DatabaseRunnable implements Runnable {


        // Thread for executing everything Database Related
        @Override
        public void run() {

            switch(functionName){

                case "populateGeofenceList":

                    break;

            }
        }





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
        // them in the variable entry. It's a For Each Loop. For each element in Geofences.entrySet
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






}
