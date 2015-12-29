package com.mfranklin.kitchnik;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
        implements
        ShoppingListFragment.OnFragmentInteractionListener,
        NewItemFragment.OnFragmentInteractionListener,
        StashFragment.OnFragmentInteractionListener,
        ReminderFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up sidebar
        String[] options = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t"};
        RecyclerView drawerList = (RecyclerView) findViewById(R.id.drawer_list);
        drawerList.setAdapter(new SidebarAdapter(options, new SidebarAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {

            }
        }));
        LinearLayoutManager linearLayoutmanager = new LinearLayoutManager(this);
        drawerList.setLayoutManager(linearLayoutmanager);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_drawer);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
                else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
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
