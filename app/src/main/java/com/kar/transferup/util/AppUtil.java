package com.kar.transferup.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.kar.transferup.R;
import com.kar.transferup.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by praveenp on 18-04-2017.
 */

public class AppUtil {
    private static final AppUtil ourInstance = new AppUtil();
    public static final  String TAG = "TransferUp";

    public static AppUtil getInstance() {
        return ourInstance;
    }

    private AppUtil() {
    }


    public enum Type {
        ME(1),
        OTHER(2);
        private int type;

        Type(int userType) {
            type = userType;
        }

        public int getType() {
            return type;
        }
    }

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @param dateFormat   Date format
     * @return String representing date in specified format
     */
    public static String getDateFromMillis(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static boolean isEmailValid(String email) {

        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isValidUser(String email, String mobile) {
        return android.util.Patterns.PHONE.matcher(mobile).matches() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    /*  ============== Account sync related =================*/
    public boolean isAccountExist(Context context) {
        AccountManager mAccountManager = AccountManager.get(context.getApplicationContext());
        Account[] accounts = mAccountManager.getAccountsByType(context.getResources().getString(R.string.ACCOUNT_TYPE));
        for (Account account : accounts) {
            if (context.getResources().getString(R.string.ACCOUNT_NAME).equalsIgnoreCase(account.name)) {
                Logger.i("Account exists");
                return true;
            }
        }
        return false;
    }

    public void createAndSync(Context context) {
        Resources resources = context.getResources();
        Account tUpaccount = new Account(resources.getString(R.string.ACCOUNT_NAME), resources.getString(R.string.ACCOUNT_TYPE));
        AccountManager accountManager = AccountManager.get(context);
        accountManager.addAccountExplicitly(tUpaccount, null, null);
        forceSync(resources);
    }

    public void forceSync(Resources resources) {
        Bundle res = new Bundle();
        res.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        res.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        res.putString(AccountManager.KEY_AUTHTOKEN, resources.getString(R.string.ACCOUNT_TOKEN));
        Account account = new Account(resources.getString(R.string.ACCOUNT_NAME), resources.getString(R.string.ACCOUNT_TYPE));
        ContentResolver.requestSync(account, ContactsContract.AUTHORITY, res);
    }

    public static String getSearchableMobile(String phone){
        String mobile = phone;
        if(phone.startsWith("+91") || phone.startsWith("+93")){
            mobile = phone.substring(3);
        } else if(phone.startsWith("+1")){
            mobile = phone.substring(2);
        }
        return mobile;
    }

}
