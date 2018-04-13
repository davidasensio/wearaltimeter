package com.handysparksoft.motionmetering;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.handysparksoft.constants.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mAltitudeTextView;

    private BarometerToWearService mBarometerToWearService;
    private boolean mIsBarometerToWearServiceBound;
    private ServiceConnection mBarometerToWearServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBarometerToWearService = ((BarometerToWearService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBarometerToWearService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAltitudeTextView = findViewById(R.id.altitudeTextView);

        //Start service
        doStartService();

        new Timer().schedule(new TimerTask() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mBarometerToWearService != null) {
                            mAltitudeTextView.setText(mBarometerToWearService.getCurrentAltitudeFormatted() + " " + getUnit());
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //doStopService();
    }

    private void doStartService() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                    //  Prompt the user to disable battery optimization
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                }
                bindAndStartBaromenterService();
            } else {
                bindAndStartBaromenterService();
            }
        }
    }

    private void bindAndStartBaromenterService() {
        doBindService();
        final Intent service = new Intent(this, BarometerToWearService.class);
        service.setAction(Constants.SERVICE_ACTION.STARTFOREGROUND_ACTION);
        startService(service);
    }

    private void doStopService() {
        doUnbindService();
        stopService(new Intent(this, BarometerToWearService.class));
    }

    void doBindService() {
        bindService(new Intent(this, BarometerToWearService.class), mBarometerToWearServiceConnection, Context.BIND_AUTO_CREATE);
        mIsBarometerToWearServiceBound = true;
    }

    void doUnbindService() {
        if (mIsBarometerToWearServiceBound) {
            // Detach our existing connection.
            unbindService(mBarometerToWearServiceConnection);
            mIsBarometerToWearServiceBound = false;
        }
    }

    public void calibrateToZero(View view) {
        if (mBarometerToWearService != null) {
            mBarometerToWearService.calibrateToZero();
        }
    }

    public void resetCalibration(View view) {
        if (mBarometerToWearService != null) {
            mBarometerToWearService.resetCalibration();
        }
    }

    public void startMetering(View view) {
        if (mBarometerToWearService != null) {
            mBarometerToWearService.startMetering();
        }
    }

    public void stopMetering(View view) {
        if (mBarometerToWearService != null) {
            mBarometerToWearService.stopMetering();
        }
    }

    public String getUnit() {
        return Constants.METRIC_UNITS ? Constants.METRIC_METERS_UNIT : Constants.METRIC_FEET_UNIT;
    }
}
