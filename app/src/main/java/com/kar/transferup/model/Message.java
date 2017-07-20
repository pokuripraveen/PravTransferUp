package com.kar.transferup.model;

import android.os.Bundle;

import com.kar.transferup.logger.Logger;
import com.kar.transferup.util.DBUtil;

import java.util.Map;

/**
 * Created by praveenp on 18-04-2017.
 */

public class Message {
    private static String KEY_DATA_CHATID = "chatId";
    private static String KEY_DATA_FROM_NAME = "fromName";
    private static String KEY_DATA_FROM_PHONE = "fromPhone";
    private static String KEY_DATA_TO_PHONE = "toPhone";
    private static String KEY_DATA_TO_NAME = "toName";
    private static String KEY_DATA_CREATE_AT = "createdAt";
    private static String KEY_DATA_MESSAGE = "message";

    String from_name;
    String to_name;
    String from_phone;
    String to_phone;
    String message;
    String createdAt;
    String chatId;

    public Message(String to_name, String to_phone, String from_name, String from_phone, String message, String createdAt) {
        this.to_name = to_name;
        this.to_phone = to_phone;
        this.from_name = from_name;
        this.from_phone = from_phone;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getTo_name() {
        return to_name;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public String getFrom_phone() {
        return from_phone;
    }

    public void setFrom_phone(String from_phone) {
        this.from_phone = from_phone;
    }

    public String getTo_phone() {
        return to_phone;
    }

    public void setTo_phone(String to_phone) {
        this.to_phone = to_phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public static Message getMessage(Map<String,String> data){
        Message message = new Message(data.get(KEY_DATA_TO_NAME), data.get(KEY_DATA_TO_PHONE),
            data.get(KEY_DATA_FROM_NAME), data.get(KEY_DATA_FROM_PHONE),
            data.get(KEY_DATA_MESSAGE), String.valueOf(System.currentTimeMillis()));
        message.setChatId(String.valueOf(DBUtil.getInstance().getChatID(data.get(KEY_DATA_FROM_PHONE))));
        return message;
    }

    public static Message getMessage(Bundle data){
        Logger.i("KEY_DATA_FROM_PHONE %s KEY_DATA_TO_PHONE %s " , data.getString(KEY_DATA_FROM_PHONE), data.getString(KEY_DATA_TO_PHONE));
        Logger.i("KEY_DATA_TO_NAME %s KEY_DATA_FROM_NAME %s " , data.getString(KEY_DATA_TO_NAME), data.getString(KEY_DATA_FROM_NAME));

        Message message = new Message(data.getString(KEY_DATA_TO_NAME), data.getString(KEY_DATA_TO_PHONE),
            data.getString(KEY_DATA_FROM_NAME), data.getString(KEY_DATA_FROM_PHONE),
            data.getString(KEY_DATA_MESSAGE), String.valueOf(System.currentTimeMillis()));
        message.setChatId(String.valueOf(DBUtil.getInstance().getChatID(data.getString(KEY_DATA_FROM_PHONE))));
        return message;
    }
}
