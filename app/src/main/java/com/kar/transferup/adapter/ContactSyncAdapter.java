package com.kar.transferup.adapter;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.kar.transferup.base.ContactManager;
import com.kar.transferup.contacts.Contact;
import com.kar.transferup.contacts.Contacts;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.MatchedContacts;
import com.kar.transferup.model.TransferUpContacts;
import com.kar.transferup.model.User;
import com.kar.transferup.storage.PreferenceManager;
import com.kar.transferup.util.NetworkUtils;

import java.util.List;

import static com.kar.transferup.R.id.user;
import static com.kar.transferup.storage.PreferenceManager.KEY_IS_FIRST_SYNC;

/**
 * Created by praveenp on 06-01-2017.
 */

@SuppressLint("NewApi")
public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String SYNC_MARKER_KEY = "com.kar.transferup.sync.marker";
    private final AccountManager mAccountManager;
    private final Context mContext;

    public ContactSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    public ContactSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon()
            .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
            .build();
    }

    /**
     * Perform a sync for this account. SyncAdapter-specific parameters may
     * be specified in extras, which is guaranteed to not be null. Invocations
     * of this method are guaranteed to be serialized.
     *
     * @param account the account that should be synced
     * @param extras SyncAdapter-specific parameters
     * @param authority the authority of this sync request
     * @param provider a ContentProviderClient that points to the ContentProvider for this
     *   authority
     * @param syncResult SyncAdapter-specific parameters
     */
    @Override
    public void onPerformSync(final Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            performSync(mContext, account, extras, authority, provider, syncResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performSync(Context context, final Account account, Bundle extras, String authority,
                                    ContentProviderClient provider, SyncResult syncResult) {

        Logger.i("performSync...!!!!authority %s ", authority);
        User user = PreferenceManager.getInstance().getUser();
        if(user == null){
            Logger.i("Unable to perform Sync operation as user info was not saved...!!!!");
            return;
        }

        // see if we already have a sync-state attached to this account. By handing
        // This value to the server, we can just get the contacts that have
        // been updated on the server-side since our last sync-up
        final long lastSyncMarker = getServerSyncMarker(account);

        // By default, contacts from a 3rd party provider are hidden in the contacts
        // list. So let's set the flag that causes them to be visible, so that users
        // can actually see these contacts.
        if (lastSyncMarker == 0) {
            ContactManager.setAccountContactsVisibility(getContext(), account, true);
        }

        // Make sure that the sample group exists
        final long groupId = ContactManager.ensureSampleGroupExists(mContext, account);
        boolean isFirstUpload = PreferenceManager.getInstance().isContactsUploaded();
        List<Contact>  innerDirtyContacts;
        if(isFirstUpload) {
            PreferenceManager.getInstance().put(KEY_IS_FIRST_SYNC, false);
             innerDirtyContacts = Contacts.getQuery().find();
        } else {
             innerDirtyContacts = ContactManager.getDirtyContacts(mContext, account);
        }
        // Find the local 'dirty' contacts that we need to tell the server about...
        // Find the local users that need to be sync'd to the server...
        final List<Contact> dirtyContacts =  innerDirtyContacts;


        MatchedContacts contact = new MatchedContacts();
        contact.setOwnerName(user.getName());
        contact.setOwnerNumber(user.getCountryCode()+user.getMobileNumber());
        contact.setOwnerSyncType(isFirstUpload ? "full" : "partial");
        contact.setContacts(dirtyContacts);

        // Send the dirty contacts to the server, and retrieve the server-side changes
        NetworkUtils.matchedContactsSync(contact, lastSyncMarker, account, groupId, new NetworkUtils.ContactLoadListener(){

            @Override
            public void onContactsLoaded(TransferUpContacts contacts) {
                if(contacts == null){
                    Logger.i("contacts might be NULL");
                    return;
                }
                long newSyncState = ContactManager.updateContacts(mContext,account.name, contacts.getInstalledContacts(), groupId, lastSyncMarker);
                // Save off the new sync marker. On our next sync, we only want to receive
                // contacts that have changed since this sync...
                setServerSyncMarker(account, newSyncState);
                if (dirtyContacts.size() > 0) {
                    ContactManager.clearSyncFlags(mContext, dirtyContacts);
                }
            }
        });
    }

    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     * @param account the account we're syncing
     * @return the change high-water-mark
     */
    private long getServerSyncMarker(Account account) {
        String markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        if (!TextUtils.isEmpty(markerString)) {
            return Long.parseLong(markerString);
        }
        return 0;
    }

    /**
     * Save off the high-water-mark we receive back from the server.
     * @param account The account we're syncing
     * @param marker The high-water-mark we want to save.
     */
    private void setServerSyncMarker(Account account, long marker) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, Long.toString(marker));
    }
}
