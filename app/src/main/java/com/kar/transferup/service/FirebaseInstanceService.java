package com.kar.transferup.service;


import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.kar.transferup.interfaces.NetworkRequestResponseListener;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.User;
import com.kar.transferup.storage.PreferenceManager;
import com.kar.transferup.util.NetworkUtils;

import retrofit2.Response;


/**
 * Created by praveenp on 09-12-2016.
 */

public class FirebaseInstanceService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseInstanceService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Logger.i(TAG+" onTokenRefresh TOKEN %s ",refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        PreferenceManager.getInstance().put(PreferenceManager.KEY_USER_ID, refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        User user = PreferenceManager.getInstance().getUser();
        if(user != null && user.getEMail() != null){
            NetworkUtils.updateUser(user , new NetworkRequestResponseListener(){

                @Override
                public void onSuccess(Response response) {
                    Logger.i("updateUser Success : "+response.body());
                }

                @Override
                public void onFailure(Throwable error) {
                    Logger.log(Logger.ERROR, "SAVE_USER", error.getMessage(), error);
                }

            });
        }
    }
}
