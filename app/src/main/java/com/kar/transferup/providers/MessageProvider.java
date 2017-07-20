package com.kar.transferup.providers;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kar.transferup.interfaces.DataSchema;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.storage.MessageStorageHelper;
import com.kar.transferup.storage.columns.ChatColumns;
import com.kar.transferup.storage.columns.ChatHeaderColumns;
import com.kar.transferup.storage.columns.UserChatColumns;

import static com.kar.transferup.interfaces.DataSchema.TBL_CHAT;
import static com.kar.transferup.interfaces.DataSchema.TBL_CHAT_HEADER;
import static com.kar.transferup.interfaces.DataSchema.TBL_USERCHAT;


/**
 * Created by praveenp on 03-01-2017.
 */

public class MessageProvider extends ContentProvider {

    private static final UriMatcher URI_MATCHER;
    private static final int CHAT_LIST = 1;
    private static final int CHAT_ID = 2;
    private static final int CHAT_HEADER_LIST = 3;
    private static final int CHAT_HEADER_ID = 4;
    private static final int USERCHAT_LIST = 5;
    private static final int USERCHAT_ID = 6;

    private static final String TAG = MessageProvider.class.getSimpleName();

    private MessageStorageHelper mDbHelper;

    /*Prepares the content provider*/
    @Override
    public boolean onCreate() {
        mDbHelper = new MessageStorageHelper(getContext());
        return false;
    }

    /*Return records based on selection criteria*/
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Logger.i("Query uri %s projection %s selection %s selectionArgs &s ",uri,projection, selection, selectionArgs);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        boolean useAuthorityUri = true;
        switch (URI_MATCHER.match(uri)) {
            case CHAT_LIST:
                builder.setTables(TBL_CHAT);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = MessageContract.Chat.SORT_ORDER_DEFAULT;
                }
                break;
            case CHAT_ID:
                builder.setTables(TBL_CHAT);
                // limit query to one row at most:
                builder.appendWhere(ChatColumns.KEY_ID + " = "
                    + uri.getLastPathSegment());
                break;
            case CHAT_HEADER_LIST:
                builder.setTables(DataSchema.TBL_CHAT_HEADER);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = MessageContract.ChatHeaders.SORT_ORDER_DEFAULT;
                }
                break;
            case CHAT_HEADER_ID:
                builder.setTables(DataSchema.TBL_CHAT_HEADER);
                // limit query to one row at most:
                builder.appendWhere(ChatHeaderColumns.KEY_CHAT_ID + " = "
                    + uri.getLastPathSegment());
                break;
            case USERCHAT_LIST:
                builder.setTables(TBL_USERCHAT);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = MessageContract.UserChat.SORT_ORDER_DEFAULT;
                }
                break;
            case USERCHAT_ID:
                builder.setTables(TBL_USERCHAT);
                // limit query to one row at most:
                builder.appendWhere(UserChatColumns.KEY_ID + " = "
                    + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // if you like you can log the query
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            logQuery(builder,  projection, selection, sortOrder);
        }
        else {
            logQueryDeprecated(builder, projection, selection, sortOrder);
        }
        Cursor cursor =  mDbHelper.query(builder, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getApplicationContext().getContentResolver(), uri);

        // if we want to be notified of any changes:
       /* if (useAuthorityUri) {
            cursor.setNotificationUri(getContext().getContentResolver(), MessageContract.Chat.CONTENT_URI);
        }
        else {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }*/

        return cursor;
    }

    /*Returns the MIME type for this URI*/
    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case CHAT_LIST:
                return MessageContract.Chat.CONTENT_TYPE;
            case CHAT_ID:
                return MessageContract.Chat.CONTENT_MESSAGE_TYPE;
            case USERCHAT_LIST:
                return MessageContract.UserChat.CONTENT_TYPE;
            case USERCHAT_ID:
                return MessageContract.UserChat.CONTENT_MESSAGE_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.i(TAG,"insert "+uri);
        if (URI_MATCHER.match(uri) != CHAT_LIST && URI_MATCHER.match(uri) != CHAT_HEADER_LIST && URI_MATCHER.match(uri) != USERCHAT_LIST ) {
            throw new IllegalArgumentException(
                "Unsupported URI for insertion: " + uri);
        }
        if (URI_MATCHER.match(uri) == CHAT_LIST) {
            Log.i(TAG, " Current List to Insert is Chat_LIST ");
            long id = mDbHelper.insert(TBL_CHAT, null, contentValues);
            return getUriForId(id, uri);
        } else if(URI_MATCHER.match(uri) == CHAT_HEADER_LIST){
            Log.i(TAG, " Current List to Insert is ChatS_LIST ");
            long id = mDbHelper.insert(TBL_CHAT_HEADER, null, contentValues);
            return getUriForId(id, uri);
        } else if(URI_MATCHER.match(uri) == USERCHAT_LIST){
            long id = mDbHelper.insert(TBL_USERCHAT, null, contentValues);
            return getUriForId(id, uri);
        }
        Log.i(TAG,"URI_MATCHER.match(uri) "+URI_MATCHER.match(uri));
        return null;
    }

    private Uri getUriForId(long id, Uri uri) {
        Log.i(TAG,"getUriForId id "+id +"   Uri: "+uri);
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            // notify all listeners of changes and return itemUri:
            Log.i(TAG,"URI_MATCHER.match(uri) "+URI_MATCHER.match(uri));
            if (URI_MATCHER.match(uri) == CHAT_LIST){
                getContext().getContentResolver().notifyChange(itemUri, null);
            }
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int updateCount = 0;
        switch (URI_MATCHER.match(uri)) {
            case CHAT_HEADER_LIST:
                updateCount = mDbHelper.update(DataSchema.TBL_CHAT_HEADER, contentValues, selection, selectionArgs);
                break;
            case CHAT_HEADER_ID:
                String idStr = uri.getLastPathSegment();
                String where = ChatHeaderColumns.KEY_CHAT_ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount =  mDbHelper.update(DataSchema.TBL_CHAT_HEADER, contentValues, where, selectionArgs);
                break;
            case USERCHAT_LIST:
                updateCount = mDbHelper.update(DataSchema.TBL_USERCHAT, contentValues, selection, selectionArgs);
                Logger.i(" message %s phone %s updateCount %s ",contentValues.getAsString(UserChatColumns.KEY_MESSAGE), contentValues.getAsString(UserChatColumns.KEY_PH_NO), updateCount);
                break;
            case USERCHAT_ID:
                String str = uri.getLastPathSegment();
                String seelction = UserChatColumns.KEY_ID + " = " + str;
                if (!TextUtils.isEmpty(selection)) {
                    seelction += " AND " + selection;
                }
                updateCount =  mDbHelper.update(DataSchema.TBL_USERCHAT, contentValues, seelction, selectionArgs);
                break;
            default:
                // no support for updating photos!
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (updateCount > 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    // prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(MessageContract.AUTHORITY, "Chat", CHAT_LIST);
        URI_MATCHER.addURI(MessageContract.AUTHORITY, "Chat/#", CHAT_ID);
        URI_MATCHER.addURI(MessageContract.AUTHORITY, "Chats", CHAT_HEADER_LIST);
        URI_MATCHER.addURI(MessageContract.AUTHORITY, "Chats/#", CHAT_HEADER_ID);
        URI_MATCHER.addURI(MessageContract.AUTHORITY, "UserChat", USERCHAT_LIST);
        URI_MATCHER.addURI(MessageContract.AUTHORITY, "UserChat/#", USERCHAT_ID);
    }

    @SuppressWarnings("deprecation")
    private void logQueryDeprecated(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) {
        Log.v(TAG, "query: " + builder.buildQuery(projection, selection, null, null, null, sortOrder, null));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void logQuery(SQLiteQueryBuilder builder, String[] projection, String selection, String sortOrder) {
        Log.v(TAG, "query: " + builder.buildQuery(projection, selection, null, null, sortOrder, null));
    }
}
