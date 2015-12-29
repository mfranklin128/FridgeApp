package com.mfranklin.kitchnik.data_model;

import android.provider.BaseColumns;

/**
 * Created by root on 9/12/15.
 */
public final class FridgeAppContract  {
    public FridgeAppContract() {}

    public static final int DB_VERSION = 21;

    public static abstract class FoodTypeEntry implements BaseColumns {
        public static final String TABLE_NAME = "food_type";
        public static final String COLUMN_NAME_NAME = "food_type_name";
        public static final String COLUMN_NAME_CATEGORY = "food_type_category";
        public static final String COLUMN_NAME_DEFAULT_REMINDER = "default_reminder";
        public static final String COLUMN_NAME_DEFAULT_LOCATION = "default_location";
    }

    public static abstract class FoodItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "food_item";
        public static final String COLUMN_NAME_FOOD_TYPE = "food_type";
        public static final String COLUMN_NAME_EXP_DATE = "exp_date";
        public static final String COLUMN_NAME_LOCATION = "food_item_location";
        public static final String COLUMN_NAME_STATUS = "food_item_status";
    }

    public static abstract class ReminderEntry implements BaseColumns {
        public static final String TABLE_NAME = "reminder";
        public static final String COLUMN_NAME_START_DATE = "start_date";
        public static final String COLUMN_NAME_DURATION_DAYS = "duration";
        public static final String COLUMN_NAME_FOOD_ITEM = "reminder_food_item";
    }
}
