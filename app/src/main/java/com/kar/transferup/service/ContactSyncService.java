package com.kar.transferup.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kar.transferup.adapter.ContactSyncAdapter;

/**
 * Created by praveenp on 06-01-2017.
 */

/**
 * The sync service is made up of two parts the sync adapter and the sync service itself.
 * The sync adapter is used to synchronize data between the server and the local database.
 * The sync service is what binds the sync adapter to the android sync framework.
 */
public class ContactSyncService extends Service {
    private static final String TAG = ContactSyncService.class.getSimpleName();
    // Storage for an instance of the sync adapter
    private static ContactSyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        Log.i(TAG,"ContactSyncService OnCreate!!!! "+sSyncAdapter);
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new ContactSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
         /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
