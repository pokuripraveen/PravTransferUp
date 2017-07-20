package com.kar.transferup.fragments;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;

import com.kar.transferup.R;
import com.kar.transferup.activities.ChatActivity;
import com.kar.transferup.adapter.PeoplesAdapter;
import com.kar.transferup.base.TransferUpApplication;
import com.kar.transferup.contacts.Contact;
import com.kar.transferup.contacts.Contacts;
import com.kar.transferup.interfaces.ContactSelectListener;
import com.kar.transferup.loaders.ContactsLoader;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.TransferUpContacts;
import com.kar.transferup.model.User;
import com.kar.transferup.util.NetworkUtils;

import java.util.List;

/**
 * Created by praveenp on 10-02-2017.
 */

public class PeoplesFragment extends Fragment implements ContactSelectListener, LoaderManager.LoaderCallbacks<Cursor> , NetworkUtils.ContactLoadListener {
    private static final int CONTACTS_ID = 0x002;
    private RecyclerView mPeopleList;
    private PeoplesAdapter mAdapter;
    private ProgressBar mProgress;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View rootView =inflater.inflate(R.layout.contacts_fragment, null);
        mPeopleList = (RecyclerView) rootView.findViewById(R.id.contact_list);
        mProgress = (ProgressBar) rootView.findViewById(R.id.contacts_progress);

        mRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
        mAdapter = new PeoplesAdapter(getActivity(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(TransferUpApplication.getContext());
        mPeopleList.setLayoutManager(mLayoutManager);
        mPeopleList.setItemAnimator(new DefaultItemAnimator());
        mPeopleList.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(CONTACTS_ID, null, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onContactSelect(User chatUser) {
        final String phone = chatUser.getMobileNumber();
        final String name = chatUser.getName();
        final String contactId = chatUser.getContactId();//contactCursor.getString(contactCursor.getColumnIndexOrThrow(Contact.InternalField.ContactId.getColumn()));
        final String email = chatUser.getEMail();//contactCursor.getString(contactCursor.getColumnIndexOrThrow(Contact.Field.Email.getColumn()));
        Intent i = new Intent(getActivity(), ChatActivity.class);
        i.putExtra("name",name);
        i.putExtra("phone",phone);
        i.putExtra("contactId",contactId);
        i.putExtra("email",email);
        startActivity(i);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ContactsLoader(getActivity());
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.i("onLoadFinished : ");
        populateView(Contacts.getQuery().getContactsFromCursor(data));
    }

    private void populateView(List<Contact> contactsFromCursor) {
        if (contactsFromCursor != null && contactsFromCursor.size() > 0 ) {
            mProgress.setVisibility(View.GONE);
            mAdapter.setPeoplesList(contactsFromCursor);
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onContactsLoaded(final TransferUpContacts contacts) {
        if(getActivity() != null ) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    populateView(contacts);
                }
            });
        }
    }

    private void populateView(TransferUpContacts contacts) {
        if (contacts != null) {
            mAdapter.setPeoplesList(contacts.getInstalledContacts());
        }
    }


    void refreshItems() {
        Logger.i("refreshItems forceLoad : ");
        getLoaderManager().getLoader(CONTACTS_ID).forceLoad();
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        mRefreshLayout.setRefreshing(false);
    };
}
