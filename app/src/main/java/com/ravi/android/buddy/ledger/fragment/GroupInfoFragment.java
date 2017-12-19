package com.ravi.android.buddy.ledger.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.ApplicationGlobals;
import com.ravi.android.buddy.ledger.CustomUiElement.FloatingAction.AddFloatingActionButton;
import com.ravi.android.buddy.ledger.CustomUiElement.FloatingAction.FloatingActionButton;
import com.ravi.android.buddy.ledger.GroupMemberOperationActivity;
import com.ravi.android.buddy.ledger.GroupOperationsActivity;
import com.ravi.android.buddy.ledger.GroupTransactionOperationActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.adaptor.GroupInfoTransactionListAdaptor;
import com.ravi.android.buddy.ledger.adaptor.GroupUserListAdaptor;
import com.ravi.android.buddy.ledger.data.GroupEntry;
import com.ravi.android.buddy.ledger.data.GroupMemberEntry;
import com.ravi.android.buddy.ledger.data.GroupTransactionEntry;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.model.Group;
import com.ravi.android.buddy.ledger.model.GroupMemberMapping;
import com.ravi.android.buddy.ledger.model.GroupMemberOperationType;
import com.ravi.android.buddy.ledger.model.GroupTransaction;
import com.ravi.android.buddy.ledger.model.GroupTransactionCustomType;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ravi on 24/2/17.
 */

public class GroupInfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DETAIL_GroupInfoFragment = "DETAIL_GroupInfoFragment";

    private Context context;

    private PrefManager prefManager;

    private String mCurrency;

    private long group_Id;

    private ImageView groupImage;

    private TextView groupName;

    private TextView groupDescription;

    private TextView creation_Date;

    private TextView memberCount;

    private TextView total_amount;

    private TextView group_category;

    private RecyclerView groupUsersRecyclerView;

    private RecyclerView transactionsRecyclerView;

    private Group group;

    private FloatingActionButton editGroupInfoFab;

    private AddFloatingActionButton addNewMemberFab;

    private FloatingActionButton removeMemberFab;

    private AddFloatingActionButton addNewTransactionFab;

    private Map<Long, Boolean> addedUserIds = new HashMap<>();

    private Map<Long, User> allGroupUsers = new HashMap<>();

    private final int GROUP_MEMBERS_LIST_LOADER = 0;

    private final int GROUP_USERS_DATA_LOADER = 1;

    private final int GROUP_TRANSACTIONS_LIST_LOADER = 2;

    private List<User> presentUserList = new ArrayList<>();

    private GroupUserListAdaptor presentUserListAdaptor;

    private List<GroupTransaction> groupTransactions = new ArrayList<>();

    private GroupInfoTransactionListAdaptor groupInfoTransactionListAdaptor;

    private double groupTotalValue = 0D;

    private List<GroupMemberMapping> groupMemberMappings = new ArrayList<>();

    private Boolean restartGroupTransactionLoader = true;

    private static Boolean groupInfoUpdated = false;

    public GroupInfoFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(context);
        mCurrency = prefManager.getUserCurrency();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            group_Id = arguments.getLong("GROUP_ID");
        }
        View rootView = inflater.inflate(R.layout.fragment_group_info, container, false);
        groupImage = (ImageView) rootView.findViewById(R.id.groupImage);
        //String user_profile_image_transition = context.getResources().getString(R.string.user_profile_image_transition);
        //profileImage.setTransitionName(user_profile_image_transition + id);
        groupName = (TextView) rootView.findViewById(R.id.groupName);
        groupDescription = (TextView) rootView.findViewById(R.id.groupDescription);
        creation_Date = (TextView) rootView.findViewById(R.id.group_creation_date);
        memberCount = (TextView) rootView.findViewById(R.id.number_of_members);
        total_amount = (TextView) rootView.findViewById(R.id.total_transaction_amount);
        group_category = (TextView) rootView.findViewById(R.id.group_category);
        groupUsersRecyclerView = (RecyclerView) rootView.findViewById(R.id.detail_group_members_list);
        transactionsRecyclerView = (RecyclerView) rootView.findViewById(R.id.detail_group_transactions_list);
        initialiseFab(rootView);
        //if (!UIToolsUtil.isScreenLarge(context) || getActivity().getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        loadGroupInfoAndRestartLoader();
        //}
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //if (UIToolsUtil.isScreenLarge(context) && getActivity().getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
        //loadGroupInfoAndRestartLoader();
        //}
    }

    private void initialiseFab(View rootView) {
        editGroupInfoFab = (FloatingActionButton) rootView.findViewById(R.id.editGroupInfoFab);
        addNewMemberFab = (AddFloatingActionButton) rootView.findViewById(R.id.addNewMemberFab);
        removeMemberFab = (FloatingActionButton) rootView.findViewById(R.id.removeMemberFab);
        addNewTransactionFab = (AddFloatingActionButton) rootView.findViewById(R.id.addNewTransactionFab);

        editGroupInfoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupOperationsActivity.class);
                intent.putExtra("GROUP_ID", group_Id);
                intent.putExtra("GROUP_NAME", group.getGroup_name());
                intent.putExtra("GROUP_TYPE", group.getGroup_type());
                intent.putExtra("GROUP_DESCRIPTION", group.getDesciption());
                intent.putExtra("EDIT", true);
                ((Activity) context).startActivityForResult(intent, ApplicationGlobals.GROUP_OPERATION_ACTIVITY_REQUEST_CODE);
                ((Activity) context).overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
            }
        });
        addNewMemberFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupMemberOperationActivity.class);
                intent.putExtra("GROUP_ID", group_Id);
                intent.putExtra("GROUP_NAME", group.getGroup_name());
                intent.putExtra("DELETE", false);
                ((Activity) context).startActivityForResult(intent, ApplicationGlobals.GROUP_MEMBER_OPERATION_ACTIVITY_REQUEST_CODE);
                ((Activity) context).overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
            }
        });
        removeMemberFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupMemberOperationActivity.class);
                intent.putExtra("GROUP_ID", group_Id);
                intent.putExtra("GROUP_NAME", group.getGroup_name());
                intent.putExtra("DELETE", true);
                ((Activity) context).startActivityForResult(intent, ApplicationGlobals.GROUP_MEMBER_OPERATION_ACTIVITY_REQUEST_CODE);
                ((Activity) context).overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
            }
        });
        addNewTransactionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupTransactionOperationActivity.class);
                intent.putExtra("GROUP_ID", group_Id);
                intent.putExtra("GROUP_NAME", group.getGroup_name());
                ((Activity) context).startActivityForResult(intent, ApplicationGlobals.GROUP_TRANSACTION_OPERATION_ACTIVITY_REQUEST_CODE);
                ((Activity) context).overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
            }
        });
    }

    private final String[] GROUP_COLUMNS = {
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_ID,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_TYPE,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_NAME,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_CREATION_DATE,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_DESCRIPTION
    };

    private void loadGroupInfoAndRestartLoader() {
        reloadGroupInfo();
        restartGroupTransactionLoader = true;
        restartGroupMemberListLoader();
    }

    public void reloadGroupInfo() {
        groupTotalValue = 0D;
        Cursor groupDataCursor = context.getContentResolver().query(GroupEntry.buildGroupUriWithId(group_Id), GROUP_COLUMNS, null, new String[]{group_Id + ""}, null);
        groupDataCursor.moveToPosition(0);
        group = new Group();
        group.setId(groupDataCursor.getLong(0));
        group.setGroup_type(groupDataCursor.getString(1));
        group.setGroup_name(groupDataCursor.getString(2));
        group.setCreationDate(groupDataCursor.getString(3));
        group.setDesciption(groupDataCursor.getString(4));
        groupDataCursor.close();
        updateGroupDetailsUI();
    }

    public void restartGroupMemberListLoader() {
        // We hold for transition here just in-case the activity
        // needs to be re-created. In a standard return transition,
        // this doesn't actually make a difference.
        //getActivity().supportPostponeEnterTransition();

        addedUserIds.clear();
        allGroupUsers.clear();
        groupMemberMappings.clear();
        getLoaderManager().restartLoader(GROUP_MEMBERS_LIST_LOADER, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateGroupDetailsUI() {
        //groupImage;
        groupName.setText(group.getGroup_name());
        groupDescription.setText(group.getDesciption());
        creation_Date.setText(DateUtil.getFormattedDateOnly(group.getCreationDate()));
        memberCount.setText(presentUserList.size() + "");
        total_amount.setText(groupTotalValue + " " + mCurrency);
        group_category.setText(Character.toLowerCase(group.getGroup_type().charAt(0)) + group.getGroup_type().substring(1).toLowerCase());
        switch (group.getGroup_type()) {
            case "Food":
                groupImage.setImageResource(R.drawable.food_big);
                break;
            case "House Rent":
                groupImage.setImageResource(R.drawable.rent_big);
                break;
            case "Party":
                groupImage.setImageResource(R.drawable.party_big);
                break;
            case "Trip":
                groupImage.setImageResource(R.drawable.trip_big);
                break;
            case "Movie":
                groupImage.setImageResource(R.drawable.movie_big);
                break;
            case "House Expenses":
                groupImage.setImageResource(R.drawable.house_expense);
                break;
            case "Class Mates":
                groupImage.setImageResource(R.drawable.school_big);
                break;
            case "Colleague":
                groupImage.setImageResource(R.drawable.colleage_big);
                break;
            case "Other":
                groupImage.setImageResource(R.drawable.rent_big);
                break;
        }
    }

    public void updateGroupInfo(String name, String type, String description) {
        group.setGroup_name(name);
        group.setDesciption(description);
        group.setGroup_type(type);
        updateGroupDetailsUI();
    }

    private final String[] GROUP_MEMBERS = {
            GroupMemberEntry.TABLE_NAME + "." + GroupMemberEntry.COLUMN_USER_ID,
            GroupMemberEntry.TABLE_NAME + "." + GroupMemberEntry.COLUMN_GROUP_OPERATION,
            GroupMemberEntry.TABLE_NAME + "." + GroupMemberEntry.COLUMN_ADDED_DATE
    };

    private final String[] USER_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC
    };

    private final String[] GROUP_TRANSACTIONS = {
            GroupTransactionEntry.TABLE_NAME + "." + GroupTransactionEntry.COLUMN_TRANSACTION_ID,
            GroupTransactionEntry.TABLE_NAME + "." + GroupTransactionEntry.COLUMN_USER_ID,
            GroupTransactionEntry.TABLE_NAME + "." + GroupTransactionEntry.COLUMN_TRANSACTION_AMOUNT,
            GroupTransactionEntry.TABLE_NAME + "." + GroupTransactionEntry.COLUMN_TRANSACTION_DATE,
            GroupTransactionEntry.TABLE_NAME + "." + GroupTransactionEntry.COLUMN_TRANSATION_DESCRIPTION
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GROUP_MEMBERS_LIST_LOADER:
                return new CursorLoader(getActivity(),
                        GroupMemberEntry.buildGroupMemberUri(),
                        GROUP_MEMBERS,
                        GroupMemberEntry.COLUMN_GROUP_ID + " = " + group.getId(),
                        null,
                        GroupMemberEntry.COLUMN_ADDED_DATE + " ASC");
            case GROUP_USERS_DATA_LOADER:
                StringBuilder condition = new StringBuilder();
                condition.append("( ");
                int index = 0;
                for (long user_id : allGroupUsers.keySet()
                        ) {
                    if (index > 0) {
                        condition.append(", ");
                    }
                    condition.append(user_id);
                    ++index;
                }
                condition.append(")");
                return new CursorLoader(getActivity(),
                        UserEntry.buildUserUri(),
                        USER_COLUMNS,
                        UserEntry.COLUMN_USER_ID + " in " + condition,
                        null,
                        UserEntry.COLUMN_USER_CREATION_DATE + " ASC");
            case GROUP_TRANSACTIONS_LIST_LOADER:
                return new CursorLoader(getActivity(),
                        GroupTransactionEntry.buildGroupTransactionUri(),
                        GROUP_TRANSACTIONS,
                        GroupTransactionEntry.COLUMN_GROUP_ID + " = " + group.getId(),
                        null,
                        GroupTransactionEntry.COLUMN_TRANSACTION_DATE + " ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        if (data != null) {
            switch (loader.getId()) {
                case GROUP_MEMBERS_LIST_LOADER:
                    Thread thread1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int index = 0; index < data.getCount(); index++) {
                                data.moveToPosition(index);
                                switch (GroupMemberOperationType.valueOf(data.getString(1))) {
                                    case ADD:
                                        addedUserIds.put(data.getLong(0), true);
                                        break;
                                    case REMOVE:
                                        addedUserIds.remove(data.getLong(0));
                                        break;
                                }
                                allGroupUsers.put(data.getLong(0), null);
                                GroupMemberMapping groupMemberMapping = new GroupMemberMapping();
                                groupMemberMapping.setGroup_id(group_Id);
                                groupMemberMapping.setUser_id(data.getLong(0));
                                groupMemberMapping.setGroupOperation(data.getString(1));
                                groupMemberMapping.setOperation_date(data.getString(2));
                                groupMemberMappings.add(groupMemberMapping);
                            }
                            if (allGroupUsers.size() > 0) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        restartGroupUserDataLoader();
                                    }
                                });
                            }
                        }
                    });
                    thread1.start();
                    break;
                case GROUP_USERS_DATA_LOADER:
                    Thread thread2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int index = 0; index < data.getCount(); index++) {
                                data.moveToPosition(index);
                                User u = new User();
                                u.setId(data.getLong(0));
                                u.setName(data.getString(1));
                                u.setGender(data.getString(2));
                                u.setUserImageLocation(data.getString(3));
                                allGroupUsers.put(u.getId(), u);
                                if (addedUserIds.containsKey(u.getId())) {
                                    presentUserList.add(u);
                                }
                            }
                            presentUserListAdaptor = new GroupUserListAdaptor(context, presentUserList, mCurrency, GroupInfoFragment.class, false);
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    memberCount.setText(presentUserList.size() + "");
                                    groupUsersRecyclerView.setAdapter(presentUserListAdaptor);
                                    LinearLayoutManager llm = new LinearLayoutManager(context);
                                    llm.setOrientation(LinearLayoutManager.HORIZONTAL);
                                    groupUsersRecyclerView.setLayoutManager(llm);
                                    restartGroupTransactionLoaderInternal();
                                }
                            });
                        }
                    });
                    thread2.start();
                    break;
                case GROUP_TRANSACTIONS_LIST_LOADER:
                    Thread thread3 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String prevDate = "";
                            double monthTotalAmount = 0D;
                            List<Map<Long, Double>> perMonthUserTotal = new ArrayList<>();
                            Map<Long, Boolean> currentActiveUserTillthispoint = new HashMap<>();
                            Map<Long, Double> currentMonthBalancePerUser = null;
                            int groupMemberMappingsIndex = 0;

                            for (int index = 0; index < data.getCount(); index++) {
                                data.moveToPosition(index);
                                groupTotalValue += data.getDouble(2);


                                //Calculation Part

                                while (groupMemberMappingsIndex < groupMemberMappings.size() && DateUtil.compareDates(groupMemberMappings.get(groupMemberMappingsIndex).getOperation_date(), data.getString(3)) <= 0) {
                                    switch (GroupMemberOperationType.valueOf(groupMemberMappings.get(groupMemberMappingsIndex).getGroupOperation())) {
                                        case ADD:
                                            currentActiveUserTillthispoint.put(groupMemberMappings.get(groupMemberMappingsIndex).getUser_id(), true);
                                            break;
                                        case REMOVE:
                                            currentActiveUserTillthispoint.remove(groupMemberMappings.get(groupMemberMappingsIndex).getUser_id());
                                            break;
                                    }
                                    ++groupMemberMappingsIndex;
                                }
                                if (index == 0 || !DateUtil.isSameMonth(prevDate, data.getString(3))) {
                                    currentMonthBalancePerUser = new HashMap<>();
                                    perMonthUserTotal.add(currentMonthBalancePerUser);
                                }
                                for (Long user_id :
                                        currentActiveUserTillthispoint.keySet()) {
                                    if (!currentMonthBalancePerUser.containsKey(user_id)) {
                                        currentMonthBalancePerUser.put(user_id, 0D);
                                    }
                                }

                                Double balanceAmount = data.getDouble(2) / currentActiveUserTillthispoint.size();
                                for (Long user_id :
                                        currentActiveUserTillthispoint.keySet()) {
                                    if (user_id.equals(data.getLong(1))) {
                                        currentMonthBalancePerUser.put(user_id, currentMonthBalancePerUser.get(user_id) +
                                                (balanceAmount * (currentActiveUserTillthispoint.size() - 1)));
                                    } else {
                                        currentMonthBalancePerUser.put(user_id, currentMonthBalancePerUser.get(user_id) - balanceAmount);
                                    }
                                }

                                /*
                                if (index == 0 || !DateUtil.isSameMonth(data.getString(3), prevDate)) {
                                    monthTotalAmount = 0D;
                                    perMonthUserTotal.add(new HashMap<Long, Double>());
                                    perMonthUserTotal.get(perMonthUserTotal.size() - 1).put(data.getLong(1), data.getDouble(2));
                                } else {
                                    monthTotalAmount += data.getDouble(2);
                                    if (perMonthUserTotal.get(perMonthUserTotal.size() - 1).containsKey(data.getLong(1))) {
                                        perMonthUserTotal.get(perMonthUserTotal.size() - 1).put(data.getLong(1),
                                                perMonthUserTotal.get(perMonthUserTotal.size() - 1).get(data.getLong(1)) + data.getDouble(2));
                                    } else {
                                        perMonthUserTotal.get(perMonthUserTotal.size() - 1).put(data.getLong(1), data.getDouble(2));
                                    }
                                }
                                */

                                GroupTransaction groupTransaction = null;

                                if (index != 0 && !DateUtil.isSameMonth(data.getString(3), prevDate)) {
                                    groupTransaction = new GroupTransaction();
                                    groupTransaction.setTransaction_type(GroupTransactionCustomType.MONTH.name());
                                    groupTransaction.setTransaction_date(prevDate);
                                    groupTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                    groupTransaction.setAmount(monthTotalAmount);
                                    groupTransactions.add(0, groupTransaction);
                                    monthTotalAmount = 0D;
                                }

                                if (index != 0 && !DateUtil.isSameDay(data.getString(3), prevDate)) {
                                    groupTransaction = new GroupTransaction();
                                    groupTransaction.setTransaction_type(GroupTransactionCustomType.DATE.name());
                                    groupTransaction.setTransaction_date(prevDate);
                                    groupTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                    groupTransactions.add(0, groupTransaction);
                                }

                                groupTransaction = new GroupTransaction();
                                groupTransaction.setTransaction_type(GroupTransactionCustomType.TRANSACTION.name());
                                groupTransaction.setId(data.getLong(0));
                                groupTransaction.setUserId(data.getLong(1));
                                groupTransaction.setAmount(data.getDouble(2));
                                groupTransaction.setTransaction_date(data.getString(3));
                                groupTransaction.setDescription(data.getString(4));
                                groupTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                groupTransactions.add(0, groupTransaction);

                                if (index == (data.getCount() - 1)) {
                                    groupTransaction = new GroupTransaction();
                                    groupTransaction.setTransaction_type(GroupTransactionCustomType.DATE.name());
                                    groupTransaction.setTransaction_date(data.getString(3));
                                    groupTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                    groupTransactions.add(0, groupTransaction);
                                }

                                if (index == (data.getCount() - 1)) {
                                    groupTransaction = new GroupTransaction();
                                    groupTransaction.setTransaction_type(GroupTransactionCustomType.MONTH.name());
                                    groupTransaction.setTransaction_date(data.getString(3));
                                    groupTransaction.setMonthTransactionIndex(perMonthUserTotal.size() - 1);
                                    groupTransaction.setAmount(monthTotalAmount);
                                    groupTransactions.add(0, groupTransaction);
                                    monthTotalAmount = 0D;
                                }
                                prevDate = data.getString(3);
                            }

                            groupInfoTransactionListAdaptor = new GroupInfoTransactionListAdaptor(context, group, mCurrency, groupTransactions, allGroupUsers, perMonthUserTotal);
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    total_amount.setText(groupTotalValue + " " + mCurrency);
                                    transactionsRecyclerView.setAdapter(groupInfoTransactionListAdaptor);
                                    transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                                }
                            });
                        }
                    });
                    thread3.start();
                    break;
            }
        }
    }


    private void restartGroupTransactionLoaderInternal() {
        if (restartGroupTransactionLoader) {
            groupTransactions.clear();
            getLoaderManager().restartLoader(GROUP_TRANSACTIONS_LIST_LOADER, null, this);
            restartGroupTransactionLoader = false;
        }
    }

    public void restartGroupTransactionLoader() {
        restartGroupTransactionLoader = true;
        restartGroupTransactionLoaderInternal();
    }

    private void restartGroupUserDataLoader() {
        presentUserList.clear();
        getLoaderManager().restartLoader(GROUP_USERS_DATA_LOADER, null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public static Boolean getGroupInfoUpdated() {
        return groupInfoUpdated;
    }

    public static void setGroupInfoUpdated(Boolean groupInfoUpdated) {
        GroupInfoFragment.groupInfoUpdated = groupInfoUpdated;
    }
}
