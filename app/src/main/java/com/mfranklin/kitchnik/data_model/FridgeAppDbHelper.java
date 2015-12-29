package com.mfranklin.kitchnik.data_model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by root on 11/30/15.
 */
public class FridgeAppDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = FridgeAppContract.DB_VERSION;
    public static final String DB_NAME = "FridgeApp.db";

    public FridgeAppDbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DATABASE_VERSION);
    }

    public static final String FOOD_TYPE_SQL_CREATE_STMTS =
            "CREATE TABLE IF NOT EXISTS " + FridgeAppContract.FoodTypeEntry.TABLE_NAME + " (" +
                    FridgeAppContract.FoodTypeEntry._ID + " INTEGER PRIMARY KEY," +
                    FridgeAppContract.FoodTypeEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                    FridgeAppContract.FoodTypeEntry.COLUMN_NAME_CATEGORY + " TEXT NOT NULL," +
                    FridgeAppContract.FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER + " INTEGER NOT NULL," +
                    FridgeAppContract.FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION + " INTEGER NOT NULL," +
                    "UNIQUE (" + FridgeAppContract.FoodTypeEntry.COLUMN_NAME_NAME + " collate nocase)" +
                    " )";

    public static final String FOOD_ITEM_SQL_CREATE_STMTS =
            "CREATE TABLE IF NOT EXISTS " + FridgeAppContract.FoodItemEntry.TABLE_NAME + " (" +
                    FridgeAppContract.FoodItemEntry._ID + " INTEGER PRIMARY KEY," +
                    FridgeAppContract.FoodItemEntry.COLUMN_NAME_FOOD_TYPE + " NOT NULL," +
                    FridgeAppContract.FoodItemEntry.COLUMN_NAME_STATUS + " INTEGER NOT NULL," +
                    FridgeAppContract.FoodItemEntry.COLUMN_NAME_LOCATION + " INTEGER," +
                    FridgeAppContract.FoodItemEntry.COLUMN_NAME_EXP_DATE + " TEXT," +
                    "FOREIGN KEY(" + FridgeAppContract.FoodItemEntry.COLUMN_NAME_FOOD_TYPE +
                    ") REFERENCES " + FridgeAppContract.FoodTypeEntry.TABLE_NAME + "(" + FridgeAppContract.FoodTypeEntry.COLUMN_NAME_NAME + ")" +
                    " )";

    public static final String REMINDER_SQL_CREATE_STMTS =
            "CREATE TABLE IF NOT EXISTS " + FridgeAppContract.ReminderEntry.TABLE_NAME + " (" +
                    FridgeAppContract.ReminderEntry._ID + " INTEGER PRIMARY KEY," +
                    FridgeAppContract.ReminderEntry.COLUMN_NAME_FOOD_ITEM + " NOT NULL," +
                    FridgeAppContract.ReminderEntry.COLUMN_NAME_START_DATE + " NOT NULL," +
                    FridgeAppContract.ReminderEntry.COLUMN_NAME_DURATION_DAYS + " INTEGER NOT NULL," +
                    "FOREIGN KEY(" + FridgeAppContract.ReminderEntry.COLUMN_NAME_FOOD_ITEM +
                    ") REFERENCES " + FridgeAppContract.FoodItemEntry.TABLE_NAME + "(" + FridgeAppContract.FoodItemEntry._ID + ")" +
                    " )";

    public static final String FOOD_TYPE_SQL_DELETE_STMTS = "" +
            "DROP TABLE IF EXISTS " + FridgeAppContract.FoodTypeEntry.TABLE_NAME;

    public static final String FOOD_ITEM_SQL_DELETE_STMTS = "" +
            "DROP TABLE IF EXISTS " + FridgeAppContract.FoodItemEntry.TABLE_NAME;

    public static final String REMINDER_SQL_DELETE_STMTS = "" +
            "DROP TABLE IF EXISTS " + FridgeAppContract.ReminderEntry.TABLE_NAME;

    public void onCreate(SQLiteDatabase db) {
        Log.d("FridgeAppDb", "creating");
        db.execSQL(FOOD_TYPE_SQL_CREATE_STMTS);
        db.execSQL(FOOD_ITEM_SQL_CREATE_STMTS);
        db.execSQL(REMINDER_SQL_CREATE_STMTS);
        if (dbIsEmpty(db)) {
            prePopulate(db);
        }
    }

    // ONLY FOR DEVELOPMENT!
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(REMINDER_SQL_DELETE_STMTS);
        db.execSQL(FOOD_ITEM_SQL_DELETE_STMTS);
        db.execSQL(FOOD_TYPE_SQL_DELETE_STMTS);
        onCreate(db);
    }

    public boolean dbIsEmpty(SQLiteDatabase db) {
        String[] columns = {FridgeAppContract.FoodTypeEntry._ID};
        Cursor c = db.query(FridgeAppContract.FoodTypeEntry.TABLE_NAME, columns, null, null, null, null, null);
        c.moveToFirst();
        return (c.getCount() == 0);
    }

    public void prePopulate(SQLiteDatabase db) {
        FoodTypePrePopulateData data = new FoodTypePrePopulateData();
        ContentValues cv = new ContentValues();
        String table = FridgeAppContract.FoodTypeEntry.TABLE_NAME;
        for (int i = 0; i < data.names.length; i++) {
            String name = data.names[i];
            String category = data.categories[i];
            int reminder = data.reminders[i];
            int location = data.locations[i];

            cv.put(FridgeAppContract.FoodTypeEntry.COLUMN_NAME_NAME, name);
            cv.put(FridgeAppContract.FoodTypeEntry.COLUMN_NAME_CATEGORY, category);
            cv.put(FridgeAppContract.FoodTypeEntry.COLUMN_NAME_DEFAULT_REMINDER, reminder);
            cv.put(FridgeAppContract.FoodTypeEntry.COLUMN_NAME_DEFAULT_LOCATION, location);
            db.insert(table, null, cv);
            cv.clear();
        }
    }
}
