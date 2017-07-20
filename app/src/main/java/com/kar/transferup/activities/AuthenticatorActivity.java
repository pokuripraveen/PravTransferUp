package com.kar.transferup.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.kar.transferup.R;


/**
 * Created by praveenp on 09-01-2017.
 */

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent res = new Intent();
        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, getResources().getString(R.string.ACCOUNT_NAME));
        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, getResources().getString(R.string.ACCOUNT_TYPE));
        res.putExtra(AccountManager.KEY_AUTHTOKEN, R.string.ACCOUNT_TOKEN);
        Account account = new Account(getResources().getString(R.string.ACCOUNT_NAME), getResources().getString(R.string.ACCOUNT_TYPE));
        mAccountManager = AccountManager.get(this);
        mAccountManager.addAccountExplicitly(account, null, null);
        ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);

        setAccountAuthenticatorResult(res.getExtras());
        setResult(RESULT_OK, res);
        finish();
    }

}
