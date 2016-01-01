package com.mfranklin.kitchnik.adapters;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
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

    public View getView(final int position, View convertView, final ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final FoodItem thisFoodItem = filteredItemList.get(position);

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.stash_item, parent, false);
        }

        // set category
        TextView category = (TextView) rowView.findViewById(R.id.stash_item_category);
        category.setText(thisFoodItem.type.category);
        // set name
        TextView name = (TextView) rowView.findViewById(R.id.stash_item_name);
        name.setText(thisFoodItem.type.name);
        // hook up delete button
        ImageButton delete = (ImageButton) rowView.findViewById(R.id.stash_item_delete_button);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Removed " + thisFoodItem.type.name + " from " + Constants.locationToString(thisFoodItem.getLocation());
                Snackbar.make(parent, message, Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // do nothing, because we don't delete until the row disappears
                    }
                }).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        switch (event) {
                            case DISMISS_EVENT_TIMEOUT:
                                thisFoodItem.delete();
                                itemList.remove(thisFoodItem);
                                filteredItemList.remove(thisFoodItem);
                                itemToReminder.remove(thisFoodItem);
                                break;
                            case DISMISS_EVENT_SWIPE:
                                thisFoodItem.delete();
                                itemList.remove(thisFoodItem);
                                filteredItemList.remove(thisFoodItem);
                                itemToReminder.remove(thisFoodItem);
                                break;
                            default:
                                break;
                        }
                        notifyDataSetChanged();
                        super.onDismissed(snackbar, event);
                    }
                }).show();
                notifyDataSetChanged();
            }
        });

        // hook up add-to-list button
        ImageButton addBack = (ImageButton) rowView.findViewById(R.id.stash_item_add_button);
        addBack.setOnClickListener(new View.OnClickListener() {
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
        LinearLayout pProgress = (LinearLayout) rowView.findViewById(R.id.stash_item_progress_bar_positive);
        LinearLayout nProgress = (LinearLayout) rowView.findViewById(R.id.stash_item_progress_bar_negative);
        ImageView progressImage = (ImageView) rowView.findViewById(R.id.stash_item_progress_bar_positive_image);
        if (rem == null || rem.getDaysRemaining() > 10) {
            pProgress.setVisibility(View.INVISIBLE);
            nProgress.setVisibility(View.INVISIBLE);
        }
        if (rem != null && rem.getDaysRemaining() <= 10) {
            float ratio = (10 - rem.getDaysRemaining())/10.0f;
            if (ratio > 1) ratio = 1;
            float pRatio = ratio;
            float nRatio = 1-ratio;
            LinearLayout.LayoutParams pParams = (LinearLayout.LayoutParams) pProgress.getLayoutParams();
            LinearLayout.LayoutParams nParams = (LinearLayout.LayoutParams) nProgress.getLayoutParams();
            pParams.weight = pRatio;
            nParams.weight = nRatio;
            pProgress.setLayoutParams(pParams);
            nProgress.setLayoutParams(nParams);
        }
        if (rem != null && rem.getDaysRemaining() <= 0) { // it's "expired", so make it red
            category.setTextColor(Color.argb(0xf0, 0xcc, 0x00, 0x00));
            name.setTextColor(Color.argb(0xf0, 0xcc, 0x00, 0x00));
            progressImage.setImageResource(R.drawable.red_rectangle);
        }

        // Hook up detail card display on touch
        class RefreshPopupWindow extends PopupWindow {
            public RefreshPopupWindow(Context ctx) {
                super(ctx);
            }

            public void dismiss() {
                filter.filter();
                notifyDataSetChanged();
                super.dismiss();
            }
        }
        final RefreshPopupWindow detailCard = new RefreshPopupWindow(ctx);
        detailCard.setOutsideTouchable(true);
        detailCard.setWidth((int) (parent.getWidth() * 0.88));
        detailCard.setHeight((int) (parent.getHeight() * 0.70));
        detailCard.setFocusable(true);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View detailCardView = FoodItemAdapter.assignItemDetailCard(ctx, inflater, thisFoodItem);
                detailCard.setContentView(detailCardView);
                detailCard.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });

        return rowView;
    }

}
