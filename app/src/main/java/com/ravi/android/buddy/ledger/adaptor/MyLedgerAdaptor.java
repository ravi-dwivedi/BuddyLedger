package com.ravi.android.buddy.ledger.adaptor;

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

import com.ravi.android.buddy.ledger.MyLedgerActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.UserLedgerOperationActivity;
import com.ravi.android.buddy.ledger.data.UserLedgerEntry;
import com.ravi.android.buddy.ledger.model.UserLedger;
import com.ravi.android.buddy.ledger.model.UserLedgerCustomType;
import com.ravi.android.buddy.ledger.utility.DateUtil;

import java.util.List;

/**
 * Created by ravi on 12/2/17.
 */

public class MyLedgerAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String mCurrency;
    final private Context mContext;
    List<UserLedger> userLedgers;
    private OnItemDeleteCallBack mCallBack;

    public MyLedgerAdaptor(Context context, String mCurrency, List<UserLedger> userLedgers, OnItemDeleteCallBack callBack) {
        super();
        this.mCurrency = mCurrency;
        this.mContext = context;
        this.userLedgers = userLedgers;
        this.mCallBack = callBack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new WalletItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ledger_list_fragment, parent, false));
        else
            return new WalletDateItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_ledger_list_fragment, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (userLedgers.get(position).getType().equals(UserLedgerCustomType.DATE.name()))
            return 1;
        else return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (userLedgers.get(position).getType().equals(UserLedgerCustomType.DATE.name())) {
            WalletDateItemViewHolder viewHolder = (WalletDateItemViewHolder) holder;
            viewHolder.dateText.setText(DateUtil.getFormattedDateOnly(userLedgers.get(position).getDate()));
        } else {
            WalletItemViewHolder viewHolder = (WalletItemViewHolder) holder;
            if (userLedgers.get(position).getDetails() == null || userLedgers.get(position).getDetails().equals(""))
                viewHolder.walletDescription.setText(mContext.getString(R.string.empty_detail_default_text));
            else viewHolder.walletDescription.setText(userLedgers.get(position).getDetails());
            if (userLedgers.get(position).getType().equals(UserLedgerCustomType.INCOME.name())) {
                bindIncome(viewHolder);
                viewHolder.walletAmount.setText(mCurrency + userLedgers.get(position).getAmount());
            }
            if (userLedgers.get(position).getType().equals(UserLedgerCustomType.EXPENSE.name())) {
                bindExpense(viewHolder);
                viewHolder.walletAmount.setText(mCurrency + (userLedgers.get(position).getAmount()));
            }
            viewHolder.walletDate.setText(DateUtil.getFormattedTimeOnly(userLedgers.get(position).getDate()));
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userLedgers.size();
    }

    public void bindIncome(WalletItemViewHolder viewHolder) {
        viewHolder.walletType.setText(mContext.getString(R.string.income));
        viewHolder.walletTypeIcon.setImageResource(R.drawable.lent_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }

    public void bindExpense(WalletItemViewHolder viewHolder) {
        viewHolder.walletType.setText(mContext.getString(R.string.expense));
        viewHolder.walletTypeIcon.setImageResource(R.drawable.borrowed_icon);
        viewHolder.viewHolderContainer.setBackgroundColor(Color.WHITE);
    }

    public class WalletItemViewHolder extends RecyclerView.ViewHolder {
        private TextView walletType;
        private TextView walletDate;
        private TextView walletAmount;
        private ImageView walletTypeIcon;
        private TextView walletDescription;
        private CardView viewHolderContainer;

        public WalletItemViewHolder(View itemView) {
            super(itemView);
            viewHolderContainer = (CardView) itemView;
            walletType = (TextView) itemView.findViewById(R.id.wallet_type_text);
            walletAmount = (TextView) itemView.findViewById(R.id.wallet_amount_text);
            walletDate = (TextView) itemView.findViewById(R.id.wallet_date_text);
            walletDescription = (TextView) itemView.findViewById(R.id.wallet_description_text);
            walletTypeIcon = (ImageView) itemView.findViewById(R.id.wallet_type_icon);
        }
    }

    public class WalletDateItemViewHolder extends RecyclerView.ViewHolder {
        private TextView dateText;

        public WalletDateItemViewHolder(View itemView) {
            super(itemView);
            dateText = (TextView) itemView.findViewById(R.id.date_item_text);
        }
    }

    public void onItemClick(final int position) {
        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(mContext);
        dialogBuilder.setItems(R.array.transaction_click_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    UserLedger item = userLedgers.get(position);
                    Intent editItem = new Intent(mContext, UserLedgerOperationActivity.class);
                    editItem.putExtra("ID", item.getId());
                    editItem.putExtra("DETAILS", item.getDetails());
                    editItem.putExtra("TYPE", item.getType());
                    editItem.putExtra("AMOUNT", item.getAmount());
                    editItem.putExtra("EDIT", true);
                    editItem.putExtra("POSITION", position);
                    editItem.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ((MyLedgerActivity) mContext).startActivityForResult(editItem, 201);
                    dialog.dismiss();
                } else if (which == 1) {
                    UserLedger item = userLedgers.get(position);
                    double amount = item.getAmount();
                    String type = item.getType();
                    mContext.getContentResolver().delete(UserLedgerEntry.buildUserLedgerUri(), UserLedgerEntry.COLUMN_LEDGER_ID + " = " + item.getId(), null);
                    if (userLedgers.get(position - 1).getType().equalsIgnoreCase(UserLedgerCustomType.DATE.name()) &&
                            (userLedgers.size() == position + 1 || userLedgers.get(position + 1).getType().equalsIgnoreCase(UserLedgerCustomType.DATE.name()))) {
                        userLedgers.remove(position);
                        userLedgers.remove(position - 1);
                    } else {
                        userLedgers.remove(position);
                    }
                    notifyDataSetChanged();
                    mCallBack.OnItemDeleted(amount, type);
                    dialog.dismiss();
                }
            }
        });
        dialogBuilder.show();
    }

    public interface OnItemDeleteCallBack {
        void OnItemDeleted(double amount, String ledgerType);

    }

    public void OnItemAdded(long id, String type, Double amount, String details) {

        if (userLedgers.size() == 0 || !DateUtil.isSameDay(userLedgers.get(0).getDate(), userLedgers.get(1).getDate())) {
            UserLedger item = new UserLedger();
            item.setType(UserLedgerCustomType.DATE.name());
            if (userLedgers.size() == 0) {
                item.setDate(DateUtil.getCurrentFormattedDateTime());
            } else {
                item.setDate(userLedgers.get(0).getDate());
            }
            userLedgers.add(0, item);
        }
        UserLedger item = new UserLedger();
        item.setId(id);
        item.setType(type);
        item.setDetails(details);
        item.setAmount(amount);
        item.setDate(DateUtil.getCurrentFormattedDateTime());
        userLedgers.add(1, item);

        notifyDataSetChanged();
    }

    public void OnItemEdited(long id, String type, Double amount, String details, int position) {
        if (userLedgers != null && (userLedgers.size() >= (position + 1))) {
            userLedgers.get(position).setAmount(amount);
            userLedgers.get(position).setType(type);
            userLedgers.get(position).setDetails(details);
            userLedgers.get(position).setId(id);
            notifyDataSetChanged();
        }
    }

    public List<UserLedger> getAdaptorItems() {
        return this.userLedgers;
    }
}
