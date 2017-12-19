package com.ravi.android.buddy.ledger.adaptor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ravi.android.buddy.ledger.HomeActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.data.UserEntry;
import com.ravi.android.buddy.ledger.data.UserTransactionEntry;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.model.UserTransactionType;
import com.ravi.android.buddy.ledger.utility.DateUtil;

/**
 * Created by ravi on 14/1/17.
 */

public class FriendsListAdaptor extends RecyclerView.Adapter<FriendsListAdaptor.FriendsListAdaptorViewHolder> {

    private Cursor mCursor;
    final private Context mContext;

    private String mCurrency;

    private int prevPosition = -1;

    private OnUserItemClick onUserItemClickHandler;

    public FriendsListAdaptor(Context context, String mCurrency, OnUserItemClick onUserItemClickHandler) {
        this.mContext = context;
        this.mCurrency = mCurrency;
        this.onUserItemClickHandler = onUserItemClickHandler;
    }

    private final String[] USER_TRANSACTIONS_COLUMNS = {
            UserTransactionEntry.COLUMN_TRANSACTION_TYPE,
            UserTransactionEntry.COLUMN_TRANSACTION_DATE,
            UserTransactionEntry.COLUMN_TRANSACTION_AMOUNT
    };

    @Override
    public FriendsListAdaptorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_friend_list_fragment, parent, false);
        return new FriendsListAdaptorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FriendsListAdaptorViewHolder holder, final int position) {
        mCursor.moveToPosition(position);
        setAnimation(holder.container, position);
        holder.mFriendName.setText(mCursor.getString(1));
        if (mCursor.getString(2) != null && !mCursor.getString(2).equalsIgnoreCase("")) {
            Glide.with(mContext)
                    .load(Uri.parse(mCursor.getString(2)))
                    .error(mCursor.getString(3).equalsIgnoreCase(Gender.MALE.name()) ? R.drawable.male_profile_big : R.drawable.female_profile_big)
                    .crossFade()
                    .into(holder.mProfileIMage);
        } else {
            switch (Gender.valueOf(mCursor.getString(3))) {
                case MALE:
                    Glide.with(mContext)
                            .load(R.drawable.male_profile_big)
                            .error(R.drawable.male_profile_big)
                            .crossFade()
                            .into(holder.mProfileIMage);
                    break;
                case FEMALE:
                    Glide.with(mContext)
                            .load(R.drawable.female_profile_big)
                            .error(R.drawable.female_profile_big)
                            .crossFade()
                            .into(holder.mProfileIMage);
                    break;
            }
        }

        /*
        if (Build.VERSION.SDK_INT >= 21) {
            int userIdColumnIndex = mCursor.getColumnIndex(UserEntry.COLUMN_USER_ID);
            String user_profile_image_transition = mContext.getResources().getString(R.string.user_profile_image_transition);
            holder.mProfileIMage.setTransitionName(user_profile_image_transition + mCursor.getLong(userIdColumnIndex));
        }
        */

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor transactionCursor = mContext.getContentResolver().query(UserTransactionEntry.buildTransactionUri(),
                        USER_TRANSACTIONS_COLUMNS, UserTransactionEntry.COLUMN_USER_ID + " = " + mCursor.getLong(0), null,
                        UserTransactionEntry.COLUMN_TRANSACTION_DATE + " DESC ");
                double totalLentAmount = 0D;
                double totalBorrowAmount = 0D;
                String lastTransactionDate = null;
                String lastTransactionType = null;
                double lastTransactionAmount = 0D;
                if (transactionCursor != null) {
                    for (int index = 0; index < transactionCursor.getCount(); index++) {
                        transactionCursor.moveToPosition(index);
                        if (index == 0) {
                            lastTransactionDate = transactionCursor.getString(1);
                            lastTransactionAmount = transactionCursor.getDouble(2);
                            lastTransactionType = transactionCursor.getString(0);
                        }
                        switch (UserTransactionType.valueOf(transactionCursor.getString(0))) {
                            case LENT:
                                totalLentAmount += transactionCursor.getDouble(2);
                                break;
                            case BORROW:
                                totalBorrowAmount += transactionCursor.getDouble(2);
                                break;
                        }
                    }
                    transactionCursor.close();
                }
                final double totalLentAmountTemp = totalLentAmount;
                final double totalBorrowAmountTemp = totalBorrowAmount;
                final String lastTransactionDateTemp = lastTransactionDate;
                final String lastTransactionTypeTemp = lastTransactionType;
                final double lastTransactionAmountTemp = lastTransactionAmount;
                ((HomeActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateItemUsingPosition(holder, totalLentAmountTemp, totalBorrowAmountTemp, lastTransactionDateTemp,
                                lastTransactionTypeTemp, lastTransactionAmountTemp);
                    }
                });
            }
        });
        thread.start();
    }

    public void updateItemUsingPosition(final FriendsListAdaptorViewHolder holder, double totalLentAmount,
                                        double totalBorrowAmount, String lastTransactionDate,
                                        String lastTransactionType, double lastTransactionAmount) {
        if (totalLentAmount == totalBorrowAmount) {
            holder.mAmountTitle.setText(mContext.getResources().getString(R.string.item_friend_list_framgnet_transfer_balanced));
            holder.mAmount.setText("");
        } else if (totalLentAmount > totalBorrowAmount) {
            holder.mAmountTitle.setText(mContext.getResources().getString(R.string.item_friend_list_framgnet_owes) + " ");
            holder.mAmount.setTextColor(Color.RED);
            holder.mAmount.setText((totalLentAmount - totalBorrowAmount) + " " + mCurrency);
        } else {
            holder.mAmountTitle.setText(mContext.getResources().getString(R.string.item_friend_list_framgnet_you_owes) + " ");
            holder.mAmount.setText((totalBorrowAmount - totalLentAmount) + " " + mCurrency);
        }
        if (lastTransactionDate != null && !lastTransactionDate.equalsIgnoreCase("")) {
            holder.mLastTransactionAmount.setText(" " + lastTransactionType.charAt(0) + lastTransactionType.substring(1).toLowerCase() + " " + lastTransactionAmount + " " + mCurrency);
            holder.mLastTransactionDate.setText(" " + mContext.getResources().getString(R.string.item_friend_list_framgnet_date) + " " + DateUtil.getFormattedDateOnly(lastTransactionDate));
        } else {
            holder.mLastTransactionAmount.setText(mContext.getResources().getString(R.string.item_friend_list_framgnet_never_done));
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor)
            return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public class FriendsListAdaptorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mProfileIMage;
        private TextView mFriendName;
        private TextView mAmountTitle;
        private TextView mAmount;
        private TextView mLastTransactionAmount;
        private TextView mLastTransactionDate;
        private LinearLayout container;

        public FriendsListAdaptorViewHolder(View view) {
            super(view);
            container = (LinearLayout) view.findViewById(R.id.item_friend_list_fragment);
            mProfileIMage = (ImageView) view.findViewById(R.id.friend_list_item_profile);
            mFriendName = (TextView) view.findViewById(R.id.friend_list_item_name);
            mAmountTitle = (TextView) view.findViewById(R.id.friend_list_item_amount_title);
            mAmount = (TextView) view.findViewById(R.id.friend_list_item_amount);
            mLastTransactionAmount = (TextView) view.findViewById(R.id.friend_list_item_last_transaction_amount);
            mLastTransactionDate = (TextView) view.findViewById(R.id.friend_list_item_last_transaction_date);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int userIdColumnIndex = mCursor.getColumnIndex(UserEntry.COLUMN_USER_ID);
            onUserItemClickHandler.onUserItemClick(mCursor.getLong(userIdColumnIndex));
        }
    }

    private void setAnimation(View viewToAnimate, final int position) {
        if (position > prevPosition) {
            if (position == 0) {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                viewToAnimate.startAnimation(animation);
                prevPosition = position;
                return;
            }
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            animation.setDuration(500);
            animation.setInterpolator(mContext, android.R.interpolator.accelerate_decelerate);
            viewToAnimate.startAnimation(animation);
            prevPosition = position;
        }
    }

    public interface OnUserItemClick {
        void onUserItemClick(long user_id);
    }
}
