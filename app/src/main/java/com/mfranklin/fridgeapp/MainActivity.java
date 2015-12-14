package com.mfranklin.fridgeapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity
        implements
        ShoppingListFragment.OnFragmentInteractionListener,
        NewItemFragment.OnFragmentInteractionListener,
        StashFragment.OnFragmentInteractionListener,
        ReminderFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_shopping_list) {
            MyFragmentManager.displayShoppingListFragment(this, false);
        }
        else if (id == R.id.action_add_item) {
            MyFragmentManager.displayNewItemFragment(this, false);
        }
        else if (id == R.id.action_fridge) {
            MyFragmentManager.displayStashFragment(this, false);
        }
        else if (id == R.id.action_reminders) {
            MyFragmentManager.displayReminderFragment(this, false);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onShoppingListFragmentInteraction() {

    }

    public void onNewItemFragmentInteraction() {

    }

    public void onStashFragmentInteraction() {

    }

    public void onReminderFragmentInteraction() {

    }
}
