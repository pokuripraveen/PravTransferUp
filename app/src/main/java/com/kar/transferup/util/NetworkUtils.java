package com.kar.transferup.util;

import android.accounts.Account;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.kar.transferup.base.ContactManager;
import com.kar.transferup.base.TransferUpApplication;
import com.kar.transferup.contacts.Contact;
import com.kar.transferup.interfaces.NetworkRequestResponseListener;
import com.kar.transferup.interfaces.RFNetworkInterface;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.MatchedContacts;
import com.kar.transferup.model.TransferUpContacts;
import com.kar.transferup.model.User;
import com.kar.transferup.net.RetrofitApiClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.kar.transferup.net.RetrofitApiClient.BASE_PRODUCTION_URL;

/**
 * Created by praveenp on 09-01-2017.
 */

public class NetworkUtils {
    /** The tag used to log to adb console. */
    private static final String TAG = NetworkUtils.class.getSimpleName();
    /** POST parameter name for the user's account name */
    public static final String PARAM_USERNAME = "username";
    /** POST parameter name for the user's password */
    public static final String PARAM_PASSWORD = "password";
    /** POST parameter name for the user's authentication token */
    public static final String PARAM_AUTH_TOKEN = "authtoken";
    /** POST parameter name for the client's last-known sync state */
    public static final String PARAM_SYNC_STATE = "syncstate";
    /** POST parameter name for the sending client-edited contact info */
    public static final String PARAM_CONTACTS_DATA = "contacts";
    /** Timeout (in ms) we specify for each http request */
    public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
    /** Base URL for the v2 Sample Sync Service */
    public static final String BASE_URL = BASE_PRODUCTION_URL;//"https://samplesyncadapter2.appspot.com";
    /** URI for authentication service */
    public static final String AUTH_URI = BASE_URL + "/auth";
    /** URI for sync service */
    public static final String SYNC_CONTACTS_URI = BASE_URL + "/sync";

    private NetworkUtils() {
    }

    public static void matchedContactsSync(MatchedContacts contacts, long syncMarker, final Account account, final long groupId, final ContactLoadListener contactLoadListener){
        RFNetworkInterface rfApiInterface = RetrofitApiClient.getClient();

        rfApiInterface.matchedContactsSync(contacts).enqueue(new Callback<TransferUpContacts>() {

            @Override
            public void onResponse(Call<TransferUpContacts> call, Response<TransferUpContacts> response) {
                TransferUpContacts contacts = response.body();
                Logger.i("matchedContactsSync:onResponse: %s" , response.body());

                if(null != contactLoadListener){
                    contactLoadListener.onContactsLoaded(contacts);
                } else {
                    List<Contact> myContacts = contacts.getInstalledContacts();
                    for(Contact contact : myContacts){
                        ContactManager.addContact(TransferUpApplication.getContext(), contact);
                    }
                }
            }

            @Override
            public void onFailure(Call<TransferUpContacts> call, Throwable t) {
                Logger.log(Logger.ERROR, TAG, t.getMessage(), t);
            }
        });
    }

    public static void updateUser(User user, final NetworkRequestResponseListener listener){
        RFNetworkInterface rfApiInterface = RetrofitApiClient.getClient();
        Logger.i("USER: %s ", user);
        rfApiInterface.updateUser(user).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful() && listener != null) {
                    listener.onSuccess(response);
                } else {
                    Logger.d("updateUser request is not successful for listener : %s " , listener);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Logger.log(Logger.ERROR, TAG, t.getMessage(), t);
                if(listener != null) {
                    listener.onFailure(t);
                }
            }
        });
    }

    /**
     * Download the avatar image from the server.
     *
     * @param avatarUrl the URL pointing to the avatar image
     * @return a byte array with the raw JPEG avatar image
     */
    public static byte[] downloadAvatar(final String avatarUrl) {
        // If there is no avatar, we're done
        if (TextUtils.isEmpty(avatarUrl)) {
            return null;
        }
        try {
            Log.i(TAG, "Downloading avatar: " + avatarUrl);
            // Request the avatar image from the server, and create a bitmap
            // object from the stream we get back.
            URL url = new URL(avatarUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                final Bitmap avatar = BitmapFactory.decodeStream(connection.getInputStream(),
                    null, options);
                // Take the image we received from the server, whatever format it
                // happens to be in, and convert it to a JPEG image. Note: we're
                // not resizing the avatar - we assume that the image we get from
                // the server is a reasonable size...
                Log.i(TAG, "Converting avatar to JPEG");
                ByteArrayOutputStream convertStream = new ByteArrayOutputStream(
                    avatar.getWidth() * avatar.getHeight() * 4);
                avatar.compress(Bitmap.CompressFormat.JPEG, 95, convertStream);
                convertStream.flush();
                convertStream.close();
                // On pre-Honeycomb systems, it's important to call recycle on bitmaps
                avatar.recycle();
                return convertStream.toByteArray();
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException muex) {
            // A bad URL - nothing we can really do about it here...
            Log.e(TAG, "Malformed avatar URL: " + avatarUrl);
        } catch (IOException ioex) {
            // If we're unable to download the avatar, it's a bummer but not the
            // end of the world. We'll try to get it next time we sync.
            Log.e(TAG, "Failed to download user avatar: " + avatarUrl);
        }
        return null;
    }

    public interface ContactLoadListener{
        void onContactsLoaded(TransferUpContacts contacts);
    }
}
