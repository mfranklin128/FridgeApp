package com.mfranklin.kitchnik.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfranklin.kitchnik.R;
import com.mfranklin.kitchnik.data_model.FoodItem;
import com.mfranklin.kitchnik.data_model.Reminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by root on 12/8/15.
 */
public class ReminderAdapter extends BaseAdapter {
    private ArrayList<Reminder> reminderList;
    private Context ctx;

    private int weekIndex = -1;
    private int nextWeekIndex = -1;
    private int weekAfterIndex = -1;
    private int thisMonthIndex = -1;
    private int laterIndex = -1;

    private HashMap<Reminder, View> reminderToRowView;

    public ReminderAdapter(Context ctx, Reminder[] reminders) {
        super();
        this.ctx = ctx;
        reminderList = new ArrayList<Reminder>(Arrays.asList(reminders));
        int i = 0;
        for (Reminder reminder : reminderList) {
            if (weekIndex == -1 && reminder.getDaysRemaining() <= 7) weekIndex = i;
            if (nextWeekIndex == -1 && reminder.getDaysRemaining() > 7 && reminder.getDaysRemaining() <= 14) nextWeekIndex = i;
            if (weekAfterIndex == -1 && reminder.getDaysRemaining() > 14 && reminder.getDaysRemaining() <= 21) weekAfterIndex = i;
            if (thisMonthIndex == -1 && reminder.getDaysRemaining() > 21 && reminder.getDaysRemaining() <= 30) thisMonthIndex = i;
            if (laterIndex == -1 && reminder.getDaysRemaining() > 30) laterIndex = i;
            i++;
        }
        reminderToRowView = new HashMap<Reminder, View>();
    }

    public int getCount() {
        return reminderList.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Reminder thisReminder = reminderList.get(position);
        final FoodItem thisItem = FoodItem.getFoodItemById(thisReminder.db, thisReminder.getItemId());
        View rowView = reminderToRowView.get(thisReminder);
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.reminder_item, parent, false);
            reminderToRowView.put(thisReminder, rowView);
        }

        TextView sectionBreak = (TextView) rowView.findViewById(R.id.reminder_item_week_break);
        // set up section headers, if possible
        if (position == weekIndex) {
            sectionBreak.setText("This Week");
        }
        if (position == nextWeekIndex) {
            sectionBreak.setText("Next Week");
        }
        if (position == weekAfterIndex) {
            sectionBreak.setText("Week After");
        }
        if (position == thisMonthIndex) {
            sectionBreak.setText("This Month");
        }
        if (position == laterIndex) {
            sectionBreak.setText("Later");
        }

        // Set up item name
        TextView foodName = (TextView) rowView.findViewById(R.id.reminder_item_name);
        foodName.setText(thisItem.type.name);
        // Set up reminder duration
        TextView duration = (TextView) rowView.findViewById(R.id.reminder_item_duration);
        duration.setText(thisReminder.getDaysRemaining() + " days");

        // Hook up buttons
        ImageButton deleteButton = (ImageButton) rowView.findViewById(R.id.reminder_item_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thisReminder.delete();
                reminderList.remove(thisReminder);
                notifyDataSetChanged();
            }
        });

        return rowView;
    }

    public Object getItem(int position) {
        return reminderList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
}
