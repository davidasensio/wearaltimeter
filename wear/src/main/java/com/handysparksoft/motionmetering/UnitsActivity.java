package com.handysparksoft.motionmetering;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;

import com.handysparksoft.constants.Constants;

public class UnitsActivity extends AppCompatActivity {

    private static final String TAG = UnitsActivity.class.getSimpleName();
    private AppCompatRadioButton unitMetricRadioGroup;
    private AppCompatRadioButton unitImperialRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        setupViews();
    }

    private void setupViews() {
        unitMetricRadioGroup = findViewById(R.id.unitMetricRadioGroup);
        unitImperialRadioGroup = findViewById(R.id.unitImperialRadioGroup);

        findViewById(R.id.unitAcceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unitMetricRadioGroup.isChecked()) {
                    Constants.METRIC_UNITS = true;
                } else {
                    Constants.METRIC_UNITS = false;
                }
                finish();
            }
        });

        findViewById(R.id.unitCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
