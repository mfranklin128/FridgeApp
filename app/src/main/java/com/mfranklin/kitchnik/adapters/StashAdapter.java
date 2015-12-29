package com.mfranklin.kitchnik.adapters;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.R;
import com.mfranklin.kitchnik.data_model.Reminder;

import java.util.HashMap;

/**
 * Created by root on 12/3/15.
 */
public class StashAdapter extends FoodItemAdapter {

    private static final int VIEW_TYPE_NORMAL_NO_PROGRESS = 0;
    private static final int VIEW_TYPE_NORMAL_SOME_PROGRESS = 1;
    private static final int VIEW_TYPE_NORMAL_FULL_PROGRESS = 2;
    private static final int VIEW_TYPE_EXPANDED_NO_PROGRESS = 3;
    private static final int VIEW_TYPE_EXPANDED_SOME_PROGRESS = 4;
    private static final int VIEW_TYPE_EXPANDED_FULL_PROGRESS = 5;

    private HashMap<Integer, Integer> positionToViewType;

    public StashAdapter(Context context, FoodItem[] stashItems) {
        super(context, stashItems);
        filter.addStatusFilter(Constants.STATUS_STASH);
        filter.filter();

        positionToViewType = new HashMap<>();
    }

    public int getViewTypeCount() {
        return 6;
    }

    public int getItemViewType(int position) {
        Integer type = positionToViewType.get(position);
        if (type != null) return type;
        return VIEW_TYPE_NORMAL_SOME_PROGRESS;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final FoodItem thisFoodItem = filteredItemList.get(position);

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.stash_item, parent, false);
        }
        StashViewHolder holder = (StashViewHolder) rowView.getTag();
        if (holder == null) {
            holder = new StashViewHolder();
            holder.category = (TextView) rowView.findViewById(R.id.stash_item_category);
            holder.name = (TextView) rowView.findViewById(R.id.stash_item_name);
            holder.delete = (ImageButton) rowView.findViewById(R.id.stash_item_delete_button);
            holder.addBack = (ImageButton) rowView.findViewById(R.id.stash_item_add_button);
            holder.pProgress = (LinearLayout) rowView.findViewById(R.id.stash_item_progress_bar_positive);
            holder.nProgress = (LinearLayout) rowView.findViewById(R.id.stash_item_progress_bar_negative);
            holder.progressImage = (ImageView) rowView.findViewById(R.id.stash_item_progress_bar_positive_image);
            holder.details = rowView.findViewById(R.id.stash_item_details);
            rowView.setTag(holder);
        }

        // set category
        holder.category.setText(thisFoodItem.type.category);
        // set name
        holder.name.setText(thisFoodItem.type.name);
        // hook up delete button
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.delete();
                itemList.remove(thisFoodItem);
                filteredItemList.remove(thisFoodItem);
                notifyDataSetChanged();
            }
        });

        // hook up add-to-list button
        holder.addBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.setStatus(Constants.STATUS_LIST);
                itemList.remove(thisFoodItem);
                thisFoodItem.save();
                filteredItemList.remove(thisFoodItem);
                notifyDataSetChanged();
            }
        });

        // set width of progress bar
        Reminder rem = Reminder.getFoodItemReminder(thisFoodItem.db, thisFoodItem);
        if (rem.getDaysRemaining() > 10) {
            holder.pProgress.setVisibility(View.INVISIBLE);
            holder.nProgress.setVisibility(View.INVISIBLE);
        }
        if (rem.getDaysRemaining() <= 10) {
            float ratio = (10 - rem.getDaysRemaining())/10.0f;
            if (ratio > 1) ratio = 1;
            float pRatio = ratio;
            float nRatio = 1-ratio;
            LinearLayout.LayoutParams pParams = (LinearLayout.LayoutParams) holder.pProgress.getLayoutParams();
            LinearLayout.LayoutParams nParams = (LinearLayout.LayoutParams) holder.nProgress.getLayoutParams();
            pParams.weight = pRatio;
            nParams.weight = nRatio;
            holder.pProgress.setLayoutParams(pParams);
            holder.nProgress.setLayoutParams(nParams);
        }
        if (rem.getDaysRemaining() <= 0) { // it's "expired", so make it red
            holder.category.setTextColor(Color.argb(0xf0, 0xcc, 0x00, 0x00));
            holder.name.setTextColor(Color.argb(0xf0, 0xcc, 0x00, 0x00));
            holder.progressImage.setImageResource(R.drawable.red_rectangle);
        }

        final int finalPosition = position;
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View details = v.findViewById(R.id.stash_item_details);
                int viewType = getItemViewType(finalPosition);
                if (details.getVisibility() == View.GONE) {
                    details.setVisibility(View.VISIBLE);
                    if (viewType == VIEW_TYPE_NORMAL_NO_PROGRESS) {
                        positionToViewType.put(finalPosition, VIEW_TYPE_EXPANDED_NO_PROGRESS);
                    }
                    if (viewType == VIEW_TYPE_NORMAL_SOME_PROGRESS) {
                        positionToViewType.put(finalPosition, VIEW_TYPE_EXPANDED_SOME_PROGRESS);
                    }
                    if (viewType == VIEW_TYPE_NORMAL_FULL_PROGRESS) {
                        positionToViewType.put(finalPosition, VIEW_TYPE_EXPANDED_FULL_PROGRESS);
                    }
                }
                else {
                    details.setVisibility(View.GONE);
                    if (viewType == VIEW_TYPE_EXPANDED_NO_PROGRESS) {
                        positionToViewType.put(finalPosition, VIEW_TYPE_NORMAL_NO_PROGRESS);
                    }
                    if (viewType == VIEW_TYPE_EXPANDED_SOME_PROGRESS) {
                        positionToViewType.put(finalPosition, VIEW_TYPE_NORMAL_SOME_PROGRESS);
                    }
                    if (viewType == VIEW_TYPE_EXPANDED_FULL_PROGRESS) {
                        positionToViewType.put(finalPosition, VIEW_TYPE_NORMAL_FULL_PROGRESS);
                    }
                }
            }
        });

        // store the initial view type
        if (rem.getDaysRemaining() > 10) positionToViewType.put(position, VIEW_TYPE_NORMAL_NO_PROGRESS);
        if (rem.getDaysRemaining() <= 10) positionToViewType.put(position, VIEW_TYPE_NORMAL_SOME_PROGRESS);
        if (rem.getDaysRemaining() <= 0) positionToViewType.put(position, VIEW_TYPE_NORMAL_FULL_PROGRESS); // this will overwrite the previous insertion
        return rowView;
    }

    private class StashViewHolder {
        TextView category;
        TextView name;
        ImageButton delete;
        ImageButton addBack;
        LinearLayout pProgress;
        LinearLayout nProgress;
        ImageView progressImage;
        View details;
    }
}
