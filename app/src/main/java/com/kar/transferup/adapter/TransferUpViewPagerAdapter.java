package com.kar.transferup.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kar.transferup.fragments.ChatsFragment;
import com.kar.transferup.fragments.PeoplesFragment;
import com.kar.transferup.fragments.TransactionsFragment;


/**
 * Created by praveenp on 10-02-2017.
 */

public class TransferUpViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final int FRAG_COUNT = 3;
    PeoplesFragment mPeoplesFragment;
    ChatsFragment mChatsFragment;
    TransactionsFragment mTransactionsFragment;

    public TransferUpViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new PeoplesFragment();
                break;
            case 1:
                fragment = new ChatsFragment();
                break;
            case 2:
                fragment = new TransactionsFragment();
                break;
            default:
                fragment = new PeoplesFragment();
                break;
        }
        return fragment;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return FRAG_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "CONTACTS";

            case 1:
                return "CHATS";

            case 2:
                return "TRANSACTIONS";

            default:
                return "CONTACTS";

        }
    }
}
