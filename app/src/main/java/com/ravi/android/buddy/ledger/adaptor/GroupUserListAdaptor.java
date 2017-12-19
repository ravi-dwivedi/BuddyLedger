package com.ravi.android.buddy.ledger.adaptor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.CustomUiElement.DynamicSizeImageView;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.UserProfileActivity;
import com.ravi.android.buddy.ledger.fragment.GroupInfoFragment;
import com.ravi.android.buddy.ledger.model.Gender;
import com.ravi.android.buddy.ledger.model.User;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by ravi on 27/2/17.
 */

public class GroupUserListAdaptor extends RecyclerView.Adapter<GroupUserListAdaptor.GroupUserListAdaptorViewHolder> {


    final private Context mContext;

    private List<User> users;

    private String mCurrency;

    private Class clazz;

    private Boolean showBalanceField = false;

    public GroupUserListAdaptor(Context context, List<User> users, String mCurrency, Class clazz, Boolean showBalanceField) {
        this.mContext = context;
        this.users = users;
        this.mCurrency = mCurrency;
        this.clazz = clazz;
        this.showBalanceField = showBalanceField;
    }

    @Override
    public GroupUserListAdaptorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_group_list_horizontal, parent, false);
        return new GroupUserListAdaptorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupUserListAdaptorViewHolder holder, int position) {
        holder.user_name.setText(users.get(position).getName());
        holder.amount.setText("");

        if (users.get(position).getUserImageLocation() != null && !users.get(position).getUserImageLocation().equalsIgnoreCase("")) {
            holder.user_profile.setImageURI(Uri.parse(users.get(position).getUserImageLocation()));
        } else {
            switch (Gender.valueOf(users.get(position).getGender())) {
                case FEMALE:
                    holder.user_profile.setImageResource(R.drawable.female_profile_big);
                    break;
                case MALE:
                    holder.user_profile.setImageResource(R.drawable.male_profile_big);
                    break;
            }
        }
        if (showBalanceField && (clazz == SummaryListAdaptor.class
                || clazz == GroupInfoTransactionListAdaptor.class)) {
            if (clazz == GroupInfoTransactionListAdaptor.class) {
                if (users.get(position).getMonthlyBalanceAmount() == 0) {
                    holder.amount.setText(mContext.getResources().getString(R.string.group_user_list_adaptor_nothing_to_give_or_take));
                } else if (users.get(position).getMonthlyBalanceAmount() < 0) {
                    DecimalFormat df = new DecimalFormat("#.##");
                    holder.amount.setText(mContext.getResources().getString(R.string.group_user_list_adaptor_nedd_to_give) + " " + df.format(users.get(position).getMonthlyBalanceAmount()) + " " + mCurrency);
                } else {
                    DecimalFormat df = new DecimalFormat("#.##");
                    holder.amount.setText(mContext.getResources().getString(R.string.group_user_list_adaptor_need_to_take) + " " + df.format(users.get(position).getMonthlyBalanceAmount()) + " " + mCurrency);
                }
            } else if (clazz == SummaryListAdaptor.class) {
                if (users.get(position).getMonthlyBalanceAmount() == 0) {
                    holder.amount.setText(mContext.getResources().getString(R.string.group_user_list_adaptor_transfer_is_balanced));
                } else if (users.get(position).getMonthlyBalanceAmount() < 0) {
                    holder.amount.setText(mContext.getResources().getString(R.string.group_user_list_adaptor_owes) + " " + (-1) * users.get(position).getMonthlyBalanceAmount() + " " + mCurrency);
                } else {
                    holder.amount.setText(mContext.getResources().getString(R.string.group_user_list_adaptor_you_owes) + " " + users.get(position).getMonthlyBalanceAmount() + " " + mCurrency);
                }
            }
            holder.amount.setVisibility(View.VISIBLE);
        } else {
            holder.amount.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (users == null)
            return 0;
        return users.size();
    }

    public class GroupUserListAdaptorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public DynamicSizeImageView user_profile;
        public TextView user_name;
        public TextView amount;

        public GroupUserListAdaptorViewHolder(View view) {
            super(view);
            user_profile = (DynamicSizeImageView) view.findViewById(R.id.group_profile);
            user_name = (TextView) view.findViewById(R.id.group_name);
            amount = (TextView) view.findViewById(R.id.group_description);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            User user = users.get(adapterPosition);
            Intent intent = new Intent(mContext, UserProfileActivity.class);
            intent.putExtra("USER_ID", user.getId());
            mContext.startActivity(intent);
        }
    }
}
