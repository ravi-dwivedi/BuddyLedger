package com.ravi.android.buddy.ledger.adaptor;

import android.content.Context;
import android.database.Cursor;
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

import com.bumptech.glide.Glide;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.model.GroupTransactionCustomType;
import com.ravi.android.buddy.ledger.model.User;
import com.ravi.android.buddy.ledger.model.UserLedgerCustomType;
import com.ravi.android.buddy.ledger.model.UserTransaction;
import com.ravi.android.buddy.ledger.utility.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ravi on 11/2/17.
 */

public class SummaryListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final private Context mContext;

    private List<UserTransaction> userTransactions;

    private Map<Long, User> allUsers;
    private List<Map<Long, Double>> perMonthUserTotal;

    private String mCurrency;

    public SummaryListAdaptor(Context context, List<UserTransaction> userTransactions, Map<Long, User> allUsers,
                              List<Map<Long, Double>> perMonthUserTotal, String mCurrency) {
        this.mContext = context;
        this.userTransactions = userTransactions;
        this.allUsers = allUsers;
        this.perMonthUserTotal = perMonthUserTotal;
        this.mCurrency = mCurrency;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new SummaryListAdaptor.TransactionItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_group_info_fragment, parent, false));
        else if (viewType == 1)
            return new SummaryListAdaptor.TransactionDateItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_fragment_user_profile, parent, false));
        else
            return new SummaryListAdaptor.TransactionMonthSummaryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_transaction_group_info_fragment, parent, false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 0:
                TransactionItemViewHolder viewHolder1 = (TransactionItemViewHolder) holder;
                if (userTransactions.get(position).getDescription() == null || userTransactions.get(position).getDescription().equals(""))
                    viewHolder1.transactionDescription.setText(mContext.getString(R.string.empty_detail_default_text));
                else
                    viewHolder1.transactionDescription.setText(userTransactions.get(position).getDescription());

                viewHolder1.transactionAmount.setText(mCurrency + userTransactions.get(position).getAmount());
                viewHolder1.transactionType.setVisibility(View.VISIBLE);
                viewHolder1.transactionType.setText(userTransactions.get(position).getTransaction_type().charAt(0) + userTransactions.get(position).getTransaction_type().substring(1).toLowerCase());
                viewHolder1.userName.setText(allUsers.get(userTransactions.get(position).getUserId()).getName());
                if (allUsers.get(userTransactions.get(position).getUserId()).getUserImageLocation() != null && !allUsers.get(userTransactions.get(position).getUserId()).getUserImageLocation().equalsIgnoreCase("")) {
                    viewHolder1.userProfileIcon.setImageURI(Uri.parse(allUsers.get(userTransactions.get(position).getUserId()).getUserImageLocation()));
                } else {
                    switch (Gender.valueOf(allUsers.get(userTransactions.get(position).getUserId()).getGender())) {
                        case FEMALE:
                            viewHolder1.userProfileIcon.setImageResource(R.drawable.female_profile_big);
                            break;
                        case MALE:
                            viewHolder1.userProfileIcon.setImageResource(R.drawable.male_profile_big);
                            break;
                    }
                }
                viewHolder1.transactionDate.setText(DateUtil.getFormattedTimeOnly(userTransactions.get(position).getTransaction_date()));
                viewHolder1.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //onItemClick(position);
                    }
                });
                viewHolder1.viewHolderContainer.setBackgroundColor(Color.WHITE);
                break;
            case 1:
                TransactionDateItemViewHolder viewHolder2 = (TransactionDateItemViewHolder) holder;
                viewHolder2.dateText.setText(DateUtil.getFormattedDateOnly(userTransactions.get(position).getTransaction_date()));
                break;
            case 2:
                TransactionMonthSummaryViewHolder viewHolder3 = (TransactionMonthSummaryViewHolder) holder;
                viewHolder3.monthValue.setText(DateUtil.getMonthAndYearOnly(userTransactions.get(position).getTransaction_date())+" ");
                List<User> users = new ArrayList<>();
                for (Map.Entry<Long, Double> entrySet :
                        perMonthUserTotal.get(userTransactions.get(position).getMonthTransactionIndex()).entrySet()) {
                    User u = new User();
                    u.setCreationDate(allUsers.get(entrySet.getKey()).getCreationDate());
                    u.setEmail(allUsers.get(entrySet.getKey()).getEmail());
                    u.setGender(allUsers.get(entrySet.getKey()).getGender());
                    u.setId(allUsers.get(entrySet.getKey()).getId());
                    u.setMonthlyBalanceAmount(entrySet.getValue());
                    u.setName(allUsers.get(entrySet.getKey()).getName());
                    u.setNumber(allUsers.get(entrySet.getKey()).getNumber());
                    u.setUserImageLocation(allUsers.get(entrySet.getKey()).getUserImageLocation());
                    users.add(u);
                }
                GroupUserListAdaptor adaptor = new GroupUserListAdaptor(mContext, users, mCurrency, SummaryListAdaptor.class, true);
                viewHolder3.monthMemberList.setAdapter(adaptor);
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                llm.setOrientation(LinearLayoutManager.HORIZONTAL);
                viewHolder3.monthMemberList.setLayoutManager(llm);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return userTransactions.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (userTransactions.get(position).getTransaction_type().equals(UserLedgerCustomType.DATE.name()))
            return 1;
        else if (userTransactions.get(position).getTransaction_type().equals(UserLedgerCustomType.MONTH.name()))
            return 2;
        else return 0;
    }

    public class TransactionItemViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView transactionDate;
        private TextView transactionAmount;
        private TextView transactionType;
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
            transactionType = (TextView) itemView.findViewById(R.id.transaction_type);
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
