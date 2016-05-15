package com.example.fanchaozhou.moview;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by Fanchao Zhou on 3/12/2016.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            addPreferencesFromResource(R.xml.fragment_pref);
            Preference sortPref = findPreference(getString(R.string.pref_sort_key));
            sortPref.setOnPreferenceChangeListener(this);   //Registering a listener for this item

            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //The following code in this method is for displaying the values of all the settings whenever
        //the app switches to the settings page
        String sort = sharedPref.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
        findPreference(getString(R.string.pref_sort_key)).setSummary(sort);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_main){
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.addToBackStack(null);    //Pushing to the stack for BACK button to trace back to previous fragments
            transaction.replace(R.id.container,  new MainActivityFragment());  //Start a settings fragment
            transaction.commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //This method is invoked whenever a change has been made to any of the settings items

        if(preference.getKey().equals(getString(R.string.pref_sort_key))){  //If the server address is changed
            String newValueStr = newValue.toString();
            preference.setSummary(newValueStr);    //Setting the summary of an item
        }

        return true;
    }
}
