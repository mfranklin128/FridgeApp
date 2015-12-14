package com.mfranklin.fridgeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mfranklin.fridgeapp.R;
import com.mfranklin.fridgeapp.data_model.Reminder;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by root on 12/8/15.
 */
public class ReminderAdapter extends BaseAdapter {
    private ArrayList<Reminder> reminderList;
    private Context ctx;

    public ReminderAdapter(Context ctx, Reminder[] reminders) {
        super();
        this.ctx = ctx;
        reminderList = new ArrayList<Reminder>(Arrays.asList(reminders));
    }

    public int getCount() {
        return reminderList.size();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.reminder_item, parent, false);
        }

        // Set up reminder duration
        final Reminder thisReminder = reminderList.get(position);
        TextView duration = (TextView) rowView.findViewById(R.id.reminder_item_num_days);
        duration.setText(""+thisReminder.getDaysRemaining());

        return rowView;
    }

    public Object getItem(int position) {
        return reminderList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
}
