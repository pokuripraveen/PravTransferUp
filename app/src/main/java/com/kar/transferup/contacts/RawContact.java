package com.kar.transferup.contacts;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by praveenp on 09-01-2017.
 */

public class RawContact {

    private static final String TAG = RawContact.class.getSimpleName();

    private final String mUserName;
    private final String mFullName;
    private final String mFirstName;
    private final String mLastName;
    private final String mCellPhone;
    private final String mOfficePhone;
    private final String mHomePhone;
    private final String mEmail;
    private final String mStatus;
    private final String mAvatarUrl;
    private final boolean mDeleted;

    private final boolean mDirty;
    private final String mServerContactId;
    private final long mRawContactId;
    private final long mSyncState;

    public String getServerContactId() {
        return mServerContactId;
    }
    public long getRawContactId() {
        return mRawContactId;
    }
    public String getUserName() {
        return mUserName;
    }
    public String getFirstName() {
        return mFirstName;
    }
    public String getLastName() {
        return mLastName;
    }
    public String getFullName() {
        return mFullName;
    }
    public String getCellPhone() {
        return mCellPhone;
    }
    public String getOfficePhone() {
        return mOfficePhone;
    }
    public String getHomePhone() {
        return mHomePhone;
    }

    public String getEmail() {
        return mEmail;
    }
    public String getStatus() {
        return mStatus;
    }
    public String getAvatarUrl() {
        return mAvatarUrl;
    }
    public boolean isDeleted() {
        return mDeleted;
    }
    public boolean isDirty() {
        return mDirty;
    }
    public long getSyncState() {
        return mSyncState;
    }
    public String getBestName() {
        if (!TextUtils.isEmpty(mFullName)) {
            return mFullName;
        } else if (TextUtils.isEmpty(mFirstName)) {
            return mLastName;
        } else {
            return mFirstName;
        }
    }
    /**
     * Convert the RawContact object into a JSON string.  From the
     * JSONString interface.
     * @return a JSON string representation of the object
     */
    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mUserName)) {
                json.put("userName", mUserName);
            }
            if (!TextUtils.isEmpty(mFirstName)) {
                json.put("firstName", mFirstName);
            }
            if (!TextUtils.isEmpty(mLastName)) {
                json.put("lstname", mLastName);
            }
            if (!TextUtils.isEmpty(mCellPhone)) {
                json.put("mobile", mCellPhone);
            }
            if (!TextUtils.isEmpty(mOfficePhone)) {
                json.put("office", mOfficePhone);
            }
            if (!TextUtils.isEmpty(mHomePhone)) {
                json.put("home", mHomePhone);
            }
            if (!TextUtils.isEmpty(mEmail)) {
                json.put("email", mEmail);
            }

            if (!TextUtils.isEmpty(mAvatarUrl)) {
                json.put("avatar", mAvatarUrl);
            }

            if (!TextUtils.isEmpty(mStatus)) {
                json.put("status", mStatus);
            }

            if (mServerContactId != null) {
                json.put("serverContactId", mServerContactId);
            }
            if (mRawContactId > 0) {
                json.put("rawContactId", mRawContactId);
            }
            if (mDeleted) {
                json.put("isDeleted", mDeleted);
            }

            if (mSyncState > 0) {
                json.put("syncState", mSyncState);
            }
        } catch (final Exception ex) {
            Log.i(TAG, "Error converting RawContact to JSONObject" + ex.toString());
        }
        return json;
    }

    public RawContact(String name, String fullName, String firstName, String lastName, String cellPhone,String officePhone, String homePhone,
                      String email, String status, String avatarUrl, boolean deleted, String serverContactId,
                      long rawContactId, long syncState, boolean dirty) {
        mUserName = name;
        mFullName = fullName;
        mFirstName = firstName;
        mLastName = lastName;
        mCellPhone = cellPhone;
        mOfficePhone = officePhone;
        mHomePhone = homePhone;
        mEmail = email;
        mStatus = status;
        mAvatarUrl = avatarUrl;
        mDeleted = deleted;
        mServerContactId = serverContactId;
        mRawContactId = rawContactId;
        mSyncState = syncState;
        mDirty = dirty;
    }
    /**
     * Creates and returns an instance of the RawContact from the provided JSON data.
     *

     * @return user The new instance of Sample RawContact created from the JSON data.
     */
    public static RawContact valueOf(JSONObject contact) {
        try {
            final String userName = !contact.isNull("userName") ? contact.getString("userName") : null;
            final String serverContactId = contact.getString("serverContactId");
            // If we didn't get either a username or serverId for the contact, then
            // we can't do anything with it locally...
            if ((userName == null) && (serverContactId != null)) {
                throw new JSONException("JSON contact missing required 'u' or 'i' fields");
            }
            final String firstName = !contact.isNull("firstName")  ? contact.getString("firstName") : null;
            final String lastName = !contact.isNull("lastName") ? contact.getString("lastName") : null;
            final int rawContactId = !contact.isNull("rawContactId") ? contact.getInt("rawContactId") : -1;
            final String cellPhone = !contact.isNull("mobile") ? contact.getString("mobile") : null;
            final String officePhone = !contact.isNull("office") ? contact.getString("office") : null;
            final String homePhone = !contact.isNull("home") ? contact.getString("home") : null;
            final String email = !contact.isNull("email") ? contact.getString("email") : null;
            final String status = !contact.isNull("status") ? contact.getString("status") : null;
            final String avatarUrl = !contact.isNull("avatar") ? contact.getString("avatar") : null;
            final boolean deleted = !contact.isNull("isDeleted") ? contact.getBoolean("isDeleted") : false;
            final long syncState = !contact.isNull("syncState") ? contact.getLong("syncState") : 0;
            return new RawContact(userName, null, firstName, lastName, cellPhone,officePhone, homePhone,
                email, status, avatarUrl, deleted,
                serverContactId, rawContactId, syncState, false);
        } catch (final Exception ex) {
            Log.i(TAG, "Error parsing JSON contact object" + ex.toString());
        }
        return null;
    }
    /**
     * Creates and returns RawContact instance from all the supplied parameters.
     */
    public static RawContact create(String fullName, String firstName, String lastName,
                                    String cellPhone, String officePhone, String homePhone,
                                    String email, String status, boolean deleted, long rawContactId,
                                    String serverContactId) {
        return new RawContact(null, fullName, firstName, lastName, cellPhone,officePhone, homePhone, email, status, null, deleted, serverContactId, rawContactId,
            -1, true);
    }
    /**
     * Creates and returns a User instance that represents a deleted user.
     * Since the user is deleted, all we need are the client/server IDs.
     * @return a minimal User object representing the deleted contact.
     */
    public static RawContact createDeletedContact(long rawContactId, String serverContactId)
    {
        return new RawContact(null, null, null, null, null, null, null, null, null, null, false, serverContactId, rawContactId, -1, true);
    }
}
