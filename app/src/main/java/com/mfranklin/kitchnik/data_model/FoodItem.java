package com.mfranklin.kitchnik.data_model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mfranklin.kitchnik.data_model.FridgeAppContract.*;

/**
 * Created by root on 9/13/15.
 */
public class FoodItem {

    public FoodType type;
    private String name;
    private String category;
    private int status;
    private int location;
    private long id;
    public SQLiteDatabase db;

    public FoodItem(SQLiteDatabase db) {
        this.db = db;
    }

    public FoodItem(FoodType type, int status, int location, long id, SQLiteDatabase db) {
        this.type = type;
        this.status = status;
        this.location = location;
        this.db = db;
        this.id = id;
    }

    // Everything should have a value; we handle null/unset/non-existent values here, rather than
    // in the DB
    public FoodItem(Cursor c, SQLiteDatabase db) {
        this.db = db;

        int statusIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_STATUS + "fooditem");
        int locationIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_LOCATION + "fooditem");
        int idIndex = c.getColumnIndex(FoodItemEntry._ID+ "fooditem");
        int foodTypeNameIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_NAME + "foodtype");
        int foodTypeCategoryIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_CATEGORY + "foodtype");
        int foodTypeReminderIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + "foodtype");
        int foodTypeLocationIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + "foodtype");
        int foodTypeIdIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "fooditem");

        status = c.getInt(statusIndex);
        location = c.getInt(locationIndex);

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

        // Put the status
        vals.put(FoodItemEntry.COLUMN_NAME_STATUS, status);

        // Put the location
        vals.put(FoodItemEntry.COLUMN_NAME_LOCATION, location);

        long typeId = type.save();
        vals.put(FoodItemEntry.COLUMN_NAME_FOOD_TYPE, typeId);
        if (id == -1) {
            result = db.insert(FoodItemEntry.TABLE_NAME, null, vals);
            id = result;
        }
        else {
            result = db.update(FoodItemEntry.TABLE_NAME, vals, FoodItemEntry._ID + "=" + id, null);
        }
        return result;
    }

    public void delete() {
        // get rid of any reminders tied to this item
        Reminder reminder = Reminder.getFoodItemReminder(db, this);
        if (reminder != null) reminder.delete();
        int deleted = db.delete(FoodItemEntry.TABLE_NAME, FoodItemEntry._ID + "=?", new String[] {""+id});
    }

    public long getId() {
        return id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getLocation() {
        return location;
    }

    public static FoodItem[] getShoppingListItems(SQLiteDatabase db) {
        Log.d("FoodItem", "getting shopping list items");
        FoodItem[] toReturn;
        String[] projection = {
                FoodItemEntry._ID,
                FoodItemEntry.COLUMN_NAME_FOOD_TYPE,
                FoodItemEntry.COLUMN_NAME_STATUS,
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
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_STATUS + " AS " + FoodItemEntry.COLUMN_NAME_STATUS + "fooditem" + ", " +
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
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_STATUS + "=" + Constants.STATUS_LIST;

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
                FoodItemEntry.COLUMN_NAME_STATUS,
                FoodItemEntry.COLUMN_NAME_LOCATION,
                FoodTypeEntry._ID,
                FoodTypeEntry.COLUMN_NAME_NAME,
                FoodTypeEntry.COLUMN_NAME_CATEGORY
        };

        String query =
                "SELECT " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID + " AS " + FoodItemEntry._ID + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + " AS " + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_STATUS + " AS " + FoodItemEntry.COLUMN_NAME_STATUS + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + " AS " + FoodItemEntry.COLUMN_NAME_LOCATION + "fooditem" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " AS " + FoodTypeEntry._ID + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_NAME + " AS " + FoodTypeEntry.COLUMN_NAME_NAME + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_CATEGORY + " AS " + FoodTypeEntry.COLUMN_NAME_CATEGORY + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + "foodtype" +
                        " FROM " + FoodItemEntry.TABLE_NAME + " INNER JOIN " +
                        FoodTypeEntry.TABLE_NAME + " ON " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "=" +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " WHERE " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_STATUS + "=" + Constants.STATUS_STASH;

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

    public static FoodItem getFoodItemById(SQLiteDatabase db, long id) {
        FoodItem toReturn;
        String[] projection = {
                FoodItemEntry._ID,
                FoodItemEntry.COLUMN_NAME_FOOD_TYPE,
                FoodItemEntry.COLUMN_NAME_STATUS,
                FoodItemEntry.COLUMN_NAME_LOCATION,
                FoodTypeEntry._ID,
                FoodTypeEntry.COLUMN_NAME_NAME,
                FoodTypeEntry.COLUMN_NAME_CATEGORY
        };

        String query =
                "SELECT " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID + " AS " + FoodItemEntry._ID + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + " AS " + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_STATUS + " AS " + FoodItemEntry.COLUMN_NAME_STATUS + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + " AS " + FoodItemEntry.COLUMN_NAME_LOCATION + "fooditem" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " AS " + FoodTypeEntry._ID + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_NAME + " AS " + FoodTypeEntry.COLUMN_NAME_NAME + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_CATEGORY + " AS " + FoodTypeEntry.COLUMN_NAME_CATEGORY + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + "foodtype" +
                        " FROM " + FoodItemEntry.TABLE_NAME + " INNER JOIN " +
                        FoodTypeEntry.TABLE_NAME + " ON " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "=" +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " WHERE " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID + "=" + id;
        Cursor c = db.rawQuery(query, new String[]{});
        if (!c.moveToFirst()) {
            return null; // no results
        }
        else {
            toReturn =  new FoodItem(c, db);
            c.close();
            return toReturn;
        }
    }

    public static FoodItem[] getFoodItemsForType(SQLiteDatabase db, FoodType type) {
        FoodItem[] toReturn;
        String[] projection = {
                FoodItemEntry._ID,
                FoodItemEntry.COLUMN_NAME_FOOD_TYPE,
                FoodItemEntry.COLUMN_NAME_STATUS,
                FoodItemEntry.COLUMN_NAME_LOCATION,
                FoodTypeEntry._ID,
                FoodTypeEntry.COLUMN_NAME_NAME,
                FoodTypeEntry.COLUMN_NAME_CATEGORY
        };

        String query =
                "SELECT " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID + " AS " + FoodItemEntry._ID + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + " AS " + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_STATUS + " AS " + FoodItemEntry.COLUMN_NAME_STATUS + "fooditem" + ", " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_LOCATION + " AS " + FoodItemEntry.COLUMN_NAME_LOCATION + "fooditem" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " AS " + FoodTypeEntry._ID + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_NAME + " AS " + FoodTypeEntry.COLUMN_NAME_NAME + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_CATEGORY + " AS " + FoodTypeEntry.COLUMN_NAME_CATEGORY + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + "foodtype" + ", " +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + " AS " + FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + "foodtype" +
                        " FROM " + FoodItemEntry.TABLE_NAME + " INNER JOIN " +
                        FoodTypeEntry.TABLE_NAME + " ON " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "=" +
                        FoodTypeEntry.TABLE_NAME + "." + FoodTypeEntry._ID + " WHERE " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry.COLUMN_NAME_FOOD_TYPE + "=" + type.getId();

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
