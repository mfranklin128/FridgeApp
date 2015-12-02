package com.mfranklin.fridgeapp;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class StashFragment extends Fragment {

    private OnFragmentInteractionListener listener;
    private ListView stashList;
    private StashAdapter stashAdapter;

    public StashFragment() {
        // Required empty public constructor
    }

    public static StashFragment newInstance() {
        StashFragment fragment = new StashFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_stash, container, false);

        // populate the list
        stashList = (ListView) toReturn.findViewById(R.id.stash_items);
        FridgeAppDbHelper dbHelper = new FridgeAppDbHelper(getActivity());
        FoodItem[] stashItems = FoodItem.getStashItems(dbHelper.getWritableDatabase());
        if (stashItems == null) stashItems = new FoodItem[0]; // ArrayAdapter doesn't like null, but 0-len is fine
        stashAdapter = new StashAdapter(getActivity(), stashItems, Constants.LOC_FRIDGE);
        stashList.setAdapter(stashAdapter);

        // Hook up Filter row expansion
        final View filterRow = toReturn.findViewById(R.id.stash_filter_row);
        View filterExpansionButton = toReturn.findViewById(R.id.stash_filter_expand_button);
        filterExpansionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterRow.getVisibility() == View.VISIBLE) {
                    filterRow.setVisibility(View.GONE);
                    ((Button) v).setText(">");
                }
                else {
                    filterRow.setVisibility(View.VISIBLE);
                    ((Button) v).setText("<");
                }
            }
        });
        // Hook up fridge/freezer/pantry filter
        final TextView fridgeHeader = (TextView) toReturn.findViewById(R.id.stash_tab_fridge);
        final TextView freezerHeader = (TextView) toReturn.findViewById(R.id.stash_tab_freezer);
        final TextView pantryHeader = (TextView) toReturn.findViewById(R.id.stash_tab_pantry);
        final StashAdapter.FoodItemFilter stashFilter = (StashAdapter.FoodItemFilter) stashAdapter.getFilter();
        fridgeHeader.setOnClickListener(new View.OnClickListener() {
            private boolean on = false;
            @Override
            public void onClick(View v) {
                if (!on) {
                    stashFilter.addLocationFilter(Constants.LOC_FRIDGE);
                    fridgeHeader.setTypeface(Typeface.DEFAULT_BOLD);
                }
                else {
                    Log.d("StashFragment", "removing location filter");
                    stashFilter.removeLocationFilter(Constants.LOC_FRIDGE);
                    fridgeHeader.setTypeface(Typeface.DEFAULT);
                }
                on = !on;
                stashFilter.filter("");
            }
        });

        freezerHeader.setOnClickListener(new View.OnClickListener() {
            private boolean on = false;
            @Override
            public void onClick(View v) {
                if (!on) {
                    stashFilter.addLocationFilter(Constants.LOC_FREEZER);
                    freezerHeader.setTypeface(Typeface.DEFAULT_BOLD);
                }
                else {
                    stashFilter.removeLocationFilter(Constants.LOC_FREEZER);
                    freezerHeader.setTypeface(Typeface.DEFAULT);
                }
                on = !on;
                stashFilter.filter("");
            }
        });

        pantryHeader.setOnClickListener(new View.OnClickListener() {
            private boolean on = false;
            @Override
            public void onClick(View v) {
                if (!on) {
                    stashFilter.addLocationFilter(Constants.LOC_PANTRY);
                    pantryHeader.setTypeface(Typeface.DEFAULT_BOLD);
                }
                else {
                    stashFilter.removeLocationFilter(Constants.LOC_PANTRY);
                    pantryHeader.setTypeface(Typeface.DEFAULT);
                }
                on = !on;
                stashFilter.filter("");
            }
        });

        // Set up filter edit text
        EditText et = (EditText) toReturn.findViewById(R.id.stash_search_text);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stashFilter.setNameFilter(s.toString());
                stashFilter.filter();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return toReturn;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onStashFragmentInteraction();
    }

    private class StashAdapter extends BaseAdapter {

        private final Context ctx;
        private ArrayList<FoodItem> stashItemList;
        private ArrayList<FoodItem> filteredStashList;
        private FoodItemFilter filter;

        public StashAdapter(Context context, FoodItem[] stashItems, int locationFilter) {
            super();
            this.ctx = context;
            stashItemList = new ArrayList<FoodItem>(Arrays.asList(stashItems));
            filteredStashList = new ArrayList<FoodItem>(Arrays.asList(stashItems));
            this.filter = new FoodItemFilter();
            filter.filter();
        }

        public Filter getFilter() { return filter; }

        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final FoodItem thisFoodItem = filteredStashList.get(position);
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
                    stashItemList.remove(thisFoodItem);
                    filteredStashList.remove(thisFoodItem);
                    notifyDataSetChanged();
                }
            });
            return rowView;
        }

        public int getCount() {
            return filteredStashList.size();
        }
        public Object getItem(int position) { return filteredStashList.get(position);}
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

            private boolean matchesLocationFilter(FoodItem item) {
                Log.d("StashFragment", "checking matchesLocationFilter: " + item.location);
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
                Log.d("StashFragment", "in performFiltering");
                FilterResults results = new FilterResults();

                if (hasNoConstraints()) {
                    results.values = stashItemList;
                    results.count = stashItemList.size();
                }
                else {
                    // add it if it matches both name and location filters
                    ArrayList<FoodItem> filteredStashList = new ArrayList<FoodItem>();
                    for (FoodItem item : stashItemList) {
                        if (matchesLocationFilter(item) && matchesNameFilter(item)) filteredStashList.add(item);
                    }
                    results.values = filteredStashList;
                    results.count = filteredStashList.size();
                }

                return results;
            }

            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d("StashFragment", "in publishResults");
                if (results.count == 0) {
                    filteredStashList = (ArrayList) results.values;
                    notifyDataSetChanged();
                }
                else {
                    Log.d("StashFragment", "non-zero count");
                    filteredStashList = (ArrayList) results.values;
                    notifyDataSetChanged();
                }
            }
        }
    }

}
