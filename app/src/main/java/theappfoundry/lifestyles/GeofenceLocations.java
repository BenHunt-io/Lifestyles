package theappfoundry.lifestyles;

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

    private HashMap<String, LatLng> Geofences = new HashMap<>();


    public static void convertAndAddGeofence(String key, float left, float top, float right, float bottom, GoogleMap myMap){


        Point topLeft = new Point((int)left,(int)top);
        Point topRight = new Point((int)right,(int)top);
        Point bottomLeft = new Point((int)left,(int)bottom);
        Point bottomRight = new Point((int)right,(int)bottom);

        LatLng topLeftLL = (myMap.getProjection().fromScreenLocation(topLeft));
        LatLng topRightLL = (myMap.getProjection().fromScreenLocation(topRight));
        LatLng bottomLeftLL = (myMap.getProjection().fromScreenLocation(bottomLeft));
        LatLng bottomRightLL = (myMap.getProjection().fromScreenLocation(bottomRight));

        Log.d(TAG, "convertAndAddGeofence: "+ topLeftLL.latitude + topLeftLL.longitude);


        drawGeofence(myMap, topLeftLL, topRightLL, bottomLeftLL, bottomRightLL);
    }

    /**
     * Draws geofence with cordinates
     * @param myMap - Pass in current map to add polygon/shape to
     */
    public static void drawGeofence(GoogleMap myMap, LatLng topLeft, LatLng topRight, LatLng bottomLeft,
                             LatLng bottomRight){

        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions()
                .add(bottomLeft, topLeft, topRight, bottomRight)
                .fillColor(Color.argb(180, 110, 155, 229)) // a - aplha 0-225 transparent
                .strokeColor(Color.rgb(59,91,142));

        // Get back the mutable Polygon, can edit polygon now, with setPoints() for ex.
        Polygon polygon = myMap.addPolygon(rectOptions);





    }

}
