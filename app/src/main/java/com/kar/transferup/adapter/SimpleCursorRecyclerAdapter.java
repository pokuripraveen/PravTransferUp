package com.kar.transferup.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kar.transferup.R;
import com.kar.transferup.contacts.Contact;
import com.kar.transferup.interfaces.ContactSelectListener;
import com.kar.transferup.model.User;
import com.kar.transferup.view.CircularTransform;
import com.squareup.picasso.Picasso;

/**
 * Created by praveenp on 15-12-2016.
 */

public class SimpleCursorRecyclerAdapter extends CursorRecyclerAdapter<SimpleCursorRecyclerAdapter.SimpleViewHolder> {

    private int mLayout;
    private int[] mFrom;
    private String[] mOriginalFrom;
    private Context mContext;
    private ContactSelectListener mClickListener;

    public SimpleCursorRecyclerAdapter(FragmentActivity activity, int layout, Cursor c, String[] from , ContactSelectListener listener) {
        super(c, MODE_CONTACTS);
        mContext = activity;
        mLayout = layout;
        mOriginalFrom = from;
        mClickListener = listener;
        findColumns(c, from);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(mLayout, parent, false);
        return new SimpleViewHolder(v);
    }

    @Override
    public void onBindViewHolder (SimpleViewHolder holder, final Cursor cursor) {
        final int[] from = mFrom;
        final String phone = cursor.getString(cursor.getColumnIndexOrThrow(Contact.Field.PhoneNumber.getColumn()));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(Contact.Field.DisplayName.getColumn()));
        final String contactId = cursor.getString(cursor.getColumnIndexOrThrow(Contact.InternalField.ContactId.getColumn()));
         String iServerId = null;
        try{
            iServerId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.SOURCE_ID));
        } catch (Exception e){
            e.printStackTrace();
        }
        final String serverId = iServerId;
        holder.mDisplayName.setText(name);
        holder.mPhone.setText(phone);
        String uri = cursor.getString(cursor.getColumnIndexOrThrow(Contact.Field.PhotoUri.getColumn()));
        holder.mContactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mClickListener != null){
                    User user = new User();
                    user.setName(name);
                    user.setMobileNumber(phone);
                    user.setContactId(contactId);
                    user.setServerContactId(serverId);
                    user.setEMail(cursor.getString(cursor.getColumnIndexOrThrow(Contact.Field.Email.getColumn())));
                    mClickListener.onContactSelect(user);
                }
            }
        });
        Picasso.with(mContext).load(uri).placeholder(mContext.getResources().getDrawable(R.drawable.contact)).transform(new CircularTransform()).into(holder.mContactImage);
    }

    /**
     * Create a map from an array of strings to an array of column-id integers in cursor c.
     * If c is null, the array will be discarded.
     *
     * @param c the cursor to find the columns from
     * @param from the Strings naming the columns of interest
     */
    private void findColumns(Cursor c, String[] from) {
        if (c != null) {
            int i;
            int count = from.length;
            if (mFrom == null || mFrom.length != count) {
                mFrom = new int[count];
            }
            for (i = 0; i < count; i++) {
                mFrom[i] = c.getColumnIndexOrThrow(from[i]);
            }
        } else {
            mFrom = null;
        }
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        findColumns(c, mOriginalFrom);
        return super.swapCursor(c);
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder
    {
        public TextView mDisplayName;
        public ImageView mContactImage;
        public TextView mPhone;
        public RelativeLayout mContactLayout;

        public SimpleViewHolder (View itemView)
        {
            super(itemView);

            mDisplayName = (TextView) itemView.findViewById(R.id.contact_name);
            mContactImage = (ImageView) itemView.findViewById(R.id.contact_image);
            mPhone = (TextView) itemView.findViewById(R.id.contact_no);
            mContactLayout = (RelativeLayout) itemView.findViewById(R.id.contact);

        }
    }
}

