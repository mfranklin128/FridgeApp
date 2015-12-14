package com.mfranklin.fridgeapp;

import android.app.Activity;
import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.mfranklin.fridgeapp.adapters.ReminderAdapter;
import com.mfranklin.fridgeapp.adapters.ShoppingListAdapter;
import com.mfranklin.fridgeapp.data_model.FoodItem;
import com.mfranklin.fridgeapp.data_model.FridgeAppDbHelper;
import com.mfranklin.fridgeapp.data_model.Reminder;

/**
 * Created by root on 12/8/15.
 */
public class ReminderFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    ReminderAdapter reminderAdapter;

    // TODO: Rename and change types and number of parameters
    public static ReminderFragment newInstance() {
        ReminderFragment fragment = new ReminderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ReminderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View toReturn = inflater.inflate(R.layout.reminders, container, false);

        // Populate lists (set adapter)
        FridgeAppDbHelper dbHelper = new FridgeAppDbHelper(getActivity());
        Log.d("Shopping List", "about to get shopping list items");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Reminder[] allReminders = Reminder.getAllReminders(db);
        Log.d("Shopping List", "got shopping list items");
        if (allReminders == null)
            allReminders = new Reminder[0]; // ArrayAdapter complains if you give it a null array
        Log.d("Shopping List", "making a new ShoppingListAdapter");
        reminderAdapter = new ReminderAdapter(getActivity(), allReminders);

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
        // TODO: Update argument type and name
        public void onReminderFragmentInteraction();
    }

    // A helper method to set the heights of the reminder sections based on how much room is left for them
    private void setWeights() {

    }

}
