package theappfoundry.lifestyles;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Ben on 5/23/2017.
 */

public class Constants {

    /**
     * Function names for DatabaseService
     */

    // Used for finding the key of the Handler in the onReceieve
    public static final String KEY ="KEY";

    public static final String POPULATE_GEOFENCE_LIST = "POPULATE_GEOFENCE_LIST";
    public static final String READ_IN_SPINNER = "READ_IN_SPINNER";


    /////////////////////////////////////////////////////////////////////////////////





    ////////////////////////////////////////////////////////////////////////////////

    // Key for msg that is being sent from TimeService to MainActivity. Holds Current Day's tracked time
    public static final String CURRENT_DAY_TIME = "CURRENT_DAY_TIME";


    public static final String ENTERED_FILTER = "Entered_Geofence";

    public static final String EXITED_FILTER = "Exited_Geofence";

    public static final int GEOFENCE_RADIUS_IN_METERS = 30 ;

    public static final int GEOFENCE_EXPIRATION_IN_HOURS = 24;

    public static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final String AWAY = "Away";

    // LatLng is public LatLng (double latitude, double longitude)
    // This is just for the starting position
    public static final HashMap<String, LatLng> STUDY_LOCATIONS = new HashMap<>();
    static {
        //STUDY_LOCATIONS.put("StudyRoom", new LatLng(29.8943909, -97.9079435));
        STUDY_LOCATIONS.put("LivingRoom", new LatLng(29.898835010757672, -97.96912059187889));
    }
}
