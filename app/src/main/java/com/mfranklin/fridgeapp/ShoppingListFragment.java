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
        shoppingListAdapter = new ShoppingListAdapter(getActivity(), listItems, container);
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


    private class ShoppingListAdapter extends ArrayAdapter<FoodItem> {

        private final Context ctx;
        private ArrayList<FoodItem> foodItemList;
        private ViewGroup topLevelViewGroup;

        public ShoppingListAdapter(Context context, FoodItem[] foodItems, ViewGroup topLevelViewGroup) {
            super(context, -1, new ArrayList<FoodItem>(Arrays.asList(foodItems)));
            this.ctx = context;
            foodItemList = new ArrayList<FoodItem>(Arrays.asList(foodItems));
            this.topLevelViewGroup = topLevelViewGroup;
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

            TextView tv = (TextView) rowView.findViewById(R.id.shopping_list_item_name);
            tv.setText(thisFoodItem.type.name);

            // Hook up add and delete buttons
            View addButton = rowView.findViewById(R.id.shopping_list_item_add);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisFoodItem.location = thisFoodItem.type.default_location;
                    Calendar cal = Calendar.getInstance(); cal.add(Calendar.DATE, thisFoodItem.type.default_reminder);
                    Date reminder = cal.getTime();
                    thisFoodItem.save();
                    foodItemList.remove(thisFoodItem);
                    notifyDataSetChanged();
                    final Toast addConfirmation = Toast.makeText(ctx, "Added " + thisFoodItem.type.name +
                        " to " + Constants.locationFlagToString(thisFoodItem.location), Toast.LENGTH_SHORT); // will be canceled after 1.5 seconds
                    addConfirmation.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addConfirmation.cancel();
                        }
                    }, 1000);
                }
            });

            View deleteButton = rowView.findViewById(R.id.shopping_list_item_remove);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    thisFoodItem.delete();
                    foodItemList.remove(thisFoodItem);
                    notifyDataSetChanged();
                }
            });

            // Hook up detail card display on touch
            final PopupWindow detailCard = new PopupWindow(ctx);
            View detailCardView = inflater.inflate(R.layout.food_item_detail_card, null);
            detailCardView.setBackgroundColor(Color.argb(0xf0, 0xB0, 0xB0, 0xB0));
            // Fill in details
            TextView name = (TextView) detailCardView.findViewById(R.id.detail_card_name_val);
            TextView category = (TextView) detailCardView.findViewById(R.id.detail_card_category_val);
            TextView location = (TextView) detailCardView.findViewById(R.id.detail_card_location_val);
            TextView defaultLocation = (TextView) detailCardView.findViewById(R.id.detail_card_default_location_val);
            name.setText(thisFoodItem.type.name);
            category.setText(thisFoodItem.type.category);
            location.setText(Constants.locationFlagToString(thisFoodItem.location));
            defaultLocation.setText(Constants.locationFlagToString(thisFoodItem.type.default_location));
            detailCard.setFocusable(true);
            detailCard.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
            detailCard.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            detailCard.setContentView(detailCardView);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ShoppingList", "in row onclick");
                    detailCard.showAsDropDown(v, -5, 0);
                }
            });
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

}
