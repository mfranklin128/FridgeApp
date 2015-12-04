package com.mfranklin.fridgeapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mfranklin.fridgeapp.*;
import com.mfranklin.fridgeapp.adapters.ShoppingListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


/**

 */
public class ShoppingListFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    ShoppingListAdapter shoppingListAdapter;
    ListView shoppingList;

    // TODO: Rename and change types and number of parameters
    public static ShoppingListFragment newInstance() {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ShoppingListFragment() {
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
        View toReturn = inflater.inflate(R.layout.fragment_shopping_list, container, false);

        // Hook up new item button
        Button newItemButton = (Button) toReturn.findViewById(R.id.shopping_list_new_item);
        newItemButton.setOnClickListener(this);

        // Populate list (set adapter)
        shoppingList = (ListView) toReturn.findViewById(R.id.shopping_list_items);
        FridgeAppDbHelper dbHelper = new FridgeAppDbHelper(getActivity());
        Log.d("Shopping List", "about to get shopping list items");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        FoodItem[] listItems = FoodItem.getShoppingListItems(db);
        Log.d("Shopping List", "got shopping list items");
        if (listItems == null) listItems = new FoodItem[0]; // ArrayAdapter complains if you give it a null array
        Log.d("Shopping List", "making a new ShoppingListAdapter");
        shoppingListAdapter = new ShoppingListAdapter(getActivity(), listItems);
        shoppingList.setAdapter(shoppingListAdapter);

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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shopping_list_new_item:
                launchNewItems(v);
                break;
        }
    }

    public void launchNewItems(View v) {
        MyFragmentManager.displayNewItemFragment(getActivity(), true);
    }

    /**
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onShoppingListFragmentInteraction();
    }

}
