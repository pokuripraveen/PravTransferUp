package com.kar.transferup.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kar.transferup.R;
import com.kar.transferup.adapter.TUChatAdapter;
import com.kar.transferup.contacts.Contact;
import com.kar.transferup.interfaces.RFNetworkInterface;
import com.kar.transferup.loaders.MessageLoader;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.Message;
import com.kar.transferup.model.User;
import com.kar.transferup.net.RetrofitApiClient;
import com.kar.transferup.storage.PreferenceManager;
import com.kar.transferup.util.AppUtil;
import com.kar.transferup.util.DBUtil;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.kar.transferup.contacts.Contacts.getQuery;

/**
 * Created by praveenp on 18-04-2017.
 */

public class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ChatActivity.class.getName();
    private static final int MESSAGES_ID = 0x02;

    private EditText inputMessage;
    private Button btnSend;
    private String mPhoneNo;
    private String mUser;
    private String mContactId;

    private RecyclerView mList;
    private TUChatAdapter mAdapter;
    private DBUtil mDbUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (Button) findViewById(R.id.btn_send);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        List<Contact> contacts = null;
        if (uri != null) {
            contacts = getQuery().getContactByUri(uri);
        }
        if (contacts != null && contacts.size() > 0) {
            mPhoneNo = contacts.get(0).getPhoneNumbers().get(0).getNumber();
            mUser = contacts.get(0).getDisplayName();
            mContactId = String.valueOf(contacts.get(0).getContactId());
        } else {
            mPhoneNo = intent.getStringExtra("phone");
            mUser = intent.getStringExtra("name");
            mContactId = intent.getStringExtra("contactId");
        }

        getSupportActionBar().setTitle(mUser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mList = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new TUChatAdapter(this, null, mPhoneNo);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(layoutManager);
        mList.setItemAnimator(new DefaultItemAnimator());
        mList.setAdapter(mAdapter);

        mDbUtils = DBUtil.getInstance();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        fetchChatThread();
    }


    private void fetchChatThread() {
        getSupportLoaderManager().initLoader(MESSAGES_ID, null, ChatActivity.this);
    }

    private void sendMessage() {
        User user = PreferenceManager.getInstance().getUser();
        if (user == null) {
            return;
        }
        Message message = new Message(mUser, mPhoneNo,
            user.getName(), user.getMobileNumber(),
            inputMessage.getText().toString(),
            String.valueOf(System.currentTimeMillis()));
        inputMessage.setText("");
        int chatId = mDbUtils.getChatID(mPhoneNo);
        Log.i("PRAV", "chatId First" + chatId + "  " + mPhoneNo);
        chatId = mDbUtils.insertMessage(message, AppUtil.Type.ME, chatId);
        DBUtil.getInstance().updateUserChat(message, AppUtil.Type.ME);
        Log.i("PRAV", "chatId " + chatId);
        if (chatId > 0) {
            message.setChatId(String.valueOf(chatId));
            RFNetworkInterface rfApiInterface = RetrofitApiClient.getClient();
            rfApiInterface.sendMessage("send", message).enqueue(new Callback<Void>() {

                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(response.body() != null) {
                        Log.d(TAG, "onResponse: " + response.body().toString());
                    } else{
                        Log.d(TAG, "onResponse: " + response.isSuccessful());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.toString());
                }
            });
        } else {
            Log.e(TAG, " Chat Idf is not Valid " + chatId);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Logger.i("onCreateLoader mPhoneNo %s ",mPhoneNo);
        return new MessageLoader(ChatActivity.this, mPhoneNo);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.i("onLoadFinished Cursor count %s ",data.getCount());
        mAdapter.updateMessages(getQuery().getMessagesFromCursor(data));
        mList.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
