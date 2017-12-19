package com.ravi.android.buddy.ledger.adaptor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.ApplicationGlobals;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.TransactionOperationsActivity;
import com.ravi.android.buddy.ledger.UserProfileActivity;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.UserLedgerCustomType;
import com.ravi.android.buddy.ledger.model.UserTransaction;
import com.ravi.android.buddy.ledger.model.UserTransactionCustomType;
import com.ravi.android.buddy.ledger.utility.DateUtil;

import java.util.List;

/**
 * Created by ravi on 22/2/17.
 */

public class UserProfileTransactionListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String mCurrency;
    final private Context mContext;

    private List<UserTransaction> userTransactions;

    private OnItemDeleteCallBack mCallBack;

    public UserProfileTransactionListAdaptor(Context context, String mCurrency, List<UserTransaction> userTransactions, OnItemDeleteCallBack callBack) {
        super();
        this.mCurrency = mCurrency;
        this.mContext = context;
        this.userTransactions = userTransactions;
        this.mCallBack = callBack;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new TransactionItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_user_profile_fragment, parent, false));
        else
            return new TransactionDateItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_fragment_user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (userTransactions.get(position).getTransaction_type().equals(UserTransactionCustomType.DATE.name())) {
            TransactionDateItemViewHolder viewHolder = (TransactionDateItemViewHolder) holder;
            viewHolder.dateText.setText(DateUtil.getFormattedDateOnly(userTransactions.get(position).getTransaction_date()));
        } else {
            TransactionItemViewHolder viewHolder = (TransactionItemViewHolder) holder;
            if (userTransactions.get(position).getDescription() == null || userTransactions.get(position).getDescription().equals(""))
                viewHolder.transactionDescription.setText(mContext.getString(R.string.empty_detail_default_text));
            else
                viewHolder.transactionDescription.setText(userTransactions.get(position).getDescription());
            if (userTransactions.get(position).getTransaction_type().equals(UserTransactionCustomType.LENT.name())) {
                bindLent(viewHolder);
                viewHolder.transactionAmount.setText(mCurrency + userTransactions.get(position).getAmount());
            }
            if (userTransactions.get(position).getTransaction_type().equals(UserTransactionCustomType.BORROW.name())) {
                bindBorrow(viewHolder);
                viewHolder.transactionAmount.setText(mCurrency + (userTransactions.get(position).getAmount()));
            }
            viewHolder.transactionDate.setText(DateUtil.getFormattedTimeOnly(userTransactions.get(position).getTransaction_date()));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }
    }

    public void onItemClick(final int position) {
        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(mContext);
        dialogBuilder.setItems(R.array.transaction_click_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    UserTransaction item = userTransactions.get(position);
                    Intent editItem = new Intent(mContext, TransactionOperationsActivity.class);
                    editItem.putExtra("TRANSACTION_ID", item.getId());
                    editItem.putExtra("USER_ID", item.getUserId());
                    editItem.putExtra("TYPE", item.getTransaction_type());
                    editItem.putExtra("AMOUNT", item.getAmount());
                    editItem.putExtra("DESCRIPTION", item.getDescription());
                    //editItem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ((Activity) mContext).startActivityForResult(editItem,
                            ApplicationGlobals.USER_TRANSACTION_OPERATION__ACTIVITY_REQUEST_CODE);
                    dialog.dismiss();
                } else if (which == 1) {
                    UserTransaction item = userTransactions.get(position);
                    double amount = item.getAmount();
                    String type = item.getTransaction_type();
                    mContext.getContentResolver().delete(UserTransactionEntry.buildTransactionUri(), UserTransactionEntry.COLUMN_TRANSACTION_ID + " = " + item.getId(), null);
                    if (userTransactions.get(position - 1).getTransaction_type().equalsIgnoreCase(UserLedgerCustomType.DATE.name()) &&
                            (userTransactions.size() == position + 1 || userTransactions.get(position + 1).getTransaction_type().equalsIgnoreCase(UserLedgerCustomType.DATE.name()))) {
                        userTransactions.remove(position);
                        userTransactions.remove(position - 1);
                    } else {
                        userTransactions.remove(position);
                    }
                    notifyDataSetChanged();
                    mCallBack.OnItemDeleted(amount, type);
                    dialog.dismiss();
                }
            }
        });
        dialogBuilder.show();
    }

    public void bindLent(TransactionItemViewHolder viewHolder) {
        viewHolder.transactionType.setText(mContext.getResources().getString(R.string.transaction_operation_transaction_lent));
        viewHolder.transactionTypeIcon.setImageResource(R.drawable.lent_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }

    public void bindBorrow(TransactionItemViewHolder viewHolder) {
        viewHolder.transactionType.setText(mContext.getResources().getString(R.string.transaction_operation_transaction_borrow));
        viewHolder.transactionTypeIcon.setImageResource(R.drawable.borrowed_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return userTransactions.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (userTransactions.get(position).getTransaction_type().equals(UserTransactionCustomType.DATE.name()))
            return 1;
        else return 0;
    }


    public class TransactionItemViewHolder extends RecyclerView.ViewHolder {
        private TextView transactionType;
        private TextView transactionDate;
        private TextView transactionAmount;
        private ImageView transactionTypeIcon;
        private TextView transactionDescription;
        private CardView viewHolderContainer;

        public TransactionItemViewHolder(View itemView) {
            super(itemView);
            viewHolderContainer = (CardView) itemView;
            transactionType = (TextView) itemView.findViewById(R.id.transaction_type_text);
            transactionAmount = (TextView) itemView.findViewById(R.id.transaction_amount_text);
            transactionDate = (TextView) itemView.findViewById(R.id.transaction_date_text);
            transactionDescription = (TextView) itemView.findViewById(R.id.transaction_description_text);
            transactionTypeIcon = (ImageView) itemView.findViewById(R.id.transaction_type_icon);
        }
    }

    public class TransactionDateItemViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;

        public TransactionDateItemViewHolder(View itemView) {
            super(itemView);
            dateText = (TextView) itemView.findViewById(R.id.date_item_text);
        }
    }

    public interface OnItemDeleteCallBack {
        void OnItemDeleted(double amount, String transactionType);
    }
}
