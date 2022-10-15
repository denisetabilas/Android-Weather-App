/*
    Settings Activity
 */
package edu.deanza.cis53.weather.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.deanza.cis53.weather.app.data.WeatherContract;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    // since we use the preference change initially to populate the summary
    // field, we'll ignore that change at start of the activity
    boolean mBindingPreference;
    static final String FORECAST_DAYS = "7";
    static String Location = "95148", Unit = "imperial";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        mBindingPreference = true;

        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

        mBindingPreference = false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences("WeatherPref", 0);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String locationKey, unitsKey;
        // are we starting the preference activity?
        if ( !mBindingPreference ) {
            if ((locationKey = preference.getKey()).equals(getString(R.string.pref_location_key))) {
                FetchWeatherTask weatherTask = new FetchWeatherTask(this);
                String location = value.toString();
                Location = location;
                editor.putString(locationKey, Location);
                FetchWeatherTask.updateWeather(MainActivity.MainContext, location, FORECAST_DAYS, Unit);
            } else if (preference.getKey().equals(unitsKey = getString(R.string.pref_units_key))) {
                String unit = value.toString();
                Unit = unit;
                editor.putString(unitsKey, Unit);
                Log.d(MainActivity.LOG_TAG, "onPreferenceChange !mBindingPreference pref_units_key " + stringValue);
                FetchWeatherTask.updateWeather(MainActivity.MainContext, Location, FORECAST_DAYS, unit);
            }
            else {
                // notify code that weather may be impacted
                getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            }
        }
        editor.commit();
        MainActivity.setPreferences(Location, Unit);
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }

}
