package com.kar.transferup.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.kar.transferup.base.TransferUpApplication;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.User;

/**
 * Created by praveenp on 09-12-2016.
 */

public class PreferenceManager {
    private String TAG = PreferenceManager.class.getSimpleName();

    // Shared Preferences
    private static SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Shared pref mode
    private static final int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "UserPref";

    // All Shared Preferences Keys
    public static final String KEY_USER_ID = "fcm_id";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_NAME = "name";
    public static final String KEY_USER_PHONE = "phone";
    public static final String KEY_USER_PHOTO_URL = "photo";
    public static final String KEY_USER_COUNTRY_CODE = "countryCode";
    public static final String KEY_IS_FIRST_SYNC = "firstSync";

    private static final String KEY_NOTIFICATIONS = "notifications";

    private static PreferenceManager sPreferenceManager;

    // Constructor
    private PreferenceManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public static PreferenceManager getInstance(){
        if(sPreferenceManager == null){
            sPreferenceManager = new PreferenceManager(TransferUpApplication.getContext());
        }
        return sPreferenceManager;
    }

    public void put(String key, String value){
        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, boolean value){
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getFcmId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_PHONE, user.getMobileNumber());
        if(user.getContactImageUrl() != null){
            editor.putString(KEY_USER_PHOTO_URL, user.getContactImageUrl());
        }
        editor.putString(KEY_USER_EMAIL, user.getEMail());
        editor.putString(KEY_USER_COUNTRY_CODE, user.getCountryCode());
        editor.commit();

        Logger.i("User is stored in shared preferences. " + user.getName() + ", " + user.getMobileNumber());
    }

    public User getUser() {
        String id = pref.getString(KEY_USER_ID, null);
        String mobile = pref.getString(KEY_USER_PHONE, null);

        if (id != null && mobile != null) {
            String name = pref.getString(KEY_USER_NAME, null);
            String email = pref.getString(KEY_USER_EMAIL, null);
            String countryCode = pref.getString(KEY_USER_COUNTRY_CODE, null);
            String photoUrl = pref.getString(KEY_USER_PHOTO_URL, null);
            User user = new User(name, mobile, email, countryCode, id, photoUrl);
            user.setCountryCode(countryCode);
            return user;
        }

        return null;
    }

    public String getFcmId(){
        return pref.getString(KEY_USER_ID, null);
    }

    public void addNotification(String notification) {

        // get old notifications
        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(KEY_NOTIFICATIONS, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return pref.getString(KEY_NOTIFICATIONS, null);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

    public boolean isContactsUploaded() {
        return pref.getBoolean(KEY_IS_FIRST_SYNC, true);
    }
}
