package com.mfranklin.fridgeapp.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.fridgeapp.Constants;
import com.mfranklin.fridgeapp.FoodItem;
import com.mfranklin.fridgeapp.R;

import java.util.ArrayList;
import java.util.Arrays;
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
        private String nameFilter = null;
        private int reminderFilter = 0; // figure this out
        private String categoryFilter = null; // figure this out

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
            return locationFilters.size() == 0 && nameFilter == null;
        }

        // will match any locations; up to the client code to handle AND vs ORing locations
        // e.g., if you only want to match one location, make sure you remove other constraints
        // when you add a new one
        private boolean matchesLocationFilter(FoodItem item) {
            if (locationFilters.size() == 0) return true;
            for (Integer location : locationFilters) if (item.location == location) return true;
            return false;
        }

        private boolean matchesNameFilter(FoodItem item) {
            if (nameFilter == null || nameFilter.length() == 0) return true;
            String[] nameWords = item.type.name.split("\\s+");
            for (String nameWord : nameWords) if (nameWord.toLowerCase().startsWith(nameFilter.toLowerCase())) return true;
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
                    if (matchesLocationFilter(item) && matchesNameFilter(item)) filteredStashList.add(item);
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

        // Set up locations spinner
        Spinner locations = (Spinner) detailCardView.findViewById(R.id.detail_card_location_vals);
        String[] locationVals = {
                Constants.locationFlagToString(Constants.LOC_LIST),
                Constants.locationFlagToString(Constants.LOC_FRIDGE),
                Constants.locationFlagToString(Constants.LOC_FREEZER),
                Constants.locationFlagToString(Constants.LOC_PANTRY)};

        locations.setAdapter(new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, locationVals));
        locations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("FoodItemAdapter", "in onItemSelected");
                int newLocation = Constants.locationStringToFlag((String) parent.getItemAtPosition(position));
                thisFoodItem.location = newLocation;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // get index to start at
        int index = 0;
        for (int i = 0; i < locationVals.length; i++) {
            if (Constants.locationFlagToString(thisFoodItem.location).equals(locationVals[i])) index = i;
        }
        locations.setSelection(index);

        // Set up save() button
        Button saveButton = (Button) detailCardView.findViewById(R.id.detail_card_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisFoodItem.save();

                Toast confirm = Toast.makeText(ctx, "Updated", Toast.LENGTH_SHORT);
                confirm.show();
            }
        });

        return detailCardView;
    }
}
