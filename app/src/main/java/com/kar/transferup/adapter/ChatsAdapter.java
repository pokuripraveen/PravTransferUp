package com.kar.transferup.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kar.transferup.R;
import com.kar.transferup.interfaces.ChatItemSelectListener;
import com.kar.transferup.model.User;
import com.kar.transferup.model.UserChat;
import com.kar.transferup.view.CircularTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.kar.transferup.R.id.user;

/**
 * Created by praveenp on 19-04-2017.
 */

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>{
    private List<UserChat> mChats;
    private ChatItemSelectListener mClickListener;
    private Context mContext;

    public ChatsAdapter(Context context, ChatItemSelectListener listener){
        mClickListener = listener;
        mContext = context;
    }
    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public ChatsAdapter.ChatsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_layout, parent, false);
        return new ChatsAdapter.ChatsViewHolder(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ChatsAdapter.ChatsViewHolder holder, int position) {
        final UserChat contact = mChats.get(position);
        if (contact != null) {
            final String phone = contact.getPhone();
            final String name = contact.getName();
            final String message = contact.getMessage();
            final String uri = contact.getContactUri();
            holder.mDisplayName.setText(name);
            holder.mMessage.setText(message);
            holder.mContactLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mClickListener != null){
                        UserChat user = new UserChat();
                        user.setName(name);
                        user.setPhone(phone);
                        user.setMessage(message);
                        user.setContactUri(uri);
                        mClickListener.onChatItemSelect(user);
                    }
                }
            });
            if(uri != null) {
                Picasso.with(mContext).load(uri).placeholder(R.drawable.default_contact).transform(new CircularTransform()).into(holder.mContactImage);
            } else{
                Picasso.with(mContext).load(R.drawable.default_contact).placeholder(R.drawable.default_contact).transform(new CircularTransform()).into(holder.mContactImage);
            }
        }

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if(mChats != null){
            return mChats.size();
        }
        return 0;
    }

    public void setChatssList(List<UserChat> chats){
        mChats = chats;
        notifyDataSetChanged();
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder{

        public TextView mDisplayName;
        public ImageView mContactImage;
        public TextView mMessage;
        public RelativeLayout mContactLayout;

        public ChatsViewHolder (View itemView) {
            super(itemView);
            mDisplayName = (TextView) itemView.findViewById(R.id.contact_name);
            mContactImage = (ImageView) itemView.findViewById(R.id.contact_image);
            mMessage = (TextView) itemView.findViewById(R.id.message);
            mContactLayout = (RelativeLayout) itemView.findViewById(R.id.contact);
        }
    }
}
