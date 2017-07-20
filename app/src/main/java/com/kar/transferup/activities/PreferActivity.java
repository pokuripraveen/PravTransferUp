package com.kar.transferup.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.util.Log;

import com.kar.transferup.R;

/**
 * Created by praveenp on 06-01-2017.
 */

public class PreferActivity extends PreferenceActivity {
    private AccountManager mAccountManager;

    private static final String TAG = PreferActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        private boolean shouldForceSync = false;

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.account_pref_resources);
            findPreference("privacy_contacts").setOnPreferenceChangeListener(syncToggle);
        }

        @Override
        public void onPause() {
            super.onPause();
            if (shouldForceSync) {
                Log.i(TAG," Going to request Sync....!!!!!");
                Bundle res = new Bundle();
                res.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                res.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                res.putString(AccountManager.KEY_AUTHTOKEN, getResources().getString(R.string.ACCOUNT_TOKEN));
                Account account = new Account(getResources().getString(R.string.ACCOUNT_NAME), getResources().getString(R.string.ACCOUNT_TYPE));
                getActivity().getContentResolver().requestSync(account, ContactsContract.AUTHORITY, res);
            }
        }

        Preference.OnPreferenceChangeListener syncToggle = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.i(TAG," onPreferenceChange....!!!!!"+newValue.toString());
                shouldForceSync = true;
                return true;
            }
        };
    }


}
