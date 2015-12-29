package com.mfranklin.kitchnik.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.R;
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

        public void addLocationFilter(int locationFilter) {
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
            String[] nameWords = item.type.name.split("\\s+");
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

    protected static View assignItemDetailCard(final Context ctx, LayoutInflater inflater, final FoodItem thisFoodItem) {
        View detailCardView = inflater.inflate(R.layout.food_item_detail_card, null);
        detailCardView.setBackgroundColor(Color.argb(0xf0, 0xB0, 0xB0, 0xB0));
        // Fill in details
        TextView name = (TextView) detailCardView.findViewById(R.id.detail_card_name_val);
        TextView category = (TextView) detailCardView.findViewById(R.id.detail_card_category_val);
        name.setText(thisFoodItem.type.name);
        category.setText(thisFoodItem.type.category);

        // Set up status spinner and location spinner
        Spinner statuses = (Spinner) detailCardView.findViewById(R.id.detail_card_status_vals);
        final Spinner locations = (Spinner) detailCardView.findViewById(R.id.detail_card_location_vals);
        final Spinner reminderPicker = (Spinner) detailCardView.findViewById(R.id.detail_card_reminder_length);
        String[] statusVals = Constants.statusStrings;
        String[] locationVals = Constants.locationStrings;
        // set adapters
        statuses.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, statusVals));
        locations.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, locationVals));
        // get index to start statuses at
        int index = 0;
        for (int i = 0; i < statusVals.length; i++) {
            if (Constants.statusToString(thisFoodItem.getStatus()).equals(statusVals[i])) index = i;
        }
        statuses.setSelection(index);
        // get index to start locations at
        index = 0;
        for (int i = 0; i < locationVals.length; i++) {
            if (Constants.locationToString(thisFoodItem.getLocation()).equals(locationVals[i])) index = i;
        }
        locations.setSelection(index);
        // Hook up status onSelected
        statuses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newStatus = Constants.stringToStatus((String) parent.getItemAtPosition(position));
                thisFoodItem.setStatus(newStatus);
                // If the item is in the stash, it has a location, potentially
                if (newStatus == Constants.STATUS_STASH) {
                    locations.setVisibility(View.VISIBLE);
                    reminderPicker.setVisibility(View.VISIBLE);
                }
                else {
                    locations.setVisibility(View.INVISIBLE);
                    reminderPicker.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        locations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int newLocation = Constants.stringToLocation((String) parent.getItemAtPosition(position));
                thisFoodItem.setLocation(newLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // Set up reminder text
        Integer[] reminderVals = new Integer[30];
        for (int i = 0; i < reminderVals.length; i++) {
            reminderVals[i] = i+1; // display days 1 - 60
        }
        reminderPicker.setAdapter(new ArrayAdapter<Integer>(ctx, android.R.layout.simple_spinner_item, reminderVals));
        // set up initial value, if any
        Calendar today = Calendar.getInstance();
        final int NUM_MILLIS_PER_DAY = 1000*60*60*24;
        Reminder rem = Reminder.getFoodItemReminder(thisFoodItem.db, thisFoodItem);
        if (rem == null) {
            Log.d("FoodItemAdapter", "didn't find reminder");
            rem = new Reminder(thisFoodItem.db);
            rem.setStartDate(Calendar.getInstance().getTime());
            rem.setDurationDays(thisFoodItem.type.default_reminder);
            rem.setItemId(thisFoodItem.getId());
        }
        Calendar startCal = Calendar.getInstance(); startCal.setTime(rem.getStartDate());
        startCal.add(Calendar.DATE, rem.getDurationDays());
        long endTime = startCal.getTime().getTime();

        long difference = (endTime - today.getTime().getTime()) / (NUM_MILLIS_PER_DAY);
        reminderPicker.setSelection((int) difference); // this isn't great, but whatever, for now. don't worry about long vs int, and subtract 1 to get the index
        // set up onSelected
        final Reminder finalRem = rem;
        reminderPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("FoodItemAdapter", "setting reminder duration: " + (position+1));
                finalRem.setDurationDays(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set up save() button
        Button saveButton = (Button) detailCardView.findViewById(R.id.detail_card_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.save();
                if (thisFoodItem.getStatus() == Constants.STATUS_STASH) {
                    Log.d("FoodItemAdapter", "saving reminder duration: " + finalRem.getDurationDays());
                    finalRem.save();
                }
                else {
                    if (finalRem.getId() != -1) {
                        // the reminder existed, but now the item is going on the list, so we delete it
                        finalRem.delete();
                    }
                }
                Toast confirm = Toast.makeText(ctx, "Updated", Toast.LENGTH_SHORT);
                confirm.show();
            }
        });

        return detailCardView;
    }
}
