package com.ravi.android.buddy.ledger.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.GroupInfoActivity;
import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.adaptor.FriendsListAdaptor;
import com.ravi.android.buddy.ledger.adaptor.GroupsListAdaptor;
import com.ravi.android.buddy.ledger.data.GroupEntry;
import com.ravi.android.buddy.ledger.data.UserEntry;

/**
 * Created by ravi on 10/1/17.
 */

public class GroupsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, GroupsListAdaptor.OnGroupItemClick {

    public static final String LOG_TAG = FriendsListFragment.class.getSimpleName();
    private Context context;
    private GroupsListAdaptor mGroupsListAdaptor;
    private RecyclerView mRecyclerView;
    private static final int GROUPS_LIST__LOADER = 0;

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
        View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);

        // Get a reference to the RecyclerView, and attach this adapter to it.
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_groups_list);
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);
        mGroupsListAdaptor = new GroupsListAdaptor(getActivity(), R.layout.item_group_list_fragment, this);
        mGroupsListAdaptor.setHasStableIds(true);
        mRecyclerView.setAdapter(mGroupsListAdaptor);

        int columnCount = 2;//getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        //GridLayoutManager gridLayout = new GridLayoutManager(getActivity(), numCols);

        mRecyclerView.setLayoutManager(sglm);
        restartGroupListLoader();
        return rootView;
    }

    public void restartGroupListLoader() {
        getLoaderManager().restartLoader(GROUPS_LIST__LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        getActivity().supportPostponeEnterTransition();
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
        getActivity().supportPostponeEnterTransition();
        super.onSaveInstanceState(outState);
    }

    private static final String[] GROUPS_COLUMNS = {
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_ID,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_NAME,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_TYPE,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_DESCRIPTION
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                GroupEntry.buildGroupUri(),
                GROUPS_COLUMNS,
                null,
                null,
                GroupEntry.COLUMN_GROUP_CREATION_DATE);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGroupsListAdaptor.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGroupsListAdaptor.swapCursor(null);
    }

    @Override
    public void onGroupItemClick(long group_id) {
        if (((HomeActivity) getActivity()).isTwoPane()) {
            ((HomeActivity) getActivity()).onGroupItemClick(group_id);
        } else {
            Intent intent = new Intent(context, GroupInfoActivity.class);
            intent.putExtra("GROUP_ID", group_id);
            context.startActivity(intent);
        }
    }

    public interface OnGroupItemClick {
        void onGroupItemClick(long group_id);
    }
}
