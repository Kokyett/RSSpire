package fr.kokyett.rsspire.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat;
import fr.kokyett.rsspire.R

class LogsPreferencesFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_logs, rootKey);
    }
}