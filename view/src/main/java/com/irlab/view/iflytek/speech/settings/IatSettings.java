package com.irlab.view.iflytek.speech.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Window;

import com.irlab.view.R;
import com.irlab.view.watcher.SettingTextWatcher;

/**
 * 听写设置界面
 */
public class IatSettings extends PreferenceActivity implements OnPreferenceChangeListener {

    public static final String PREFER_NAME = "com.iflytek.setting";

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(PREFER_NAME);
        addPreferencesFromResource(R.xml.iat_setting);

        EditTextPreference mVadbosPreference = (EditTextPreference) findPreference("iat_vadbos_preference");
        mVadbosPreference.getEditText().addTextChangedListener(new SettingTextWatcher(IatSettings.this, mVadbosPreference, 0, 10000));

        EditTextPreference mVadeosPreference = (EditTextPreference) findPreference("iat_vadeos_preference");
        mVadeosPreference.getEditText().addTextChangedListener(new SettingTextWatcher(IatSettings.this, mVadeosPreference, 0, 10000));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }
}
