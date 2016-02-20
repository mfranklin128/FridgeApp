package com.mfranklin.kitchnik.data_model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mfranklin.kitchnik.data_model.FridgeAppContract.*;

import java.util.ArrayList;


/**
 * Created by root on 9/12/15.
 */
public class FoodType {

    public String default_name = null; // e.g., chicken
    public String default_category = null; // e.g., fruit, vegetable, meat
    public int default_reminder = 0;
    public int default_location = Constants.LOC_FRIDGE;
    public SQLiteDatabase db;
    private long id = -1;

    // Create a FoodType with existing values
    public FoodType(String name, String category, int default_reminder, int default_location, long id, SQLiteDatabase db) {
        this.default_name = name;
        this.default_category = category;
        this.default_reminder = default_reminder;
        this.default_location = default_location;
        this.id = id;
        this.db = db;
    }

    // Create a FoodType from a Cursor
    public FoodType(Cursor c, SQLiteDatabase db) {
        int nameIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_NAME);
        int categoryIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_CATEGORY);
        int reminderIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER);
        int locationIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION);
        int idIndex = c.getColumnIndex(FoodTypeEntry._ID);

        // assume we have all the columns
        default_name = c.getString(nameIndex);
        default_category = c.getString(categoryIndex);
        default_reminder = c.getInt(reminderIndex);
        default_location = c.getInt(locationIndex);
        id = c.getLong(idIndex);
        this.db = db;
    }

    // Create an empty FoodType
    public FoodType(SQLiteDatabase db) {

    }

    public long getId() {
        return id;
    }

    public long save() {
        long result = -1;
        ContentValues vals = new ContentValues();
        vals.put(FoodTypeEntry.COLUMN_NAME_NAME, default_name);
        vals.put(FoodTypeEntry.COLUMN_NAME_CATEGORY, default_category);
        vals.put(FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER, default_reminder);
        vals.put(FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION, default_location);
        if (id == -1) { // this is a new entry
            result = db.insert(FoodTypeEntry.TABLE_NAME, null, vals);
        }
        else {
            if (db.update(FoodTypeEntry.TABLE_NAME, vals, "_id=" + id, null) > 0) result = id;
        }
        return result;
    }

    public static FoodType[] getAllFoodTypes(SQLiteDatabase db) {
        FoodType[] toReturn;
        String[] projection = {
                FoodTypeEntry._ID,
                FoodTypeEntry.COLUMN_NAME_NAME,
                FoodTypeEntry.COLUMN_NAME_CATEGORY,
                FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER,
                FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION
        };

        String sortOrder = FoodTypeEntry.COLUMN_NAME_CATEGORY + " ASC, " +
                FoodTypeEntry.COLUMN_NAME_NAME + " ASC";

        Cursor c = db.query(
                FoodTypeEntry.TABLE_NAME, projection, null, null, null, null, sortOrder
        );

        if (!c.moveToFirst()) {
            return null; // fail
        }
        else {
            toReturn = new FoodType[c.getCount()];
            int i = 0;
            do {
                toReturn[i] = new FoodType(c, db);
                i++;
            }
            while (!c.isLast() && c.moveToNext());
            c.close();
            return toReturn;
        }
    }

    public static String[] getAllCategories(SQLiteDatabase db) {
        // build the categories cache now, so it doesn't have to query DB later
        String[] toReturn;
        FoodType[] foodTypes = FoodType.getAllFoodTypes(db);
        ArrayList<String> categoriesList = new ArrayList<>();
        for (FoodType type : foodTypes) {
            if (!categoriesList.contains(type.default_category)) categoriesList.add(type.default_category);
        }
        toReturn = new String[categoriesList.size()];
        toReturn = categoriesList.toArray(toReturn);
        return toReturn;
    }

}
