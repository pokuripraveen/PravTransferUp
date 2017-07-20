package com.kar.transferup.service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kar.transferup.R;
import com.kar.transferup.activities.AuthenticatorActivity;
import com.kar.transferup.contacts.Constants;


/**
 * Created by praveenp on 05-01-2017.
 */

public class AccountAuthenticatorService extends Service {

    private static final String TAG = AccountAuthenticatorService.class.getSimpleName();
    private static AccountAuthenticatorImpl sAccountAuthenticator = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = null;
        //if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT))
            ret = getAuthenticator().getIBinder();
        return ret;
    }

    public AbstractAccountAuthenticator getAuthenticator() {
        if (sAccountAuthenticator == null)
            sAccountAuthenticator = new AccountAuthenticatorImpl(this);
        return sAccountAuthenticator;
    }

    /** The authenticator service is used to create an account for our app on the device.
    It plugs into the android accounts and authentication framework through which an account type
    is created on the device corresponding to our app. */
    private static class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {

        private Context mContext;

        public AccountAuthenticatorImpl(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
            return null;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
            final Bundle reply = new Bundle();

            final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
            //i.setAction("com.kar.transferup.sync.LOGIN");
            intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE,
                authTokenType);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
            reply.putParcelable(AccountManager.KEY_INTENT, intent);

            return reply;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String authTokenType, Bundle bundle) throws NetworkErrorException {
            Log.v(TAG, "getAuthToken() Type: "+authTokenType);
            // If the caller requested an authToken type we don't support, then
            // return an error
            if (!authTokenType.equals(Constants.AUTHTOKEN_TYPE)) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
                return result;
            }
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, mContext.getResources().getString(R.string.ACCOUNT_TYPE));
            return result;
        }

        @Override
        public String getAuthTokenLabel(String s) {
            // null means we don't support multiple authToken types
            Log.v(TAG, "getAuthTokenLabel()");
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            Log.v(TAG, "updateCredentials()");
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
            // This call is used to query whether the Authenticator supports
            // specific features. We don't expect to get called, so we always
            // return false (no) for any queries.
            Log.v(TAG, "hasFeatures()");
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
            return result;
        }
    }
}
