package com.mfranklin.kitchnik.adapters;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.R;
import com.mfranklin.kitchnik.data_model.Reminder;

import java.util.Calendar;
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

    private int expandedPosition = -1;
    private View expandedView = null;
    private HashMap<FoodItem, Reminder> itemToReminder = new HashMap<>(); // to cache reminders

    public StashAdapter(Context context, FoodItem[] stashItems) {
        super(context, stashItems);
        filter.addStatusFilter(Constants.STATUS_STASH);
        filter.filter();

        // build the reminders cache now, so it doesn't have to query DB later
        for (FoodItem item : stashItems) {
            itemToReminder.put(item, Reminder.getFoodItemReminder(item.db, item));
        }
    }

    public int getViewTypeCount() {
        return 6; // sure.
    }

    public int getItemViewType(int position) {
        FoodItem item = filteredItemList.get(position);
        Reminder rem = itemToReminder.get(item);
        if (rem == null) {
            rem = Reminder.getFoodItemReminder(item.db, item);
            itemToReminder.put(item, rem);
        }
        boolean isExpanded = (expandedPosition == position);
        if (isExpanded) {
            if (rem.getDaysRemaining() > 10)
                return VIEW_TYPE_EXPANDED_NO_PROGRESS;
            if (rem.getDaysRemaining() <= 10 && rem.getDaysRemaining() > 0)
                return VIEW_TYPE_EXPANDED_SOME_PROGRESS;
            if (rem.getDaysRemaining() <= 0)
                return VIEW_TYPE_EXPANDED_FULL_PROGRESS;
        }
        else {
            if (rem.getDaysRemaining() > 10) {
                //Log.d("StashAdapter", "returning normal no progress, " + item.type.name);
                return VIEW_TYPE_NORMAL_NO_PROGRESS;
            }
            if (rem.getDaysRemaining() <= 10 && rem.getDaysRemaining() > 0) {
                //Log.d("StashAdapter", "returning normal some progress, " + item.type.name);
                return VIEW_TYPE_NORMAL_SOME_PROGRESS;
            }
            if (rem.getDaysRemaining() <= 0) {
                return VIEW_TYPE_NORMAL_FULL_PROGRESS;
            }
        }
        return VIEW_TYPE_NORMAL_SOME_PROGRESS;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
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
            holder.statuses = (Spinner) rowView.findViewById(R.id.stash_item_details_status_val);
            holder. locations = (Spinner) rowView.findViewById(R.id.stash_item_details_location_val);
            holder.reminderPicker = (Spinner) rowView.findViewById(R.id.stash_item_details_reminder_length);
            holder.saveButton = (Button) rowView.findViewById(R.id.detail_card_save_button);
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

        // fill in details
        // status and location pickers
        holder.statuses.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, Constants.statusStrings));
        holder.locations.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, Constants.locationStrings));
        int statusIndex, locationIndex, i = 0;
        while (i < Constants.statusStrings.length) {
            if (Constants.statusToString(thisFoodItem.getStatus()).equals(Constants.statusStrings[i])) break;
            i++;
        }
        statusIndex = i;
        i = 0;
        while (i < Constants.locationStrings.length) {
            if (Constants.locationToString(thisFoodItem.getLocation()).equals(Constants.locationStrings[i])) break;
            i++;
        }
        locationIndex = i;
        holder.statuses.setSelection(statusIndex);
        holder.locations.setSelection(locationIndex);
        final View locationsRef = holder.locations;
        final View reminderRef = holder.reminderPicker;
        holder.statuses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newStatus = Constants.stringToStatus((String) parent.getItemAtPosition(position));
                thisFoodItem.setStatus(newStatus);
                if (newStatus == Constants.STATUS_STASH) {
                    locationsRef.setVisibility(View.VISIBLE);
                    reminderRef.setVisibility(View.VISIBLE);
                } else {
                    locationsRef.setVisibility(View.INVISIBLE);
                    reminderRef.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        holder.locations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newLocation = Constants.stringToLocation((String) parent.getItemAtPosition(position));
                thisFoodItem.setLocation(newLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Integer[] reminderVals = new Integer[(int) Math.max(30, rem.getDaysRemaining() + 1)];
        for (i = 0; i < reminderVals.length; i++) reminderVals[i] = i;
        holder.reminderPicker.setAdapter(new ArrayAdapter<Integer>(ctx, android.R.layout.simple_spinner_dropdown_item, reminderVals));
        holder.reminderPicker.setSelection((int) rem.getDaysRemaining());
        final Reminder remRef = rem;
        holder.reminderPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int oldDuration = remRef.getDurationDays();
                int diff = position - (int) remRef.getDaysRemaining();
                remRef.setDurationDays(oldDuration + diff); // automatically updates endDate
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // set up save button
        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.save();
                if (thisFoodItem.getStatus() == Constants.STATUS_STASH) {
                    remRef.save();
                }
                else {
                    if (remRef.getId() != -1) {
                        remRef.delete();
                    }
                }
                notifyDataSetChanged();
                notifyDataSetInvalidated();
                itemToReminder.put(thisFoodItem, remRef);
            }
        });

        // set up expand/contract functionality
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("StashAdapter", "is this where we crash?");
                View details = v.findViewById(R.id.stash_item_details);
                if (position == expandedPosition) { // we're closing
                    details.setVisibility(View.GONE);
                    v.setBackgroundResource(0);
                    expandedPosition = -1;
                    expandedView = null;
                }
                else {
                    details.setVisibility(View.VISIBLE);
                    v.setBackgroundResource(R.drawable.grey_rectangle);
                    if (expandedPosition != -1) {
                        View expandedDetails = expandedView.findViewById(R.id.stash_item_details);
                        expandedDetails.setVisibility(View.GONE);
                        expandedView.setBackgroundResource(0);
                    }
                    expandedPosition = position;
                    expandedView = v;
                }
            }
        });

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
        Spinner statuses;
        Spinner locations;
        Spinner reminderPicker;
        Button saveButton;
    }
}
