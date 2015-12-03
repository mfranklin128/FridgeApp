package com.mfranklin.fridgeapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.fridgeapp.Constants;
import com.mfranklin.fridgeapp.FoodItem;
import com.mfranklin.fridgeapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by root on 12/3/15.
 */
public class ShoppingListAdapter extends FoodItemAdapter {
    public ShoppingListAdapter(Context context, FoodItem[] foodItems) {
        super(context, foodItems);
        filter.addLocationFilter(Constants.LOC_LIST);
        filter.filter();
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
        tv.setText(thisFoodItem.type.name);

        // Hook up add and delete buttons
        View addButton = rowView.findViewById(R.id.shopping_list_item_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.location = thisFoodItem.type.default_location;
                Calendar cal = Calendar.getInstance(); cal.add(Calendar.DATE, thisFoodItem.type.default_reminder);
                Date reminder = cal.getTime();
                thisFoodItem.save();
                itemList.remove(thisFoodItem);
                filteredItemList.remove(thisFoodItem);
                notifyDataSetChanged();
                final Toast addConfirmation = Toast.makeText(ctx, "Added " + thisFoodItem.type.name +
                        " to " + Constants.locationFlagToString(thisFoodItem.location), Toast.LENGTH_SHORT); // will be canceled after 1.5 seconds
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
                Log.d("StashAdapter", "dismiss() being called");
                filter.filter();
                notifyDataSetChanged();
                super.dismiss();
            }
        }
        final RefreshPopupWindow detailCard = new RefreshPopupWindow(ctx);
        detailCard.setOutsideTouchable(true);
        View detailCardView = FoodItemAdapter.assignItemDetailCard(ctx, inflater, thisFoodItem);
        detailCard.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        detailCard.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        detailCard.setContentView(detailCardView);
        detailCard.setOutsideTouchable(true);
        detailCard.setFocusable(true);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailCard.showAsDropDown(v, -5, 0);
            }
        });
        return rowView;
    }
}
