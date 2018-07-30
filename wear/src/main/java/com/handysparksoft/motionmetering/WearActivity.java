package com.handysparksoft.motionmetering;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.wear.widget.drawer.WearableActionDrawerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.handysparksoft.constants.Constants;

public class WearActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_BODY_SENSORS = 111;
    private static final String TAG = WearActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private SensorManager mSensorManager;
    private WearableActionDrawerView mWearableActionDrawer;

    private boolean isActionPlaySelected = false;
    private BarometerFragment mBarometerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity);

        //Keep screen ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBarometerFragment = BarometerFragment.getSection();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mBarometerFragment)
                .commit();

        initGoogleApiClient();
        setupViews();
        //checkPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.MessageApi.removeListener(mGoogleApiClient, messageListener);
    }

    private void actionPlay() {
        isActionPlaySelected = true;
        mWearableActionDrawer.getMenu().findItem(R.id.action_play_stop_button).setIcon(R.drawable.ic_action_playback_stop);
        mWearableActionDrawer.getMenu().findItem(R.id.action_play_stop_button).setTitle(getString(R.string.action_stop));
        //Toast.makeText(WearActivity.this, "play", Toast.LENGTH_SHORT).show();

        if (mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), Constants.PATH_ACTION_MESSAGE, Constants.ACTION_START_METERING.getBytes()).await();
                        if (!result.getStatus().isSuccess()) {
                            Log.e("test", "error");
                        } else {
                            Log.i("test", "success!! sent to: " + node.getDisplayName());
                        }
                    }
                }
            }).start();
        }
    }

    private void actionStop() {
        isActionPlaySelected = false;
        mWearableActionDrawer.getMenu().findItem(R.id.action_play_stop_button).setIcon(android.R.drawable.ic_media_play);
        mWearableActionDrawer.getMenu().findItem(R.id.action_play_stop_button).setTitle(getString(R.string.action_play));
        //Toast.makeText(WearActivity.this, "pause", Toast.LENGTH_SHORT).show();

        if (mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), Constants.PATH_ACTION_MESSAGE, Constants.ACTION_STOP_METERING.getBytes()).await();
                        if (!result.getStatus().isSuccess()) {
                            Log.e("test", "error");
                        } else {
                            Log.i("test", "success!! sent to: " + node.getDisplayName());
                        }
                    }
                }
            }).start();
        }
    }

    private void actionSound() {
        startActivity(new Intent(this, SoundActivity.class));
    }

    private void actionUnits() {
        startActivity(new Intent(this, UnitsActivity.class));
    }

    private void actionQuit() {
        actionStop();
        mGoogleApiClient.disconnect();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerSensor();
    }

    private void registerSensor() {
        final Sensor hrSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
//        mSensorManager.registerListener(this, hrSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BODY_SENSORS: {
                if (grantResults.length > 0) {
                    registerSensor();
                } else {
                    Log.d(TAG, "Permissions denied");

                }
                return;
            }
        }
    }

    private void setupViews() {
        mWearableActionDrawer = findViewById(R.id.bottom_action_drawer);
        mWearableActionDrawer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                closeDrawer();

                switch (menuItem.getItemId()) {
                    case R.id.action_play_stop_button:
                        if (isActionPlaySelected) {
                            actionStop();
                        } else {
                            actionPlay();
                        }
                        return true;
                    case R.id.action_sound_button:
                        actionSound();
                        return true;
                    case R.id.action_units_button:
                        actionUnits();
                        return true;
                    case R.id.action_quit_button:
                        actionQuit();
                        return true;
                }
                return false;
            }
        });
        mWearableActionDrawer.getController().peekDrawer();
    }

    public void closeDrawer() {
        mWearableActionDrawer.getController().closeDrawer();
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
                            Wearable.MessageApi.addListener(mGoogleApiClient, messageListener);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(TAG, "onConnectionSuspended: " + i);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.d(TAG, "onConnectionFailed: " + connectionResult);
                        }
                    })
                    .addApi(Wearable.API)
                    .build();

            mGoogleApiClient.connect();
        }
    }

    MessageApi.MessageListener messageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(final MessageEvent messageEvent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (messageEvent.getPath().equalsIgnoreCase(Constants.PATH_DATA_MESSAGE)) {
                        final String msg = new String(messageEvent.getData());
                        Log.d(TAG, msg);
                        mBarometerFragment.updateAltitude(msg);
                    }
                }
            });
        }
    };


    private void checkPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BODY_SENSORS)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, MY_PERMISSIONS_REQUEST_BODY_SENSORS);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
//            }
        } else {
            registerSensor();
        }
    }
}
