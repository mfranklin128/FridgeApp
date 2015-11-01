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
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
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

        // Manual ExpandableListView hack
        FoodType.FoodTypeDbHelper dbHelper = new FoodType.FoodTypeDbHelper(getActivity());
        FoodType[] foodTypes = FoodType.getAllFoodTypes(dbHelper.getWritableDatabase());
        if (foodTypes == null) foodTypes = new FoodType[0];

        NewItemCategoryListAdapter categoryListAdapter = new NewItemCategoryListAdapter(foodTypes, getActivity());
        ListView lv = (ListView) toReturn.findViewById(R.id.new_item_fragment_container);
        lv.setAdapter(categoryListAdapter);

        // Set listeners for List and Fridge destinations
        ImageView listDest = (ImageView) toReturn.findViewById(R.id.new_item_dest_list);
        listDest.setOnDragListener(new NewItemDestinationDragListener(Constants.NEW_ITEM_DEST_LIST));

        ImageView fridgeDest = (ImageView) toReturn.findViewById(R.id.new_item_dest_fridge);
        fridgeDest.setOnDragListener(new NewItemDestinationDragListener(Constants.NEW_ITEM_DEST_FRIDGE));

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

    private class NewItemCategoryListAdapter extends BaseAdapter {

        private HashMap<String, ArrayList<FoodType>> categoryToTypes = new HashMap<String, ArrayList<FoodType>>();
        private ArrayList<String> categoryOrder = new ArrayList<String>();
        private Context ctx;

        public NewItemCategoryListAdapter(FoodType[] foodTypes, Context ctx) {
            super();
            this.ctx = ctx;
            for (FoodType type : foodTypes) {
                String category = type.category;
                ArrayList<FoodType> types = categoryToTypes.get(category);
                if (types == null) {
                    types = new ArrayList<FoodType>();
                }
                types.add(type);
                categoryToTypes.put(category, types);
                if (!categoryOrder.contains(category)) categoryOrder.add(category);
            }
        }

        public long getItemId(int position) {
            return position;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isEmpty() {
            return (categoryOrder.size() == 0);
        }

        public int getCount() {
            return categoryOrder.size();
        }

        public Object getItem(int position) {
            return categoryToTypes.get(categoryOrder.get(position));
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final String category = categoryOrder.get(position);

            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) ((Activity) ctx).getLayoutInflater();
                v = inflater.inflate(R.layout.new_item_category_layout, null);
            }

            TextView tv = (TextView) v.findViewById(R.id.new_item_category_header);
            tv.setText(category);
            tv.setGravity(Gravity.CENTER);
            Constants.setColor(category, tv);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View parent = (View) view.getParent();
                    GridView grid = (GridView) parent.findViewById(R.id.new_item_category_grid);
                    // toggle height
                    int visibility = grid.getVisibility();
                    if (visibility == View.VISIBLE) {
                        TextView tv = (TextView) view;
                        tv.setText(category);
                        grid.setVisibility(View.GONE);
                    } else {
                        TextView tv = (TextView) view;
                        tv.setText("");
                        grid.setVisibility(View.VISIBLE);
                    }
                }
            });

            GridView gv = (GridView) v.findViewById(R.id.new_item_category_grid);
            NewItemCategoryGridAdapter categoryGridAdapter = new NewItemCategoryGridAdapter(categoryToTypes.get(category), ctx);
            gv.setAdapter(categoryGridAdapter);
            double numColumns = Constants.getDpWidth(ctx)/80.0;
            int numRows = (int) Math.abs(Math.ceil(categoryToTypes.get(category).size() / numColumns));
            Log.d("NewItemFragment", "numRows = " + numRows);
            Log.d("NewItemFragment", "numCols = " + numColumns);
            ViewGroup.LayoutParams params = gv.getLayoutParams();
            params.height = numRows*Constants.convertDpToPixels(90, ctx);
            gv.setLayoutParams(params);

            return v;
        }

        public int getItemViewType(int position) {
            return Adapter.IGNORE_ITEM_VIEW_TYPE;
        }

        public int getViewTypeCount() {
            return 1;
        }
    }

    private class NewItemCategoryGridAdapter extends BaseAdapter {

        private ArrayList<FoodType> foodTypes;
        private Context ctx;

        public NewItemCategoryGridAdapter(ArrayList<FoodType> foodTypes, Context context) {
            this.foodTypes = new ArrayList<FoodType>(foodTypes);
            ctx = context;
        }

        public int getCount() {
            return foodTypes.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final int finalPos = position;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.new_item_food_type, parent, false);
            }

            Log.d("NewItemFragment", position + ", " + foodTypes.get(position).name);
            TextView tv = (TextView) convertView.findViewById(R.id.new_item_food_type_name);
            tv.setText(foodTypes.get(position).name);
            tv.setGravity(Gravity.CENTER);

            Drawable background = getResources().getDrawable(R.drawable.new_item_food_type_background);
            int color = Constants.getColor(foodTypes.get(position).category);
            background.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            convertView.setBackgroundDrawable(background);

            // Handle drag-and-drop
            convertView.setTag(foodTypes.get(position));
            NewItemFoodTypeDragListener listener = new NewItemFoodTypeDragListener(foodTypes.get(position));
            convertView.setOnDragListener(listener);
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    ClipData.Item item = new ClipData.Item(((FoodType) view.getTag()).name);

                    ClipData dragData = new ClipData(
                            ((FoodType) view.getTag()).name,
                            new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN},
                            item);

                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

                    view.startDrag(
                            dragData,
                            shadowBuilder,
                            foodTypes.get(finalPos),
                            0);
                    return true;
                }
            });

            return convertView;
        }

        public long getItemId(int position) {
            return position;
        }

        public Object getItem(int position) {
            return foodTypes.get(position);
        }


    }

    private class NewItemFoodTypeDragListener implements View.OnDragListener {
        private FoodType type;

        public NewItemFoodTypeDragListener(FoodType type) {
            this.type = type;
        }

        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d("DragListener", "I'm being dragged!");
                    Log.d("DragListener", ((FoodType) v.getTag()).toString());
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    break;
            }
            return false;
        }
    }

    private class NewItemDestinationDragListener implements View.OnDragListener {
        private final int type;

        public NewItemDestinationDragListener(int type) {
            this.type = type;
        }

        public boolean onDrag(View v, DragEvent event) {
            switch(event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    ImageView dest = (ImageView) v;
                    dest.setBackgroundColor(Color.argb(0x80, 0xcc, 0xff, 0xcc));
                    dest.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    dest = (ImageView) v;
                    dest.setBackgroundColor(Color.WHITE);
                    dest.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    Log.d("DragListener", "Dropping in list");
                    FoodType type = (FoodType) event.getLocalState();
                    int location = Constants.LOC_LIST;
                    if (this.type == Constants.NEW_ITEM_DEST_LIST) location = Constants.LOC_LIST;
                    else if (this.type == Constants.NEW_ITEM_DEST_FRIDGE) location = Constants.LOC_FRIDGE;
                    FoodItem newItem = new FoodItem(type, Calendar.getInstance().getTime(), location, -1, type.db);
                    newItem.save();
                    dest = (ImageView) v;
                    dest.setBackgroundColor(Color.WHITE);
                    dest.invalidate();
                    return true;
            }
            return true;
        }
    }
}
