package com.mfranklin.fridgeapp.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Filter;

import com.mfranklin.fridgeapp.FoodItem;

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
}
