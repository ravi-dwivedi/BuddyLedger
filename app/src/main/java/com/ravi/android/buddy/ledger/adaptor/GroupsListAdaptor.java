package com.ravi.android.buddy.ledger.adaptor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ravi.android.buddy.ledger.CustomUiElement.DynamicSizeImageView;
import com.ravi.android.buddy.ledger.GroupInfoActivity;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.UserProfileActivity;
import com.ravi.android.buddy.ledger.data.GroupEntry;
import com.ravi.android.buddy.ledger.data.UserEntry;

/**
 * Created by ravi on 22/1/17.
 */

public class GroupsListAdaptor extends RecyclerView.Adapter<GroupsListAdaptor.GroupsListAdaptorViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    private int layoutId;
    private OnGroupItemClick onGroupItemClickHandler;
    private int prevPosition = -1;

    public GroupsListAdaptor(Context context, int layoutId, OnGroupItemClick onGroupItemClickHandler) {
        this.mContext = context;
        this.layoutId = layoutId;
        this.onGroupItemClickHandler = onGroupItemClickHandler;
    }

    @Override
    public GroupsListAdaptor.GroupsListAdaptorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        return new GroupsListAdaptor.GroupsListAdaptorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupsListAdaptor.GroupsListAdaptorViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        //setAnimation(holder.container, position);
        holder.group_name.setText(mCursor.getString(1));
        if (mCursor.getString(3) == null || mCursor.getString(3).equals("")) {
            holder.group_description.setText(mContext.getResources().getString(R.string.empty_detail_default_text));
        } else {
            holder.group_description.setText(mCursor.getString(3));
        }

        switch (mCursor.getString(2)) {
            case "Food":
                holder.group_profile.setImageResource(R.drawable.food_big);
                break;
            case "House Rent":
                holder.group_profile.setImageResource(R.drawable.rent_big);
                break;
            case "Party":
                holder.group_profile.setImageResource(R.drawable.party_big);
                break;
            case "Trip":
                holder.group_profile.setImageResource(R.drawable.trip_big);
                break;
            case "Movie":
                holder.group_profile.setImageResource(R.drawable.movie_big);
                break;
            case "House Expenses":
                holder.group_profile.setImageResource(R.drawable.house_expense);
                break;
            case "Class Mates":
                holder.group_profile.setImageResource(R.drawable.school_big);
                break;
            case "Colleague":
                holder.group_profile.setImageResource(R.drawable.colleage_big);
                break;
            case "Other":
                holder.group_profile.setImageResource(R.drawable.rent_big);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor)
            return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(0);
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public class GroupsListAdaptorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public DynamicSizeImageView group_profile;
        public TextView group_name;
        public TextView group_description;
        public CardView container;

        public GroupsListAdaptorViewHolder(View view) {
            super(view);
            group_profile = (DynamicSizeImageView) view.findViewById(R.id.group_profile);
            group_name = (TextView) view.findViewById(R.id.group_name);
            group_description = (TextView) view.findViewById(R.id.group_description);
            container = (CardView) view.findViewById(R.id.groupInfoContainer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int groupID = mCursor.getColumnIndex(GroupEntry.COLUMN_GROUP_ID);

            if (onGroupItemClickHandler != null) {
                onGroupItemClickHandler.onGroupItemClick(mCursor.getLong(groupID));
            } else {
                Intent intent = new Intent(mContext, GroupInfoActivity.class);
                intent.putExtra("GROUP_ID", mCursor.getLong(groupID));
                mContext.startActivity(intent);
            }

            /*
            String user_profile_image_transition = mContext.getResources().getString(R.string.user_profile_image_transition);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mProfileIMage.setTransitionName(user_profile_image_transition + mCursor.getInt(userIdColumnIndex));
            }
            if (Build.VERSION.SDK_INT >= 21) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, (View) mProfileIMage, user_profile_image_transition + getAdapterPosition());
                mContext.startActivity(intent, options.toBundle());
            } else
                mContext.startActivity(intent);
            */
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
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_out_right);
            animation.setDuration(500);
            animation.setInterpolator(mContext, android.R.interpolator.accelerate_decelerate);
            viewToAnimate.startAnimation(animation);
            prevPosition = position;
        }
    }

    public interface OnGroupItemClick {
        void onGroupItemClick(long group_id);
    }
}
