package com.ravi.android.buddy.ledger.adaptor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.ApplicationGlobals;
import com.ravi.android.buddy.ledger.GroupInfoActivity;
import com.ravi.android.buddy.ledger.GroupTransactionOperationActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.model.Group;
import com.ravi.android.buddy.ledger.model.GroupTransaction;
import com.ravi.android.buddy.ledger.model.GroupTransactionCustomType;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.utility.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ravi on 1/3/17.
 */

public class GroupInfoTransactionListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String mCurrency;
    final private Context mContext;

    private List<GroupTransaction> groupTransactions;
    private Map<Long, User> allGroupUsers;
    private List<Map<Long, Double>> perMonthUserTotal;
    private Group group;

    public GroupInfoTransactionListAdaptor(Context context, Group group, String mCurrency, List<GroupTransaction> groupTransactions, Map<Long, User> allGroupUsers, List<Map<Long, Double>> perMonthUserTotal) {
        super();
        this.mCurrency = mCurrency;
        this.mContext = context;
        this.groupTransactions = groupTransactions;
        this.allGroupUsers = allGroupUsers;
        this.perMonthUserTotal = perMonthUserTotal;
        this.group = group;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new TransactionItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_group_info_fragment, parent, false));
        else if (viewType == 1)
            return new TransactionDateItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_fragment_user_profile, parent, false));
        else
            return new TransactionMonthSummaryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_transaction_group_info_fragment, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case 0:
                TransactionItemViewHolder viewHolder1 = (TransactionItemViewHolder) holder;
                if (groupTransactions.get(position).getDescription() == null || groupTransactions.get(position).getDescription().equals(""))
                    viewHolder1.transactionDescription.setText(mContext.getString(R.string.empty_detail_default_text));
                else
                    viewHolder1.transactionDescription.setText(groupTransactions.get(position).getDescription());

                viewHolder1.transactionAmount.setText(mCurrency + groupTransactions.get(position).getAmount());
                viewHolder1.userName.setText(allGroupUsers.get(groupTransactions.get(position).getUserId()).getName());
                if (allGroupUsers.get(groupTransactions.get(position).getUserId()).getUserImageLocation() != null && !allGroupUsers.get(groupTransactions.get(position).getUserId()).getUserImageLocation().equalsIgnoreCase("")) {
                    viewHolder1.userProfileIcon.setImageURI(Uri.parse(allGroupUsers.get(groupTransactions.get(position).getUserId()).getUserImageLocation()));
                } else {
                    switch (Gender.valueOf(allGroupUsers.get(groupTransactions.get(position).getUserId()).getGender())) {
                        case FEMALE:
                            viewHolder1.userProfileIcon.setImageResource(R.drawable.female_profile_big);
                            break;
                        case MALE:
                            viewHolder1.userProfileIcon.setImageResource(R.drawable.male_profile_big);
                            break;
                    }
                }
                viewHolder1.transactionDate.setText(DateUtil.getFormattedTimeOnly(groupTransactions.get(position).getTransaction_date()));
                viewHolder1.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClick(position);
                    }
                });
                viewHolder1.viewHolderContainer.setBackgroundColor(Color.WHITE);
                break;
            case 1:
                TransactionDateItemViewHolder viewHolder2 = (TransactionDateItemViewHolder) holder;
                viewHolder2.dateText.setText(DateUtil.getFormattedDateOnly(groupTransactions.get(position).getTransaction_date()));
                break;
            case 2:
                TransactionMonthSummaryViewHolder viewHolder3 = (TransactionMonthSummaryViewHolder) holder;
                viewHolder3.monthValue.setText(DateUtil.getMonthAndYearOnly(groupTransactions.get(position).getTransaction_date()) + " ");
                List<User> users = new ArrayList<>();
                for (Map.Entry<Long, Double> entrySet :
                        perMonthUserTotal.get(groupTransactions.get(position).getMonthTransactionIndex()).entrySet()) {
                    allGroupUsers.get(entrySet.getKey()).setMonthlyBalanceAmount(entrySet.getValue());
                    users.add(allGroupUsers.get(entrySet.getKey()));
                }
                GroupUserListAdaptor adaptor = new GroupUserListAdaptor(mContext, users, mCurrency, GroupInfoTransactionListAdaptor.class, true);
                viewHolder3.monthMemberList.setAdapter(adaptor);
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                llm.setOrientation(LinearLayoutManager.HORIZONTAL);
                viewHolder3.monthMemberList.setLayoutManager(llm);
                break;
        }
    }


    public void onItemClick(final int position) {
        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(mContext);
        dialogBuilder.setItems(R.array.transaction_click_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                GroupTransaction item = groupTransactions.get(position);
                Intent editItem = new Intent(mContext, GroupTransactionOperationActivity.class);
                editItem.putExtra("TRANSACTION_ID", item.getId());
                editItem.putExtra("USER_ID", item.getUserId());
                editItem.putExtra("AMOUNT", item.getAmount());
                editItem.putExtra("DESCRIPTION", item.getDescription());
                editItem.putExtra("GROUP_ID", group.getId());
                editItem.putExtra("GROUP_NAME", group.getGroup_name());
                if (which == 0) {
                    editItem.putExtra("EDIT", true);
                    dialog.dismiss();
                } else if (which == 1) {
                    editItem.putExtra("DELETE", true);
                }
                ((Activity) mContext).startActivityForResult(editItem, ApplicationGlobals.GROUP_TRANSACTION_OPERATION_ACTIVITY_REQUEST_CODE);
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }

    @Override
    public int getItemCount() {
        return groupTransactions.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (groupTransactions.get(position).getTransaction_type().equals(GroupTransactionCustomType.DATE.name()))
            return 1;
        else if (groupTransactions.get(position).getTransaction_type().equals(GroupTransactionCustomType.MONTH.name()))
            return 2;
        else return 0;
    }


    public class TransactionItemViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView transactionDate;
        private TextView transactionAmount;
        private ImageView userProfileIcon;
        private TextView transactionDescription;
        private CardView viewHolderContainer;

        public TransactionItemViewHolder(View itemView) {
            super(itemView);
            viewHolderContainer = (CardView) itemView;
            userName = (TextView) itemView.findViewById(R.id.group_member_name);
            transactionAmount = (TextView) itemView.findViewById(R.id.transaction_amount_text);
            transactionDate = (TextView) itemView.findViewById(R.id.transaction_date_text);
            transactionDescription = (TextView) itemView.findViewById(R.id.transaction_description_text);
            userProfileIcon = (ImageView) itemView.findViewById(R.id.user_prifile_icon);
        }
    }

    public class TransactionDateItemViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;

        public TransactionDateItemViewHolder(View itemView) {
            super(itemView);
            dateText = (TextView) itemView.findViewById(R.id.date_item_text);
        }
    }

    public class TransactionMonthSummaryViewHolder extends RecyclerView.ViewHolder {

        private TextView monthValue;

        private RecyclerView monthMemberList;

        public TransactionMonthSummaryViewHolder(View itemView) {
            super(itemView);
            monthValue = (TextView) itemView.findViewById(R.id.monthValue);
            monthMemberList = (RecyclerView) itemView.findViewById(R.id.monthMemberList);
        }
    }
}