package com.kar.transferup.interfaces;

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
import static com.kar.transferup.storage.columns.UserColumns.KEY_IS_TUP_USER;
import static com.kar.transferup.storage.columns.UserColumns.KEY_NAME;
import static com.kar.transferup.storage.columns.UserColumns.KEY_PH_NO;
import static com.kar.transferup.storage.columns.UserColumns.KEY_RAW_CONTACT_ID;

/**
 * Created by praveenp on 18-04-2017.
 */

public interface DataSchema {

    String DATABASE_NAME = "chat.db";
    int DATABASE_VERSION = 1;

    /*tables*/
    String TBL_USER = "User";
    String TBL_CHAT_HEADER = "ChatHeader";
    String TBL_CHAT = "Chat";
    String TBL_USERCHAT = "UserChat";

    String DDL_CREATE_TBL_USER = "CREATE TABLE IF NOT EXISTS " + TBL_USER + "("
        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_NAME + " TEXT, " +
        KEY_PH_NO + " TEXT, " +
        KEY_IS_TUP_USER + " TEXT, " +
        KEY_RAW_CONTACT_ID + " TEXT)";

    String DDL_CREATE_TBL_CHAT_HEADER = "CREATE TABLE IF NOT EXISTS " + TBL_CHAT_HEADER + "("
        + KEY_SENDER_NAME + " TEXT, " +
        KEY_SENDER_PHONE + " TEXT, " +
        KEY_RECEIVER_NAME + " TEXT, " +
        KEY_RECEIVER_PHONE + " TEXT, " +
        KEY_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT)";

    String DDL_CREATE_TBL_CHAT = "CREATE TABLE IF NOT EXISTS " + TBL_CHAT + "("
        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + KEY_CHAT_ID + " INTEGER, " +
        KEY_MESSAGE + " TEXT, " +
        KEY_TIME_STAMP + " TEXT, " +
        KEY_MSG_OWNER + " TEXT, " + " FOREIGN KEY (" + KEY_CHAT_ID + ") REFERENCES " +
        TBL_CHAT_HEADER + " ( " + KEY_CHAT_ID + ") )";

    String DDL_CREATE_TBL_USERCHAT = "CREATE TABLE IF NOT EXISTS " + TBL_USERCHAT + "("
        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        UserChatColumns.KEY_NAME + " TEXT, " +
        UserChatColumns.KEY_PH_NO + " TEXT, " +
        UserChatColumns.KEY_MESSAGE + " TEXT, " +
        UserChatColumns.KEY_PHOTO_URI + " TEXT, " +
        UserChatColumns.KEY_TIME_STAMP + " TEXT)";
}
