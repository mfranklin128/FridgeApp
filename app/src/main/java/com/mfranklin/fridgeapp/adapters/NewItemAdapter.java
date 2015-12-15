package com.mfranklin.fridgeapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mfranklin.fridgeapp.data_model.Constants;
import com.mfranklin.fridgeapp.data_model.FoodItem;
import com.mfranklin.fridgeapp.data_model.FoodType;
import com.mfranklin.fridgeapp.R;
import com.mfranklin.fridgeapp.data_model.Reminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by root on 12/4/15.
 */
public class NewItemAdapter extends BaseAdapter implements Filterable {
    private ArrayList<FoodType> foodTypes;
    private ArrayList<FoodType> filteredFoodTypes;
    private Context ctx;
    private FoodTypeFilter filter;

    public NewItemAdapter(FoodType[] foodTypes, Context context) {
        super();
        this.ctx = context;
        this.foodTypes = new ArrayList<FoodType>(Arrays.asList(foodTypes));
        this.filteredFoodTypes = new ArrayList<FoodType>();
        filter = new FoodTypeFilter();
        filter.filter("");
    }

    public Filter getFilter() { return filter; }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) ((Activity) ctx).getLayoutInflater();
            v = inflater.inflate(R.layout.new_item_food_type, null);
        }

        final FoodType type = filteredFoodTypes.get(position);

        v.setTag(type);

        TextView tv = (TextView) v.findViewById(R.id.new_item_food_type_name);
        tv.setText(type.name);

        // Hook up buttons
        View listButton = v.findViewById(R.id.new_item_list_add);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();

                FoodItem newItem = new FoodItem(type, Constants.STATUS_LIST, type.default_location, -1, type.db);
                newItem.save();

                // We don't create a reminder when we add it to the list
            }
        });

        View addButton = v.findViewById(R.id.new_item_default_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();

                FoodItem newItem = new FoodItem(type, Constants.STATUS_STASH, type.default_location, -1, type.db);
                newItem.save();

                // Create the Reminder
                Reminder reminder = new Reminder(newItem.getId(), cal.getTime(), type.default_reminder, -1, newItem.db);
                reminder.save();
            }
        });

        return v;
    }

    public long getItemId(int position) {
        return position;
    }

    public Object getItem(int position) {
        return filteredFoodTypes.get(position);
    }

    public int getCount() {
        return filteredFoodTypes.size();
    }

    public void addItem(FoodType type) {
        if (!foodTypes.contains(type)) foodTypes.add(type);
        notifyDataSetChanged();
    }

    private class FoodTypeFilter extends Filter {

        public FoodTypeFilter() {

        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = foodTypes;
                results.count = foodTypes.size();
            }
            else {
                ArrayList<FoodType> filteredFoodTypes = new ArrayList<FoodType>();
                for (FoodType type : foodTypes) {
                    if (type.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredFoodTypes.add(type);
                    }
                }
                results.values = filteredFoodTypes;
                results.count = filteredFoodTypes.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredFoodTypes = (ArrayList<FoodType>) results.values;
            notifyDataSetChanged();
        }
    }
}