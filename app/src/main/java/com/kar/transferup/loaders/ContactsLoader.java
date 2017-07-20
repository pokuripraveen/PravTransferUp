package com.kar.transferup.loaders;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.kar.transferup.contacts.Contacts;

/**
 * Created by praveenp on 15-12-2016.
 */

public class ContactsLoader extends AsyncTaskLoader<Cursor> {
    private static final String TAG = ContactsLoader.class.getSimpleName();
    private Cursor mData;
    private ForceLoadContentObserver mObserver;

    public ContactsLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mData);
        }

        if (mObserver == null) {
            mObserver = new ForceLoadContentObserver();
            getContext().getApplicationContext().getContentResolver().registerContentObserver(ContactsContract.RawContacts.CONTENT_URI, true, mObserver);
        }

        if (takeContentChanged() || mData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    public Cursor loadInBackground() {
        Log.i("PRAV1","Contacts loadInBackground ");
        return Contacts.getQuery().getTransferUpContacts();
    }

    @Override
    public void deliverResult(Cursor data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            releaseResources(data);
            return;
        }
        Cursor oldData = mData;
        mData = data;
        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    @Override
    public void onCanceled(Cursor data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        if (mData != null) {
            releaseResources(mData);
            mData = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
        if (mObserver != null) {
            getContext().getApplicationContext().getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

    private void releaseResources(Cursor oldData) {
        if(null != oldData){
            oldData.close();
        }
    }
}
