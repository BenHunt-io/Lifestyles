package theappfoundry.lifestyles;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Created by Ben on 5/23/2017.
 */

public class GeofenceTransitionsIntentService extends IntentService{





        protected static final String TAG = "GeofenceTransitionsIS";



     /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
        public GeofenceTransitionsIntentService(){
            super(TAG);
        }


        // Location services will send an intent. So this IntentService thread is just kind of
        // sitting around until it arrives. Once it arrives it is checked. From what I've read it
        // appears that Location services adds that GEOFENCE transition data to the pending intent
        // and then sends it to this service with the intent we provided.
        protected void onHandleIntent(Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(this,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                //Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show();
                return;
            }

            //Toast.makeText(this, "IN GEOFENCE TIS", Toast.LENGTH_SHORT).show();
            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();


                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        triggeringGeofences
                );

                // Send notification and log the transition details.
                sendNotification(geofenceTransitionDetails);
                Log.i(TAG, geofenceTransitionDetails);
                //Toast.makeText(this, "After sendNotification: " + geofenceTransitionDetails, Toast.LENGTH_SHORT).show();


                // Sends a broadcast intent to the receiver in the TimeService class.
                // Sends over data of which location entered/excited so the TimeService class can
                // keep track of time spent in the area in a separate background thread.
                Intent broadcastIntent = new Intent();
                for(int i = 0; i < triggeringGeofences.size(); i++) {
                    broadcastIntent.putExtra("Location", triggeringGeofences.get(i).getRequestId());
                    broadcastIntent.putExtra("key", "broadcastIntent");
                }
                if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                    broadcastIntent.setAction("Entered_Geofence"); // Intent Filter
                }
                else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
                    broadcastIntent.setAction("Exited_Geofence"); // Intent filter
                }
                sendBroadcast(broadcastIntent);


            } else {
                // Log the error.
                // Error code 4 is a Transition Dwell
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));
                // Using a toast in a service can have it suspended indefinitely if service closes
                //Toast.makeText(this, "invalid transition type", Toast.LENGTH_SHORT).show();
            }



        }


    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }





    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }


    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), Geofencing.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(Geofencing.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }



}






