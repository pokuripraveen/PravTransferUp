package com.kar.transferup.base;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import com.kar.transferup.contacts.BatchOperation;
import com.kar.transferup.contacts.Constants;
import com.kar.transferup.contacts.Contact;
import com.kar.transferup.contacts.ContactOperations;
import com.kar.transferup.contacts.Contacts;
import com.kar.transferup.contacts.PhoneNumber;
import com.kar.transferup.contacts.RawContact;
import com.kar.transferup.contacts.TransferUpSyncAdapterColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by praveenp on 09-01-2017.
 */

public class ContactManager {

    public static final String CUSTOM_IM_PROTOCOL = "SampleSyncAdapter";
    private static final String TAG = ContactManager.class.getSimpleName();
    public static final String SAMPLE_GROUP_NAME = "TransferUp";

    /**
     * Take a list of updated contacts and apply those changes to the
     * contacts database. Typically this list of contacts would have been
     * returned from the server, and we want to apply those changes locally.
     *
     * @param context The context of Authenticator Activity
     * @param account The username for the account
     * @param rawContacts The list of contacts to update
     * @param lastSyncMarker The previous server sync-state
     * @return the server syncState that should be used in our next
     * sync request.
     */
    public static synchronized long updateRawContacts(Context context, String account,
                                                      List<RawContact> rawContacts, long groupId, long lastSyncMarker) {
        long currentSyncMarker = lastSyncMarker;
        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(context, resolver);
        final List<RawContact> newUsers = new ArrayList<RawContact>();
        Log.d(TAG, "In SyncContacts");
        for (final RawContact rawContact : rawContacts) {
            // The server returns a syncState (x) value with each contact record.
            // The syncState is sequential, so higher values represent more recent
            // changes than lower values. We keep track of the highest value we
            // see, and consider that a "high water mark" for the changes we've
            // received from the server.  That way, on our next sync, we can just
            // ask for changes that have occurred since that most-recent change.
            if (rawContact.getSyncState() > currentSyncMarker) {
                currentSyncMarker = rawContact.getSyncState();
            }
            // If the server returned a clientId for this user, then it's likely
            // that the user was added here, and was just pushed to the server
            // for the first time. In that case, we need to update the main
            // row for this contact so that the RawContacts.SOURCE_ID value
            // contains the correct serverId.
            final long rawContactId;
            final boolean updateServerId;
            if (rawContact.getRawContactId() > 0) {
                rawContactId = rawContact.getRawContactId();
                updateServerId = true;
            } else {
                String serverContactId = rawContact.getServerContactId();
                rawContactId = lookupRawContact(resolver, serverContactId);
                updateServerId = false;
            }
            if (rawContactId != 0) {
                if (!rawContact.isDeleted()) {
                    updateContact(context, resolver, rawContact, updateServerId,
                        true, true, true, rawContactId, batchOperation);
                } else {
                    deleteContact(context, rawContactId, batchOperation);
                }
            } else {
                Log.d(TAG, "In addContact");
                if (!rawContact.isDeleted()) {
                    newUsers.add(rawContact);
                    addContact(context, account, rawContact, groupId, true, batchOperation);
                }
            }
            // A sync adapter should batch operations on multiple contacts,
            // because it will make a dramatic performance difference.
            // (UI updates, etc)
            if (batchOperation.size() >= 50) {
                batchOperation.execute();
            }
        }
        batchOperation.execute();
        return currentSyncMarker;
    }


    /**
     * Take a list of updated contacts and apply those changes to the
     * contacts database. Typically this list of contacts would have been
     * returned from the server, and we want to apply those changes locally.
     *
     * @param context The context of Authenticator Activity
     * @param account The username for the account
     * @param contacts The list of contacts to update
     * @param lastSyncMarker The previous server sync-state
     * @return the server syncState that should be used in our next
     * sync request.
     */
    public static synchronized long updateContacts(Context context, String account,
                                                   List<Contact> contacts, long groupId, long lastSyncMarker) {
        long currentSyncMarker = lastSyncMarker;
        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(context, resolver);
        if(contacts == null || contacts.size() == 0){
            Log.i(TAG," Contact sto Update is Empty");
            return lastSyncMarker;
        }
        Log.d(TAG, "In SyncContacts Update Contacts Size: "+contacts.size());
        for (final Contact contact : contacts) {
            // The server returns a syncState (x) value with each contact record.
            // The syncState is sequential, so higher values represent more recent
            // changes than lower values. We keep track of the highest value we
            // see, and consider that a "high water mark" for the changes we've
            // received from the server.  That way, on our next sync, we can just
            // ask for changes that have occurred since that most-recent change.
            if (contact.getSyncTime() > currentSyncMarker) {
                currentSyncMarker = contact.getSyncTime();
            }
            // If the server returned a clientId for this user, then it's likely
            // that the user was added here, and was just pushed to the server
            // for the first time. In that case, we need to update the main
            // row for this contact so that the RawContacts.SOURCE_ID value
            // contains the correct serverId.
            final long rawContactId;
            final boolean updateServerId;
            Log.i(TAG,"NAme: "+contact.getDisplayName() +" RawID: "+contact.getRawContactId());
            if (contact.getRawContactId() > 0) {
                rawContactId = contact.getRawContactId();
                updateServerId = true;
            } else {
                String serverContactId = contact.getServerId();
                rawContactId = lookupRawContact(resolver, serverContactId);
                Log.i(TAG,"else RawID: "+rawContactId +" serverContactId "+serverContactId);
                updateServerId = false;
            }
            if (rawContactId != 0) {
                if (!contact.isDeleted()) {
                    Log.d(TAG, "updateContact rawContactId: "+rawContactId);
                    updateContact(context, resolver, contact, updateServerId,
                        true, true, true, rawContactId, batchOperation);
                } else {
                    Log.d(TAG, "deleteContact rawContactId: "+rawContactId);
                    deleteContact(context, rawContactId, batchOperation);
                }
            } else {
                Log.d(TAG, "In addContact ");
                if (!contact.isDeleted()) {
                    Log.i("PRAV1","Adding Contacts...!!!!");
                    addContact(context, contact, groupId, true, batchOperation);
                }
            }
            // A sync adapter should batch operations on multiple contacts,
            // because it will make a dramatic performance difference.
            // (UI updates, etc)
            if (batchOperation.size() >= 3) {
                batchOperation.execute();
            }
        }
        batchOperation.execute();
        return currentSyncMarker;
    }


    /**
     * Deletes a contact from the platform contacts provider. This method is used
     * both for contacts that were deleted locally and then that deletion was synced
     * to the server, and for contacts that were deleted on the server and the
     * deletion was synced to the client.
     *
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     */
    private static void deleteContact(Context context, long rawContactId,
                                      BatchOperation batchOperation) {
        batchOperation.add(ContactOperations.newDeleteCpo(
            ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
            true, true).build());
    }

    /**
     * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
     * sample SyncAdapter user isn't found.
     *
     * @param resolver the content resolver to use
     * @param serverContactId the sample SyncAdapter user ID to lookup
     * @return the RawContact id, or 0 if not found
     */
    private static long lookupRawContact(ContentResolver resolver, String serverContactId) {
        long rawContactId = 0;
        final Cursor c = resolver.query(
            UserIdQuery.CONTENT_URI,
            UserIdQuery.PROJECTION,
            UserIdQuery.SELECTION,
            new String[] {String.valueOf(serverContactId)},
            null);
        try {
            if ((c != null) && c.moveToFirst()) {
                rawContactId = c.getLong(UserIdQuery.COLUMN_RAW_CONTACT_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return rawContactId;
    }

    /**
     * Returns the Data id for a sample SyncAdapter contact's profile row, or 0
     * if the sample SyncAdapter user isn't found.
     *
     * @param resolver a content resolver
     * @param userId the sample SyncAdapter user ID to lookup
     * @return the profile Data row id, or 0 if not found
     */
    private static long lookupProfile(ContentResolver resolver, String userId) {
        long profileId = 0;
        final Cursor c =
            resolver.query(Data.CONTENT_URI, ProfileQuery.PROJECTION, ProfileQuery.SELECTION,
                new String[] {userId}, null);
        try {
            if ((c != null) && c.moveToFirst()) {
                profileId = c.getLong(ProfileQuery.COLUMN_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return profileId;
    }

    /**
     * Adds a single contact to the platform contacts provider.
     * This can be used to respond to a new contact found as part
     * of sync information returned from the server, or because a
     * user added a new contact.
     *
     * @param context the Authenticator Activity context
     * @param accountName the account the contact belongs to
     * @param rawContact the sample SyncAdapter User object
     * @param groupId the id of the sample group
     * @param inSync is the add part of a client-server sync?
     * @param batchOperation allow us to batch together multiple operations
     *        into a single provider call
     */
    public static void addContact(Context context, String accountName, RawContact rawContact,
                                  long groupId, boolean inSync, BatchOperation batchOperation) {
        // Put the data in the contacts provider
        final ContactOperations contactOp = ContactOperations.createNewContact(
            context, 0 ,rawContact.getServerContactId(), accountName, inSync, batchOperation);
        contactOp.addName(rawContact.getFullName(), rawContact.getFirstName(),
            rawContact.getLastName())
            .addEmail(rawContact.getEmail())
            .addPhone(rawContact.getCellPhone(), Phone.TYPE_MOBILE)
            .addPhone(rawContact.getHomePhone(), Phone.TYPE_HOME)
            .addPhone(rawContact.getOfficePhone(), Phone.TYPE_WORK)
            .addGroupMembership(groupId)
            .addAvatar(rawContact.getAvatarUrl());
        // If we have a serverId, then go ahead and create our status profile.
        // Otherwise skip it - and we'll create it after we sync-up to the
        // server later on.
        if (rawContact.getServerContactId() != null) {
            contactOp.addProfileAction(rawContact);
        }
    }

    public static void addContact(Context context, Contact contact, long groupId, Boolean isSync, BatchOperation batchOperation) {
        List<PhoneNumber> numbers = contact.getPhoneNumbers();
        String lHomePhone ="";
        String lMobile ="";
        String lWork = "";
        Log.d(TAG, "In addContact "+contact.getDisplayName());
        for(PhoneNumber number : numbers){
            if(PhoneNumber.Type.HOME.equals(number.getType())){
                lHomePhone = number.getNumber();
            }
            if(PhoneNumber.Type.MOBILE.equals(number.getType())){
                lMobile = number.getNumber();
            }
            if(PhoneNumber.Type.WORK.equals(number.getType())){
                lWork = number.getNumber();
            }
        }
        final ContactOperations contactOp = ContactOperations.createNewContact(
            context, contact.getContactId(), contact.getServerId() == null ? ""+contact.getContactId() : contact.getServerId(), Constants.ACCOUNT_NAME, isSync, batchOperation);
        contactOp.addName(contact.getDisplayName(), contact.getGivenName(),
            contact.getFamilyName())
            .addEmail(contact.getEmails())
            .addPhone(lMobile, Phone.TYPE_MOBILE)
            .addPhone(lHomePhone, Phone.TYPE_HOME)
            .addPhone(lWork, Phone.TYPE_WORK)
            .addGroupMembership(groupId)
            .addAvatar(contact.getPhotoUri());
        // If we have a serverId, then go ahead and create our status profile.
        // Otherwise skip it - and we'll create it after we sync-up to the
        // server later on.
        if(contact.getServerId() != null) {
            contactOp.addProfileAction(contact);
        }
        batchOperation.execute();
    }

    public static void addContact(Context context, Contact contact) {
        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(context, resolver);
        final ContactOperations contactOp =
            ContactOperations.createNewContact(context, contact.getContactId(), contact.getServerId(), Constants.ACCOUNT_NAME, true, batchOperation);


        contactOp.addName(contact.getDisplayName(), contact.getGivenName(), contact.getFamilyName()).addEmail(
            contact.getEmails()).addPhones(contact.getPhoneNumbers())
            .addProfileAction(contact);

        batchOperation.execute();
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri,
                                                       boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                    "true").build();
        }
        return uri;
    }

    /**
     * Updates a single contact to the platform contacts provider.
     * This method can be used to update a contact from a sync
     * operation or as a result of a user editing a contact
     * record.
     *
     * This operation is actually relatively complex.  We query
     * the database to find all the rows of info that already
     * exist for this Contact. For rows that exist (and thus we're
     * modifying existing fields), we create an update operation
     * to change that field.  But for fields we're adding, we create
     * "add" operations to create new rows for those fields.
     *
     * @param context the Authenticator Activity context
     * @param resolver the ContentResolver to use
     * @param rawContact the sample SyncAdapter contact object
     * @param updateStatus should we update this user's status
     * @param updateAvatar should we update this user's avatar image
     * @param inSync is the update part of a client-server sync?
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     * @param batchOperation allow us to batch together multiple operations
     *        into a single provider call
     */
    public static void updateContact(Context context, ContentResolver resolver,
                                     RawContact rawContact, boolean updateServerId, boolean updateStatus, boolean updateAvatar,
                                     boolean inSync, long rawContactId, BatchOperation batchOperation) {
        boolean existingCellPhone = false;
        boolean existingHomePhone = false;
        boolean existingWorkPhone = false;
        boolean existingEmail = false;
        boolean existingAvatar = false;
        final Cursor c =
            resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                new String[] {String.valueOf(rawContactId)}, null);
        final ContactOperations contactOp =
            ContactOperations.updateExistingContact(context, rawContactId,
                inSync, batchOperation);
        try {
            // Iterate over the existing rows of data, and update each one
            // with the information we received from the server.
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                    contactOp.updateName(uri,
                        c.getString(DataQuery.COLUMN_GIVEN_NAME),
                        c.getString(DataQuery.COLUMN_FAMILY_NAME),
                        c.getString(DataQuery.COLUMN_FULL_NAME),
                        rawContact.getFirstName(),
                        rawContact.getLastName(),
                        rawContact.getFullName());
                } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == Phone.TYPE_MOBILE) {
                        existingCellPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                            rawContact.getCellPhone(), uri);
                    } else if (type == Phone.TYPE_HOME) {
                        existingHomePhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                            rawContact.getHomePhone(), uri);
                    } else if (type == Phone.TYPE_WORK) {
                        existingWorkPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                            rawContact.getOfficePhone(), uri);
                    }
                } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                    existingEmail = true;
                    contactOp.updateEmail(rawContact.getEmail(),
                        c.getString(DataQuery.COLUMN_EMAIL_ADDRESS), uri);
                } else if (mimeType.equals(Photo.CONTENT_ITEM_TYPE)) {
                    existingAvatar = true;
                    contactOp.updateAvatar(rawContact.getAvatarUrl(), uri);
                }
            } // while
        } finally {
            c.close();
        }
        // Add the cell phone, if present and not updated above
        if (!existingCellPhone) {
            contactOp.addPhone(rawContact.getCellPhone(), Phone.TYPE_MOBILE);
        }
        // Add the home phone, if present and not updated above
        if (!existingHomePhone) {
            contactOp.addPhone(rawContact.getHomePhone(), Phone.TYPE_HOME);
        }
        // Add the work phone, if present and not updated above
        if (!existingWorkPhone) {
            contactOp.addPhone(rawContact.getOfficePhone(), Phone.TYPE_WORK);
        }
        // Add the email address, if present and not updated above
        if (!existingEmail) {
            contactOp.addEmail(rawContact.getEmail());
        }
        // Add the avatar if we didn't update the existing avatar
        if (!existingAvatar) {
            contactOp.addAvatar(rawContact.getAvatarUrl());
        }
        // If we need to update the serverId of the contact record, take
        // care of that.  This will happen if the contact is created on the
        // client, and then synced to the server. When we get the updated
        // record back from the server, we can set the SOURCE_ID property
        // on the contact, so we can (in the future) lookup contacts by
        // the serverId.
        if (updateServerId) {
            Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
            contactOp.updateServerId(rawContact.getServerContactId(), uri);
        }
        // If we don't have a status profile, then create one.  This could
        // happen for contacts that were created on the client - we don't
        // create the status profile until after the first sync...
        final String serverId = rawContact.getServerContactId();
        final long profileId = lookupProfile(resolver, serverId);
        if (profileId <= 0) {
            contactOp.addProfileAction(rawContact);
        }
    }

    /**
     * Updates a single contact to the platform contacts provider.
     * This method can be used to update a contact from a sync
     * operation or as a result of a user editing a contact
     * record.
     *
     * This operation is actually relatively complex.  We query
     * the database to find all the rows of info that already
     * exist for this Contact. For rows that exist (and thus we're
     * modifying existing fields), we create an update operation
     * to change that field.  But for fields we're adding, we create
     * "add" operations to create new rows for those fields.
     *
     * @param context the Authenticator Activity context
     * @param resolver the ContentResolver to use
     * @param rawContact the sample SyncAdapter contact object
     * @param updateStatus should we update this user's status
     * @param updateAvatar should we update this user's avatar image
     * @param inSync is the update part of a client-server sync?
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     * @param batchOperation allow us to batch together multiple operations
     *        into a single provider call
     */
    public static void updateContact(Context context, ContentResolver resolver,
                                     Contact rawContact, boolean updateServerId, boolean updateStatus, boolean updateAvatar,
                                     boolean inSync, long rawContactId, BatchOperation batchOperation) {
        boolean existingCellPhone = false;
        boolean existingHomePhone = false;
        boolean existingWorkPhone = false;
        boolean existingEmail = false;
        boolean existingAvatar = false;
        final Cursor c =
            resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                new String[] {String.valueOf(rawContactId)}, null);
        final ContactOperations contactOp =
            ContactOperations.updateExistingContact(context, rawContactId,
                inSync, batchOperation);
        List<PhoneNumber> numbers = rawContact.getPhoneNumbers();
        Log.i(TAG," updateContact:  "+rawContact.getDisplayName());
        String lHomePhone ="";
        String lMobile ="";
        String lWork = "";

        for(PhoneNumber number : numbers){
            if(PhoneNumber.Type.HOME.equals(number.getType())){
                lHomePhone = number.getNumber();
            }
            if(PhoneNumber.Type.MOBILE.equals(number.getType())){
                lMobile = number.getNumber();
            }
            if(PhoneNumber.Type.WORK.equals(number.getType())){
                lWork = number.getNumber();
            }
        }

        try {
            // Iterate over the existing rows of data, and update each one
            // with the information we received from the server.
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                    contactOp.updateName(uri,
                        c.getString(DataQuery.COLUMN_GIVEN_NAME),
                        c.getString(DataQuery.COLUMN_FAMILY_NAME),
                        c.getString(DataQuery.COLUMN_FULL_NAME),
                        rawContact.getGivenName(),
                        rawContact.getFamilyName(),
                        rawContact.getDisplayName());
                } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);

                    if (type == Phone.TYPE_MOBILE) {
                        existingCellPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                            lMobile, uri);
                    } else if (type == Phone.TYPE_HOME) {
                        existingHomePhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                            lHomePhone, uri);
                    } else if (type == Phone.TYPE_WORK) {
                        existingWorkPhone = true;
                        contactOp.updatePhone(c.getString(DataQuery.COLUMN_PHONE_NUMBER),
                            lWork, uri);
                    }
                } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                    existingEmail = true;
                    contactOp.updateEmail(rawContact.getEmails().get(0) == null || rawContact.getEmails().size() == 0  ? "" : rawContact.getEmails().get(0).toString(),
                        c.getString(DataQuery.COLUMN_EMAIL_ADDRESS), uri);
                } else if (mimeType.equals(Photo.CONTENT_ITEM_TYPE)) {
                    existingAvatar = true;
                    contactOp.updateAvatar(rawContact.getPhotoUri(), uri);
                }
            } // while
        } finally {
            c.close();
        }
        // Add the cell phone, if present and not updated above
        if (!existingCellPhone) {
            contactOp.addPhone(lMobile, Phone.TYPE_MOBILE);
        }
        // Add the home phone, if present and not updated above
        if (!existingHomePhone) {
            contactOp.addPhone(lHomePhone, Phone.TYPE_HOME);
        }
        // Add the work phone, if present and not updated above
        if (!existingWorkPhone) {
            contactOp.addPhone(lWork, Phone.TYPE_WORK);
        }
        // Add the email address, if present and not updated above
        if (!existingEmail) {
            contactOp.addEmail(rawContact.getEmails());
        }
        // Add the avatar if we didn't update the existing avatar
        if (!existingAvatar) {
            contactOp.addAvatar(rawContact.getPhotoUri());
        }
        // If we need to update the serverId of the contact record, take
        // care of that.  This will happen if the contact is created on the
        // client, and then synced to the server. When we get the updated
        // record back from the server, we can set the SOURCE_ID property
        // on the contact, so we can (in the future) lookup contacts by
        // the serverId.
        if (updateServerId) {
            Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
            contactOp.updateServerId(rawContact.getServerId(), uri);
        }
        // If we don't have a status profile, then create one.  This could
        // happen for contacts that were created on the client - we don't
        // create the status profile until after the first sync...
        final String serverId = rawContact.getServerId();
        final long profileId = lookupProfile(resolver, serverId);
        if (profileId <= 0) {
            contactOp.addProfileAction(rawContact);
        }
    }


    /**
     * When we first add a sync adapter to the system, the contacts from that
     * sync adapter will be hidden unless they're merged/grouped with an existing
     * contact.  But typically we want to actually show those contacts, so we
     * need to mess with the Settings table to get them to show up.
     *
     * @param context the Authenticator Activity context
     * @param account the Account who's visibility we're changing
     * @param visible true if we want the contacts visible, false for hidden
     */
    public static void setAccountContactsVisibility(Context context, Account account,
                                                    boolean visible) {
        ContentValues values = new ContentValues();
        values.put(RawContacts.ACCOUNT_NAME, account.name);
        values.put(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        values.put(ContactsContract.Settings.UNGROUPED_VISIBLE, visible ? 1 : 0);
        context.getContentResolver().insert(ContactsContract.Settings.CONTENT_URI, values);
    }

    /**
     * Return a list of the local contacts that have been marked as
     * "dirty", and need syncing to the SampleSync server.
     *
     * @param context The context of Authenticator Activity
     * @param account The account that we're interested in syncing
     * @return a list of Users that are considered "dirty"
     */
    public static List<Contact> getDirtyContacts(Context context, Account account) {
        Log.i(TAG, "*** Looking for local dirty contacts");
        List<Contact> dirtyContacts = new ArrayList<Contact>();
        final ContentResolver resolver = context.getContentResolver();
        final Cursor c = resolver.query(DirtyQuery.CONTENT_URI,
            DirtyQuery.PROJECTION,
            DirtyQuery.DIRTY_SELECTION,null,
            null);
        Log.i(TAG,"Dirty Contact count "+c.getCount());
        try {
            while (c.moveToNext()) {
                final long rawContactId = c.getLong(DirtyQuery.COLUMN_RAW_CONTACT_ID);
                final String  nameSource = c.getString(DirtyQuery.COLUMN_DISPLAY_NAME_PRIMARY);
                final String serverContactId = c.getString(DirtyQuery.COLUMN_SERVER_ID);
                final long contactId = c.getLong(DirtyQuery.COLUMN_CONTACT_ID);
                final int version = c.getInt(DirtyQuery.COLUMN_VERSION);
                final boolean isDirty = "1".equals(c.getString(DirtyQuery.COLUMN_DIRTY));
                final boolean isDeleted = "1".equals(c.getString(DirtyQuery.COLUMN_DELETED));
                // The system actually keeps track of a change version number for
                // each contact. It may be something you're interested in for your
                // client-server sync protocol. We're not using it in this example,
                // other than to log it.
                Log.i(TAG, "Dirty Contact: " + Long.toString(rawContactId));
                Log.i(TAG, "serverContactId: " + serverContactId);
                Log.i(TAG, "contactId: " + Long.toString(contactId));
                Log.i(TAG, "Contact Name: " + nameSource +"  version  "+version);
                if (isDeleted) {
                    Log.i(TAG, "Contact is marked for deletion");
                    Contact contact = new Contact().addDisplayName(nameSource)
                        .addRawContactId(rawContactId)
                        .addServerId(serverContactId)
                        .addIsDeleted(true);
                    dirtyContacts.add(contact);

                } else if (isDirty) {
                    List<Contact> contacts = Contacts.getQuery().getContactById(contactId);
                    for(Contact contact : contacts){
                        contact.addRawContactId(rawContactId);
                        contact.addServerId(serverContactId);
                        contact.addIsDirty(true);
                    }
                    dirtyContacts.addAll(contacts);
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return dirtyContacts;
    }

    /**
     * Return a User object with data extracted from a contact stored
     * in the local contacts database.
     *
     * Because a contact is actually stored over several rows in the
     * database, our query will return those multiple rows of information.
     * We then iterate over the rows and build the User structure from
     * what we find.
     *
     * @param context the Authenticator Activity context
     * @param rawContactId the unique ID for the local contact
     * @return a User object containing info on that contact
     */
    private static RawContact getRawContact(Context context, long rawContactId) {

        String firstName = null;
        String lastName = null;
        String fullName = null;
        String cellPhone = null;
        String homePhone = null;
        String workPhone = null;
        String email = null;
        String serverId = null;
        final ContentResolver resolver = context.getContentResolver();
        final Cursor c =
            resolver.query(DataQuery.CONTENT_URI, DataQuery.PROJECTION, DataQuery.SELECTION,
                new String[] {String.valueOf(rawContactId)}, null);
        try {
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                final String tempServerId = c.getString(DataQuery.COLUMN_SERVER_ID);
                if (tempServerId != null) {
                    serverId = tempServerId;
                }
                final Uri uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);
                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                    lastName = c.getString(DataQuery.COLUMN_FAMILY_NAME);
                    firstName = c.getString(DataQuery.COLUMN_GIVEN_NAME);
                    fullName = c.getString(DataQuery.COLUMN_FULL_NAME);
                } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == Phone.TYPE_MOBILE) {
                        cellPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                    } else if (type == Phone.TYPE_HOME) {
                        homePhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                    } else if (type == Phone.TYPE_WORK) {
                        workPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                    }
                } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                    email = c.getString(DataQuery.COLUMN_EMAIL_ADDRESS);
                }
            } // while
        } finally {
            c.close();
        }
        // Now that we've extracted all the information we care about,
        // create the actual User object.
        RawContact rawContact = RawContact.create(fullName, firstName, lastName, cellPhone,
            workPhone, homePhone, email, null, false, rawContactId, serverId);
        return rawContact;
    }

    public static long ensureSampleGroupExists(Context context, Account account) {
        final ContentResolver resolver = context.getContentResolver();

        // Lookup the sample group
        long groupId = 0;
        final Cursor cursor = resolver.query(Groups.CONTENT_URI, new String[] { Groups._ID },
            Groups.ACCOUNT_NAME + "=? AND " + Groups.ACCOUNT_TYPE + "=? AND " +
                Groups.TITLE + "=?",
            new String[] { account.name, account.type, SAMPLE_GROUP_NAME }, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    groupId = cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        if (groupId == 0) {
            // Sample group doesn't exist yet, so create it
            final ContentValues contentValues = new ContentValues();
            contentValues.put(Groups.ACCOUNT_NAME, account.name);
            contentValues.put(Groups.ACCOUNT_TYPE, account.type);
            contentValues.put(Groups.TITLE, SAMPLE_GROUP_NAME);
            contentValues.put(Groups.GROUP_IS_READ_ONLY, true);

            final Uri newGroupUri = resolver.insert(Groups.CONTENT_URI, contentValues);
            groupId = ContentUris.parseId(newGroupUri);
        }
        return groupId;
    }


    /**
     * After we've finished up a sync operation, we want to clean up the sync-state
     * so that we're ready for the next time.  This involves clearing out the 'dirty'
     * flag on the synced contacts - but we also have to finish the DELETE operation
     * on deleted contacts.  When the user initially deletes them on the client, they're
     * marked for deletion - but they're not actually deleted until we delete them
     * again, and include the ContactsContract.CALLER_IS_SYNCADAPTER parameter to
     * tell the contacts provider that we're really ready to let go of this contact.
     *
     * @param context The context of Authenticator Activity
     * @param dirtyContacts The list of contacts that we're cleaning up
     */
    public static void clearSyncFlags(Context context, List<Contact> dirtyContacts) {
        Log.i(TAG, "*** Clearing Sync-related Flags");
        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation = new BatchOperation(context, resolver);
        for (Contact rawContact : dirtyContacts) {
            if (rawContact.isDeleted()) {
                Log.i(TAG, "Deleting contact: " + Long.toString(rawContact.getRawContactId()));
                deleteContact(context, rawContact.getRawContactId(), batchOperation);
            } else if (rawContact.isDirty()) {
                Log.i(TAG, "Clearing dirty flag for: " + rawContact.getDisplayName());
                clearDirtyFlag(context, rawContact.getRawContactId(), batchOperation);
            }
        }
        batchOperation.execute();
    }

    /**
     * Clear the local system 'dirty' flag for a contact.
     *
     * @param context the Authenticator Activity context
     * @param rawContactId the id of the contact update
     * @param batchOperation allow us to batch together multiple operations
     */
    private static void clearDirtyFlag(Context context, long rawContactId,
                                       BatchOperation batchOperation) {
        final ContactOperations contactOp =
            ContactOperations.updateExistingContact(context, rawContactId, true,
                batchOperation);

        final Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        contactOp.updateDirtyFlag(false, uri);
    }

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    final private static class ProfileQuery {
        private ProfileQuery() {
        }
        public final static String[] PROJECTION = new String[] {Data._ID};
        public final static int COLUMN_ID = 0;
        public static final String SELECTION =
            Data.MIMETYPE + "='" + TransferUpSyncAdapterColumns.MIME_PROFILE + "' AND "
                + TransferUpSyncAdapterColumns.DATA_PID + "=?";
    }


    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    final private static class UserIdQuery {
        private UserIdQuery() {
        }
        public final static String[] PROJECTION = new String[] {
            RawContacts._ID,
            RawContacts.CONTACT_ID
        };
        public final static int COLUMN_RAW_CONTACT_ID = 0;
        public final static int COLUMN_LINKED_CONTACT_ID = 1;
        public final static Uri CONTENT_URI = RawContacts.CONTENT_URI;
        public static final String SELECTION =
            RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND "
                + RawContacts.SOURCE_ID + "=?";
    }


    /**
     * Constants for a query to find SampleSyncAdapter contacts that are
     * in need of syncing to the server. This should cover new, edited,
     * and deleted contacts.
     */
    final private static class DirtyQuery {
        private DirtyQuery() {
        }
        public final static String[] PROJECTION = new String[] {
            RawContacts._ID,
            RawContacts.SOURCE_ID,
            RawContacts.DIRTY,
            RawContacts.DELETED,
            RawContacts.VERSION,
            RawContacts.DISPLAY_NAME_PRIMARY,
            RawContacts.CONTACT_ID
        };
        public final static int COLUMN_RAW_CONTACT_ID = 0;
        public final static int COLUMN_SERVER_ID = 1;
        public final static int COLUMN_DIRTY = 2;
        public final static int COLUMN_DELETED = 3;
        public final static int COLUMN_VERSION = 4;
        public final static int COLUMN_DISPLAY_NAME_PRIMARY = 5;
        public final static int COLUMN_CONTACT_ID = 6;

        public static final Uri CONTENT_URI = RawContacts.CONTENT_URI.buildUpon()
            .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
            .build();
        public static final String SELECTION =
            RawContacts.DIRTY + "=1 AND "
                +
                RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND "
                + RawContacts.ACCOUNT_NAME + "=?";
        public static final String DIRTY_SELECTION =  RawContacts.DIRTY + "=1";

    }

    /**
     * Constants for a query to get contact data for a given rawContactId
     */
    final private static class DataQuery {
        private DataQuery() {
        }
        public static final String[] PROJECTION =
            new String[] {Data._ID, RawContacts.SOURCE_ID, Data.MIMETYPE, Data.DATA1,
                Data.DATA2, Data.DATA3, Data.DATA15, Data.SYNC1};
        public static final int COLUMN_ID = 0;
        public static final int COLUMN_SERVER_ID = 1;
        public static final int COLUMN_MIMETYPE = 2;
        public static final int COLUMN_DATA1 = 3;
        public static final int COLUMN_DATA2 = 4;
        public static final int COLUMN_DATA3 = 5;
        public static final int COLUMN_DATA15 = 6;
        public static final int COLUMN_SYNC1 = 7;
        public static final Uri CONTENT_URI = Data.CONTENT_URI;
        public static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
        public static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;
        public static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
        public static final int COLUMN_EMAIL_TYPE = COLUMN_DATA2;
        public static final int COLUMN_FULL_NAME = COLUMN_DATA1;
        public static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
        public static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;
        public static final int COLUMN_AVATAR_IMAGE = COLUMN_DATA15;
        public static final int COLUMN_SYNC_DIRTY = COLUMN_SYNC1;
        public static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
    }

    /**
     * Constants for a query to read basic contact columns
     */
    final public static class ContactQuery {
        private ContactQuery() {
        }
        public static final String[] PROJECTION =
            new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        public static final int COLUMN_ID = 0;
        public static final int COLUMN_DISPLAY_NAME = 1;
    }

    /**
     * Updates a single contact to the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param resolver the ContentResolver to use
     * @param user the sample SyncAdapter contact object.
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     */
    private static void updateContact(Context context,
                                      ContentResolver resolver,  Contact user,
                                      long rawContactId, BatchOperation batchOperation) {
        Uri uri;
        String cellPhone = null;
        String otherPhone = null;
        String email = null;

        final Cursor c =
            resolver.query(Data.CONTENT_URI, DataQuery.PROJECTION,
                DataQuery.SELECTION,
                new String[] {String.valueOf(rawContactId)}, null);
        final ContactOperations contactOp =
            ContactOperations.updateExistingContact(context, rawContactId,
                true, batchOperation);
        String lHomePhone = "";
        String lMobile = "";
        String lWork ="";

        try {
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);

                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                    final String lastName =
                        c.getString(DataQuery.COLUMN_FAMILY_NAME);
                    final String firstName =
                        c.getString(DataQuery.COLUMN_GIVEN_NAME);
                    final String fullName =
                        c.getString(DataQuery.COLUMN_FULL_NAME);
                    contactOp.updateName(uri, firstName, lastName,fullName, user
                        .getGivenName(), user.getFamilyName(), user.getDisplayName());
                }

                else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    List<PhoneNumber> numbers = user.getPhoneNumbers();

                    for(PhoneNumber number : numbers){
                        if(PhoneNumber.Type.HOME.equals(number.getType())){
                            lHomePhone = number.getNumber();
                        }
                        if(PhoneNumber.Type.MOBILE.equals(number.getType())){
                            lMobile = number.getNumber();
                        }
                        if(PhoneNumber.Type.WORK.equals(number.getType())){
                            lWork = number.getNumber();
                        }
                    }
                    if (type == Phone.TYPE_MOBILE) {
                        cellPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);

                        contactOp.updatePhone(cellPhone, lMobile,
                            uri);
                    } else if (type == Phone.TYPE_OTHER) {
                        otherPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                        contactOp.updatePhone(otherPhone, lHomePhone,
                            uri);
                    }
                }

                else if (Data.MIMETYPE.equals(Email.CONTENT_ITEM_TYPE)) {
                    email = c.getString(DataQuery.COLUMN_EMAIL_ADDRESS);
                    contactOp.updateEmail(user.getEmails().get(0) == null ? "" : user.getEmails().get(0).toString(), email, uri);

                }
            } // while
        } finally {
            c.close();
        }

        // Add the cell phone, if present and not updated above
        if (cellPhone == null) {
            contactOp.addPhone(lMobile, Phone.TYPE_MOBILE);
        }

        // Add the other phone, if present and not updated above
        if (otherPhone == null) {
            contactOp.addPhone(lHomePhone, Phone.TYPE_OTHER);
        }

        // Add the email address, if present and not updated above
        if (email == null) {
            contactOp.addEmail(user.getEmails().get(0) == null ? "" : user.getEmails().get(0).toString());
        }

    }
}
