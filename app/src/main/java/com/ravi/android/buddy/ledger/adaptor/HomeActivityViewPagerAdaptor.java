package com.ravi.android.buddy.ledger.adaptor;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.fragment.FriendsListFragment;
import com.ravi.android.buddy.ledger.fragment.GroupsListFragment;
import com.ravi.android.buddy.ledger.fragment.SummaryFragment;

import java.io.Serializable;

/**
 * Created by ravi on 10/1/17.
 */

public class HomeActivityViewPagerAdaptor extends FragmentPagerAdapter implements Serializable {

    private Context context;

    private FriendsListFragment friendsListFragment;

    private GroupsListFragment groupsListFragment;

    private SummaryFragment summaryFragment;

    public HomeActivityViewPagerAdaptor(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (friendsListFragment == null) {
                    friendsListFragment = new FriendsListFragment();
                }
                return friendsListFragment;
            case 1:
                if (groupsListFragment == null) {
                    groupsListFragment = new GroupsListFragment();
                }
                return groupsListFragment;
            case 2:
                if (summaryFragment == null) {
                    summaryFragment = new SummaryFragment();
                }
                return summaryFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.tab_friends_list);
            case 1:
                return context.getString(R.string.tab_groups_list);
            case 2:
                return context.getString(R.string.tab_summary);
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        switch (position) {
            case 0:
                friendsListFragment = (FriendsListFragment) fragment;
                break;
            case 1:
                groupsListFragment = (GroupsListFragment) fragment;
                break;
            case 2:
                summaryFragment = (SummaryFragment) fragment;
                break;
            default:
                return null;
        }
        return fragment;
    }
}
