package com.kar.transferup.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kar.transferup.R;
import com.kar.transferup.activities.ChatActivity;
import com.kar.transferup.adapter.ChatsAdapter;
import com.kar.transferup.base.TransferUpApplication;
import com.kar.transferup.interfaces.ChatItemSelectListener;
import com.kar.transferup.loaders.ChatsLoader;
import com.kar.transferup.model.UserChat;
import com.kar.transferup.util.DBUtil;

import java.util.List;

/**
 * Created by praveenp on 10-02-2017.
 */

public class ChatsFragment extends Fragment implements ChatItemSelectListener, LoaderManager.LoaderCallbacks<Cursor>  {
    private static final String TAG = ChatsFragment.class.getSimpleName();
    private static final int CHATS_ID = 0x003;

    private RecyclerView mChatList;
    private ChatsAdapter mAdapter;

    private String mPhoneNo;
    private String mUser;
    private String mContactId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.chats_fragment, null);
        mChatList = (RecyclerView) rootView.findViewById(R.id.chat_list);

        mAdapter = new ChatsAdapter(getActivity(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(TransferUpApplication.getContext());
        mChatList.setLayoutManager(mLayoutManager);
        mChatList.setItemAnimator(new DefaultItemAnimator());
        mChatList.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CHATS_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ChatsLoader(getActivity());
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            populateView(DBUtil.getInstance().getUserChatsFromCursor(data));
        }

    }
    private void populateView(List<UserChat> chatUsers) {
        if (chatUsers != null && chatUsers.size() > 0 ) {
            mAdapter.setChatssList(chatUsers);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onChatItemSelect(UserChat userChat) {
        Intent i = new Intent(getActivity(), ChatActivity.class);
        i.putExtra("name",userChat.getName());
        i.putExtra("phone",userChat.getPhone());
        startActivity(i);
    }
}
