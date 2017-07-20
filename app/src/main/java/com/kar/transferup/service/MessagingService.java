package com.kar.transferup.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kar.transferup.R;
import com.kar.transferup.activities.TransferUpActivity;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.Message;
import com.kar.transferup.util.AppUtil;
import com.kar.transferup.util.DBUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by praveenp on 09-12-2016.
 */

public class MessagingService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]


        Map<String,String> dataMap = null;
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            dataMap = remoteMessage.getData();
            Logger.i("Message data payload: " + remoteMessage.getData());
            DBUtil util = DBUtil.getInstance();
            Message message = Message.getMessage(dataMap);
            util.insertMessage(message, AppUtil.Type.OTHER, util.getChatID(dataMap.get("fromPhone")));
            util.updateUserChat(message, AppUtil.Type.OTHER);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Logger.i("Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle(), dataMap);

        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *  @param messageBody FCM message body received.
     * @param title sender name
     * @param dataMap
     */
    private void sendNotification(String messageBody, String title, Map<String, String> dataMap) {
        Intent intent = new Intent(this, TransferUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(dataMap == null){
            dataMap = new HashMap<>();
        }
        dataMap.put("from","foreground");

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.app_icon)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
