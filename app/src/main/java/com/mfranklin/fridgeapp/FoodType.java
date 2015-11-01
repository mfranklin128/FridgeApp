package com.mfranklin.fridgeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.mfranklin.fridgeapp.FridgeAppContract.*;

import java.util.ArrayList;

/**
 * Created by root on 9/12/15.
 */
public class FoodType {

    public String name = null; // e.g., chicken
    public String category = null; // e.g., fruit, vegetable, meat
    public SQLiteDatabase db;
    private long id = -1;

    // Create a FoodType with existing values
    public FoodType(String name, String category, long id, SQLiteDatabase db) {
        this.name = name;
        this.category = category;
        this.id = id;
        this.db = db;
    }

    // Create a FoodType from a Cursor
    public FoodType(Cursor c, SQLiteDatabase db) {
        int nameIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_NAME);
        int categoryIndex = c.getColumnIndex(FoodTypeEntry.COLUMN_NAME_CATEGORY);
        int idIndex = c.getColumnIndex(FoodTypeEntry._ID);

        // assume we have all the columns
        name = c.getString(nameIndex);
        category = c.getString(categoryIndex);
        id = c.getLong(idIndex);
        this.db = db;
    }

    // Create an empty FoodType
    public FoodType(SQLiteDatabase db) {

    }

    public long save() {
        long result = -1;
        ContentValues vals = new ContentValues();
        vals.put(FoodTypeEntry.COLUMN_NAME_NAME, name);
        vals.put(FoodTypeEntry.COLUMN_NAME_CATEGORY, category);
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

    public static class FoodTypeDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 5;
        public static final String DATABASE_NAME = "FridgeApp.db";

        public FoodTypeDbHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public static final String SQL_CREATE_STMTS =
                "CREATE TABLE IF NOT EXISTS " + FoodTypeEntry.TABLE_NAME + " (" +
                        FoodTypeEntry._ID + " INTEGER PRIMARY KEY," +
                        FoodTypeEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                        FoodTypeEntry.COLUMN_NAME_CATEGORY + " TEXT NOT NULL," +
                        "UNIQUE (" + FoodTypeEntry.COLUMN_NAME_NAME + " collate nocase)" +
                        " )";

        public static final String SQL_DELETE_STMTS = "" +
                "DROP TABLE IF EXISTS " + FoodTypeEntry.TABLE_NAME;

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_STMTS);
            if (FoodTypePrePopulateSql.dbIsEmpty(db)) {
                FoodTypePrePopulateSql.prePopulate(db);
            }
        }

        // ONLY FOR DEVELOPMENT!
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_STMTS);
            onCreate(db);
        }
    }

    public static class FoodTypePrePopulateSql {
        public static boolean dbIsEmpty(SQLiteDatabase db) {
            String[] columns = {FoodTypeEntry._ID};
            Cursor c = db.query(FoodTypeEntry.TABLE_NAME, columns, null, null, null, null, null);
            c.moveToFirst();
            return (c.getCount() == 0);
        }

        public static String[] fruits = {"Bananas", "Apples", "Oranges", "Grapes", "Cherries", "DURIAN"};
        public static String[] vegetables = {"Lettuce", "Carrots", "Peas", "Broccoli"};
        public static String[] meats = {"Chicken", "Turkey", "Beef", "Lamb","Pork"};
        public static String[] dairy = {"Cheese", "Milk", "Yogurt"};
        public static String[] grains = {"Potatoes", "Pasta", "Cereal"};

        public static void prePopulate(SQLiteDatabase db) {
            String table = FoodTypeEntry.TABLE_NAME;
            ContentValues cv = new ContentValues();
            for (String fruit : fruits) {
                cv.put(FoodTypeEntry.COLUMN_NAME_CATEGORY, "Fruit");
                cv.put(FoodTypeEntry.COLUMN_NAME_NAME, fruit);
                db.insert(table, null, cv);
                cv.clear();
            }

            for (String veggie : vegetables) {
                cv.put(FoodTypeEntry.COLUMN_NAME_CATEGORY, "Vegetable");
                cv.put(FoodTypeEntry.COLUMN_NAME_NAME, veggie);
                db.insert(table, null, cv);
                cv.clear();
            }

            for (String meat : meats) {
                cv.put(FoodTypeEntry.COLUMN_NAME_CATEGORY, "Meat");
                cv.put(FoodTypeEntry.COLUMN_NAME_NAME, meat);
                db.insert(table, null, cv);
                cv.clear();
            }

            for (String thisDairy : dairy) {
                cv.put(FoodTypeEntry.COLUMN_NAME_CATEGORY, "Dairy");
                cv.put(FoodTypeEntry.COLUMN_NAME_NAME, thisDairy);
                db.insert(table, null, cv);
                cv.clear();
            }

            for (String grain : grains) {
                cv.put(FoodTypeEntry.COLUMN_NAME_CATEGORY, "Grain");
                cv.put(FoodTypeEntry.COLUMN_NAME_NAME, grain);
                db.insert(table, null, cv);
                cv.clear();
            }
        }
    }
}
