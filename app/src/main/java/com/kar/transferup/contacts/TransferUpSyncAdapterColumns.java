package com.kar.transferup.contacts;
import android.provider.ContactsContract.Data;

/**
 * Created by praveenp on 09-01-2017.
 */
public class TransferUpSyncAdapterColumns {
    private TransferUpSyncAdapterColumns() {
    }
    /**
     * MIME-type used when storing a profile {@link Data} entry.
     */
    public static final String MIME_PROFILE = "vnd.android.cursor.item/vnd.com.transferup.profile";
    public static final String DATA_PID = Data.DATA1;
    public static final String DATA_SYNC = Data.SYNC1;
    public static final String DATA_SUMMARY = Data.DATA2;
    public static final String DATA_DETAIL = Data.DATA3;
}
