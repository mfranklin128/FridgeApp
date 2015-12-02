package com.mfranklin.fridgeapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewItemFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewItemFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    private ArrayList<FoodType> shoppingCart = new ArrayList<FoodType>();

    /**
     */
    // TODO: Rename and change types and number of parameters
    public static NewItemFragment newInstance() {
        NewItemFragment fragment = new NewItemFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NewItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View toReturn = inflater.inflate(R.layout.fragment_new_item, container, false);

        FridgeAppDbHelper dbHelper = new FridgeAppDbHelper(getActivity());
        FoodType[] foodTypes = FoodType.getAllFoodTypes(dbHelper.getWritableDatabase());
        if (foodTypes == null) foodTypes = new FoodType[0];

        final NewItemAdapter itemArrayAdapter = new NewItemAdapter(foodTypes, getActivity());
        ListView lv = (ListView) toReturn.findViewById(R.id.new_item_fragment_container);
        lv.setAdapter(itemArrayAdapter);

        // Set up filter edit text
        EditText et = (EditText) toReturn.findViewById(R.id.new_item_search_text);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("NewItem", "onTextChanged: " + s);
                itemArrayAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return toReturn;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     */
    public interface OnFragmentInteractionListener {
        public void onNewItemFragmentInteraction();
    }

    private class NewItemAdapter extends BaseAdapter implements Filterable {
        private ArrayList<FoodType> foodTypes;
        private ArrayList<FoodType> filteredFoodTypes;
        private Context ctx;
        private FoodTypeFilter filter;

        public NewItemAdapter(FoodType[] foodTypes, Context context) {
            super();
            this.ctx = context;
            this.foodTypes = new ArrayList<FoodType>(Arrays.asList(foodTypes));
            this.filteredFoodTypes = new ArrayList<FoodType>();
            filter = new FoodTypeFilter();
            filter.filter("");
        }

        public Filter getFilter() { return filter; }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) ((Activity) ctx).getLayoutInflater();
                v = inflater.inflate(R.layout.new_item_food_type, null);
            }

            final FoodType type = filteredFoodTypes.get(position);

            v.setTag(type);

            TextView tv = (TextView) v.findViewById(R.id.new_item_food_type_name);
            tv.setText(type.name);

            // Hook up buttons
            View listButton = v.findViewById(R.id.new_item_list_add);
            listButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar cal = Calendar.getInstance(); cal.add(Calendar.DATE, type.default_reminder);
                    Date reminder = cal.getTime();

                    FoodItem newItem = new FoodItem(type, reminder, Constants.LOC_LIST, -1, type.db);
                    newItem.save();
                }
            });

            View addButton = v.findViewById(R.id.new_item_default_add);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar cal = Calendar.getInstance(); cal.add(Calendar.DATE, type.default_reminder);
                    Date reminder = cal.getTime();

                    FoodItem newItem = new FoodItem(type, reminder, type.default_location, -1, type.db);
                    newItem.save();
                }
            });

            //Constants.setColor(type.category, v);

            return v;
        }

        public long getItemId(int position) {
            return position;
        }

        public Object getItem(int position) {
            return filteredFoodTypes.get(position);
        }

        public int getCount() {
            return filteredFoodTypes.size();
        }

        private class FoodTypeFilter extends Filter {
            private String filter;

            public FoodTypeFilter() {

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    results.values = foodTypes;
                    results.count = foodTypes.size();
                }
                else {
                    ArrayList<FoodType> filteredFoodTypes = new ArrayList<FoodType>();
                    for (FoodType type : foodTypes) {
                        if (type.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredFoodTypes.add(type);
                        }
                    }
                    results.values = filteredFoodTypes;
                    results.count = filteredFoodTypes.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredFoodTypes = (ArrayList<FoodType>) results.values;
                notifyDataSetChanged();
            }
        }
    }
}
