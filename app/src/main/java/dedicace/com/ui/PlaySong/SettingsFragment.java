package dedicace.com.ui.PlaySong;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dedicace.com.R;
import dedicace.com.data.database.AppDataBase;
import dedicace.com.data.database.Saison;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private CharSequence[] entries;
    private CharSequence[] entryValues;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private List<Saison> saisons = new ArrayList<>();
    private List<String> idSaisons = new ArrayList<>();
    private List<String> nameSaisons= new ArrayList<>();

    private Saison currentSaison;
    private ArrayList<String> listSpectacles ;
    private AppDataBase database;
    private Thread thread;



    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        addPreferencesFromResource(R.xml.pref_chorale);
        Context context = getPreferenceManager().getContext();
        database = AppDataBase.getInstance(context.getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        //gestion des summaries
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();

        createListPreference(context, preferenceScreen);

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

                if(preference instanceof ListPreference){
                    ListPreference listPreference = (ListPreference) preference;
                    String value = sharedPreferences.getString(listPreference.getKey(),"");
                    Set<String> values = new HashSet<>();
                    values.add(value);
                    setPreferenceSummary(preference,values);
                }
            }
        }

    }

    private void createListPreference(Context context, PreferenceScreen preferenceScreen) {

        remplirSaisons();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("coucou", "SF onCreateListPreference: saisons "+saisons);

        String currentSaisonId = sharedPreferences.getString("currentSaison","");

        for (Saison saison : saisons){
            if(!TextUtils.isEmpty(currentSaisonId)&&saison.getIdsaisonCloud().equals(currentSaisonId)){
               currentSaison=saison;
            }
        }
        Log.d("coucou", "SF onCreatePreferences: currentsaison "+currentSaison);

        for (Saison saison : saisons){
            idSaisons.add(saison.getIdsaisonCloud());
            nameSaisons.add(saison.getSaisonName());
        }

        entryValues= idSaisons.toArray(new CharSequence[0]);
        entries= nameSaisons.toArray(new CharSequence[0]);


        ListPreference listPreference = new ListPreference(context);
        listPreference.setKey("currentSaison");
        listPreference.setTitle("Saison actuelle");
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entryValues);
        listPreference.setDefaultValue(currentSaison);
        listPreference.setOrder(0);

        preferenceScreen.addPreference(listPreference);
    }

    private void remplirSaisons() {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                saisons =database.saisonDao().getAllSaisons();
            }
        });

        thread.start();
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

        if(preference instanceof ListPreference){
            Log.d("coucou", "SF setPreferenceSummary: B"+preference.getKey());
            ListPreference listPreference =(ListPreference) preference;
            String value = values.toArray()[0].toString();
            int prefIndex = listPreference.findIndexOfValue(value);

            if(prefIndex>=0){
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
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

                if(preference instanceof ListPreference){
                    Log.d("coucou", "SF onCreatePreferences: "+preference.getKey());
                    ListPreference listPreference =(ListPreference) preference;
                    String value = sharedPreferences.getString(listPreference.getKey(),"");
                    Set<String> values = new HashSet<>();
                    values.add(value);
                    setPreferenceSummary(preference,values);
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
