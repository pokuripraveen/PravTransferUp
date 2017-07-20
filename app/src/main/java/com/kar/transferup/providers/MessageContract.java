package com.kar.transferup.providers;

import android.content.ContentResolver;
import android.net.Uri;

import com.kar.transferup.storage.columns.UserChatColumns;

import static com.kar.transferup.interfaces.CommonColumns.KEY_CHAT_ID;
import static com.kar.transferup.interfaces.CommonColumns.KEY_ID;
import static com.kar.transferup.storage.columns.ChatColumns.KEY_MESSAGE;
import static com.kar.transferup.storage.columns.ChatColumns.KEY_MSG_OWNER;
import static com.kar.transferup.storage.columns.ChatColumns.KEY_TIME_STAMP;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_RECEIVER_NAME;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_RECEIVER_PHONE;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_SENDER_NAME;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_SENDER_PHONE;

/**
 * Created by praveenp on 29-12-2016.
 */

public final class MessageContract {
    /**
     * The authority of the messages provider.
     */
    public static final String AUTHORITY = "com.kar.transferup.messages";

    public static final String PREFIX ="content://";

    /**
     * The content URI for the top-level
     * messages authority.
     */
    public static final Uri CONTENT_URI =  Uri.parse(PREFIX + AUTHORITY);

    public static final class Chat {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(MessageContract.CONTENT_URI, "Chat");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.transferup.chat";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_MESSAGE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.transferup.chat";
        /**
         * The default sort order for queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT = KEY_ID + " ASC";

        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {KEY_CHAT_ID, KEY_MESSAGE, KEY_TIME_STAMP, KEY_MSG_OWNER};
    }

    public static final class ChatHeaders {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(MessageContract.CONTENT_URI, "Chats");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.transferup.chats";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_MESSAGE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.transferup.chats";

        /**
         * The default sort order for queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT = KEY_CHAT_ID + " ASC";

        /**
         * A projection of all columns in the items table.
         */
        public static final String[] PROJECTION_ALL = {KEY_CHAT_ID, KEY_SENDER_NAME, KEY_SENDER_PHONE, KEY_RECEIVER_NAME, KEY_RECEIVER_PHONE};
    }

    public static final class UserChat {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(MessageContract.CONTENT_URI, "UserChat");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.transferup.userchat";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_MESSAGE_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.transferup.userchat";
        /**
         * The default sort order for queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT = UserChatColumns.KEY_TIME_STAMP + " DESC";

        public static final String[] PROJECTION_ALL = {UserChatColumns.KEY_MESSAGE, UserChatColumns.KEY_TIME_STAMP, UserChatColumns.KEY_PH_NO, UserChatColumns.KEY_NAME, UserChatColumns.KEY_PHOTO_URI};

    }
}
