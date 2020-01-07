package com.troop6quincy.bottledrivetimelog;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main activity, contains a list of currently checked-in Scouts.
 *
 * @author Joe Desmond
 */
public class MainActivity extends AppCompatActivity implements DeleteScoutDialogListener {
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

        registerForContextMenu(scoutListView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            final Intent intent = new Intent(view.getContext(), CheckInActivity.class);
            ((Activity) view.getContext()).startActivityForResult(intent, CheckInActivity.CHECKIN_NEW_REQUEST);
        });
    }

    @Override
    public void onDialogPositiveClick(final DialogFragment dialog) {
        final Bundle arguments = dialog.getArguments();
        final Scout scout = (Scout) arguments.get("scout");
        removeItem(scout);
    }

    @Override
    public void onDialogNegativeClick(final DialogFragment dialog) {

    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View view, final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scoutlist, menu);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Scout selectedScout = (Scout) scoutListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.checkout_scout:
                checkOut(selectedScout);
                return true;
            case R.id.delete_scout:
                showDeleteScoutDialog(selectedScout);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showDeleteScoutDialog(final Scout scout) {
        final DialogFragment dialog = new DeleteScoutDialogFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable("scout", scout);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "DeleteScoutDialogFragment");
    }

    private void checkOut(final Scout scout) {
        scout.setCheckOut(new Date());
        removeItem(scout);
    }

    @Override
    protected void onActivityResult(final int aRequestCode, final int aResultCode, final Intent aData) {
        switch (aRequestCode) {
            case CheckInActivity.CHECKIN_NEW_REQUEST:
                if (aResultCode == RESULT_OK) {
                    final Scout scout = (Scout) aData.getSerializableExtra("scout");
                    if (!tryAdd(scout)) {
                        final Toast toast = Toast.makeText(getApplicationContext(), "Scout already checked in!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                break;
        }
    }

    /**
     * Attempts to add a Scout to the list, fails if the Scout is already in the list.
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

    private final void removeItem(final Scout item) {
        listItems.remove(item);
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
