package com.mfranklin.fridgeapp.data_model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mfranklin.fridgeapp.data_model.FridgeAppContract.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Comparator;
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
    public SQLiteDatabase db;

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
        int startDateIndex = c.getColumnIndex(ReminderEntry.COLUMN_NAME_START_DATE);
        int durationIndex = c.getColumnIndex(ReminderEntry.COLUMN_NAME_DURATION_DAYS);
        int idIndex = c.getColumnIndex(ReminderEntry._ID);
        int foodItemIdIndex = c.getColumnIndex(ReminderEntry.COLUMN_NAME_FOOD_ITEM);

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

    // Convenience method
    public long getDaysRemaining() {
        final long NUM_MILLIS_PER_DAY = 1000*60*60*24;
        Calendar startCal = Calendar.getInstance(); startCal.setTime(getStartDate());
        startCal.add(Calendar.DATE, getDurationDays());
        long endTime = startCal.getTimeInMillis();
        long nowTime = Calendar.getInstance().getTimeInMillis();
        return ((endTime - nowTime) / NUM_MILLIS_PER_DAY);
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
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
        String[] columns = {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_NAME_FOOD_ITEM,
                ReminderEntry.COLUMN_NAME_DURATION_DAYS,
                ReminderEntry.COLUMN_NAME_START_DATE
        };

        String selection = ReminderEntry.COLUMN_NAME_FOOD_ITEM + "=" + item.getId();
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = "1";

        Cursor c = db.query(ReminderEntry.TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        if (!c.moveToFirst()) {
            Log.d("Reminder", "no results???");
            return null;
        }
        else {
            toReturn = new Reminder(c, db);
            c.close();
            return toReturn;
        }
    }

    public static Reminder[] getAllReminders(SQLiteDatabase db) {
        Reminder[] toReturn;
        String[] columns = {
                ReminderEntry._ID,
                ReminderEntry.COLUMN_NAME_FOOD_ITEM,
                ReminderEntry.COLUMN_NAME_DURATION_DAYS,
                ReminderEntry.COLUMN_NAME_START_DATE
        };

        String selection = null;
        String[] selectionArgs = null;
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;

        Cursor c = db.query(ReminderEntry.TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        if (!c.moveToFirst()) {
            Log.d("Reminder", "no results???");
            return null;
        }
        else {
            toReturn = new Reminder[c.getCount()];
            int i = 0;
            do {
                toReturn[i] = new Reminder(c, db);
                i++;
            }
            while (!c.isLast() && c.moveToNext());
            c.close();
            return toReturn;
        }
    }

    public static Comparator<Reminder> expirationOrderComparator() {
        return new Comparator<Reminder>() {
            @Override
            public int compare(Reminder lhs, Reminder rhs) {
                return ((int) (lhs.getDaysRemaining() - rhs.getDaysRemaining()));
            }
        };
    }
}
