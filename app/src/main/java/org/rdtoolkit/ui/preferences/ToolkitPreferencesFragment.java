package org.rdtoolkit.ui.preferences;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import androidx.core.os.ConfigurationCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.zeugmasolutions.localehelper.LocaleAwareCompatActivity;
import com.zeugmasolutions.localehelper.LocaleHelper;

import org.rdtoolkit.R;
import org.rdtoolkit.model.session.AppRepository;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ToolkitPreferencesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    static final String LANG_CODE_SYSTEM_DEFAULT = "system_default";
    static final String PREFERENCE_KEY_LANGUAGE = "language";
    static final String PREFERENCE_KEY_RESET_DISCLAIMERS = "reset_disclaimers";
    static final String TAG = ToolkitPreferencesFragment.class.getName();

    LinkedHashMap<String, Locale> locales;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
        locales = getTranslatedLocales();

        addLanguagePreference();
        findPreference(PREFERENCE_KEY_RESET_DISCLAIMERS).setOnPreferenceClickListener(it -> {
            new AppRepository(this.getContext()).clearDisclaimers();
            it.setEnabled(false);
            return true;
        });
    }

    private void addLanguagePreference() {
        final ListPreference listPreference = findPreference(PREFERENCE_KEY_LANGUAGE);

        Pair<CharSequence[], CharSequence[]> values = getSettingsPreferenceValues();

        listPreference.setEntries(values.second);
        listPreference.setEntryValues(values.first);
        listPreference.setOnPreferenceChangeListener(this);
    }

    private Pair<CharSequence[], CharSequence[]> getSettingsPreferenceValues() {
        CharSequence[] localeIds = new CharSequence[locales.size() + 1];
        CharSequence[] localeNames = new CharSequence[locales.size() + 1];

        Locale systemDefault = defaultSystemLocale();

        localeIds[0] = LANG_CODE_SYSTEM_DEFAULT;

        localeNames[0] = String.format(getString(R.string.preference_app_language_system_default),
                systemDefault.getDisplayLanguage(systemDefault));

        int index = 1;
        for(Map.Entry<String, Locale> item : locales.entrySet()) {
            localeIds[index] = item.getValue().getLanguage();
            localeNames[index] = item.getKey();
            index++;
        }
        return new Pair(localeIds, localeNames);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals(PREFERENCE_KEY_LANGUAGE)) {
            String value = (String)newValue;
            LocaleAwareCompatActivity activity = (LocaleAwareCompatActivity)this.getActivity();
            if (LANG_CODE_SYSTEM_DEFAULT.equals(newValue)) {
                activity.updateLocale(defaultSystemLocale());
                wipeLocaleHelperKeys();
            } else {
                activity.updateLocale(Locale.forLanguageTag(value));
            }
        }
        return true;
    }

    private void wipeLocaleHelperKeys() {
        //wipe stored keys in the helper lib to start over
        getContext().getSharedPreferences(LocaleHelper.class.getName(), Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    public LinkedHashMap<String, Locale> getTranslatedLocales() {
        Locale referenceLocale = Locale.ENGLISH;

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        AssetManager assets = getContext().getAssets();

        Locale existingLocale = configuration.locale;
        configuration.locale = referenceLocale;
        Resources res = new Resources(assets, metrics, configuration);
        String referenceString = res.getString(R.string.locale_key);

        Locale[] locales = Locale.getAvailableLocales();

        LinkedHashMap<String, Locale> localeSet = new LinkedHashMap();

        for (int i = 0; i < locales.length; i++) {
            Locale locale = locales[i];
            configuration.locale = locale;
            Resources localeSpecificResources = new Resources(assets, metrics, configuration);
            String displayName = localeSpecificResources.getString(R.string.locale_key);

            if (!referenceString.equals(displayName) || referenceLocale.equals(locale)) {
                localeSet.put(displayName, locale);
            }
        }
        configuration.locale = existingLocale;

        return localeSet;
    }

    public Locale defaultSystemLocale() {
        return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
    }
}