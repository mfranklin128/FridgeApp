package com.mfranklin.fridgeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Filter;

import com.mfranklin.fridgeapp.FridgeAppContract.*;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by root on 9/13/15.
 */
public class FoodItem {

    public FoodType type;
    public Date expDate;
    public int location;
    private long id;
    SQLiteDatabase db;

    public FoodItem(SQLiteDatabase db) {
        this.db = db;
    }

    public FoodItem(FoodType type, Date expDate, int location, long id, SQLiteDatabase db) {
        this.type = type;
        this.expDate = expDate;
        this.location = location;
        this.db = db;
        this.id = id;
    }

    public FoodItem(Cursor c, SQLiteDatabase db) {
        this.db = db;
        for (String columnName : c.getColumnNames()) {
            Log.d("FoodItem", "column name - " + columnName);
        }
        int locationIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_LOCATION + "fooditem");
        int expDateIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_EXP_DATE + "fooditem");
        int idIndex = c.getColumnIndex(FoodItemEntry._ID+ "fooditem");
        int foodTypeNameIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_NAME + "foodtype");
        int foodTypeCategoryIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_CATEGORY + "foodtype");
        int foodTypeReminderIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + "foodtype");
        int foodTypeLocationIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + "foodtype");
        int foodTypeIdIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "fooditem");

        // We need a location, but a null expDate is permitted if the item is on the list
        location = c.getInt(locationIndex);
        try {
            if ((location == Constants.LOC_FRIDGE || location == Constants.LOC_FREEZER) && (expDateIndex >= 0)) {
                expDate = Constants.expDateFormat.parse(c.getString(expDateIndex));
            }
            else {
                expDate = null;
            }
        }
        catch (ParseException e) {
            Log.e("FoodItem", "FoodItem creation error - ", e);
        }

        id = c.getLong(idIndex);

        String foodTypeName = c.getString(foodTypeNameIndex);
        String foodTypeCategory = c.getString(foodTypeCategoryIndex);
        int defaultReminder = c.getInt(foodTypeReminderIndex);
        int defaultLocation = c.getInt(foodTypeLocationIndex);
        long foodTypeId = c.getLong(foodTypeIdIndex);
        type = new FoodType(foodTypeName, foodTypeCategory, defaultReminder, defaultLocation, foodTypeId, db);
    }

    public long save() {
        long result = -1;
        ContentValues vals = new ContentValues();
        vals.put(FoodItemEntry.COLUMN_NAME_LOCATION, location);
        if (expDate != null) {
            vals.put(FoodItemEntry.COLUMN_NAME_EXP_DATE, Constants.expDateFormat.format(expDate));
        }

        long typeId = type.save();
        vals.put(FoodItemEntry.COLUMN_NAME_FOOD_TYPE, typeId);
        Log.d("FoodItem", "loc = " + location + " typeID = " + typeId);
        if (id == -1) {
            result = db.insert(FoodItemEntry.TABLE_NAME, null, vals);
        }
        else {
            Log.d("FoodItem", "doing update on " + id);
            result = db.update(FoodItemEntry.TABLE_NAME, vals, FoodItemEntry._ID + "=" + id, null);
        }
        Log.d("FoodItem", "saving - " + result);
        return result;
    }

    public void delete() {
        int deleted = db.delete(FoodItemEntry.TABLE_NAME, FoodItemEntry._ID + "=?", new String[] {""+id});
        Log.d("FoodItem", "num deleted - " + deleted);
    }

    public static FoodItem[] getShoppingListItems(SQLiteDatabase db) {
        Log.d("FoodItem", "getting shopping list items");
        FoodItem[] toReturn;
        String[] projection = {
                FoodItemEntry._ID,
                FoodItemEntry.COLUMN_NAME_FOOD_TYPE,
                FoodItemEntry.COLUMN_NAME_LOCATION,
                FoodItemEntry.COLUMN_NAME_EXP_DATE,
                FoodTypeEntry._ID,
                FoodTypeEntry.COLUMN_NAME_NAME,
                FoodTypeEntry.COLUMN_NAME_CATEGORY,
                FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER,
                FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION
        };

        String sortOrder = FoodTypeEntry.COLUMN_NAME_NAME + " ASC";
        String query =
                "SELECT " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID + " AS " + FoodItemEntry._ID + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + " AS " + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + " AS " + FoodItemEntry.COLUMN_NAME_LOCATION + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_EXP_DATE + " AS " + FoodItemEntry.COLUMN_NAME_EXP_DATE + "fooditem" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " AS " + FoodTypeEntry._ID + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_NAME + " AS " + FoodTypeEntry.COLUMN_NAME_NAME + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_CATEGORY + " AS " + FoodTypeEntry.COLUMN_NAME_CATEGORY + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + "foodtype" +
                        " FROM " + FoodItemEntry.TABLE_NAME + " INNER JOIN " +
                        FoodTypeEntry.TABLE_NAME + " ON " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "=" +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " WHERE " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + "=" + Constants.LOC_LIST;

        Cursor c = db.rawQuery(query, new String[]{});
        if (!c.moveToFirst()) {
            return null; // no results
        }
        else {
            toReturn = new FoodItem[c.getCount()];
            int i = 0;
            do {
                toReturn[i] = new FoodItem(c, db);
                i++;
            }
            while (!c.isLast() && c.moveToNext());
            c.close();
            return toReturn;
        }
    }

    public static FoodItem[] getStashItems(SQLiteDatabase db) {
        FoodItem[] toReturn;
        String[] projection = {
                FoodItemEntry._ID,
                FoodItemEntry.COLUMN_NAME_FOOD_TYPE,
                FoodItemEntry.COLUMN_NAME_LOCATION,
                FoodItemEntry.COLUMN_NAME_EXP_DATE,
                FoodTypeEntry._ID,
                FoodTypeEntry.COLUMN_NAME_NAME,
                FoodTypeEntry.COLUMN_NAME_CATEGORY
        };

        String query =
                "SELECT " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID + " AS " + FoodItemEntry._ID + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + " AS " + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + " AS " + FoodItemEntry.COLUMN_NAME_LOCATION + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_EXP_DATE + " AS " + FoodItemEntry.COLUMN_NAME_EXP_DATE + "fooditem" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " AS " + FoodTypeEntry._ID + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_NAME + " AS " + FoodTypeEntry.COLUMN_NAME_NAME + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_CATEGORY + " AS " + FoodTypeEntry.COLUMN_NAME_CATEGORY + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + "foodtype" +
                        " FROM " + FoodItemEntry.TABLE_NAME + " INNER JOIN " +
                        FoodTypeEntry.TABLE_NAME + " ON " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "=" +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " WHERE " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + "=" + Constants.LOC_FRIDGE + " OR " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + "=" + Constants.LOC_FREEZER + " OR " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + "=" + Constants.LOC_PANTRY;

        Cursor c = db.rawQuery(query, new String[]{});
        if (!c.moveToFirst()) {
            return null; // no results
        }
        else {
            toReturn = new FoodItem[c.getCount()];
            int i = 0;
            do {
                toReturn[i] = new FoodItem(c, db);
                i++;
            }
            while (!c.isLast() && c.moveToNext());
            c.close();
            return toReturn;
        }
    }

}
