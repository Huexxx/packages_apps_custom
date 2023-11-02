/*
 *  Copyright (C) 2015 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.cherish.settings.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import android.os.SystemProperties;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.cherish.OmniJawsClient;
import com.android.internal.util.cherish.udfps.UdfpsUtils;
import com.android.internal.util.cherish.CherishUtils;
import com.cherish.settings.preferences.SystemSettingListPreference;
import com.cherish.settings.preferences.CustomSeekBarPreference;
import com.cherish.settings.preferences.SecureSettingListPreference;
import com.cherish.settings.preferences.SystemSettingSwitchPreference;
import com.cherish.settings.preferences.SystemSettingListPreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import android.provider.SearchIndexableResource;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

	private static final String UDFPS_CATEGORY = "udfps_category";
	private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";

    private static final String FINGERPRINT_SUCCESS_VIB = "fingerprint_success_vib";
    private static final String FINGERPRINT_ERROR_VIB = "fingerprint_error_vib";

    private static final String KEY_WEATHER = "lockscreen_weather_enabled";
    private static final String KEY_WEATHER_STYLE = "lockscreen_weather_style";

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintSuccessVib;
    private SwitchPreference mFingerprintErrorVib;
	
	
	private ListPreference mLockClockStyles;
	private PreferenceCategory mUdfpsCategory;
	private Context mContext;
	private ListPreference mTorchPowerButton;
    private SwitchPreference mWeather;
    private Preference mWeatherStyle;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.cherish_settings_lockscreen);
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();
        final PackageManager mPm = getActivity().getPackageManager();
		
		Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
		
		mUdfpsCategory = findPreference(UDFPS_CATEGORY);
        if (!UdfpsUtils.hasUdfpsSupport(getContext())) {
            prefSet.removePreference(mUdfpsCategory);
        }

        // screen off torch
        mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
        int mTorchPowerButtonValue = Settings.System.getInt(resolver,
                Settings.System.TORCH_POWER_BUTTON_GESTURE, 0);
        mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
        mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
        mTorchPowerButton.setOnPreferenceChangeListener(this);

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerprintSuccessVib = (SwitchPreference) findPreference(FINGERPRINT_SUCCESS_VIB);
        mFingerprintErrorVib = (SwitchPreference) findPreference(FINGERPRINT_ERROR_VIB);
        if (mPm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) &&
                 mFingerprintManager != null) {
            if (!mFingerprintManager.isHardwareDetected()){
                prefSet.removePreference(mFingerprintSuccessVib);
                prefSet.removePreference(mFingerprintErrorVib);
            } else {
                mFingerprintSuccessVib.setChecked((Settings.System.getInt(getContentResolver(),
                        Settings.System.FP_SUCCESS_VIBRATE, 1) == 1));
                mFingerprintSuccessVib.setOnPreferenceChangeListener(this);
                mFingerprintErrorVib.setChecked((Settings.System.getInt(getContentResolver(),
                        Settings.System.FP_ERROR_VIBRATE, 1) == 1));
                mFingerprintErrorVib.setOnPreferenceChangeListener(this);
            }
        } else {
            prefSet.removePreference(mFingerprintSuccessVib);
            prefSet.removePreference(mFingerprintErrorVib);
        }

        mWeather = (SwitchPreference) findPreference(KEY_WEATHER);
        OmniJawsClient weatherClient = new OmniJawsClient(getContext());
        boolean weatherEnabled = weatherClient.isOmniJawsEnabled();
        if (!weatherEnabled) {
            mWeather.setEnabled(false);
            mWeather.setSummary(R.string.lockscreen_weather_enabled_info);
        } else {
            mWeather.setChecked((Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_WEATHER_ENABLED, 1) == 1));
            mWeather.setOnPreferenceChangeListener(this);
        }

        mWeatherStyle = findPreference(KEY_WEATHER_STYLE);
        updateLws();

        mCustomSettingsObserver.observe();
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.LOCKSCREEN_WEATHER_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.LOCKSCREEN_WEATHER_STYLE))) {
                updateLws();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
       if (preference == mTorchPowerButton) {
            int mTorchPowerButtonValue = Integer.valueOf((String) newValue);
            int index = mTorchPowerButton.findIndexOfValue((String) newValue);
            mTorchPowerButton.setSummary(
                    mTorchPowerButton.getEntries()[index]);
            Settings.System.putInt(resolver, Settings.System.TORCH_POWER_BUTTON_GESTURE,
                    mTorchPowerButtonValue);
            return true;
        } else if (preference == mFingerprintSuccessVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FP_SUCCESS_VIBRATE, value ? 1 : 0);
            return true;
        } else if (preference == mFingerprintErrorVib) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FP_ERROR_VIBRATE, value ? 1 : 0);
            return true;
        } else if (preference == mWeather) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_WEATHER_ENABLED, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private void updateLws() {
        ContentResolver resolver = getActivity().getContentResolver();

        int lws = Settings.System.getIntForUser(getContext().getContentResolver(),
            Settings.System.LOCKSCREEN_WEATHER_STYLE, 64, UserHandle.USER_CURRENT);

        boolean lwsTemp = (lws > 63);
        lws = lws - (lwsTemp ? 64 : 0);
        boolean lwsFeel = (lws > 31);
        lws = lws - (lwsFeel ? 32 : 0);
        boolean lwsMaxmin = (lws > 15);
        lws = lws - (lwsMaxmin ? 16 : 0);
        boolean lwsCond = (lws > 7);
        lws = lws - (lwsCond ? 8 : 0);
        boolean lwsWind = (lws > 3);
        lws = lws - (lwsWind ? 4 : 0);
        boolean lwsRhum = (lws > 1);
        lws = lws - (lwsRhum ? 2 : 0);
        boolean lwsCity = (lws > 0);

        mWeatherStyle.setSummary(String.format("%s%s%s%s%s%s%s%s", getString(R.string.lws_temp_summary),
            (lwsTemp && lwsFeel) ? getString(R.string.lws_feel_summary) : "", getString(R.string.lws_temp_unit),
            lwsMaxmin ? " \u2022 " + getString(R.string.lws_maxmin_summary) : "",
            lwsCond ? " \u2022 " + getString(R.string.lws_cond_summary) : "",
            lwsWind ? " \u2022 " + getString(R.string.lws_wind_summary) : "",
            lwsRhum ? " \u2022 " + getString(R.string.lws_rhum_summary) : "",
            lwsCity ? " \u2022 " + getString(R.string.lws_city_summary) : ""));
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CHERISH_SETTINGS;
    }
	
	/**
     * For Search.
     */

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();
                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.cherish_settings_lockscreen;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };

}
