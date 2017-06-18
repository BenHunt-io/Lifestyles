package theappfoundry.lifestyles;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Ben on 6/10/2017.
 */

public class TimeService extends Service {









    // IF I want to bind it to an activity.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

