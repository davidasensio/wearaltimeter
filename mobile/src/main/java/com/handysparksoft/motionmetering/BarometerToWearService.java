package com.handysparksoft.motionmetering;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.handysparksoft.constants.Constants;

import java.util.concurrent.TimeUnit;

public class BarometerToWearService extends Service {

    private static final String TAG = BarometerToWearService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private BarometerHelper mBarometerHelper;
    private float mCurrentAltitudeValue = 0;
    private String mCurrentAltitudeValueFormatted = "0";
    private String mPreviousAltitudeValueFormatted = "0";

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    private PowerManager.WakeLock wl;

    //------------------------------------------------------------------------------
    //------------------------ Service Notification Methods ------------------------
    //------------------------------------------------------------------------------

    private static PendingIntent createNotficationPendingIntent(Context context) {
        Intent notificationIntent = new Intent(context, BarometerToWearService.class);
        notificationIntent.setAction(Constants.SERVICE_ACTION.STOPFOREGROUND_ACTION);
        return PendingIntent.getService(context, 0, notificationIntent, 0);
    }

    private static PendingIntent createNotficationToMainActivityPendingIntent(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Constants.SERVICE_ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 1, notificationIntent, 0);
    }

    private static PendingIntent createActionStopPendingIntert(Context context) {
        Intent intent = new Intent(context, BarometerToWearService.class);
        intent.setAction(Constants.SERVICE_ACTION.STOPFOREGROUND_ACTION);
        return PendingIntent.getService(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Notification createNotification(Context context, PendingIntent intent) {
        return new Notification.Builder(context)
                .setContentTitle(context.getText(R.string.notificationTitle))
                .setTicker(context.getText(R.string.notificationTitle))
                .setContentText(context.getText(R.string.notificationText))
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                //.addAction(android.R.drawable.ic_media_pause, "Stop", createActionStopPendingIntert(context))
                .build();
    }

    public BarometerToWearService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        initGoogleApiClient();
        mBarometerHelper = BarometerHelper.getInstance(this);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BarometeServiceWakeLock");
            wl.acquire(TimeUnit.HOURS.toMillis(8));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mBarometerHelper != null) {
            mBarometerHelper.resume();
        }

        if (intent.getAction().equals(Constants.SERVICE_ACTION.STARTFOREGROUND_ACTION)) {
            final Notification foregroundServiceNotification = createNotification(this, createNotficationPendingIntent(this));
            startForeground(Constants.SERVICE_ACTION.FOREGROUND_ALTIMETER_SERVICE_NOTIFICATION_ID, foregroundServiceNotification);
        } else if (intent.getAction().equals(Constants.SERVICE_ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            releaseAll();
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAll();
    }

    private void releaseAll() {
        mGoogleApiClient.disconnect();
        if (mBarometerHelper != null) {
            mBarometerHelper.pause();
            mBarometerHelper = null;
        }
        wl.release();
        stopForeground(true);
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        BarometerToWearService getService() {
            return BarometerToWearService.this;
        }
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "Connected");
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Log.d(TAG, "onConnected: " + bundle);
                            Wearable.MessageApi.addListener(mGoogleApiClient, new ListenerService(BarometerToWearService.this));
                            sendAltitudeToWearable();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(TAG, "onConnectionSuspended: " + i);
                        }
                    })
                    .addApi(Wearable.API)
                    .build();

            mGoogleApiClient.connect();
        }
    }

    public void startMetering() {
        mBarometerHelper = BarometerHelper.getInstance(this);
        mBarometerHelper.resume();
        mBarometerHelper.setOnSensorChangeListener(new BarometerHelper.PressureEventListener() {
            @Override
            public void onPressureChanged(float value) {
                mCurrentAltitudeValue = value;
                mCurrentAltitudeValueFormatted = String.format(Math.abs(mCurrentAltitudeValue) >= 10 ? "%.0f" : "%.0f", mCurrentAltitudeValue);
                if (!mCurrentAltitudeValueFormatted.equals(mPreviousAltitudeValueFormatted)) {
                    sendAltitudeToWearable();
                }
                mPreviousAltitudeValueFormatted = mCurrentAltitudeValueFormatted;
            }
        });
    }

    public void stopMetering() {
        if (mBarometerHelper != null) {
            mBarometerHelper.pause();
        }
    }

    public void calibrateToZero() {
        if (mBarometerHelper != null) {
            mBarometerHelper.calibrateToZero();
        }
    }

    public void resetCalibration() {
        if (mBarometerHelper != null) {
            mBarometerHelper.resetCalibration();
        }
    }

    public float getCurrentAltitude() {
        return mCurrentAltitudeValue;
    }

    public String getCurrentAltitudeFormatted() {
        return mCurrentAltitudeValueFormatted;
    }

    private void sendAltitudeToWearable() {
        sendDataToWearable(mCurrentAltitudeValueFormatted);
        Log.d(TAG, "Sending altitude to wearable: " + mCurrentAltitudeValueFormatted);
    }

    private void sendDataToWearable(final String currentAltitudeValue) {
        if (currentAltitudeValue != null) {
            sendMessageToWearable(Constants.PATH_DATA_MESSAGE, currentAltitudeValue);
        }
    }

    private void sendMessageToWearable(final String path, final String message) {
        if (mGoogleApiClient != null) {
            runAsync(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        final MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
                    }
                }
            });
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void runAsync(final Runnable runnable) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                runnable.run();
                return null;
            }
        }.execute();
    }
}
