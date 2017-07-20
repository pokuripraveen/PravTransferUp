package com.kar.transferup.storage;

import android.content.ContentValues;

import com.kar.transferup.model.Message;
import com.kar.transferup.storage.columns.UserChatColumns;
import com.kar.transferup.util.AppUtil;

import static com.kar.transferup.interfaces.CommonColumns.KEY_CHAT_ID;
import static com.kar.transferup.interfaces.DataSchema.TBL_CHAT;
import static com.kar.transferup.interfaces.DataSchema.TBL_CHAT_HEADER;
import static com.kar.transferup.storage.columns.ChatColumns.KEY_MESSAGE;
import static com.kar.transferup.storage.columns.ChatColumns.KEY_MSG_OWNER;
import static com.kar.transferup.storage.columns.ChatColumns.KEY_TIME_STAMP;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_RECEIVER_NAME;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_RECEIVER_PHONE;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_SENDER_NAME;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_SENDER_PHONE;


/**
 * Created by praveenp on 14-12-2016.
 */

public class ChatTable {

    public static ContentValues insertChat(Message message, int chatId, int type) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CHAT_ID, chatId);
        initialValues.put(KEY_TIME_STAMP, AppUtil.getDateFromMillis(Long.valueOf(message.getCreatedAt()), "dd-MM-yyyy HH:mm:ss"));
        initialValues.put(KEY_MESSAGE, message.getMessage());
        initialValues.put(KEY_MSG_OWNER, type);
        return initialValues;
    }

    public static ContentValues insertChatHeader(Message message) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_RECEIVER_PHONE, message.getTo_phone());
        initialValues.put(KEY_RECEIVER_NAME, message.getTo_name());
        initialValues.put(KEY_SENDER_PHONE, message.getFrom_phone());
        initialValues.put(KEY_SENDER_NAME, message.getFrom_name());
        initialValues.put(KEY_SENDER_NAME, message.getFrom_name());
        return initialValues;
    }

    public static ContentValues insertUserChat(Message message, AppUtil.Type type) {
        ContentValues initialValues = new ContentValues();

        initialValues.put(UserChatColumns.KEY_NAME, message.getMessage());
        initialValues.put(UserChatColumns.KEY_MESSAGE, message.getMessage());
        initialValues.put(UserChatColumns.KEY_TIME_STAMP, message.getCreatedAt());

        return initialValues;
    }

    public static String queryMessages(int chatId) {
        return "SELECT  " + KEY_CHAT_ID + "," + KEY_MESSAGE + "," + KEY_TIME_STAMP + "," + KEY_MSG_OWNER + " FROM " + TBL_CHAT + " WHERE " + KEY_CHAT_ID + " = " + chatId;
    }

    public static String queryChatId(String phone) {
        return "SELECT  " + KEY_CHAT_ID + " FROM " + TBL_CHAT_HEADER + " WHERE " + KEY_RECEIVER_PHONE + " = " + phone;
    }

}
