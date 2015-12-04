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

        // Hook up Filter row expansion
        final View filterRow = toReturn.findViewById(R.id.stash_filter_row);
        View filterExpansionButton = toReturn.findViewById(R.id.stash_filter_expand_button);
        filterExpansionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filterRow.getVisibility() == View.VISIBLE) {
                    filterRow.setVisibility(View.GONE);
                    ((Button) v).setText(">");
                    stashFilter.removeAllLocationFilters();
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
        fridgeHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fridgeHeader.getTypeface() == Typeface.DEFAULT) { // using typeface as an on/off flag
                    stashFilter.removeAllLocationFilters();
                    stashFilter.addLocationFilter(Constants.LOC_FRIDGE);
                    fridgeHeader.setTypeface(Typeface.DEFAULT_BOLD);
                    freezerHeader.setTypeface(Typeface.DEFAULT);
                    pantryHeader.setTypeface(Typeface.DEFAULT);
                }
                else {
                    Log.d("StashFragment", "removing location filter");
                    stashFilter.removeLocationFilter(Constants.LOC_FRIDGE);
                    fridgeHeader.setTypeface(Typeface.DEFAULT);
                }
                stashFilter.filter("");
            }
        });

        freezerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("StashFragment", "somehow here");
                if (freezerHeader.getTypeface() == Typeface.DEFAULT) {
                    Log.d("StashFragment", "here");
                    stashFilter.removeAllLocationFilters();
                    stashFilter.addLocationFilter(Constants.LOC_FREEZER);
                    freezerHeader.setTypeface(Typeface.DEFAULT_BOLD);
                    fridgeHeader.setTypeface(Typeface.DEFAULT);
                    pantryHeader.setTypeface(Typeface.DEFAULT);
                }
                else {
                    stashFilter.removeLocationFilter(Constants.LOC_FREEZER);
                    freezerHeader.setTypeface(Typeface.DEFAULT);
                }
                stashFilter.filter("");
            }
        });

        pantryHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pantryHeader.getTypeface() == Typeface.DEFAULT) {
                    stashFilter.removeAllLocationFilters();
                    stashFilter.addLocationFilter(Constants.LOC_PANTRY);
                    pantryHeader.setTypeface(Typeface.DEFAULT_BOLD);
                    fridgeHeader.setTypeface(Typeface.DEFAULT);
                    freezerHeader.setTypeface(Typeface.DEFAULT);
                }
                else {
                    stashFilter.removeLocationFilter(Constants.LOC_PANTRY);
                    pantryHeader.setTypeface(Typeface.DEFAULT);
                }
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
}
