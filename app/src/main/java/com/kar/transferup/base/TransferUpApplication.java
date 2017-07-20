package com.kar.transferup.base;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.kar.transferup.contacts.Contacts;


/**
 * Created by praveenp on 09-12-2016.
 */

public class TransferUpApplication extends MultiDexApplication {

    private static TransferUpApplication sInstance ;

    public static synchronized TransferUpApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        Contacts.initialize(this);
    }

    @Override
    public void attachBaseContext(Context base) {
        MultiDex.install(base);
        super.attachBaseContext(base);
    }

    /**
     * Singleton fetching. It can be {@code null}.
     *
     * @return Instance of the {@link Context}. It can be {@code null}.
     */
    public static synchronized Context getContext() {
        return getInstance().getApplicationContext();
    }
}
