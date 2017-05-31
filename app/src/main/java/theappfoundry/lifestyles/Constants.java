package theappfoundry.lifestyles;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Ben on 5/23/2017.
 */

public class Constants {


    public static final int GEOFENCE_RADIUS_IN_METERS = 30;

    public static final int GEOFENCE_EXPIRATION_IN_HOURS = 24;

    public static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    // LatLng is public LatLng (double latitude, double longitude)
    // Populate Hashmap
    public static final HashMap<String, LatLng> STUDY_LOCATIONS = new HashMap<>();
    static {
        //STUDY_LOCATIONS.put("StudyRoom", new LatLng(29.8943909, -97.9079435));
        STUDY_LOCATIONS.put("LivingRoom", new LatLng(29.894529, -97.908082));
    }
}
