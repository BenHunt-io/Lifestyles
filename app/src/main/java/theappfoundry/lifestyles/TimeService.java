package theappfoundry.lifestyles;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

import static android.content.ContentValues.TAG;

/**
 * Created by Ben on 6/10/2017.
 */

public class TimeService extends Service {



    private Messenger messenger;
    public int time = 0; // Time in seconds
    public boolean stopClock = false;
    private Context context;

    public TimeService(){
        super(); // Super constructor?
    }


    public TimeService(Context context){

        this.context = context;
    }

    @Override
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(TAG, "onStartCommand: test " + intent.getParcelableExtra("Handler"));
        if(intent != null) {
            String key = intent.getExtras().getString("key");
            switch (key) {
                case "broadcastIntent":
                    Log.d(TAG, "onStartCommand: Broadcast recieved");
                    break;
                case "startService":
                    // Make the thread with the class that implements Runnable that I made.
                    // Then start the thread.

                    Thread backgroundThread = new Thread(new BackgroundRunnable());
                    backgroundThread.start();
                    messenger = intent.getParcelableExtra("Handler");


            }
        }


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
    public class BackgroundRunnable implements Runnable {

        String location = new String();

        @Override
        public void run() {
            Bundle bundle = new Bundle();

            StringBuffer buffer = new StringBuffer();

            //DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
            while(!stopClock) {
                Log.d(TAG, "run: test");
                time += 1;
                int tempTime = time;
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

                String currentDayTime = buffer.toString();

                bundle.clear();
                bundle.putString("CurrentDayTime", currentDayTime);
                bundle.putString("Location", location);

                Log.d(TAG, "run: " + currentDayTime);

                // Send message back to activity to updated UI. msg contains the time data in
                // String format. The Messenger holds a reference to the handler in the activity
                Message msg = new Message();
                msg.setData(bundle);

                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    Log.d(TAG, "run: " + e);
                }

                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){}
            }
        }
    }


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
            if(intent.getAction() == Constants.ENTERED_FILTER){
                Log.d(TAG, "onReceive: " + intent.getExtras().getString("Location"));
            }
            else if(intent.getAction() == Constants.EXITED_FILTER){

            }
        }
    }
}

