package com.handysparksoft.motionmetering;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class BarometerHelper implements SensorEventListener {
    private static final String TAG = BarometerHelper.class.getSimpleName();
    private static int SENSOR_THRESHOLD = 2;

    private SensorManager mSensorManager;
    private PressureEventListener mPressureEventListener;
    private float mAltitude = 0f;
    private float mDiff = 0f;
    private Context mContext;

    public interface PressureEventListener {
        void onPressureChanged(float value);
    }

    public static boolean hasBarometerSensor(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER);
    }

    private static BarometerHelper ourInstance;

    static BarometerHelper getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new BarometerHelper(context, SENSOR_THRESHOLD);
            ourInstance.mContext = context;
            ourInstance.mDiff = SharedPreferencesManager.getInstance(context).getFloatValue(SharedPreferencesManager.PREFERENCE_ALTITUDE_DIFF_KEY, 0f);
        }
        return ourInstance;
    }


    private BarometerHelper(Context context) {
        this(context, SENSOR_THRESHOLD);
    }

    private BarometerHelper(Context context, int sensorThreshold) {
        if (sensorThreshold > 0) {
            SENSOR_THRESHOLD = Math.abs(sensorThreshold);
        }
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void setOnSensorChangeListener(PressureEventListener pressureEventListener) {
        this.mPressureEventListener = pressureEventListener;
    }

    public float getAltitude() {
        return mAltitude + mDiff;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float[] values = sensorEvent.values;
        if (Sensor.TYPE_PRESSURE == sensorEvent.sensor.getType()) {
            mAltitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, values[0]);

            updateAltitude();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Accuracy changed
    }

    public void resume() {
        if (mSensorManager != null) {
            registerSensor();
        }
    }

    public void pause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    private void registerSensor() {
        final Sensor pressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void updateAltitude() {
        final Float altitude = getAltitude();
        if (Math.abs(altitude) * 10 >= SENSOR_THRESHOLD) {
            if (Math.round(altitude * 10) % SENSOR_THRESHOLD == 0 || Math.abs(altitude) <= 10 ) {
                if (mPressureEventListener != null) {
                    mPressureEventListener.onPressureChanged(altitude);
                }
            }
        }
    }

    public void calibrateToZero() {
        mDiff = mAltitude * -1;
        SharedPreferencesManager.getInstance(mContext).setValue(SharedPreferencesManager.PREFERENCE_ALTITUDE_DIFF_KEY, mDiff);
    }

    public void resetCalibration() {
        mDiff = 0;
        SharedPreferencesManager.getInstance(mContext).removeKey(SharedPreferencesManager.PREFERENCE_ALTITUDE_DIFF_KEY);
    }
}
