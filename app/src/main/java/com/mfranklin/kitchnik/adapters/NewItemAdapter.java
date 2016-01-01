package com.mfranklin.kitchnik.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.data_model.FoodType;
import com.mfranklin.kitchnik.R;
import com.mfranklin.kitchnik.data_model.Reminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by root on 12/4/15.
 */
public class NewItemAdapter extends BaseAdapter implements Filterable {
    private ArrayList<FoodType> foodTypes;
    private ArrayList<FoodType> filteredFoodTypes;
    private Context ctx;
    private FoodTypeFilter filter;
    private HashMap<FoodType, ArrayList<FoodItem>> typeToListItems = new HashMap<>();
    private HashMap<FoodType, ArrayList<FoodItem>> typeToStashItems = new HashMap<>();

    public NewItemAdapter(FoodType[] foodTypes, Context context) {
        super();
        this.ctx = context;
        this.foodTypes = new ArrayList<FoodType>(Arrays.asList(foodTypes));
        this.filteredFoodTypes = new ArrayList<FoodType>();
        filter = new FoodTypeFilter();
        filter.filter("");

        // set up food type to items mapping
        for (FoodType type : foodTypes) {
            FoodItem[] items = FoodItem.getFoodItemsForType(type.db, type);
            if (items == null) {
                typeToListItems.put(type, new ArrayList<FoodItem>());
                typeToStashItems.put(type, new ArrayList<FoodItem>());
            }
            else {
                ArrayList<FoodItem> listItems = new ArrayList<>();
                ArrayList<FoodItem> stashItems = new ArrayList<>();
                for (FoodItem item : items) {
                    if (item.getStatus() == Constants.STATUS_STASH) stashItems.add(item);
                    else if (item.getStatus() == Constants.STATUS_LIST) listItems.add(item);
                }
                typeToListItems.put(type, listItems);
                typeToStashItems.put(type, stashItems);
            }

        }
    }


    public Filter getFilter() { return filter; }

    public View getView(int position, View convertView, final ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) ((Activity) ctx).getLayoutInflater();
            v = inflater.inflate(R.layout.new_item_food_type, null);
        }

        final FoodType type = filteredFoodTypes.get(position);

        v.setTag(type);

        // Hook up buttons
        ImageButton listButton = (ImageButton) v.findViewById(R.id.new_item_list_add);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();

                final FoodItem newItem = new FoodItem(type, Constants.STATUS_LIST, type.default_location, -1, type.db);
                newItem.save();

                String message = "Added " + newItem.type.name + " to shopping list";
                Snackbar.make(parent, message, Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // undo
                        newItem.delete();
                        ArrayList<FoodItem> stashItems = typeToListItems.get(type);
                        stashItems.remove(newItem);
                        typeToListItems.put(type, stashItems);
                        notifyDataSetChanged();
                    }
                }).show();

                ArrayList<FoodItem> listItems = typeToListItems.get(type);
                listItems.add(newItem);
                typeToListItems.put(type, listItems);
                // We don't create a reminder when we add it to the list
                notifyDataSetChanged();
            }
        });
        ImageButton addButton = (ImageButton) v.findViewById(R.id.new_item_default_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();

                final FoodItem newItem = new FoodItem(type, Constants.STATUS_STASH, type.default_location, -1, type.db);
                newItem.save();

                String message = "Added " + newItem.type.name + " to " + Constants.locationToString(newItem.getLocation());
                Snackbar.make(parent, message, Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { // undo
                        newItem.delete();
                        ArrayList<FoodItem> stashItems = typeToStashItems.get(type);
                        stashItems.remove(newItem);
                        typeToStashItems.put(type, stashItems);
                        notifyDataSetChanged();
                    }
                }).show();
                // Create the Reminder
                Reminder reminder = new Reminder(newItem.getId(), cal.getTime(), type.default_reminder, -1, newItem.db);
                reminder.save();

                ArrayList<FoodItem> stashItems = typeToStashItems.get(type);
                stashItems.add(newItem);
                typeToStashItems.put(type, stashItems);
                notifyDataSetChanged();
            }
        });

        // some stuff with numbers?
        TextView listCountView = (TextView) v.findViewById(R.id.new_item_add_list_counter);
        int listCount = typeToListItems.get(type).size();
        if (listCount > 0) {
            listCountView.setText("" + listCount);
        }
        else {
            listCountView.setText(" ");
        }
        TextView stashCountView = (TextView) v.findViewById(R.id.new_item_add_stash_counter);
        int stashCount = typeToStashItems.get(type).size();
        if (stashCount > 0) {
            stashCountView.setText("" + stashCount);
        }
        else {
            stashCountView.setText(" ");
        }
        // Fill in details
        TextView name = (TextView) v.findViewById(R.id.new_item_food_type_name);
        name.setText(type.name);
        TextView category = (TextView) v.findViewById(R.id.new_item_food_type_category);
        category.setText(type.category);
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

    public class FoodTypeFilter extends Filter {

        private String nameFilter = null;
        private String categoryFilter = null;
        public FoodTypeFilter() {

        }

        public void filter() {
            filter("");
        }

        public void setNameFilter(String filter) {
            nameFilter = filter;
        }

        public void removeNameFilter() {
            nameFilter = null;
        }

        public void setCategoryFilter(String category) {
            categoryFilter = category;
        }

        public void removeCategoryFilter() {
            categoryFilter = null;
        }

        private boolean hasNoConstraints() {
            return (nameFilter == null && categoryFilter == null);
        }

        private boolean matchesNameFilter(FoodType type) {
            if (nameFilter == null || nameFilter.length() == 0) return true;
            String[] words = type.name.split("\\s+");
            for (String word : words) if (word.toLowerCase().startsWith(nameFilter.toLowerCase())) return true;
            return false;
        }

        private boolean matchesCategoryFilter(FoodType type) {
            if (categoryFilter == null || categoryFilter.length() == 0) return true;
            return (type.category.toLowerCase().equals(categoryFilter.toLowerCase()));
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (hasNoConstraints()) {
                results.values = foodTypes;
                results.count = foodTypes.size();
            }
            else {
                Log.d("NewItemAdapter", "filtering");
                ArrayList<FoodType> filteredFoodTypes = new ArrayList<FoodType>();
                for (FoodType type : foodTypes) {
                    if (matchesNameFilter(type) && matchesCategoryFilter(type)) {
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