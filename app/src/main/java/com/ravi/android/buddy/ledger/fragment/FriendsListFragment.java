package com.ravi.android.buddy.ledger.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ravi.android.buddy.ledger.ApplicationGlobals;
import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.UserProfileActivity;
import com.ravi.android.buddy.ledger.adaptor.FriendsListAdaptor;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.utility.PrefManager;

/**
 * Created by ravi on 10/1/17.
 */

public class FriendsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, FriendsListAdaptor.OnUserItemClick {

    public static final String LOG_TAG = FriendsListFragment.class.getSimpleName();
    private Context context;
    private FriendsListAdaptor mFriendsListAdaptor;
    private RecyclerView mRecyclerView;
    private final int FRIENDS_LIST__LOADER = 0;

    private PrefManager prefManager;

    private Boolean isTwoPane = false;

    public void isTwoPane(Boolean isTwoPane) {
        this.isTwoPane = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_friend_list, container, false);

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_friends_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        prefManager = new PrefManager(context);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mFriendsListAdaptor = new FriendsListAdaptor(getActivity(), prefManager.getUserCurrency(), this);
        mRecyclerView.setAdapter(mFriendsListAdaptor);
        restartFriendListLoader();

        if (savedInstanceState != null) {
            //mFriendsListAdaptor.onRestoreInstanceState(savedInstanceState);
        }
        return rootView;
    }

    public void restartFriendListLoader() {
        getLoaderManager().restartLoader(FRIENDS_LIST__LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        //mFriendsListAdaptor.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    private static final String[] FRIENDS_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER
    };


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                UserEntry.buildUserUri(),
                FRIENDS_COLUMNS,
                UserEntry.COLUMN_USER_ID + " != " + ApplicationGlobals.superUserId,
                null,
                UserEntry.COLUMN_USER_CREATION_DATE + " DESC ");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFriendsListAdaptor.swapCursor(data);
        if (data != null && data.getCount() > 0 && ((HomeActivity) getActivity()).isTwoPane()) {
            data.moveToFirst();
            // ((OnUserItemClick) getActivity()).onUserItemClick(data.getLong(0));
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFriendsListAdaptor.swapCursor(null);
    }

    @Override
    public void onUserItemClick(long user_id) {
        if (((HomeActivity) getActivity()).isTwoPane()) {
            ((OnUserItemClick) getActivity()).onUserItemClick(user_id);
        } else {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("USER_ID", user_id);

            if (Build.VERSION.SDK_INT >= 21) {
                //Animation in future
            }
            ((Activity) context).startActivityForResult(intent,
                    ApplicationGlobals.USER_PROFILE_ACTIVITY_REQUEST_CODE);
        }
    }

    public interface OnUserItemClick {
        void onUserItemClick(long user_id);
    }
}
