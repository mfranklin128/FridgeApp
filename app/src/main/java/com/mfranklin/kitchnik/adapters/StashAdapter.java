package com.mfranklin.kitchnik.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.R;

/**
 * Created by root on 12/3/15.
 */
public class StashAdapter extends FoodItemAdapter {
    public StashAdapter(Context context, FoodItem[] stashItems) {
        super(context, stashItems);
        filter.addStatusFilter(Constants.STATUS_STASH);
        filter.filter();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final FoodItem thisFoodItem = filteredItemList.get(position);

        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.stash_item, parent, false);
        }

        // set name
        TextView tv = (TextView) rowView.findViewById(R.id.stash_item_name);
        tv.setText(thisFoodItem.type.name);

        // hook up delete button
        ImageButton b = (ImageButton) rowView.findViewById(R.id.stash_item_remove);
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
        class RefreshPopupWindow extends PopupWindow {
            public RefreshPopupWindow(Context ctx) {
                super(ctx);
            }

            public void dismiss() {
                Log.d("StashAdapter", "calling dismiss here");
                filter.filter();
                notifyDataSetChanged();
                super.dismiss();
            }
        }
        final RefreshPopupWindow detailCard = new RefreshPopupWindow(ctx);
        detailCard.setOutsideTouchable(true);
        detailCard.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        detailCard.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        detailCard.setOutsideTouchable(true);
        detailCard.setFocusable(true);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View detailCardView = FoodItemAdapter.assignItemDetailCard(ctx, inflater, thisFoodItem);
                detailCard.setContentView(detailCardView);
                detailCard.showAsDropDown(v, 0, 0);
            }
        });
        return rowView;
    }
}
