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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kar.transferup.R;
import com.kar.transferup.activities.ChatActivity;
import com.kar.transferup.adapter.SimpleCursorRecyclerAdapter;
import com.kar.transferup.base.TransferUpApplication;
import com.kar.transferup.contacts.Contacts;
import com.kar.transferup.contacts.RawContact;
import com.kar.transferup.interfaces.ContactSelectListener;
import com.kar.transferup.loaders.ContactsLoader;
import com.kar.transferup.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by praveenp on 15-12-2016.
 */

public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ContactSelectListener {
    private static final int CONTACTS_ID = 0x01;
    private static final String TAG = "ContactsFragment";
    private SimpleCursorRecyclerAdapter mAdapter;
    private static List<RawContact> mContacts;
    private RecyclerView mListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.contacts_fragment, container, false);
        mListView = (RecyclerView) rootView.findViewById(R.id.contact_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(TransferUpApplication.getInstance());
        mListView.setLayoutManager(mLayoutManager);
        mListView.setItemAnimator(new DefaultItemAnimator());
        mContacts = new ArrayList<>();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new SimpleCursorRecyclerAdapter(getActivity(), R.layout.contact_layout, null, Contacts.getQuery().getProjection(), this);
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(CONTACTS_ID, null, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG , "onCreateLoader");
        return new ContactsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("PRAV","onLoadFinished");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onContactSelect(User chatUser) {
        final String phone = chatUser.getMobileNumber();
        final String name = chatUser.getName();
        final String contactId = chatUser.getContactId();//contactCursor.getString(contactCursor.getColumnIndexOrThrow(Contact.InternalField.ContactId.getColumn()));
        final String email = chatUser.getEMail();//contactCursor.getString(contactCursor.getColumnIndexOrThrow(Contact.Field.Email.getColumn()));
        RawContact contact = new RawContact(name,null,null,null, phone,null,null, email, null,null,false, chatUser.getServerContactId() ,Long.valueOf(contactId), System.currentTimeMillis(),true);
        mContacts.add(contact);
        Intent i = new Intent(getActivity(), ChatActivity.class);
        i.putExtra("name",name);
        i.putExtra("phone",phone);
        i.putExtra("contactId",contactId);
        startActivity(i);
    }


    public static List<RawContact> getSampleContacts(){
        return mContacts;
    }
}