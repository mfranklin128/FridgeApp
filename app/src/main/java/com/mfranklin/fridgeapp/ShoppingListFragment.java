package com.mfranklin.fridgeapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.Resources;
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
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mfranklin.fridgeapp.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


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
        FoodItem.FoodItemDbHelper dbHelper = new FoodItem.FoodItemDbHelper(getActivity());
        Log.d("Shopping List", "about to get shopping list items");
        FoodItem[] listItems = FoodItem.getShoppingListItems(dbHelper.getWritableDatabase());
        Log.d("Shopping List", "got shopping list items");
        if (listItems == null) listItems = new FoodItem[0]; // ArrayAdapter complains if you give it a null array
        Log.d("Shopping List", "making a new ShoppingListAdapter");
        shoppingListAdapter = new ShoppingListAdapter(getActivity(), listItems);
        shoppingList.setAdapter(shoppingListAdapter);

        // Set listeners for destinations
        View listDest = toReturn.findViewById(R.id.dest_list);
        listDest.setOnDragListener(new ShoppingListDestinationDragListener(Constants.DEST_LIST, shoppingListAdapter));
        ImageView listDestIcon = (ImageView) listDest.findViewById(R.id.dest_list_icon);
        listDestIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFragmentManager.displayShoppingListFragment(getActivity(), false);
            }
        });

        View fridgeDest = toReturn.findViewById(R.id.dest_fridge);
        fridgeDest.setOnDragListener(new ShoppingListDestinationDragListener(Constants.DEST_FRIDGE, shoppingListAdapter));
        ImageView fridgeDestIcon = (ImageView) fridgeDest.findViewById(R.id.dest_fridge_icon);
        fridgeDestIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFragmentManager.displayStashFragment(getActivity(), false);
            }
        });

        View freezerDest = toReturn.findViewById(R.id.dest_freezer);
        freezerDest.setOnDragListener(new ShoppingListDestinationDragListener(Constants.DEST_FREEZER, shoppingListAdapter));
        ImageView freezerDestIcon = (ImageView) freezerDest.findViewById(R.id.dest_freezer_icon);
        freezerDestIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFragmentManager.displayStashFragment(getActivity(), false);
            }
        });

        View pantryDest = toReturn.findViewById(R.id.dest_pantry);
        pantryDest.setOnDragListener(new ShoppingListDestinationDragListener(Constants.DEST_PANTRY, shoppingListAdapter));
        ImageView pantryDestIcon = (ImageView) pantryDest.findViewById(R.id.dest_pantry_icon);
        pantryDestIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyFragmentManager.displayStashFragment(getActivity(), false);
            }
        });

        // Bold the text for List
        TextView label = (TextView) listDest.findViewById(R.id.dest_list_text);
        label.setTypeface(null, Typeface.BOLD);
        listDest.setBackgroundColor(Color.argb(0x80, 0x80, 0x80, 0x80));

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


    private class ShoppingListAdapter extends ArrayAdapter<FoodItem> {

        private final Context ctx;
        private ArrayList<FoodItem> foodItemList;

        public ShoppingListAdapter(Context context, FoodItem[] foodItems) {
            super(context, -1, new ArrayList<FoodItem>(Arrays.asList(foodItems)));
            this.ctx = context;
            foodItemList = new ArrayList<FoodItem>(Arrays.asList(foodItems));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewGroup finalParent = parent;
            final FoodItem thisFoodItem = foodItemList.get(position);
            final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = convertView;
            if (rowView == null) {
               rowView = inflater.inflate(R.layout.shopping_list_item, parent, false);
            }

            rowView.setTag(thisFoodItem);

            ShoppingListItemDragListener listener = new ShoppingListItemDragListener(thisFoodItem);
            rowView.setOnDragListener(listener);

            rowView.setTag(thisFoodItem);

            rowView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    String name = ((FoodItem) view.getTag()).type.name;
                    Log.d("Shopping List", "in OnLongClick");
                    ClipData.Item item = new ClipData.Item(name);

                    ClipData dragData = new ClipData(
                            name,
                            new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN},
                            item);

                    ImageView iv = (ImageView) view.findViewById(R.id.add_icon);
                    iv.startDrag(
                            dragData,
                            new View.DragShadowBuilder(iv),
                            thisFoodItem,
                            0);
                    return true;
                }
            });

            TextView tv = (TextView) rowView.findViewById(R.id.shopping_list_item_name);
            tv.setText(thisFoodItem.type.name);

            // TODO: hook up the checkbox for multiple items

            return rowView;
        }

        public void remove(FoodItem item) {
            super.remove(item);
            foodItemList.remove(item);
        }

        public int getCount() {
            return foodItemList.size();
        }
    }

    private class ShoppingListItemDragListener implements View.OnDragListener {
        private FoodItem item;

        public ShoppingListItemDragListener(FoodItem item) {
            this.item = item;
        }

        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
            }
            return false;
        }
    }

    private class ShoppingListDestinationDragListener implements View.OnDragListener {
        private final int type;
        private final ShoppingListAdapter adapter;
        private int originalColor;

        public ShoppingListDestinationDragListener(int type, ShoppingListAdapter slAdapter) {
            this.type = type;
            adapter = slAdapter;
            if (this.type == Constants.DEST_LIST) {
                originalColor = Color.argb(0x80, 0x80, 0x80, 0x80);
            }
            else {
                originalColor = Color.WHITE;
            }
        }

        public boolean onDrag(View v, DragEvent event) {
            switch(event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    View dest = v;
                    dest.setBackgroundColor(Color.argb(0x80, 0xcc, 0xff, 0xcc));
                    dest.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(originalColor);
                    return true;
                case DragEvent.ACTION_DROP:
                    FoodItem item = (FoodItem) event.getLocalState();
                    int location = Constants.LOC_FRIDGE;
                    switch (this.type) {
                        case Constants.DEST_FRIDGE:
                            location = Constants.LOC_FRIDGE;
                            break;
                        case Constants.DEST_FREEZER:
                            location = Constants.LOC_FREEZER;
                            break;
                        case Constants.DEST_LIST:
                            location = Constants.LOC_LIST;
                            break;
                        case Constants.DEST_PANTRY:
                            location = Constants.LOC_PANTRY;
                            break;
                    }
                    item.location = location;
                    item.save();
                    if (this.type != Constants.DEST_LIST) {
                        adapter.remove(item);
                        adapter.notifyDataSetChanged();
                    }
                    v.setBackgroundColor(originalColor);
                    return true;
            }
            return true;
        }
    }
}
