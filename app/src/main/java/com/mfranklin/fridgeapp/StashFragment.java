package com.mfranklin.fridgeapp;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
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

import com.mfranklin.fridgeapp.adapters.StashAdapter;

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
        stashAdapter = new StashAdapter(getActivity(), stashItems);
        final StashAdapter.FoodItemFilter stashFilter = (StashAdapter.FoodItemFilter) stashAdapter.getFilter();
        stashList.setAdapter(stashAdapter);

        // Hook up Filters
        final View filterRow = toReturn.findViewById(R.id.stash_filter_row);
        final Button fridgeHeader = (Button) toReturn.findViewById(R.id.stash_tab_fridge);
        final Button freezerHeader = (Button) toReturn.findViewById(R.id.stash_tab_freezer);
        final Button pantryHeader = (Button) toReturn.findViewById(R.id.stash_tab_pantry);
        final Button otherHeader = (Button) toReturn.findViewById(R.id.stash_tab_other);
        fridgeHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_FRIDGE));
        freezerHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_FREEZER));
        pantryHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_PANTRY));
        otherHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_NONE));
        View filterExpansionButton = toReturn.findViewById(R.id.stash_filter_expand_button);
        filterExpansionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterRow.getVisibility() == View.VISIBLE) {
                    filterRow.setVisibility(View.GONE);
                    ((Button) v).setText(">");
                    stashFilter.removeLocationFilter(Constants.LOC_FRIDGE);
                    stashFilter.removeLocationFilter(Constants.LOC_FREEZER);
                    stashFilter.removeLocationFilter(Constants.LOC_PANTRY);
                    fridgeHeader.setTypeface(Typeface.DEFAULT);
                    fridgeHeader.setBackgroundColor(Color.TRANSPARENT);
                    freezerHeader.setTypeface(Typeface.DEFAULT);
                    freezerHeader.setBackgroundColor(Color.TRANSPARENT);
                    pantryHeader.setTypeface(Typeface.DEFAULT);
                    pantryHeader.setBackgroundColor(Color.TRANSPARENT);
                    otherHeader.setTypeface(Typeface.DEFAULT);
                    otherHeader.setBackgroundColor(Color.TRANSPARENT);
                }
                else {
                    filterRow.setVisibility(View.VISIBLE);
                    ((Button) v).setText("<");
                }
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

    private class LocationFilterOnClickListener implements View.OnClickListener {
        private int location;
        private boolean on = false;
        private StashAdapter.FoodItemFilter filter;

        public LocationFilterOnClickListener(StashAdapter.FoodItemFilter filter, int location) {
            this.location = location;
            this.filter = filter;
        }

        public void onClick(View v) {
            Button b = (Button) v;
            if (!on) {
                filter.addLocationFilter(location);
                b.setTypeface(Typeface.DEFAULT_BOLD);
                b.setBackgroundColor(Color.LTGRAY);
            }
            else {
                filter.removeLocationFilter(location);
                b.setTypeface(Typeface.DEFAULT);
                b.setBackgroundColor(Color.TRANSPARENT);
            }
            on = !on;
            filter.filter();
        }
    }
}
