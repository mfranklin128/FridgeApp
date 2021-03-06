package com.mfranklin.kitchnik.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.R;
import com.mfranklin.kitchnik.data_model.FoodType;
import com.mfranklin.kitchnik.data_model.FridgeAppDbHelper;
import com.mfranklin.kitchnik.data_model.Reminder;

import java.util.Calendar;

/**
 * Created by root on 12/3/15.
 */
public class ShoppingListAdapter extends FoodItemAdapter {

    private String[] categories;

    public ShoppingListAdapter(Context context, FoodItem[] foodItems) {
        super(context, foodItems);
        filter.addStatusFilter(Constants.STATUS_LIST);
        filter.filter();

        categories = FoodType.getAllCategories(new FridgeAppDbHelper(context).getReadableDatabase());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewGroup finalParent = parent;
        final FoodItem thisFoodItem = filteredItemList.get(position);
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.shopping_list_item, parent, false);
        }

        TextView tv = (TextView) rowView.findViewById(R.id.shopping_list_item_name);
        tv.setText(thisFoodItem.getName());

        // Hook up add and delete buttons
        View addButton = rowView.findViewById(R.id.shopping_list_item_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.setLocation(thisFoodItem.type.default_location);
                thisFoodItem.setStatus(Constants.STATUS_STASH);
                Calendar cal = Calendar.getInstance();
                thisFoodItem.save();
                Reminder reminder = new Reminder(thisFoodItem.getId(), cal.getTime(), thisFoodItem.type.default_reminder, -1, thisFoodItem.db);
                reminder.save();
                itemList.remove(thisFoodItem);
                filteredItemList.remove(thisFoodItem);
                notifyDataSetChanged();
                final Toast addConfirmation = Toast.makeText(ctx, "Added " + thisFoodItem.getName() +
                        " to " + Constants.locationToString(thisFoodItem.getLocation()), Toast.LENGTH_SHORT); // will be canceled after 1.5 seconds
                addConfirmation.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addConfirmation.cancel();
                    }
                }, 1500);
            }
        });

        View deleteButton = rowView.findViewById(R.id.shopping_list_item_remove);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.delete();
                itemList.remove(thisFoodItem);
                filteredItemList.remove(thisFoodItem);
                notifyDataSetChanged();
            }
        });

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
                View detailCardView = FoodItemAdapter.assignItemDetailCard(ctx, inflater, thisFoodItem, categories);
                detailCard.setContentView(detailCardView);
                detailCard.showAtLocation(v, Gravity.CENTER, 0, 0);
            }
        });
        return rowView;
    }
}
