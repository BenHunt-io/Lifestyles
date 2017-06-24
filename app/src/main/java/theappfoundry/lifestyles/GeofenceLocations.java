package theappfoundry.lifestyles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static android.content.ContentValues.TAG;

/**
 * Created by Ben on 5/29/2017.
 */

public class GeofenceLocations {

    private final int  RECT_SIZE = 4;
    private float top;
    private float right;
    private float bottom;
    private Context context;
    private DatabaseAdapter databaseAdapter;

    // I want to be able to access this without making another variable
    // and make it independent of GeofenceLocation instances.
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor editor;

    /**
     * Need to update these colors with the Geofence drawn so that the polygon made with GMapsAPI
     * is correct color.
     */
    public static int fillColor = 0;
    public static int strokeColor;


    public static HashMap<String, LatLng> Geofences = new HashMap<>();

    public GeofenceLocations(Context context){
        this.context = context;
        databaseAdapter = new DatabaseAdapter(context);
    }


    public void convertAndAddGeofence(Context context, String key, float left, float top, float right, float bottom, GoogleMap myMap){

        // If sharedPreferences "GeofenceData" does not exist, it will be created
        sharedPreferences = context.getSharedPreferences("GeofenceData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit(); // Create editor to edit



        Point topLeft = new Point((int)left,(int)top);
        Point topRight = new Point((int)right,(int)top);
        Point bottomLeft = new Point((int)left,(int)bottom);
        Point bottomRight = new Point((int)right,(int)bottom);

        LatLng topLeftLL = (myMap.getProjection().fromScreenLocation(topLeft));
        LatLng topRightLL = (myMap.getProjection().fromScreenLocation(topRight));
        LatLng bottomLeftLL = (myMap.getProjection().fromScreenLocation(bottomLeft));
        LatLng bottomRightLL = (myMap.getProjection().fromScreenLocation(bottomRight));

        Log.d(TAG, "convertAndAddGeofence: "+ topLeftLL.latitude + topLeftLL.longitude);


        drawGeofence(myMap, topLeftLL, topRightLL, bottomLeftLL, bottomRightLL, key);


        double rectMidy = (topLeftLL.latitude + topRightLL.latitude + bottomLeftLL.latitude
                        +bottomRightLL.latitude)/4;
        double rectMidx = (topLeftLL.longitude + topRightLL.longitude + bottomLeftLL.longitude
                +bottomRightLL.longitude)/4;

        LatLng middleOfRect = new LatLng(rectMidy,rectMidx);
        Geofences.put(key,middleOfRect);

        // Key is the name of the location. so it's key,location. Just looks weird
        editor.putString(key, key);

    }

    /**
     * Draws geofence with cordinates
     * @param myMap - Pass in current map to add polygon/shape to
     */
    public void drawGeofence(GoogleMap myMap, LatLng topLeft, LatLng topRight, LatLng bottomLeft,
                             LatLng bottomRight, String location){




        fillColor = (fillColor &0x00FFFFFF);
        fillColor = (fillColor |0xC0000000);

//        fillColor = (fillColor & 0x00FFFFFF);
//        int transparent = 255 / sliderVariable;
//        fillColor |= (transparent << 24);

        Log.d(TAG, "drawGeofence: " + location.toString());
        databaseAdapter.insertLocation(location,fillColor,strokeColor,bottomLeft.latitude,
                bottomLeft.longitude,topLeft.latitude, topLeft.longitude, topRight.latitude,
                topRight.longitude, bottomRight.latitude, bottomRight.longitude);

        databaseAdapter.insertTime(location); // Default integers = 0 for time values
        startDatabaseService();

        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions()
                .add(bottomLeft, topLeft, topRight, bottomRight)
                .fillColor(fillColor)// a - aplha 0-225 transparent
                .strokeColor(strokeColor);


        // Get back the mutable Polygon, can edit polygon now, with setPoints() for ex.
        Polygon polygon = myMap.addPolygon(rectOptions);





    }

    public void startDatabaseService(){


        Intent intent = new Intent(context, DatabaseService.class);
        intent.putExtra("functionName", Constants.POPULATE_GEOFENCE_LIST); // function name
        context.startService(intent);


    }

}
