package com.mfranklin.fridgeapp.data_model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mfranklin.fridgeapp.data_model.FridgeAppContract.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by root on 12/6/15.
 */
public class Reminder {
    private long itemId;
    private Date startDate;
    private int durationDays;
    private Date endDate;
    private long id;
    private SQLiteDatabase db;

    public Reminder(SQLiteDatabase db) {
        this.db = db;
        this.id = -1;
    }

    public Reminder(long itemId, Date startDate, int durationDays, long id, SQLiteDatabase db) {
        this.db = db;
        this.itemId = itemId;
        this.startDate = startDate;
        this.durationDays = durationDays;
        this.id = id;
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, durationDays);
        endDate = c.getTime();
    }

    public Reminder(Cursor c, SQLiteDatabase db) {
        this.db = db;

        // all required
        int startDateIndex = c.getColumnIndex(ReminderEntry.COLUMN_NAME_START_DATE + "reminder");
        int durationIndex = c.getColumnIndex(ReminderEntry.COLUMN_NAME_DURATION_DAYS + "reminder");
        int idIndex = c.getColumnIndex(ReminderEntry._ID + "reminder");
        int foodItemIdIndex = c.getColumnIndex(ReminderEntry.COLUMN_NAME_FOOD_ITEM + "reminder");

        try {
            startDate = Constants.expDateFormat.parse(c.getString(startDateIndex));
        }
        catch (ParseException e) {
            startDate = null; // this shouldn't happen
        }
        durationDays = c.getInt(durationIndex);
        id = c.getLong(idIndex);
        itemId = c.getLong(foodItemIdIndex);
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public int getDurationDays() {
        return durationDays;
    }

    // could never use endDate, and only rely on startDate and duration
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public long save() {
        long result = -1;
        ContentValues vals = new ContentValues();

        // Put the item id
        vals.put(ReminderEntry.COLUMN_NAME_FOOD_ITEM, itemId);
        // Put the start date
        vals.put(ReminderEntry.COLUMN_NAME_START_DATE, Constants.expDateFormat.format(startDate));
        vals.put(ReminderEntry.COLUMN_NAME_DURATION_DAYS, durationDays);

        if (id == -1) {
            result = db.insert(ReminderEntry.TABLE_NAME, null, vals);
        }
        else {
            result = db.update(ReminderEntry.TABLE_NAME, vals, ReminderEntry._ID + "=" + id, null);
        }
        return result;
    }

    public void delete() {
        int deleted = db.delete(ReminderEntry.TABLE_NAME, ReminderEntry._ID + "=?", new String[] {""+id});
    }

    public long getId() { return id; }

    // For now, we only have one reminder per food item, which we update
    public static Reminder getFoodItemReminder(SQLiteDatabase db, FoodItem item) {
        Reminder toReturn;
        String[] projection = {
                FoodItemEntry._ID,
                ReminderEntry.COLUMN_NAME_FOOD_ITEM,
                ReminderEntry.COLUMN_NAME_DURATION_DAYS,
                ReminderEntry.COLUMN_NAME_START_DATE
        };

        String query =
                "SELECT " +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID + " AS " + FoodItemEntry._ID + "fooditem" + ", " +
                        ReminderEntry.TABLE_NAME + "." + ReminderEntry.COLUMN_NAME_FOOD_ITEM + " AS " + ReminderEntry.COLUMN_NAME_FOOD_ITEM + "reminder" + ", " +
                        ReminderEntry.TABLE_NAME + "." + ReminderEntry.COLUMN_NAME_START_DATE + " AS " + ReminderEntry.COLUMN_NAME_START_DATE + "reminder" + ", " +
                        ReminderEntry.TABLE_NAME + "." + ReminderEntry.COLUMN_NAME_DURATION_DAYS + " AS " + ReminderEntry.COLUMN_NAME_DURATION_DAYS + "reminder" +
                        " FROM " + FoodItemEntry.TABLE_NAME + " INNER JOIN " +
                        ReminderEntry.TABLE_NAME + " ON " +
                        ReminderEntry.TABLE_NAME + "." + ReminderEntry.COLUMN_NAME_FOOD_ITEM + "=" +
                        FoodItemEntry.TABLE_NAME + "." + FoodItemEntry._ID;

        Cursor c = db.rawQuery(query, new String[] {});
        if (!c.moveToFirst()) {
            return null;
        }
        else {
            toReturn = new Reminder(c, db);
            c.close();
            return toReturn;
        }
    }
}
