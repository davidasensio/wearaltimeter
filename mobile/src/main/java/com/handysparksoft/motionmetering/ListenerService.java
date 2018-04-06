package com.handysparksoft.motionmetering;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.handysparksoft.constants.Constants;


public class ListenerService extends WearableListenerService {

    private final Context context;

    public ListenerService(Context context) {
        this.context = context;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final String msg = new String(messageEvent.getData());
        Log.d("MobileTrack", String.format("onMessageReceived(%s)", msg));
        if (messageEvent.getPath().equals(Constants.PATH_ACTION_MESSAGE)) {
            System.out.println(messageEvent);
            if (Constants.ACTION_START_METERING.equals(msg)) {
                ((BarometerToWearService)context).startMetering();
            } else if (Constants.ACTION_STOP_METERING.equals(msg)) {
                ((BarometerToWearService)context).stopMetering();
            }


//            final String message = new String(messageEvent.getData());
//            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
//            b.setContentText(message);
//            b.setSmallIcon(R.drawable.ic_launcher);
//            b.setContentTitle("Test Notification");
//            b.setLocalOnly(true);
//            NotificationManagerCompat man = NotificationManagerCompat.from(this);
//            man.notify(0, b.build());
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
