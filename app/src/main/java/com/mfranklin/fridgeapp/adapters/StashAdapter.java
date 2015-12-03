package com.mfranklin.fridgeapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mfranklin.fridgeapp.Constants;
import com.mfranklin.fridgeapp.FoodItem;
import com.mfranklin.fridgeapp.R;

/**
 * Created by root on 12/3/15.
 */
public class StashAdapter extends FoodItemAdapter {
    public StashAdapter(Context context, FoodItem[] stashItems) {
        super(context, stashItems);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final FoodItem thisFoodItem = filteredItemList.get(position);
        final int finalPosition = position;

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.stash_item, parent, false);
        }

        // set name
        TextView tv = (TextView) rowView.findViewById(R.id.stash_item_name);
        tv.setText(thisFoodItem.type.name);

        // hook up delete button
        Button b = (Button) rowView.findViewById(R.id.stash_item_delete);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.delete();
                itemList.remove(thisFoodItem);
                filteredItemList.remove(thisFoodItem);
                notifyDataSetChanged();
            }
        });

        // Hook up detail card display on touch
        final PopupWindow detailCard = new PopupWindow(ctx);
        View detailCardView = inflater.inflate(R.layout.food_item_detail_card, null);
        detailCardView.setBackgroundColor(Color.argb(0xf0, 0xB0, 0xB0, 0xB0));
        // Fill in details
        TextView name = (TextView) detailCardView.findViewById(R.id.detail_card_name_val);
        TextView category = (TextView) detailCardView.findViewById(R.id.detail_card_category_val);
        TextView location = (TextView) detailCardView.findViewById(R.id.detail_card_location_val);
        TextView defaultLocation = (TextView) detailCardView.findViewById(R.id.detail_card_default_location_val);
        name.setText(thisFoodItem.type.name);
        category.setText(thisFoodItem.type.category);
        location.setText(Constants.locationFlagToString(thisFoodItem.location));
        defaultLocation.setText(Constants.locationFlagToString(thisFoodItem.type.default_location));
        detailCard.setFocusable(true);
        detailCard.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        detailCard.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        detailCard.setContentView(detailCardView);
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailCard.showAsDropDown(v, -5, 0);
            }
        });
        return rowView;
    }
}
