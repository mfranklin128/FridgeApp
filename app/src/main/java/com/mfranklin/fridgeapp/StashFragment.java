package com.mfranklin.fridgeapp;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

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
        stashAdapter = new StashAdapter(getActivity(), stashItems);
        stashList.setAdapter(stashAdapter);

        return toReturn;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onStashFragmentInteraction();
    }

    private class StashAdapter extends ArrayAdapter<FoodItem> {

        private final Context ctx;
        private ArrayList<FoodItem> stashItemList;

        public StashAdapter(Context context, FoodItem[] stashItems) {
            super(context, -1, stashItems);
            this.ctx = context;
            stashItemList = new ArrayList<FoodItem>(Arrays.asList(stashItems));
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final FoodItem thisFoodItem = stashItemList.get(position);
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
                    notifyDataSetChanged();
                }
            });
            return rowView;
        }

        public int getCount() {
            return stashItemList.size();
        }
    }

}
