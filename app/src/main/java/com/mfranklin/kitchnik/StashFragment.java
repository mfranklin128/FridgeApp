package com.mfranklin.kitchnik;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mfranklin.kitchnik.adapters.StashAdapter;
import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.data_model.FridgeAppDbHelper;

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
        fridgeHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_FRIDGE, new Button[] {freezerHeader, pantryHeader, otherHeader}));
        freezerHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_FREEZER, new Button[] {fridgeHeader, pantryHeader, otherHeader}));
        pantryHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_PANTRY, new Button[] {fridgeHeader, freezerHeader, otherHeader}));
        otherHeader.setOnClickListener(new LocationFilterOnClickListener(stashFilter, Constants.LOC_NONE, new Button[] {fridgeHeader, freezerHeader, pantryHeader}));

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
        private StashAdapter.FoodItemFilter filter;
        private Button[] otherHeaders;

        public LocationFilterOnClickListener(StashAdapter.FoodItemFilter filter, int location, Button[] otherHeaders) {
            this.location = location;
            this.filter = filter;
            this.otherHeaders = otherHeaders;
        }

        public void onClick(View v) {
            Button b = (Button) v;
            if (!(b.getTypeface() == Typeface.DEFAULT_BOLD)) {
                filter.setLocationFilter(location);
                b.setTypeface(Typeface.DEFAULT_BOLD);
                b.setBackgroundColor(Color.LTGRAY);
            }
            else {
                filter.removeLocationFilter(location);
                b.setTypeface(Typeface.DEFAULT);
                b.setBackgroundColor(Color.TRANSPARENT);
            }
            for (Button header : otherHeaders) {
                header.setTypeface(Typeface.DEFAULT);
                header.setBackgroundColor(Color.TRANSPARENT);
            }
            filter.filter();
        }
    }
}
