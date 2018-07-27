package com.handysparksoft.motionmetering;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.handysparksoft.constants.Constants;


public class BarometerFragment extends Fragment {


    private static final int ASCEND_THRESHOLD_IN_METERS = 1;
    private static final int DESCEND_THRESHOLD_IN_METERS = 1;

    private TextView mAltitudeTextView;
    private TextView mAltitudeUnitTextView;
    private Integer mPreviousAltitude = 0;
    private Integer mAscendsCounter = 0;
    private Integer mDescendsCounter = 0;
    private Vibrator mVibrator;
    private ToneGenerator toneGenerator;
    private int userStreamVolume = -1;


    /**
     * Helper method to quickly create sections.
     *
     * @return A new BarometerFragment with arguments set based on the provided Section.
     */
    public static BarometerFragment getSection() {
        final BarometerFragment newSection = new BarometerFragment();
        final Bundle arguments = new Bundle();
        newSection.setArguments(arguments);
        return newSection;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        final View view = inflater.inflate(R.layout.activity_altimeter, container, false);
        mAltitudeTextView = view.findViewById(R.id.altitudeTextView);
        mAltitudeTextView.setText("0");
        mAltitudeUnitTextView = view.findViewById(R.id.altitudeUnitTextView);
        mAltitudeUnitTextView.setText(getUnit());


        view.findViewById(R.id.altitudeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WearActivity) getActivity()).closeDrawer();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setAlarmVolume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        restoreAlarmVolume();
        releaseToneGenerator();
    }

    private void releaseToneGenerator() {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (toneGenerator != null) {
                        toneGenerator.release();
                        toneGenerator = null;
                    }
                }
            }, 100);
        } catch (Exception e) {
            Log.e(Constants.TAG, e.toString());
        }
    }

    private void setAlarmVolume() {
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            if (userStreamVolume == -1) {
                userStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            }
            final int maxStreamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxStreamMaxVolume, 0);
        }
    }

    private void restoreAlarmVolume() {
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, userStreamVolume, 0);
        }
    }

    public void updateAltitude(String value) {
        checkAltitudeDifference(value);
    }

    private void checkAltitudeDifference(String value) {
        try {
            Integer currentAltitude = Integer.valueOf(value);
            if (currentAltitude - mPreviousAltitude >= ASCEND_THRESHOLD_IN_METERS) {
                if (!value.equals(mAltitudeTextView.getText().toString())) {
                    mAltitudeTextView.setText(value);
                    mAltitudeUnitTextView.setText(getUnit());
                    mDescendsCounter = 0;
                    mAscendsCounter++;
                    if (mAscendsCounter > 2) {
                        feedbackAscending();
                    }
                }
            } else if (mPreviousAltitude - currentAltitude >= DESCEND_THRESHOLD_IN_METERS) {
                mAltitudeTextView.setText(value);
                mAltitudeUnitTextView.setText(getUnit());
                mAscendsCounter = 0;
                mDescendsCounter++;
                if (mDescendsCounter > 2) {
                    feedbackDescending();
                }
            }
            mPreviousAltitude = currentAltitude;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void feedbackAscending() {
        //vibrate();
        beep(true);
    }

    private void feedbackDescending() {
        beep(false);
    }

    @SuppressWarnings("unused")
    private void vibrate() {
        long[] vibrationSimplePattern = {0, 250};
        long[] vibrationPattern = {0, 500, 50, 300};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        mVibrator.vibrate(vibrationSimplePattern, indexInPatternToRepeat);
    }

    private void beep(Boolean treble) {
        if (toneGenerator == null) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        }
        if (treble) {
            toneGenerator.startTone( ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        } else {
            toneGenerator.startTone(ToneGenerator.TONE_SUP_CONGESTION_ABBREV, 200);

        }
    }

    public String getUnit() {
        return Constants.METRIC_UNITS ? Constants.METRIC_METERS_UNIT : Constants.METRIC_FEET_UNIT;
    }
}
