package com.kar.transferup.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.kar.transferup.logger.Logger;
import com.kar.transferup.util.AppUtil;

import static com.kar.transferup.interfaces.DataSchema.DATABASE_NAME;
import static com.kar.transferup.interfaces.DataSchema.DATABASE_VERSION;
import static com.kar.transferup.interfaces.DataSchema.DDL_CREATE_TBL_CHAT;
import static com.kar.transferup.interfaces.DataSchema.DDL_CREATE_TBL_CHAT_HEADER;
import static com.kar.transferup.interfaces.DataSchema.DDL_CREATE_TBL_USER;
import static com.kar.transferup.interfaces.DataSchema.DDL_CREATE_TBL_USERCHAT;
import static com.kar.transferup.interfaces.DataSchema.TBL_CHAT;
import static com.kar.transferup.interfaces.DataSchema.TBL_CHAT_HEADER;
import static com.kar.transferup.interfaces.DataSchema.TBL_USER;
import static com.kar.transferup.interfaces.DataSchema.TBL_USERCHAT;

/**
 * Created by praveenp on 14-12-2016.
 */

public class MessageStorageHelper extends SQLiteOpenHelper {

    private Context mContext;
    private SQLiteDatabase mDb;

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    public MessageStorageHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public MessageStorageHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MessageStorageHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDb) {
        sqLiteDb.execSQL(DDL_CREATE_TBL_USER);
        sqLiteDb.execSQL(DDL_CREATE_TBL_CHAT_HEADER);
        sqLiteDb.execSQL(DDL_CREATE_TBL_CHAT);
        sqLiteDb.execSQL(DDL_CREATE_TBL_USERCHAT);
        mDb = sqLiteDb == null ? getWritableDatabase() : sqLiteDb;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDb, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            sqLiteDb.execSQL("DROP TABLE IF EXISTS " + TBL_USER);
            sqLiteDb.execSQL("DROP TABLE IF EXISTS " + TBL_CHAT_HEADER);
            sqLiteDb.execSQL("DROP TABLE IF EXISTS " + TBL_CHAT);
            sqLiteDb.execSQL("DROP TABLE IF EXISTS " + TBL_USERCHAT);
            onCreate(sqLiteDb);
        }
    }

    @Override
    public final void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // not supported!
    }

    public String getOwnerNumber() {
        try {
            if(mDb == null){
                mDb = getWritableDatabase();
            }
            Cursor cursor = mDb.rawQuery(UserTable.queryPhoneNo(), null);
            String Phone = cursor.getString(0);
            cursor.close();
            return Phone;
        }catch(Exception e){
            Logger.log(Logger.ERROR, AppUtil.TAG, e.getMessage(), e);
        }
        return null;
    }

    public String getOwnerName() {
        try {
            if(mDb == null){
                mDb = getWritableDatabase();
            }
            Cursor cursor = mDb.rawQuery(UserTable.queryName(), null);
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }catch (Exception e){
            Logger.log(Logger.ERROR, AppUtil.TAG, e.getMessage(), e);
            return "Praveen";
        }
    }

    public long insert(String tableName, Object o, ContentValues contentValues) {
        long result = -1;
        if(mDb == null){
            mDb = getWritableDatabase();
        }
        Logger.i("Insert values for %s are %s ",tableName, contentValues);
        switch (tableName){
            case TBL_CHAT:
                result = mDb.insert(TBL_CHAT, null, contentValues);
                break;
            case TBL_CHAT_HEADER:
                result =  mDb.insert(TBL_CHAT_HEADER, null, contentValues);
                break;
            case TBL_USERCHAT:
                result =  mDb.insert(TBL_USERCHAT, null, contentValues);
                break;
            default:
                Logger.e("ERROR: There is no such table exist: %s", tableName);
                break;
        }
        Logger.i("Insert values for %s are Success and result is  ",tableName, result);
        return result;
    }

    public Cursor query(SQLiteQueryBuilder builder, String[] projection, String selection, String[] selectionArgs, Object o, Object o1, String sortOrder) {
        if(mDb == null){
            mDb = getWritableDatabase();
        }
        return builder.query(mDb, projection, selection, selectionArgs,
            null, null, sortOrder);
    }

    public int update(String tableName, ContentValues contentValues, String selection, String[] selectionArgs) {
        if(mDb == null){
            mDb = getWritableDatabase();
        }
        return mDb.update(tableName, contentValues, selection, selectionArgs);
    }
}
