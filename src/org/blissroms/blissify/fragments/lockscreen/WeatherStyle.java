 /*
  * Copyright (C) 2015 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package org.blissroms.blissify.fragments.lockscreen;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;

public class WeatherStyle extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = WeatherStyle.class.getSimpleName();

    private static final String LWS_TEMP_KEY = "lws_temp_enabled";
    private static final String LWS_FEEL_KEY = "lws_feel_enabled";
    private static final String LWS_MAXMIN_KEY = "lws_maxmin_enabled";
    private static final String LWS_COND_KEY = "lws_cond_enabled";
    private static final String LWS_WIND_KEY = "lws_wind_enabled";
    private static final String LWS_RHUM_KEY = "lws_rhum_enabled";
    private static final String LWS_CITY_KEY = "lws_city_enabled";
    private static final String LWS_FOOTER = "lws_footer";

    private SwitchPreference mLwsTemp;
    private SwitchPreference mLwsFeel;
    private SwitchPreference mLwsMaxmin;
    private SwitchPreference mLwsCond;
    private SwitchPreference mLwsWind;
    private SwitchPreference mLwsRhum;
    private SwitchPreference mLwsCity;
    private Preference mFooterPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_weather_style);

        ContentResolver resolver = getContext().getContentResolver();

        mLwsTemp = (SwitchPreference) findPreference(LWS_TEMP_KEY);
        boolean lwsTemp = Settings.System.getIntForUser(resolver,
                Settings.System.LWS_TEMP_ENABLED, 1, UserHandle.USER_CURRENT) != 0;
        mLwsTemp.setChecked(lwsTemp);
        mLwsTemp.setOnPreferenceChangeListener(this);

        mLwsFeel = (SwitchPreference) findPreference(LWS_FEEL_KEY);
        boolean lwsFeel = Settings.System.getIntForUser(resolver,
                Settings.System.LWS_FEEL_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mLwsFeel.setChecked(lwsFeel);
        mLwsFeel.setOnPreferenceChangeListener(this);

        mLwsMaxmin = (SwitchPreference) findPreference(LWS_MAXMIN_KEY);
        boolean lwsMaxmin = Settings.System.getIntForUser(resolver,
                Settings.System.LWS_MAXMIN_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mLwsMaxmin.setChecked(lwsMaxmin);
        mLwsMaxmin.setOnPreferenceChangeListener(this);

        mLwsCond = (SwitchPreference) findPreference(LWS_COND_KEY);
        boolean lwsCond = Settings.System.getIntForUser(resolver,
                Settings.System.LWS_COND_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mLwsCond.setChecked(lwsCond);
        mLwsCond.setOnPreferenceChangeListener(this);

        mLwsWind = (SwitchPreference) findPreference(LWS_WIND_KEY);
        boolean lwsWind = Settings.System.getIntForUser(resolver,
                Settings.System.LWS_WIND_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mLwsWind.setChecked(lwsWind);
        mLwsWind.setOnPreferenceChangeListener(this);

        mLwsRhum = (SwitchPreference) findPreference(LWS_RHUM_KEY);
        boolean lwsRhum = Settings.System.getIntForUser(resolver,
                Settings.System.LWS_RHUM_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mLwsRhum.setChecked(lwsRhum);
        mLwsRhum.setOnPreferenceChangeListener(this);

        mLwsCity = (SwitchPreference) findPreference(LWS_CITY_KEY);
        boolean lwsCity = Settings.System.getIntForUser(resolver,
                Settings.System.LWS_CITY_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        mLwsCity.setChecked(lwsCity);
        mLwsCity.setOnPreferenceChangeListener(this);

        mFooterPref = findPreference(LWS_FOOTER);

        mLwsTemp.setSummary(String.format("%s%s", getString(R.string.lws_temp_summary),
            getString(R.string.lws_temp_unit)));
        mLwsFeel.setSummary(String.format("%s%s", getString(R.string.lws_feel_summary),
            getString(R.string.lws_temp_unit)));

        updateAllPrefs();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContext().getContentResolver();
        if (preference == mLwsTemp) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.LWS_TEMP_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mLwsFeel) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.LWS_FEEL_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mLwsMaxmin) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.LWS_MAXMIN_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mLwsCond) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.LWS_COND_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mLwsWind) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.LWS_WIND_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mLwsRhum) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.LWS_RHUM_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        } else if (preference == mLwsCity) {
            boolean val = (Boolean) newValue;
            Settings.System.putIntForUser(resolver,
                Settings.System.LWS_CITY_ENABLED, val ? 1 : 0, UserHandle.USER_CURRENT);
            updateAllPrefs();
            return true;
        }
        return false;
    }

    private void updateAllPrefs() {
        ContentResolver resolver = getContext().getContentResolver();

        boolean lwsTemp = Settings.System.getIntForUser(resolver,
            Settings.System.LWS_TEMP_ENABLED, 1, UserHandle.USER_CURRENT) != 0;
        boolean lwsFeel = Settings.System.getIntForUser(resolver,
            Settings.System.LWS_FEEL_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean lwsMaxmin = Settings.System.getIntForUser(resolver,
            Settings.System.LWS_MAXMIN_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean lwsCond = Settings.System.getIntForUser(resolver,
            Settings.System.LWS_COND_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean lwsWind = Settings.System.getIntForUser(resolver,
            Settings.System.LWS_WIND_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean lwsRhum = Settings.System.getIntForUser(resolver,
            Settings.System.LWS_RHUM_ENABLED, 0, UserHandle.USER_CURRENT) != 0;
        boolean lwsCity = Settings.System.getIntForUser(resolver,
            Settings.System.LWS_CITY_ENABLED, 0, UserHandle.USER_CURRENT) != 0;

        int lws = (lwsTemp ? 64 : 0) + (lwsFeel ? 32 : 0) + (lwsMaxmin ? 16 : 0)
            + (lwsCond ? 8 : 0) + (lwsWind ? 4 : 0) + (lwsRhum ? 2 : 0) + (lwsCity ? 1 : 0);

        Settings.System.putIntForUser(resolver, Settings.System.LOCKSCREEN_WEATHER_STYLE,
            lws, UserHandle.USER_CURRENT);

        mLwsTemp.setEnabled(!(lwsTemp && !lwsFeel));
        mLwsFeel.setEnabled(!(!lwsTemp && lwsFeel));

        mFooterPref.setTitle(String.format("%s%s%s%s%s%s%s%s", getString(R.string.lws_temp_summary),
            (lwsTemp && lwsFeel) ? getString(R.string.lws_feel_summary) : "", getString(R.string.lws_temp_unit),
            lwsMaxmin ? " \u2022 " + getString(R.string.lws_maxmin_summary) : "",
            lwsCond ? " \u2022 " + getString(R.string.lws_cond_summary) : "",
            lwsWind ? " \u2022 " + getString(R.string.lws_wind_summary) : "",
            lwsRhum ? " \u2022 " + getString(R.string.lws_rhum_summary) : "",
            lwsCity ? " \u2022 " + getString(R.string.lws_city_summary) : ""));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.BLISSIFY;
    }

    /**
     * For Search.
     */

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.lockscreen_weather_style);
}
