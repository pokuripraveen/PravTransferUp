package com.kar.transferup.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.kar.transferup.R;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.Chat;
import com.kar.transferup.util.AppUtil;

import java.util.List;

import static com.kar.transferup.R.id.submit_area;
import static com.kar.transferup.R.id.timestamp;

/**
 * Created by praveenp on 06-03-2017.
 */

public class TUChatAdapter extends RecyclerView.Adapter<TUChatAdapter.ChatViewHolder> {
    private int SELF = 0x100;
    private int GUEST = 0x200;
    private List<Chat> mMessages;
    private Context mContext;

    public TUChatAdapter(Context context, List<Chat> messages, String mPhoneNo) {
        if(messages != null){
            mMessages = messages;
        }
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
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_self, parent, false);
        if (viewType == SELF) {
            // self message
            itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item_self, parent, false);
        } else if(viewType == GUEST){
            // others message
            itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item_other, parent, false);
        }

        return new TUChatAdapter.ChatViewHolder(itemView);
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
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        final Chat chat = mMessages.get(position);
        if(chat != null){
            holder.mMessage.setText(chat.getMessage());
            holder.mTimeStamp.setText(chat.getCreatedAt());
        }
    }

    public void updateMessages(List<Chat> messages){
        mMessages = messages;
        notifyDataSetChanged();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if(mMessages == null) {
            return 0;
        }
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        if(mMessages != null && mMessages.size() > position){
            int type = Integer.valueOf(mMessages.get(position).getOwner());
            if(AppUtil.Type.ME.getType() == type){
                return SELF;
            } else {
                return GUEST;
            }
        }
        return -1;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView mMessage;
        public TextView mTimeStamp;


        public ChatViewHolder (View itemView)
        {
            super(itemView);
            mMessage = (TextView) itemView.findViewById(R.id.message);
            mTimeStamp = (TextView) itemView.findViewById(R.id.timestamp);
        }
    }
}
