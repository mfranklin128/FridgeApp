package com.mfranklin.fridgeapp;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        FoodItem.FoodItemDbHelper dbHelper = new FoodItem.FoodItemDbHelper(getActivity());
        FoodItem[] stashItems = FoodItem.getStashItems(dbHelper.getWritableDatabase());
        if (stashItems == null) stashItems = new FoodItem[0]; // ArrayAdapter doesn't like null, but 0-len is fine
        stashAdapter = new StashAdapter(getActivity(), stashItems, Constants.LOC_FRIDGE);
        stashList.setAdapter(stashAdapter);

        // Hook up fridge/freezer/pantry filter
        View fridgeHeader = toReturn.findViewById(R.id.stash_tab_fridge);
        fridgeHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("StashFragment", "in fridge header onclick");
                stashAdapter.setLocationFilter(Constants.LOC_FRIDGE);
                stashAdapter.getFilter().filter(Constants.LOC_FRIDGE + "");
                stashAdapter.notifyDataSetChanged();

            }
        });

        View freezerHeader = toReturn.findViewById(R.id.stash_tab_freezer);
        freezerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stashAdapter.setLocationFilter(Constants.LOC_FREEZER);
                stashAdapter.getFilter().filter(Constants.LOC_FREEZER + "");
                stashAdapter.notifyDataSetChanged();
            }
        });

        View pantryHeader = toReturn.findViewById(R.id.stash_tab_pantry);
        pantryHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stashAdapter.setLocationFilter(Constants.LOC_PANTRY);
                stashAdapter.getFilter().filter(Constants.LOC_PANTRY + "");
                stashAdapter.notifyDataSetChanged();
            }
        });

        stashAdapter.getFilter().filter(Constants.LOC_FRIDGE + "");
        return toReturn;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onStashFragmentInteraction();
    }

    private class StashAdapter extends BaseAdapter implements Filterable {

        private final Context ctx;
        private ArrayList<FoodItem> stashItemList;
        private ArrayList<FoodItem> filteredStashList;
        private FoodItemFilter filter;

        public StashAdapter(Context context, FoodItem[] stashItems, int locationFilter) {
            super();
            this.ctx = context;
            stashItemList = new ArrayList<FoodItem>(Arrays.asList(stashItems));
            filteredStashList = new ArrayList<FoodItem>(Arrays.asList(stashItems));
            filter = new FoodItemFilter(locationFilter);
            filter.filter(locationFilter+"");
        }

        public Filter getFilter() { return filter; }

        public void setLocationFilter(int locationConstraint) { filter = new FoodItemFilter(locationConstraint);}

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

        private class FoodItemFilter extends Filter {

            private int location;

            public FoodItemFilter(int location) {
                this.location = location;
            }

            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                Log.d("StashFragment", "in performFiltering");
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    results.values = stashItemList;
                    results.count = stashItemList.size();
                }
                else {
                    int filterLocation = Integer.parseInt(constraint.toString());
                    Log.d("StashFragment", "location = " + filterLocation);
                    ArrayList<FoodItem> filteredStashList = new ArrayList<FoodItem>();
                    for (FoodItem item : stashItemList) {
                        if (item.location == filterLocation) filteredStashList.add(item);
                    }
                    results.values = filteredStashList;
                    results.count = filteredStashList.size();
                }

                return results;
            }

            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d("StashFragment", "in publishResults");
                if (results.count == 0) notifyDataSetInvalidated();
                else {
                    Log.d("StashFragment", "non-zero count");
                    filteredStashList = (ArrayList) results.values;
                    notifyDataSetChanged();
                }
            }
        }
    }

}
