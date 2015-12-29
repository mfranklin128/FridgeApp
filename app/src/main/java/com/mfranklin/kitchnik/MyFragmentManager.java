package com.mfranklin.kitchnik;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * Created by root on 9/13/15.
 */
public class MyFragmentManager {
    public static boolean displayShoppingListFragment(Activity a, boolean addToBack) {
        FragmentManager fm = a.getFragmentManager();

        if (fm == null) return false;

        Fragment placeholder = fm.findFragmentById(R.id.fragment_container);
        FragmentTransaction ft = fm.beginTransaction();

        if (placeholder != null) {
            ft.replace(R.id.fragment_container, new ShoppingListFragment());
            if (addToBack) {
                ft.addToBackStack("shopping_list_fragment");
            }
            ft.commit();
        }
        else {
            ft.add(R.id.fragment_container, new ShoppingListFragment());
            ft.commit();
        }

        return true;
    }

    public static boolean displayNewItemFragment(Activity a, boolean addToBack) {
        FragmentManager fm  = a.getFragmentManager();

        if (fm == null) return false;

        Fragment placeholder = fm.findFragmentById(R.id.fragment_container);
        FragmentTransaction ft = fm.beginTransaction();

        if (placeholder != null) {
            ft.replace(R.id.fragment_container, new NewItemFragment());
        }
        else {
            ft.add(R.id.fragment_container, new NewItemFragment());
        }
        if (addToBack) {
            ft.addToBackStack("new_item_fragment");
        }
        ft.commit();
        return true;
    }

    public static boolean displayStashFragment(Activity a, boolean addToBack) {
        FragmentManager fm  = a.getFragmentManager();

        if (fm == null) return false;

        Fragment placeholder = fm.findFragmentById(R.id.fragment_container);
        FragmentTransaction ft = fm.beginTransaction();

        if (placeholder != null) {
            ft.replace(R.id.fragment_container, new StashFragment());
        }
        else {
            ft.add(R.id.fragment_container, new StashFragment());
        }
        if (addToBack) {
            ft.addToBackStack("stash_fragment");
        }
        ft.commit();
        return true;
    }

    public static boolean displayReminderFragment(Activity a, boolean addToBack) {
        FragmentManager fm  = a.getFragmentManager();

        if (fm == null) return false;

        Fragment placeholder = fm.findFragmentById(R.id.fragment_container);
        FragmentTransaction ft = fm.beginTransaction();

        if (placeholder != null) {
            ft.replace(R.id.fragment_container, new ReminderFragment());
        }
        else {
            ft.add(R.id.fragment_container, new ReminderFragment());
        }
        if (addToBack) {
            ft.addToBackStack("stash_fragment");
        }
        ft.commit();
        return true;
    }
}
