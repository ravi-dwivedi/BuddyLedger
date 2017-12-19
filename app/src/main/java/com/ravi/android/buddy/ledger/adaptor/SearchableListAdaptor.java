package com.ravi.android.buddy.ledger.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.ravi.android.buddy.ledger.CustomUiElement.ColorGenerator;
import com.ravi.android.buddy.ledger.CustomUiElement.TextDrawable;
import com.ravi.android.buddy.ledger.R;
import com.ravi.android.buddy.ledger.utility.UIToolsUtil;

import java.util.ArrayList;

/**
 * Created by ravi on 8/2/17.
 */

public class SearchableListAdaptor extends ArrayAdapter implements Filterable {

    private Context mContext;
    private ArrayList<String> mBackupStrings;
    private ArrayList<String> mStrings;
    private StringFilter mStringFilter = new StringFilter();


    public SearchableListAdaptor(Context context, ArrayList<String> strings) {
        super(context,R.layout.searchable_list_item);
        mContext = context;
        mStrings = strings;
        mBackupStrings = strings;
    }

    @Override
    public int getCount() {
        return mStrings == null ? 0 : mStrings.size();
    }

    @Override
    public String getItem(int position) {
        return mStrings == null ? null : mStrings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mStrings == null ? 0L : mStrings.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView == null ? View.inflate(mContext, R.layout.searchable_list_item, null) : convertView;
        ImageView letters = (ImageView) view.findViewById(R.id.ImgVw_Letters);
        TextView dispalyName = (TextView) view.findViewById(R.id.TxtVw_DisplayName);
        letters.setImageDrawable(getTextDrawable(mStrings.get(position)));
        dispalyName.setText(mStrings.get(position));
        return view;
    }

    private TextDrawable getTextDrawable(String displayName) {
        TextDrawable drawable = null;
        if (!TextUtils.isEmpty(displayName)) {
            int color2 = ColorGenerator.MATERIAL.getColor(displayName);
            drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(UIToolsUtil.dpToPx(mContext, 32))
                    .height(UIToolsUtil.dpToPx(mContext, 32))
                    .textColor(Color.WHITE)
                    .toUpperCase()
                    .endConfig()
                    .round()
                    .build(displayName.substring(0, 1), color2);
        } else {
            drawable = TextDrawable.builder()
                    .beginConfig()
                    .width(UIToolsUtil.dpToPx(mContext, 32))
                    .height(UIToolsUtil.dpToPx(mContext, 32))
                    .endConfig()
                    .round()
                    .build("?", Color.GRAY);
        }
        return drawable;
    }

    @Override
    public Filter getFilter() {
        return mStringFilter;
    }

    public class StringFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults filterResults = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.count = mBackupStrings.size();
                filterResults.values = mBackupStrings;
                return filterResults;
            }
            final ArrayList<String> filterStrings = new ArrayList<>();
            for (String text : mBackupStrings) {
                if (text.toLowerCase().contains(constraint)) {
                    filterStrings.add(text);
                }
            }
            filterResults.count = filterStrings.size();
            filterResults.values = filterStrings;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mStrings = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    }

    private class ItemView {
        public ImageView mImageView;
        public TextView mTextView;
    }
}
