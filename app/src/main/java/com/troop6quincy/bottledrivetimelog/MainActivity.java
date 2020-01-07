package com.troop6quincy.bottledrivetimelog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity, contains a list of currently checked-in Scouts.
 *
 * @author Joe Desmond
 */
public class MainActivity extends AppCompatActivity {
    private ListView scoutListView;
    private final List<Scout> listItems = new ArrayList<Scout>();
    private ArrayAdapter<Scout> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scoutListView = (ListView) findViewById(R.id.scoutNameView);
        adapter = new ArrayAdapter<Scout>(getApplicationContext(), android.R.layout.simple_list_item_1, listItems);
        scoutListView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            final Intent intent = new Intent(view.getContext(), CheckInActivity.class);
            ((Activity) view.getContext()).startActivityForResult(intent, CheckInActivity.CHECKIN_NEW_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(final int aRequestCode, final int aResultCode, final Intent aData) {
        switch (aRequestCode) {
            case CheckInActivity.CHECKIN_NEW_REQUEST:
                if (aResultCode == RESULT_OK) {
                    final Scout scout = (Scout) aData.getSerializableExtra("scout");
                    tryAdd(scout);
                }
        }
    }

    /**
     * Attempts to add the Scout to the list, fails if the Scout is already in the list.
     *
     * @param item Scout to add
     * @return true if the operation succeeded, false if not
     */
    private final boolean tryAdd(final Scout item) {
        for (Scout scout : listItems) {
            if (item.equals(scout)) {
                return false;
            }
        }

        addItem(item);
        return true;
    }

    /**
     * Adds a Scout to the list.
     *
     * @param item Scout to add
     */
    private final void addItem(final Scout item) {
        listItems.add(item);
        adapter.notifyDataSetChanged();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
