package com.ravi.android.buddy.ledger.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.UserOperationsActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.TransactionOperationsActivity;
import com.ravi.android.buddy.ledger.adaptor.GroupsListAdaptor;
import com.ravi.android.buddy.ledger.adaptor.UserProfileTransactionListAdaptor;
import com.ravi.android.buddy.ledger.data.GroupEntry;
import com.ravi.android.buddy.ledger.data.GroupMemberEntry;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.GroupMemberOperationType;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.model.UserTransaction;
import com.ravi.android.buddy.ledger.model.UserTransactionCustomType;
import com.ravi.android.buddy.ledger.model.UserTransactionType;
import com.ravi.android.buddy.ledger.utility.DateUtil;
import com.ravi.android.buddy.ledger.utility.PrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by ravi on 20/2/17.
 */

public class UserProfileFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        UserProfileTransactionListAdaptor.OnItemDeleteCallBack {

    public static final String DETAIL_UserProfileFragment = "DETAIL_UserProfileFragment";
    private static final String LOG_TAG = UserProfileFragment.class.getSimpleName();

    private Context context;

    private ImageView profileImage;

    private TextView userName;

    private TextView total_lent_amount;

    private TextView total_borrow_amount;

    private TextView balanceTextView;

    private RecyclerView groupsRecyclerView;

    private RecyclerView transactionsRecyclerView;

    private PrefManager prefManager;

    private String mCurrency;

    private final int USER_TRANSACTION_LIST_LOADER = 0;

    private final int USER_GROUPS_MEMBERS_LIST_LOADER = 1;

    private final int GROUPS_LIST_LOADER = 2;

    private double totalLentAmount = 0D;

    private double totalBorrowAmount = 0D;

    private User user = null;

    private long user_Id = -1;

    private UserProfileTransactionListAdaptor transactionListAdaptor;

    public UserProfileFragment() {
        setHasOptionsMenu(true);
    }

    private Map<Long, Boolean> groupIds = new HashMap<>();

    private GroupsListAdaptor mGroupsListAdaptor;

    private FloatingActionButton editUserInfoFab;

    private AddFloatingActionButton addNewTransactionFab;

    private List<UserTransaction> userTransactions = new ArrayList<>();

    private static boolean userInfoUpdated = false;

    private static boolean transactionUpdated = false;

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
            user_Id = arguments.getLong("USER_ID");
        }
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        profileImage = (ImageView) rootView.findViewById(R.id.profileImage);

        userName = (TextView) rootView.findViewById(R.id.userName);
        total_lent_amount = (TextView) rootView.findViewById(R.id.total_lent_amount);
        total_borrow_amount = (TextView) rootView.findViewById(R.id.total_borrow_amount);
        balanceTextView = (TextView) rootView.findViewById(R.id.balanceTextView);
        groupsRecyclerView = (RecyclerView) rootView.findViewById(R.id.detail_groups_list);
        transactionsRecyclerView = (RecyclerView) rootView.findViewById(R.id.detail_transactions_list);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        intialiseFabs(rootView);
        //if (!UIToolsUtil.isScreenLarge(context) || getActivity().getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        //if(savedInstanceState == null) {
        restartAllLoaders();
        //}
        return rootView;
    }

    private void intialiseFabs(View rootView) {
        editUserInfoFab = (FloatingActionButton) rootView.findViewById(R.id.editUserInfoFab);
        addNewTransactionFab = (AddFloatingActionButton) rootView.findViewById(R.id.add_transaction);
        editUserInfoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserOperationsActivity.class);
                intent.putExtra("USER_ID", user.getId());
                intent.putExtra("GENDER", user.getGender());
                intent.putExtra("IMG_LOC", user.getUserImageLocation());
                intent.putExtra("PHONE_NUM", user.getNumber());
                intent.putExtra("EMAIL", user.getEmail());
                intent.putExtra("NAME", user.getName());
                intent.putExtra("EDIT", true);
                ((Activity) context).startActivityForResult(intent, ApplicationGlobals.USER_OPERATION_ACTIVITY_REQUEST_CODE);
                ((Activity) context).overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
            }
        });
        addNewTransactionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAddNewTransactionActivity();
            }
        });
    }

    private void launchAddNewTransactionActivity() {
        Intent intent = new Intent(context, TransactionOperationsActivity.class);
        intent.putExtra("USER_ID", user.getId());
        ((Activity) context).startActivityForResult(intent, ApplicationGlobals.USER_TRANSACTION_OPERATION__ACTIVITY_REQUEST_CODE);
        ((Activity) context).overridePendingTransition(R.anim.add_activity_enter_animation, R.anim.no_animation);
    }

    private final String[] USER_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_ID,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_CREATION_DATE,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_EMAIL,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NAME,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_NUMBER,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_IMAGE_LOC,
            UserEntry.TABLE_NAME + "." + UserEntry.COLUMN_USER_GENDER
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void restartAllLoaders() {
        reloadUserInfo();
        restartTransactionLoader();
        groupIds.clear();
        getLoaderManager().initLoader(USER_GROUPS_MEMBERS_LIST_LOADER, null, this);
    }

    public void reloadUserInfo() {
        Cursor userDataCursor = context.getContentResolver().query(UserEntry.buildUserUri(), USER_COLUMNS, UserEntry.COLUMN_USER_ID + " = " + user_Id, null, null);
        if (userDataCursor != null && userDataCursor.moveToFirst()) {
            if (user == null)
                user = new User();
            user.setId(userDataCursor.getLong(0));
            user.setCreationDate(userDataCursor.getString(1));
            user.setEmail(userDataCursor.getString(2));
            user.setName(userDataCursor.getString(3));
            user.setNumber(userDataCursor.getString(4));
            user.setUserImageLocation(userDataCursor.getString(5));
            user.setGender(userDataCursor.getString(6));
            updateUIUserDetails();
        }
        if (userDataCursor != null) {
            userDataCursor.close();
        }
    }

    public void updateUserData(String name, String imgLocation, String gender, String email, String number) {
        user.setName(name);
        user.setUserImageLocation(imgLocation);
        user.setGender(gender);
        user.setEmail(email);
        user.setNumber(number);
        updateUIUserDetails();
    }

    private void updateUIUserDetails() {
        userName.setText(user.getName());
        if (user.getUserImageLocation() != null && !user.getUserImageLocation().equalsIgnoreCase("")) {
            profileImage.setImageURI(Uri.parse(user.getUserImageLocation()));
        } else {
            switch (user.getGender()) {
                case "MALE":
                    profileImage.setImageResource(R.drawable.male_profile_big);
                    break;
                case "FEMALE":
                    profileImage.setImageResource(R.drawable.female_profile_big);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //restartAllLoaders();
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

    private final String[] USER_TRANSACTIONS_COLUMNS = {
            UserTransactionEntry.COLUMN_TRANSACTION_ID,
            UserTransactionEntry.COLUMN_TRANSACTION_TYPE,
            UserTransactionEntry.COLUMN_TRANSACTION_DATE,
            UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT,
            UserTransactionEntry.COLUMN_TRANSATION_DESCRIPTION
    };

    private final String[] GROUP_MEMBERS = {
            GroupMemberEntry.TABLE_NAME + "." + GroupMemberEntry.COLUMN_GROUP_ID,
            GroupMemberEntry.TABLE_NAME + "." + GroupMemberEntry.COLUMN_GROUP_OPERATION
    };

    private final String[] GROUPS_COLUMNS = {
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_ID,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_NAME,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_TYPE,
            GroupEntry.TABLE_NAME + "." + GroupEntry.COLUMN_GROUP_DESCRIPTION
    };

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case USER_TRANSACTION_LIST_LOADER:
                return new CursorLoader(getActivity(),
                        UserTransactionEntry.buildTransactionUri(),
                        USER_TRANSACTIONS_COLUMNS,
                        UserTransactionEntry.COLUMN_USER_ID + " = " + user_Id,
                        null,
                        UserTransactionEntry.COLUMN_TRANSACTION_DATE + " DESC");
            case USER_GROUPS_MEMBERS_LIST_LOADER:
                return new CursorLoader(getActivity(),
                        GroupMemberEntry.buildGroupMemberUri(),
                        GROUP_MEMBERS,
                        GroupMemberEntry.COLUMN_USER_ID + " = " + user_Id,
                        null,
                        GroupMemberEntry.COLUMN_ADDED_DATE + " ASC");
            case GROUPS_LIST_LOADER:
                StringBuilder condition = new StringBuilder();
                condition.append("( ");
                int index = 0;
                for (long id : groupIds.keySet()
                        ) {
                    if (index > 0) {
                        condition.append(", ");
                    }
                    condition.append(id);
                    ++index;
                }
                condition.append(")");
                return new CursorLoader(getActivity(),
                        GroupEntry.buildGroupUri(),
                        GROUPS_COLUMNS,
                        GroupEntry.COLUMN_GROUP_ID + " in " + condition,
                        null,
                        GroupEntry.COLUMN_GROUP_CREATION_DATE + " ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            switch (loader.getId()) {
                case USER_TRANSACTION_LIST_LOADER:
                    String prevTime = null;
                    for (int index = 0; index < data.getCount(); index++) {
                        data.moveToPosition(index);
                        switch (UserTransactionType.valueOf(data.getString(1))) {
                            case LENT:
                                totalLentAmount += data.getDouble(3);
                                break;
                            case BORROW:
                                totalBorrowAmount += data.getDouble(3);
                                break;
                        }
                        if (index == 0 || !DateUtil.isSameDay(data.getString(2), prevTime)) {
                            UserTransaction userTransaction = new UserTransaction();
                            userTransaction.setTransaction_date(data.getString(2));
                            userTransaction.setTransaction_type(UserTransactionCustomType.DATE.name());
                            userTransactions.add(userTransaction);
                        }
                        UserTransaction userTransaction = new UserTransaction();
                        userTransaction.setId(data.getInt(0));
                        userTransaction.setTransaction_type(data.getString(1));
                        userTransaction.setTransaction_date(data.getString(2));
                        userTransaction.setAmount(data.getDouble(3));
                        userTransaction.setDescription(data.getString(4));
                        userTransaction.setUserId(user.getId());
                        userTransactions.add(userTransaction);
                        prevTime = userTransaction.getTransaction_date();
                    }
                    updateAmountDisplayUI();
                    transactionListAdaptor = new UserProfileTransactionListAdaptor(context, mCurrency, userTransactions, this);
                    transactionsRecyclerView.setAdapter(transactionListAdaptor);
                    break;
                case USER_GROUPS_MEMBERS_LIST_LOADER:
                    for (int index = 0; index < data.getCount(); index++) {
                        data.moveToPosition(index);
                        switch (GroupMemberOperationType.valueOf(data.getString(1))) {
                            case ADD:
                                groupIds.put(data.getLong(0), true);
                                break;
                            case REMOVE:
                                groupIds.remove(data.getLong(0));
                                break;
                        }
                    }
                    if (groupIds.size() > 0) {
                        getLoaderManager().initLoader(GROUPS_LIST_LOADER, null, this);
                    }
                    break;
                case GROUPS_LIST_LOADER:
                    mGroupsListAdaptor = new GroupsListAdaptor(getActivity(), R.layout.item_group_list_horizontal, null);
                    mGroupsListAdaptor.swapCursor(data);
                    groupsRecyclerView.setAdapter(mGroupsListAdaptor);
                    LinearLayoutManager llm = new LinearLayoutManager(context);
                    llm.setOrientation(LinearLayoutManager.HORIZONTAL);
                    groupsRecyclerView.setLayoutManager(llm);
                    break;
            }
        }
    }

    private void updateAmountDisplayUI() {
        total_lent_amount.setText(mCurrency + " " + totalLentAmount);
        total_borrow_amount.setText(mCurrency + " " + totalBorrowAmount);
        if (totalLentAmount > totalBorrowAmount) {
            balanceTextView.setText(user.getName() + " " +
                    context.getResources().getString(R.string.fragment_user_profile_owes_you) + mCurrency + " " + (totalLentAmount - totalBorrowAmount));
        } else if (totalBorrowAmount > totalLentAmount) {
            balanceTextView.setText(context.getResources().getString(R.string.fragment_user_profile_you_owes) + " "
                    + user.getName() + " " + mCurrency + " " + (totalBorrowAmount - totalLentAmount));
        } else {
            balanceTextView.setText(context.getResources().getString(R.string.fragment_user_profile_your_exchange)
                    + " " + user.getName() + " " +
                    context.getResources().getString(R.string.fragment_user_profile_is_balanced));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //myLedgerAdaptor.swapCursor(null);
    }

    @Override
    public void OnItemDeleted(double amount, String transactionType) {
        if (transactionType != null && transactionType != "") {
            setTransactionUpdated(true);
            if (transactionType.equalsIgnoreCase(UserTransactionType.LENT.name())) {
                totalLentAmount -= amount;
            } else if (transactionType.equalsIgnoreCase(UserTransactionType.BORROW.name())) {
                totalBorrowAmount -= amount;
            }
            updateAmountDisplayUI();
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).userTransactionDelete();
            }
        }
    }

    public void restartTransactionLoader() {
        totalLentAmount = 0D;
        totalBorrowAmount = 0D;
        userTransactions.clear();
        getLoaderManager().restartLoader(USER_TRANSACTION_LIST_LOADER, null, this);
    }

    public boolean isUserInfoUpdated() {
        return userInfoUpdated;
    }

    public void setUserInfoUpdated(boolean userInfoUpdated) {
        this.userInfoUpdated = userInfoUpdated;
    }

    public boolean isTransactionUpdated() {
        return transactionUpdated;
    }

    public void setTransactionUpdated(boolean transactionUpdated) {
        this.transactionUpdated = transactionUpdated;
    }

    public interface OnUserTransactionDelete {
        void userTransactionDelete();
    }
}