package com.mfranklin.kitchnik.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.R;
import com.mfranklin.kitchnik.data_model.FoodType;
import com.mfranklin.kitchnik.data_model.Reminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by root on 12/3/15.
 */
abstract class FoodItemAdapter extends BaseAdapter {
    protected final Context ctx;
    protected ArrayList<FoodItem> itemList;
    protected ArrayList<FoodItem> filteredItemList;
    protected FoodItemFilter filter;

    public FoodItemAdapter(Context context, FoodItem[] itemArray) {
        super();
        ctx = context;
        itemList = new ArrayList<FoodItem>(Arrays.asList(itemArray));
        filteredItemList = new ArrayList<FoodItem>(Arrays.asList(itemArray));
        filter = new FoodItemFilter();
    }

    public Filter getFilter() { return filter; }

    public int getCount() {
        return filteredItemList.size();
    }
    public Object getItem(int position) { return filteredItemList.get(position);}
    public long getItemId(int position) { return position;}

    public class FoodItemFilter extends Filter {

        private Set<Integer> locationFilters = new HashSet<Integer>();
        private Set<Integer> statusFilters = new HashSet<Integer>();
        private String nameFilter = null;
        private int reminderFilter = 0; // figure this out
        private String categoryFilter = null; // figure this out

        public void addStatusFilter(int statusFilter) {
            statusFilters.add(statusFilter);
        }

        public void removeStatusFilter(int statusFilter) { statusFilters.remove(statusFilter); }

        public void setLocationFilter(int locationFilter) {
            locationFilters.clear();
            locationFilters.add(locationFilter);
        }

        public void removeLocationFilter(int locationFilter) {
            locationFilters.remove(locationFilter);
        }

        public void setNameFilter(String nameFilter) {
            this.nameFilter = nameFilter;
        }

        private boolean hasNoConstraints() {
            return locationFilters.size() == 0 && nameFilter == null && statusFilters.size() == 0;
        }

        // will match any locations; up to the client code to handle AND vs ORing locations
        // e.g., if you only want to match one location, make sure you remove other constraints
        // when you add a new one
        private boolean matchesLocationFilter(FoodItem item) {
            if (locationFilters.size() == 0) return true;
            for (Integer location : locationFilters) if (item.getLocation() == location) return true;
            return false;
        }

        private boolean matchesNameFilter(FoodItem item) {
            if (nameFilter == null || nameFilter.length() == 0) return true;
            String[] nameWords = item.getName().split("\\s+");
            for (String nameWord : nameWords) if (nameWord.toLowerCase().startsWith(nameFilter.toLowerCase())) return true;
            return false;
        }

        private boolean matchesStatusFilter(FoodItem item) {
            if (statusFilters.size() == 0) return true;
            for (Integer status : statusFilters) if (item.getStatus() == status) return true;
            return false;
        }

        public void filter() {
            filter("");
        }
        //Instead of using a constraint passed as an argument, we separate the filtering
        // (update the view) from the constraining (searching the data)
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (hasNoConstraints()) {
                results.values = itemList;
                results.count = itemList.size();
            }
            else {
                // add it if it matches both name and location filters
                ArrayList<FoodItem> filteredStashList = new ArrayList<FoodItem>();
                for (FoodItem item : itemList) {
                    if (matchesLocationFilter(item) && matchesNameFilter(item) && matchesStatusFilter(item)) {
                        filteredStashList.add(item);
                    }
                }
                results.values = filteredStashList;
                results.count = filteredStashList.size();
            }

            return results;
        }

        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count == 0) {
                filteredItemList = (ArrayList) results.values;
                notifyDataSetChanged();
            }
            else {
                filteredItemList = (ArrayList) results.values;
                notifyDataSetChanged();
            }
        }
    }

    protected static View assignItemDetailCard(final Context ctx, LayoutInflater inflater, final FoodItem thisFoodItem, final String[] categories) {
        View detailCardView = inflater.inflate(R.layout.food_item_detail_card, null);
        // Fill in details
        TextView name = (TextView) detailCardView.findViewById(R.id.detail_card_name_val);
        name.setText(thisFoodItem.getName());

        // Set up location buttons
        final TextView fridgeLoc = (TextView) detailCardView.findViewById(R.id.detail_card_location_fridge);
        final TextView freezerLoc = (TextView) detailCardView.findViewById(R.id.detail_card_location_freezer);
        final TextView pantryLoc = (TextView) detailCardView.findViewById(R.id.detail_card_location_pantry);
        final TextView otherLoc = (TextView) detailCardView.findViewById(R.id.detail_card_location_other);
        switch (thisFoodItem.getLocation()) {
            case Constants.LOC_FRIDGE:
                fridgeLoc.setTypeface(Typeface.DEFAULT_BOLD);
                break;
            case Constants.LOC_FREEZER:
                freezerLoc.setTypeface(Typeface.DEFAULT_BOLD);
                break;
            case Constants.LOC_PANTRY:
                pantryLoc.setTypeface(Typeface.DEFAULT_BOLD);
                break;
            default:
                otherLoc.setTypeface(Typeface.DEFAULT_BOLD);
        }
        fridgeLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fridgeLoc.setTypeface(Typeface.DEFAULT_BOLD);
                freezerLoc.setTypeface(Typeface.DEFAULT);
                pantryLoc.setTypeface(Typeface.DEFAULT);
                otherLoc.setTypeface(Typeface.DEFAULT);
                thisFoodItem.setLocation(Constants.LOC_FRIDGE);
            }
        });
        freezerLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fridgeLoc.setTypeface(Typeface.DEFAULT);
                freezerLoc.setTypeface(Typeface.DEFAULT_BOLD);
                pantryLoc.setTypeface(Typeface.DEFAULT);
                otherLoc.setTypeface(Typeface.DEFAULT);
                thisFoodItem.setLocation(Constants.LOC_FREEZER);
            }
        });
        pantryLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fridgeLoc.setTypeface(Typeface.DEFAULT);
                freezerLoc.setTypeface(Typeface.DEFAULT);
                pantryLoc.setTypeface(Typeface.DEFAULT_BOLD);
                otherLoc.setTypeface(Typeface.DEFAULT);
                thisFoodItem.setLocation(Constants.LOC_PANTRY);
            }
        });
        otherLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fridgeLoc.setTypeface(Typeface.DEFAULT);
                freezerLoc.setTypeface(Typeface.DEFAULT);
                pantryLoc.setTypeface(Typeface.DEFAULT);
                otherLoc.setTypeface(Typeface.DEFAULT_BOLD);
                thisFoodItem.setLocation(Constants.LOC_NONE);
            }
        });


        // Set up category spinner
        Spinner categorySpinner = (Spinner) detailCardView.findViewById(R.id.detail_card_category_text);
        categorySpinner.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, categories));
        for (int i = 0; i < categories.length; i++) {
            if (thisFoodItem.getCategory().equals(categories[i])) categorySpinner.setSelection(i);
        }
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                thisFoodItem.setCategory(categories[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set up reminder row
        Reminder reminder = Reminder.getFoodItemReminder(thisFoodItem.db, thisFoodItem);
        TextView reminderDays = (TextView) detailCardView.findViewById(R.id.detail_card_reminder_text);
        reminderDays.setText((int) reminder.getDaysRemaining()+"");

        // Set up save button
        Button saveButton = (Button) detailCardView.findViewById(R.id.detail_card_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.save();
            }
        });
        return detailCardView;
    }
}
