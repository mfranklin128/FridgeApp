package com.mfranklin.kitchnik;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;

import com.mfranklin.kitchnik.adapters.NewItemAdapter;
import com.mfranklin.kitchnik.data_model.Constants;
import com.mfranklin.kitchnik.data_model.FoodType;
import com.mfranklin.kitchnik.data_model.FridgeAppDbHelper;

import java.util.ArrayList;
import java.util.HashSet;


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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View toReturn = inflater.inflate(R.layout.fragment_new_item, container, false);



        final FridgeAppDbHelper dbHelper = new FridgeAppDbHelper(getActivity());
        FoodType[] foodTypes = FoodType.getAllFoodTypes(dbHelper.getWritableDatabase());
        if (foodTypes == null) foodTypes = new FoodType[0];
        ArrayList<String> categories = new ArrayList<>();
        for (FoodType type : foodTypes) {
            if (!categories.contains(type.default_category)) categories.add(type.default_category);
        }

        final NewItemAdapter typeAdapter = new NewItemAdapter(foodTypes, getActivity());
        ListView lv = (ListView) toReturn.findViewById(R.id.new_item_fragment_container);
        lv.setAdapter(typeAdapter);

        // Set up new type creation
        final PopupWindow newTypeDialog = new PopupWindow(getActivity());
        final View newTypeView = inflater.inflate(R.layout.create_type_card, null);
        newTypeView.setBackgroundColor(Color.argb(0xf0, 0xb0, 0xb0, 0xb0));
        // Set up new type creation dialog behavior
        final Spinner categorySpinner = (Spinner) newTypeView.findViewById(R.id.new_type_category_vals);
        final Spinner defaultLocationSpinner = (Spinner) newTypeView.findViewById(R.id.new_type_default_location_vals);
        // set up category values
        categorySpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories));
        // set up location values
        defaultLocationSpinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Constants.locationStrings));
        // set up submit button
        View submitButton = newTypeView.findViewById(R.id.new_type_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) newTypeView.findViewById(R.id.new_type_name_val)).getText().toString();
                String category = categorySpinner.getSelectedItem().toString();
                int defaultLocation = Constants.stringToLocation(defaultLocationSpinner.getSelectedItem().toString());
                FoodType newType = new FoodType(name, category, defaultLocation, 1, -1, dbHelper.getWritableDatabase());
                newType.save();
                typeAdapter.addItem(newType);
                typeAdapter.notifyDataSetChanged();
                newTypeDialog.dismiss();
            }
        });
        newTypeDialog.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        newTypeDialog.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        newTypeDialog.setContentView(newTypeView);
        newTypeDialog.setOutsideTouchable(true);
        newTypeDialog.setFocusable(true);

        // Set up filter edit text
        EditText et = (EditText) toReturn.findViewById(R.id.new_item_search_text);
        final NewItemAdapter.FoodTypeFilter typeFilter = (NewItemAdapter.FoodTypeFilter) typeAdapter.getFilter();
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("NewItem", "onTextChanged: " + s);
                typeFilter.setNameFilter("" + s);
                typeFilter.filter("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // set up the category filters
        LinearLayout horizontalScoll = (LinearLayout) toReturn.findViewById(R.id.new_item_category_container);
        final ArrayList<Button> categoryFilterButtons = new ArrayList<>();
        for (final String category : categories) {
            // inflate a new button
            Log.d("NewItemFragment", "inflating: " + category);
            View categoryButtonLayout = inflater.inflate(R.layout.category_filter_view, horizontalScoll, false);
            Button categoryButton = (Button) categoryButtonLayout.findViewById(R.id.category_filter_button);
            categoryButton.setText(category);
            categoryButton.setTypeface(Typeface.DEFAULT);
            categoryFilterButtons.add(categoryButton);
            horizontalScoll.addView(categoryButtonLayout);
        }
        for (final Button categoryButton : categoryFilterButtons) {
            categoryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (categoryButton.getTypeface() == Typeface.DEFAULT) { // button is off
                        typeFilter.setCategoryFilter(categoryButton.getText().toString()); // using buttons to store state?
                        categoryButton.setTypeface(Typeface.DEFAULT_BOLD);
                        categoryButton.setBackgroundColor(Color.LTGRAY);
                        for (Button otherCategoryButton : categoryFilterButtons) {
                            if (otherCategoryButton != categoryButton) {
                                otherCategoryButton.setTypeface(Typeface.DEFAULT);
                                otherCategoryButton.setBackgroundColor(Color.TRANSPARENT);
                            }
                        }
                    }
                    else { // button is already on
                        typeFilter.removeCategoryFilter();
                        categoryButton.setTypeface(Typeface.DEFAULT);
                        categoryButton.setBackgroundColor(Color.TRANSPARENT);
                    }
                    typeFilter.filter();
                }
            });
        }
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
}
