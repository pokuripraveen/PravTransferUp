package com.kar.transferup.storage;

import static com.kar.transferup.interfaces.DataSchema.TBL_USER;
import static com.kar.transferup.storage.columns.UserColumns.KEY_NAME;
import static com.kar.transferup.storage.columns.UserColumns.KEY_PH_NO;

/**
 * Created by praveenp on 14-12-2016.
 */

public class UserTable {

    public static String queryPhoneNo() {
        return "SELECT  " + KEY_PH_NO + " FROM " + TBL_USER;
    }

    public static String queryName() {
        return "SELECT  " + KEY_NAME + " FROM " + TBL_USER;
    }
}
