package theappfoundry.lifestyles;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.sql.Time;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by Ben on 6/10/2017.
 */

public class TimeService extends Service {

    // Was giving error to not put zero argument constructor. Online says it's something to do with
    // Reflection. Read on it.
    public TimeService(){
    }

    private static Intent intent;
    private static Messenger messenger;
    public static int time = 0; // Time in seconds
    public static boolean stopClock = false;
    private Context context;
    // Make it a class variable so I interrupt it from the broadcastReciever
    private static Thread backgroundThread;
    private static BackgroundRunnable backgroundRunnable;
    private static DatabaseAdapter databaseAdapter;


    // For storing last location, files can be accessed by only this application.
    private SharedPreferences sharedPreferences;


    public TimeService(Context context){
        this.context = context;
    }

    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate: Service");
        databaseAdapter = new DatabaseAdapter(this);
        sharedPreferences = getBaseContext().getSharedPreferences("LAST_LOCATION", MODE_PRIVATE);
        backgroundRunnable = new BackgroundRunnable(sharedPreferences);


        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(TAG, "onStartCommand: test " + intent.getParcelableExtra("Handler"));
        Log.d(TAG, "onStartCommand: In command");
        if(intent != null) {
            String key = intent.getExtras().getString("key");
            switch (key) {
                case "broadcastIntent":
                    Log.d(TAG, "onStartCommand: Broadcast recieved");
                    break;
                case "startService":
                    Log.d(TAG, "onStartCommand: startServiceSwitch");
                    // Make the thread with the class that implements Runnable that I made.
                    // Then start the thread.

                    // When service starts, start counting as if you are "Away" Until a geofence
                    // transition occurs
                    backgroundThread = new Thread(backgroundRunnable);
                    backgroundThread.start();

                    // Get Messenger reference, which itself was passed with a reference to the
                    // handler of the UI Thread. For posting on UI thread
                    messenger = intent.getParcelableExtra("Handler");
                    break;
                


            }

        }
        else if(intent == null){
            backgroundThread = new Thread(backgroundRunnable);
            backgroundThread.start();
        }

        // START_STICKY: Service is restarted in the event that it is destroyed
        return START_STICKY;
    }

    // IF I want to bind it to an activity.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // Make a runnable class so that I can make a Thread.
    // What is in the run() method is executed in the Thread. Runs a thread indefinitely keeping
    // count of time in each area that the user enters
    public static class BackgroundRunnable implements Runnable {

        Bundle bundle = new Bundle();
        StringBuffer buffer = new StringBuffer();
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        public BackgroundRunnable(SharedPreferences sharedPreferences){
            this.sharedPreferences = sharedPreferences;
            editor = sharedPreferences.edit();
        }


        @Override
        public void run() {

            HashMap<String,Integer> timeMap; // To store currentDayTime & TotalTime
            String lastLocation;
            editor = sharedPreferences.edit();

            if(intent == null){
                Log.d(TAG, "run: intent is null");
                // Service restarted because application closed and process was killed. Retrieve
                // the last location being monitored, then get the last known values for that location
                // Start up the thread again.
                // Get Last Location, default is "Away"
                lastLocation = sharedPreferences.getString("LAST_LOCATION", Constants.AWAY);

                timeMap = databaseAdapter.getTime(lastLocation);

                counting(timeMap.get("totalTime"),timeMap.get("currentDayTime"),
                        (lastLocation));




            }
            // If location entered, load data from database. Location/TotalTime/CurrentDayTime
            else if(intent.getAction() == Constants.ENTERED_FILTER) {

                lastLocation = (intent.getExtras().getString("Location"));

                // Read in the totalTime & currentDayTime from database into HashMap
                timeMap = databaseAdapter.getTime(intent.getExtras().getString("Location"));

                // Only 1 thread can access this method at a time
                counting(timeMap.get("totalTime"),timeMap.get("currentDayTime"),
                        (lastLocation));

            }

            // If exited load data from the "Away" row. Where you are not in a designated Geofence
            // Has it's own time, and is preset in the table without insertion
            else if(intent.getAction() == Constants.EXITED_FILTER){

                timeMap = databaseAdapter.getTime("Away");

                // Only 1 thread can access this method at a time
                counting(timeMap.get("totalTime"),timeMap.get("currentDayTime"), Constants.AWAY);

                editor.clear();
            }







            }

            long sTime; // start time.. Measured  time since epoch (1970)
            long eTime; // end time..  Measured time since epoch (1970) used to find elapsed time
        public synchronized void counting(int totalTime, int currentDayTime, String location){

            // Writes to SharedPreferences with lastLocation being monitored right when it starts
            editor.clear();
            editor.putString("Location", location).apply();

            while(!stopClock) {

                Log.d(TAG, "run: test");
                sTime = System.currentTimeMillis();
                int startTime = currentDayTime; // Starting time. Used to see how much time passed
                currentDayTime += 1; // increment currentDayTime as well as totalTime
                totalTime += 1;

                int tempTime = currentDayTime; // save in temp to manipulate
                buffer.delete(0, buffer.length());
                // Convert time into 00:00:00 format. Hours/Minutes/Seconds
                int seconds = tempTime % 60; // Time in seconds
                tempTime -= seconds; // Time in Seconds
                tempTime /= 60; // Time in minutes
                int minutes = tempTime % 60;
                tempTime -= minutes; // Time in minutes
                int hours = tempTime / 60;

                if (hours > 9)
                    buffer.append(hours + ":");
                else
                    buffer.append("0" + hours + ":");
                if (minutes > 9)
                    buffer.append(minutes + ":");
                else
                    buffer.append("0" + minutes + ":");
                if (seconds > 9)
                    buffer.append(seconds);
                else
                    buffer.append("0" + seconds);

                String currentDayTimeStr = buffer.toString();

                bundle.clear();
                bundle.putString(Constants.CURRENT_DAY_TIME, currentDayTimeStr);
                bundle.putString(Constants.KEY, Constants.CURRENT_DAY_TIME);


                Log.d(TAG, "run: " + currentDayTimeStr);

                // Send message back to activity to updated UI. msg contains the time data in
                // String format. The Messenger holds a reference to the handler in the activity
                Message msg = Message.obtain();
                msg.setData(bundle);

                if(messenger != null) {
                    try {
                        messenger.send(msg);
                    } catch (RemoteException e) {
                        Log.d(TAG, "run: " + e);
                    }
                }

                // Updates every 30 sec
                if(startTime % 30 == 0){
                    databaseAdapter.updateTime(totalTime,currentDayTime,location);
                    // Write to sharedPreferences with last location
                    editor.clear();
                    editor.putString("Location", location).apply();

                }

                eTime = System.currentTimeMillis();
                try {
                    Log.d(TAG, "RUN" +(eTime-sTime));
                    Thread.sleep(1000 -(sTime - eTime)); // Find elapsed time to get accurate 1 sec
                }
                catch (InterruptedException e){ // Switched locations, flag thrown. End thread
                    Log.d(TAG, "THREAD WAS INTERRUPTED");
                    return;
                }
            }
        }
    }


    /**
     * After further investigation it looks like it DOESN'T call onstartcommand automatically when
     * it recieves an intent
     */
    // For some reason, when a broadcast intent is recieved, the onstartcommand is started.
    // My guess is that because MyBroadcastReciever is an innerclass to the TimeService class , and
    // the onReceive is ran on the main thread as is the TimeService class by default. This leads
    // me to believe that's the reason OnStartCommand is triggered.
    // inner Broadcast receiver must be static ( to be registered through Manifest ) IE standalone
    // not an instance of a class
    public static class myBroadcastReceiever extends BroadcastReceiver{


        public myBroadcastReceiever(){

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(databaseAdapter == null)
                databaseAdapter = new DatabaseAdapter(context);
            if(intent.getAction() == Constants.ENTERED_FILTER){

                TimeService.intent = intent; // save the intent passed in, to retrieve location triggered

                if(backgroundThread != null)
                    backgroundThread.interrupt(); // interrupt old thread

                backgroundThread = new Thread(backgroundRunnable); // Make new thread to be ran
                backgroundThread.start();

                Log.d(TAG, "onReceive: " + intent.getExtras().getString("Location"));
            }
            else if(intent.getAction() == Constants.EXITED_FILTER){
                TimeService.intent = intent;
                backgroundThread.interrupt(); // interrupt old thread

                backgroundThread = new Thread(backgroundRunnable);
                backgroundThread.start();
            }
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: TIMESERVICE");
        super.onDestroy();
    }
}

