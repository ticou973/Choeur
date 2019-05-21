package dedicace.com.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dedicace.com.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        addPreferencesFromResource(R.xml.pref_chorale);

        //gestion des summaries
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();

        int listInt = preferenceScreen.getPreferenceCount();
        Log.d("coucou", "SF onCreatePreferences: "+listInt);

        for(int i = 0;i<listInt;i++){
            Preference preference = preferenceScreen.getPreference(i);
            if(!(preference instanceof CheckBoxPreference)){

                if(preference instanceof MultiSelectListPreference){
                    Log.d("coucou", "SF onCreatePreferences: "+preference.getKey());
                    MultiSelectListPreference multiSelectListPreference =(MultiSelectListPreference) preference;
                    Set<String> values = new HashSet<>();
                    values = sharedPreferences.getStringSet(multiSelectListPreference.getKey(),null);
                    setPreferenceSummary(preference,values);
                }
            }
        }

    }

    ///Pour mettre les summary sur autres choses que des checkBoxes
    private void setPreferenceSummary(Preference preference, Set<String> values) {

        if(preference instanceof MultiSelectListPreference){
            Log.d("coucou", "SF setPreferenceSummary: "+preference.getKey());
            MultiSelectListPreference multiSelectListPreference =(MultiSelectListPreference) preference;
            List<String> valuesString = new ArrayList<>();

            for(String value : values) {
                int prefIndex = multiSelectListPreference.findIndexOfValue(value);
                valuesString.add(multiSelectListPreference.getEntries()[prefIndex].toString());
            }

            multiSelectListPreference.setSummary(valuesString.toString());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if(preference!=null) {
            if(!(preference instanceof CheckBoxPreference)){
                if(preference instanceof MultiSelectListPreference){
                    Log.d("coucou", "SF onCreatePreferences: "+preference.getKey());
                    MultiSelectListPreference multiSelectListPreference =(MultiSelectListPreference) preference;
                    Set<String> values = new HashSet<>();
                    values = sharedPreferences.getStringSet(multiSelectListPreference.getKey(),null);
                    setPreferenceSummary(preference,values);
                    Log.d("coucou", "SF onSharedPreferenceChanged: "+values);
                }
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }
}
