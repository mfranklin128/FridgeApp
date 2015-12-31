package com.mfranklin.kitchnik.data_model;

import java.text.SimpleDateFormat;

/**
 * Created by root on 9/12/15.
 */
public class Constants {

    // FoodItem status
    public static final int STATUS_LIST = 0;
    public static final int STATUS_STASH = 1;

    public static final int LOC_FRIDGE = 0;
    public static final int LOC_FREEZER = 1;
    public static final int LOC_PANTRY = 2;
    public static final int LOC_NONE = 3;

    public static final String[] locationStrings = {
            "Fridge",
            "Freezer",
            "Pantry",
            "Stash"
    };

    public static final String[] statusStrings = {
            "Shopping List",
            "Stash",
    };

    public static String statusToString(int status) {
        switch (status) {
            case STATUS_LIST: return "Shopping List";
            case STATUS_STASH: return "Stash";
            default: return "";
        }
    }

    public static int stringToStatus(String status) {
        if (status.equals("Shopping List")) return STATUS_LIST;
        if (status.equals("Stash")) return STATUS_STASH;
        return -1; // shouldn't happen
    }

    public static String locationToString(int location) {
        switch (location) {
            case LOC_FRIDGE: return "Fridge";
            case LOC_FREEZER: return "Freezer";
            case LOC_PANTRY: return "Pantry";
            case LOC_NONE: return "Stash";
            default: return "";
        }
    }

    public static int stringToLocation(String location) {
        if (location.equals("Fridge")) return LOC_FRIDGE;
        if (location.equals("Freezer")) return LOC_FREEZER;
        if (location.equals("Pantry")) return LOC_PANTRY;
        if (location.equals("Stash")) return LOC_NONE;
        return LOC_NONE;
    }

    public static final SimpleDateFormat expDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

}
