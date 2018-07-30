package com.handysparksoft.motionmetering;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;

public class SoundActivity extends AppCompatActivity {

    public static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String TAG = SoundActivity.class.getSimpleName();

    private AppCompatRadioButton soundEnabledRadioGroup;
    private AppCompatRadioButton soundDisabledRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);

        setupViews();
    }

    private void setupViews() {
        boolean soundEnabledPreference = isSoundEnabled();

        soundEnabledRadioGroup = findViewById(R.id.soundEnabledRadioGroup);
        soundDisabledRadioGroup = findViewById(R.id.soundDisabledRadioGroup);

        soundEnabledRadioGroup.setChecked(soundEnabledPreference);
        soundDisabledRadioGroup.setChecked(!soundEnabledPreference);

        findViewById(R.id.soundAcceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean soundEnabled = soundEnabledRadioGroup.isChecked();
                setSoundEnabled(soundEnabled);
                finish();
            }
        });

        findViewById(R.id.soundCancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean isSoundEnabled() {
        return SharedPreferencesManager.getInstance(this).getBoolanValue(KEY_SOUND_ENABLED, true);
    }

    private void setSoundEnabled(Boolean enabled) {
        SharedPreferencesManager.getInstance(this).setValue(KEY_SOUND_ENABLED, enabled);
    }

}
