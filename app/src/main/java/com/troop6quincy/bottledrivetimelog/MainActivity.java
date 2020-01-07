package com.troop6quincy.bottledrivetimelog;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.TimePicker;
import android.widget.Toast;

import com.troop6quincy.bottledrivetimelog.checkout.CheckOutDialogFragment;
import com.troop6quincy.bottledrivetimelog.checkout.CheckOutDialogListener;
import com.troop6quincy.bottledrivetimelog.deletescout.DeleteScoutDialogFragment;
import com.troop6quincy.bottledrivetimelog.deletescout.DeleteScoutDialogListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main activity, contains a list of currently checked-in Scouts.
 *
 * @author Joe Desmond
 */
public class MainActivity extends AppCompatActivity implements DeleteScoutDialogListener, CheckOutDialogListener {
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
        final Scout scout = (Scout) arguments.get(getResources().getString(R.string.scout_obj_key));
        removeItem(scout);
        final Toast toast = Toast.makeText(getApplicationContext(), "Deleted " + scout.name + "!", Toast.LENGTH_SHORT);
        toast.show();
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
                showScoutDialog(new CheckOutDialogFragment(), selectedScout, "CheckOutDialogFragment");
                return true;
            case R.id.delete_scout:
                showScoutDialog(new DeleteScoutDialogFragment(), selectedScout, "DeleteScoutDialogFragment");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showScoutDialog(final DialogFragment dialog, final Scout scout, final String dialogName) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(getResources().getString(R.string.scout_obj_key), scout);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), dialogName);
    }

    @Override
    public void onTimeSet(final DialogFragment dialog, final TimePicker picker, int hour, int minute) {
        final Bundle bundle = dialog.getArguments();
        final Scout scout = (Scout) bundle.get(getResources().getString(R.string.scout_obj_key));

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(year, month, day, hour, minute);

        scout.checkOut(calendar.getTime());
        removeItem(scout);
        final Toast toast = Toast.makeText(getApplicationContext(), scout.name + " has left!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onActivityResult(final int aRequestCode, final int aResultCode, final Intent aData) {
        switch (aRequestCode) {
            case CheckInActivity.CHECKIN_NEW_REQUEST:
                if (aResultCode == RESULT_OK) {
                    final Scout scout = (Scout) aData.getSerializableExtra(getResources().getString(R.string.scout_obj_key));
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
