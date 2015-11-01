package com.mfranklin.fridgeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

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

        int locationIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_LOCATION);
        int expDateIndex = c.getColumnIndex(FoodItemEntry.COLUMN_NAME_EXP_DATE);
        int idIndex = c.getColumnIndex(FoodItemEntry._ID);
        int foodTypeNameIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_NAME);
        int foodTypeCategoryIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_CATEGORY);
        int foodTypeIdIndex = c.getColumnIndex(FoodTypeEntry._ID);

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
        long foodTypeId = c.getLong(foodTypeIdIndex);
        type = new FoodType(foodTypeName, foodTypeCategory, foodTypeId, db);
    }

    public long save() {
        long result = -1;
        ContentValues vals = new ContentValues();
        vals.put(FoodItemEntry.COLUMN_NAME_LOCATION, location);
        vals.put(FoodItemEntry.COLUMN_NAME_EXP_DATE, Constants.expDateFormat.format(expDate));

        long typeId = type.save();
        vals.put(FoodItemEntry.COLUMN_NAME_FOOD_TYPE, typeId);
        Log.d("FoodItem", "loc = " + location + " typeID = " + typeId);
        if (id == -1) {
            result = db.insert(FoodItemEntry.TABLE_NAME, null, vals);
        }
        else {
            result = db.update(FoodItemEntry.TABLE_NAME, vals, FoodItemEntry._ID + "=" + id, null);
        }
        Log.d("FoodItem", "saving - " + result);
        return result;
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
                FoodTypeEntry.COLUMN_NAME_CATEGORY
        };

        String sortOrder = FoodTypeEntry.COLUMN_NAME_NAME + " ASC";
        String query =
                "SELECT * FROM " + FoodItemEntry.TABLE_NAME + " INNER JOIN " +
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

    public static class FoodItemDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 5;
        public static final String DATABASE_NAME = "FridgeApp.db";

        public FoodItemDbHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public static final String SQL_CREATE_STMTS =
                "CREATE TABLE IF NOT EXISTS " + FridgeAppContract.FoodItemEntry.TABLE_NAME + " (" +
                        FridgeAppContract.FoodItemEntry._ID + " INTEGER PRIMARY KEY," +
                        FridgeAppContract.FoodItemEntry.COLUMN_NAME_FOOD_TYPE + " NOT NULL," +
                        FridgeAppContract.FoodItemEntry.COLUMN_NAME_LOCATION + " INTEGER," +
                        FridgeAppContract.FoodItemEntry.COLUMN_NAME_EXP_DATE + " TEXT," +
                        "FOREIGN KEY(" + FridgeAppContract.FoodItemEntry.COLUMN_NAME_FOOD_TYPE +
                        ") REFERENCES " + FridgeAppContract.FoodTypeEntry.TABLE_NAME + "(" + FridgeAppContract.FoodTypeEntry.COLUMN_NAME_NAME + ")" +
                        " )";

        public static final String SQL_DELETE_STMTS = "" +
                "DROP TABLE IF EXISTS " + FridgeAppContract.FoodItemEntry.TABLE_NAME;

        public void onCreate(SQLiteDatabase db) {
            db.execSQL((FoodType.FoodTypeDbHelper.SQL_CREATE_STMTS));
            if (FoodType.FoodTypePrePopulateSql.dbIsEmpty(db)) {
                FoodType.FoodTypePrePopulateSql.prePopulate(db);
            }
            db.execSQL(SQL_CREATE_STMTS);
        }

        // ONLY FOR DEVELOPMENT!
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_STMTS);
            onCreate(db);
        }
    }
}
