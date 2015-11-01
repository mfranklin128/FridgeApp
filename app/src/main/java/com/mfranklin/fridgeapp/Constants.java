package com.mfranklin.fridgeapp;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by root on 9/12/15.
 */
public class Constants {

    public static final int LOC_FRIDGE = 0;
    public static final int LOC_FREEZER = 1;
    public static final int LOC_LIST = 2;

    public static final SimpleDateFormat expDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final int NEW_ITEM_DEST_FRIDGE = 0;
    public static final int NEW_ITEM_DEST_LIST = 1;

    public static final int SHOPPING_LIST_DEST_FRIDGE = 0;
    public static final int SHOPPING_LIST_DEST_FREEZER = 1;

    public static final int VEGETABLE_GREEN = 0xA012AD2A;
    public static final int MEAT_RED = 0xA0D68A59;
    public static final int FRUIT_PURPLE = 0xA0421C52;
    public static final int GRAIN_BROWN = 0xA0685642;
    public static final int DAIRY_YELLOW = 0xA0F8E2B1;

    public static void setColor(String category, View v) {
        if (category.equals("Vegetable")) v.setBackgroundColor(Constants.VEGETABLE_GREEN);
        else if (category.equals("Fruit")) v.setBackgroundColor(Constants.FRUIT_PURPLE);
        else if (category.equals("Meat")) v.setBackgroundColor(Constants.MEAT_RED);
        else if (category.equals("Dairy")) v.setBackgroundColor(Constants.DAIRY_YELLOW);
        else if (category.equals("Grain")) v.setBackgroundColor(Constants.GRAIN_BROWN);
    }

    public static int getColor(String category) {
        if (category.equals("Vegetable")) return Constants.VEGETABLE_GREEN;
        else if (category.equals("Fruit")) return Constants.FRUIT_PURPLE;
        else if (category.equals("Meat")) return Constants.MEAT_RED;
        else if (category.equals("Dairy")) return Constants.DAIRY_YELLOW;
        else if (category.equals("Grain")) return Constants.GRAIN_BROWN;
        else return 0;
    }

    public static int convertDpToPixels(float dp, Context context){
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }

    public static float getDpHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        return dpHeight;
    }

    public static float getDpWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;
    }
}
