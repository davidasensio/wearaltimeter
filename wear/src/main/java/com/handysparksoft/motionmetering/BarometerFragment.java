package com.handysparksoft.motionmetering;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class BarometerFragment extends Fragment {


    private static final int ASCEND_THRESHOLD_IN_METERS = 1;
    private static final int DESCEND_THRESHOLD_IN_METERS = 2;

    private TextView mAltitudeTextView;
    private Integer mPreviousAltitude = 0;
    private Vibrator mVibrator;

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

        return view;
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
                    vibrate();
                }
            } else if (mPreviousAltitude - currentAltitude >= DESCEND_THRESHOLD_IN_METERS) {
                mAltitudeTextView.setText(value);
            } else if (Math.abs(currentAltitude) <= 10) {
                mAltitudeTextView.setText(value);
            }
            mPreviousAltitude = currentAltitude;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void vibrate() {
        long[] vibrationSimplePattern = {0, 250};
        long[] vibrationPattern = {0, 500, 50, 300};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        mVibrator.vibrate(vibrationSimplePattern, indexInPatternToRepeat);
    }
}
