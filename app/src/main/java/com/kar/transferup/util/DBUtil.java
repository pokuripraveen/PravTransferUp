package com.kar.transferup.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.kar.transferup.base.TransferUpApplication;
import com.kar.transferup.contacts.Contacts;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.Message;
import com.kar.transferup.model.UserChat;
import com.kar.transferup.providers.MessageContract;
import com.kar.transferup.storage.ChatTable;
import com.kar.transferup.storage.MessageStorageHelper;
import com.kar.transferup.storage.columns.UserChatColumns;

import java.util.ArrayList;
import java.util.List;

import static com.kar.transferup.interfaces.CommonColumns.KEY_CHAT_ID;
import static com.kar.transferup.interfaces.CommonColumns.KEY_ID;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_RECEIVER_PHONE;
import static com.kar.transferup.storage.columns.ChatHeaderColumns.KEY_SENDER_NAME;

/**
 * Created by praveenp on 14-12-2016.
 */
public class DBUtil {

    private static DBUtil instance;
    private MessageStorageHelper mDb;
    private ContentResolver mResolver;

    private DBUtil() {
        mDb = new MessageStorageHelper(TransferUpApplication.getContext());
        mResolver = TransferUpApplication.getContext().getContentResolver();
    }

    public static DBUtil getInstance(){
        if(instance == null){
            synchronized (DBUtil.class) {
                if(instance == null){
                    instance = new DBUtil();
                }
            }
        }
        return instance;
    }

    public int insertMessage(Message message, AppUtil.Type type, int chatId){
        Uri uri = null;
        ContentValues initialValues = ChatTable.insertChatHeader(message);

        Logger.i("While Inserting ChatMessage Chat ID for Mobile : %s is : %s ",message.getTo_phone(), chatId);
        if(chatId > 0){
            ContentValues messageValues = ChatTable.insertChat(message, chatId, type.getType());
            uri = mResolver.insert(MessageContract.Chat.CONTENT_URI, messageValues);

        } else {

            if(type == AppUtil.Type.OTHER ) {
                String[] contactInfo = Contacts.getQuery().getDisplayNameAndPhoto(message.getFrom_phone());
                Logger.i("contactInfo[0] %s ",contactInfo[0]);
                if (contactInfo[0] != null) {
                    initialValues.put(KEY_SENDER_NAME, contactInfo[0]);
                }
            }
            Logger.i("insertMessage type: %s KEY_SENDER_NAME : %s ",type.getType(), initialValues.get(KEY_SENDER_NAME));
            Uri insertUri = mResolver.insert(MessageContract.ChatHeaders.CONTENT_URI, initialValues);
            if (insertUri != null ) {
                chatId = getChatID(message.getTo_phone());
                Logger.i("Chat id is not AVaible for this user. new Chat ID :  %s  for User : %s ",chatId , message.getTo_phone());
                ContentValues messageValues = ChatTable.insertChat(message, chatId, type.getType());
                uri = mResolver.insert(MessageContract.Chat.CONTENT_URI, messageValues);

            }
        }
        Logger.i("Insertion Success ChatID %s uri %s",chatId , uri);
        return chatId;
    }

    public int getChatID(String phone){

        int chatId = -1;
        try {
            String[] projection = new String[]{KEY_CHAT_ID};
            String mobile = AppUtil.getSearchableMobile(phone);
            String selection = KEY_RECEIVER_PHONE + " LIKE ?";

            Cursor cursor = mResolver.query(MessageContract.ChatHeaders.CONTENT_URI, projection, selection, new String[]{"%"+mobile+"%"}, KEY_CHAT_ID);
            if(cursor != null && cursor.moveToFirst()) {
                chatId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CHAT_ID));
                cursor.close();
            } else {
                Logger.i("While getting ChatID, cursor count may null or size is zero for the Chatid %s of phone: %s ",ChatTable.queryChatId(phone), phone);
            }
        }catch(Exception e){
            Logger.log(Logger.ERROR, AppUtil.TAG,e.getMessage()+"will return -1 as ChatID ",e );
        }
        Logger.i("ChatdID: %s ",chatId);
        return chatId;
    }

    public String getUserName(String phone){
        String name = "";

        return name;
    }

    public String getOwnerNumber() {
       return mDb.getOwnerNumber();
    }

    public String getOwnerName() {
        return mDb.getOwnerName();
    }

    public Cursor getStoredMessages(String fromNo) {
        try {
            String[] projection = null;
            String selection = KEY_CHAT_ID + "= ? ";
            String chatId = String.valueOf(getChatID(fromNo));

            Cursor cursor = mResolver.query(MessageContract.Chat.CONTENT_URI, projection, selection, new String[]{chatId}, KEY_CHAT_ID);
            Logger.i("StoredMessages Count for Phone %s with Chatid %s is %s ", fromNo, chatId , cursor.getCount());
            return cursor;
        }catch (Exception e){
            Logger.log(Logger.ERROR, AppUtil.TAG,e.getMessage()+"Stored message Cursor is NULL ", e );
            return null;
        }
    }

    public Cursor getStoredChats() {
        try {
            String[] projection = MessageContract.UserChat.PROJECTION_ALL;
            Cursor cursor = mResolver.query(MessageContract.UserChat.CONTENT_URI, projection, null, null, null);
            return cursor;
        }catch (Exception e){
            Logger.log(Logger.ERROR, AppUtil.TAG,e.getMessage()+"getStoredChats Cursor is NULL ", e );
            return null;
        }
    }

    public void updateUserChat(Message message, AppUtil.Type type) {
        ContentValues messageValues = ChatTable.insertUserChat(message, type);
        String phone = message.getFrom_phone();
        String name = message.getFrom_name();
        String uri = null;

        if(type == AppUtil.Type.ME) {
            phone = message.getTo_phone();
            name = message.getTo_name();
            uri =  Contacts.getQuery().getPhotoUri(phone);
            Logger.i("ME Photo uri %s ",uri);
        } else {
            String[] contactInfo = Contacts.getQuery().getDisplayNameAndPhoto(phone);
            if(contactInfo[0] != null) {
                name = contactInfo[0];
            }
            if(contactInfo[1] != null) {
                uri = contactInfo[1];
            }
            Logger.i("NOT ME Photo uri %s ",uri);
            if ("UnKnown".equalsIgnoreCase(name)) {
                name = message.getFrom_name();
            }
        }
        Logger.i("FINAL Photo uri %s ",uri);
        messageValues.put(UserChatColumns.KEY_PHOTO_URI, uri);
        insertOrUpdateMessage(messageValues, name, phone);
    }

    private void insertOrUpdateMessage(ContentValues messageValues, String name, String phone) {
        Logger.i("insertOrUpdateMessage phone %s name %s ",phone, name);
        String mobile = phone;
        if(phone.startsWith("+91") || phone.startsWith("+93")){
            mobile = phone.substring(3);
        } else if(phone.startsWith("+1")){
            mobile = phone.substring(2);
        }
        Logger.i("insertOrUpdateMessage AFter phone %s name %s ",mobile, name);

        String where = UserChatColumns.KEY_PH_NO + " LIKE ?";
        Cursor c = mResolver.query(MessageContract.UserChat.CONTENT_URI,new String[]{KEY_ID, UserChatColumns.KEY_NAME}, where, new String[]{"%"+mobile+"%"}, null);

        if(c != null && c.moveToFirst()){
            messageValues.put(UserChatColumns.KEY_NAME, c.getString(c.getColumnIndex(UserChatColumns.KEY_NAME)));
            messageValues.put(UserChatColumns.KEY_PH_NO, phone);
            Logger.i("insertOrUpdateMessage updating rescord...!!!");
            mResolver.update(MessageContract.UserChat.CONTENT_URI, messageValues, where, new String[]{"%"+mobile+"%"});
        } else {
            messageValues.put(UserChatColumns.KEY_NAME, name);
            messageValues.put(UserChatColumns.KEY_PH_NO, phone);
            Logger.i("insertOrUpdateMessage inserting record...!!!");
            mResolver.insert(MessageContract.UserChat.CONTENT_URI, messageValues);
        }
        if(c != null){
            c.close();
        }
    }

    public List<UserChat> getUserChatsFromCursor(Cursor cursor){
        Logger.i("getUserChatsFromCursor..!!! %s",cursor.getCount());
        List<UserChat> userChat = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Logger.i("Message is %s ",cursor.getString(cursor.getColumnIndex(UserChatColumns.KEY_MESSAGE)));
                UserChat user = new UserChat();
                user.setName(cursor.getString(cursor.getColumnIndex(UserChatColumns.KEY_NAME)));
                user.setPhone(cursor.getString(cursor.getColumnIndex(UserChatColumns.KEY_PH_NO)));
                user.setMessage(cursor.getString(cursor.getColumnIndex(UserChatColumns.KEY_MESSAGE)));
                user.setCreatedAt(cursor.getString(cursor.getColumnIndex(UserChatColumns.KEY_TIME_STAMP)));
                user.setContactUri(cursor.getString(cursor.getColumnIndex(UserChatColumns.KEY_PHOTO_URI)));
                userChat.add(user);
            }
            cursor.close();
        }

        return userChat;
    }
}
