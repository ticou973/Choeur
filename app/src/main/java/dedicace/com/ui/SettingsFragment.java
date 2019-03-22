package dedicace.com.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import dedicace.com.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        addPreferencesFromResource(R.xml.pref_chorale);

    }
}
